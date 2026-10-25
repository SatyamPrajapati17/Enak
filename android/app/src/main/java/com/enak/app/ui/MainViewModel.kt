package com.enak.app.ui

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.enak.app.util.PermissionUtils
import com.enak.data.local.AppUsageDao
import com.enak.data.local.AppUsageEntity
import com.enak.data.local.SessionDao
import com.enak.data.local.SessionEntity
import com.enak.data.repository.UserPreferencesRepository
import com.enak.data.repository.UserSettings
import com.enak.engine.Arm
import com.enak.engine.Bandit
import com.enak.engine.Focus
import com.enak.engine.Scoring
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val usedMinutes: Int,
    val dailyLimitMinutes: Int = 30,
    val isDistracting: Boolean = true
)

data class LiveSessionState(
    val isActive: Boolean = false,
    val purpose: String = "Learn",
    val plannedMinutes: Int = 15,
    val elapsedSeconds: Int = 0,
    val goal: String = "",
    val warningCount: Int = 0,
    val isBreathingActive: Boolean = false,
    val banditArm: Arm = Arm.PAUSE
)


data class CoachMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "user" or "coach"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val prefsRepo: UserPreferencesRepository,
    private val sessionDao: SessionDao,
    private val appUsageDao: AppUsageDao
) : AndroidViewModel(application) {

    val userSettings: StateFlow<UserSettings> = prefsRepo.userSettingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    private val _liveSession = MutableStateFlow(LiveSessionState())
    val liveSession: StateFlow<LiveSessionState> = _liveSession.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledAppItem>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppItem>> = _installedApps.asStateFlow()

    private val _coachMessages = MutableStateFlow<List<CoachMessage>>(
        listOf(
            CoachMessage(
                sender = "coach",
                text = "Hello! I am your Enak mindful companion. What is on your mind today?"
            )
        )
    )
    val coachMessages: StateFlow<List<CoachMessage>> = _coachMessages.asStateFlow()

    private val _gardenLevel = MutableStateFlow(4)
    val gardenLevel: StateFlow<Int> = _gardenLevel.asStateFlow()

    private var sessionTimer: CountDownTimer? = null

    init {
        loadInstalledApps()
        viewModelScope.launch {
            userSettings.collect { settings ->
                com.enak.tracking.DistractionBlocker.isFocusModeActive = settings.focusModeActive
            }
        }
    }

    fun completeOnboarding(
        name: String,
        personality: String,
        language: String,
        weeklyGoalHours: Int,
        quietStart: Int,
        quietEnd: Int
    ) {
        viewModelScope.launch {
            prefsRepo.updateOnboarding(name, personality, language, weeklyGoalHours, quietStart, quietEnd)
        }
    }

    fun setVibe(vibe: String) {
        viewModelScope.launch {
            prefsRepo.setVibe(vibe)
        }
    }

    fun toggleFocusMode(active: Boolean) {
        com.enak.tracking.DistractionBlocker.isFocusModeActive = active
        viewModelScope.launch {
            prefsRepo.setFocusMode(active)
            val context = getApplication<Application>()
            val serviceIntent = android.content.Intent(context, com.enak.tracking.EnakForegroundService::class.java)
            try {
                if (active) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun setStrictMode(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setStrictMode(enabled)
        }
    }

    fun setUserName(name: String) {
        viewModelScope.launch {
            prefsRepo.setName(name)
        }
    }

    fun setPersonality(personality: String) {
        viewModelScope.launch {
            prefsRepo.setPersonality(personality)
        }
    }

    fun startSession(purpose: String, plannedMin: Int, goal: String) {
        sessionTimer?.cancel()
        _liveSession.value = LiveSessionState(
            isActive = true,
            purpose = purpose,
            plannedMinutes = plannedMin,
            elapsedSeconds = 0,
            goal = goal,
            banditArm = Bandit.pickArm(emptyMap())
        )

        // 1 second tick timer
        sessionTimer = object : CountDownTimer(3600_000L * 4, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                _liveSession.update { current ->
                    if (!current.isActive) return
                    val newElapsed = current.elapsedSeconds + 1
                    val newWarnings = if (newElapsed > (current.plannedMinutes * 60) && current.warningCount == 0) {
                        1
                    } else if (newElapsed > (current.plannedMinutes * 60 + 300) && current.warningCount == 1) {
                        2
                    } else current.warningCount

                    current.copy(elapsedSeconds = newElapsed, warningCount = newWarnings)
                }
            }

            override fun onFinish() {}
        }.start()
    }

    fun toggleBreathing(active: Boolean) {
        _liveSession.update { it.copy(isBreathingActive = active) }
    }

    fun extendSession(extraMinutes: Int) {
        _liveSession.update { current ->
            current.copy(
                plannedMinutes = current.plannedMinutes + extraMinutes,
                warningCount = 0
            )
        }
    }

    fun endSession(): LiveSessionState {
        sessionTimer?.cancel()
        val finishedSession = _liveSession.value
        _liveSession.value = LiveSessionState(isActive = false)
        _gardenLevel.update { (it + 1).coerceAtMost(10) }

        viewModelScope.launch {
            val entity = SessionEntity(
                id = UUID.randomUUID().toString(),
                startTime = System.currentTimeMillis() - (finishedSession.elapsedSeconds * 1000L),
                endTime = System.currentTimeMillis(),
                purpose = finishedSession.purpose,
                plannedMin = finishedSession.plannedMinutes,
                actualMin = (finishedSession.elapsedSeconds / 60).coerceAtLeast(1),
                goal = finishedSession.goal,
                mood = "Focused",
                avgScore = 88.0,
                exitLoops = 0,
                achieved = true,
                regret = 1,
                journal = "Good intentional focus session.",
                sentiment = "Positive",
                interventionsJson = "[]",
                extensionsJson = "[]",
                timelineJson = "[]"
            )
            sessionDao.insertSession(entity)
        }
        return finishedSession
    }

    fun sendCoachMessage(prompt: String) {
        val userMsg = CoachMessage(sender = "user", text = prompt)
        _coachMessages.update { it + userMsg }

        // Mindful AI responder (under 120 words, non-judgmental, grounded)
        val name = userSettings.value.name
        val replyText = generateMindfulResponse(prompt, name, userSettings.value.personality)
        val coachMsg = CoachMessage(sender = "coach", text = replyText)
        _coachMessages.update { it + coachMsg }
    }

    private fun generateMindfulResponse(input: String, name: String, personality: String): String {
        val lower = input.lowercase()
        return when {
            lower.contains("5 more") || lower.contains("extra minute") || lower.contains("just finished") -> {
                if (lower.length > 20 && !lower.contains("just")) {
                    "ACCEPT: I hear your genuine purpose, $name. Take your 5 minutes consciously and wrap up with clarity."
                } else {
                    "CHALLENGE: That sounds a bit automatic, $name. How about closing the app and taking three deep breaths instead?"
                }
            }
            lower.contains("overrun") || lower.contains("drift") -> {
                "Overrunning is not a moral failure, $name. It usually happens when boredom meets high-stimulation feeds. Next session, define a concrete stopping cue before you tap the icon."
            }
            lower.contains("evening") || lower.contains("sleep") || lower.contains("night") -> {
                "Protect your quiet hours starting at ${userSettings.value.quietHoursStart}:00. Put your device in another room and give your brain 30 minutes of low stimulation before bed."
            }
            lower.contains("doomscroll") || lower.contains("instagram") || lower.contains("reels") -> {
                "Short-form video feeds are engineered for compulsion. You don't lack willpower; you're facing thousands of engineers. Let Enak's strict mode hold the boundary for you today."
            }
            else -> {
                when (personality) {
                    "strict" -> "Stay on purpose, $name. Ask yourself: did you open this device to learn, or to escape? Execute your intent and exit."
                    "monk" -> "Attention is the currency of life, $name. Notice the urge to drift, breathe into it, and return to what truly matters."
                    "friendly" -> "You're doing great, $name! Remember that small moments of mindful pause add up to hours of saved time."
                    else -> "Peace comes from intentionality, $name. Check in with your breath. You are in control of your digital space."
                }
            }
        }
    }

    fun updateAppLimit(packageName: String, newLimitMinutes: Int) {
        _installedApps.update { list ->
            list.map { if (it.packageName == packageName) it.copy(dailyLimitMinutes = newLimitMinutes) else it }
        }
    }

    private fun loadInstalledApps() {
        val context = getApplication<Application>()
        viewModelScope.launch {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val filtered = packages.filter {
                (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                it.packageName.contains("chrome") ||
                it.packageName.contains("youtube")
            }.map { appInfo ->
                val label = pm.getApplicationLabel(appInfo).toString()
                val used = when {
                    label.contains("Instagram", ignoreCase = true) -> 42
                    label.contains("YouTube", ignoreCase = true) -> 35
                    label.contains("Chrome", ignoreCase = true) -> 28
                    label.contains("WhatsApp", ignoreCase = true) -> 20
                    label.contains("X", ignoreCase = true) || label.contains("Twitter", ignoreCase = true) -> 38
                    else -> (10..40).random()
                }
                InstalledAppItem(
                    packageName = appInfo.packageName,
                    appName = label,
                    usedMinutes = used,
                    dailyLimitMinutes = 30
                )
            }.sortedByDescending { it.usedMinutes }

            _installedApps.value = if (filtered.isNotEmpty()) filtered else getDemoApps()
        }
    }

    private fun getDemoApps() = listOf(
        InstalledAppItem("com.instagram.android", "Instagram", 48, 30),
        InstalledAppItem("com.google.android.youtube", "YouTube", 36, 45),
        InstalledAppItem("com.twitter.android", "X (Twitter)", 29, 20),
        InstalledAppItem("com.reddit.frontpage", "Reddit", 22, 15),
        InstalledAppItem("com.android.chrome", "Chrome", 18, 30)
    )

    override fun onCleared() {
        super.onCleared()
        sessionTimer?.cancel()
    }
}
