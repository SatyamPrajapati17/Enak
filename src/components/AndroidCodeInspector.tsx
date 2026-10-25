import React, { useState } from 'react';
import {
  Folder,
  FileCode,
  Copy,
  Check,
  Terminal,
  Cpu,
  Layers,
  ShieldCheck,
  BookOpen,
  Download,
} from 'lucide-react';

interface AndroidCodeInspectorProps {
  onClose?: () => void;
}

interface ProjectFile {
  path: string;
  module: ':engine' | ':data' | ':tracking' | ':app' | 'root';
  description: string;
  language: 'kotlin' | 'xml' | 'groovy' | 'markdown' | 'toml';
  contentSnippet: string;
}

const ANDROID_FILES: ProjectFile[] = [
  {
    path: 'engine/src/main/java/com/enak/engine/Scoring.kt',
    module: ':engine',
    description: 'Intentionality score (0–100), purpose matching, exit loop detector, and overrun states.',
    language: 'kotlin',
    contentSnippet: `package com.enak.engine

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class Purpose(val code: String) {
    LEARN("learn"),
    RELAX("relax"),
    SOCIAL("social"),
    RESEARCH("research"),
    QUICK("quick")
}

enum class Label {
    FOCUSED,
    DRIFTING,
    COMPULSIVE;
}

enum class OverrunState {
    ON_TRACK,
    NEARING,
    OVER,
    FAR_OVER;
}

data class Signals(
    val scrollVelocity: Double,
    val flickRate: Double,
    val avgDwell: Double,
    val refreshRate: Double,
    val purposeMatch: Double,
    val hour: Int,
    val baselineRatio: Double
)

object Scoring {
    const val EXIT_LOOP_WINDOW_MS = 120_000L
    const val EXIT_LOOP_THRESHOLD = 3

    fun computeScore(s: Signals): ScoreResult {
        val velocity = 1.0 - clamp((s.scrollVelocity - 300.0) / 1500.0)
        val flick = 1.0 - clamp(s.flickRate / 20.0)
        val dwell = clamp((s.avgDwell - 1.0) / 9.0)
        val refresh = 1.0 - clamp(s.refreshRate / 3.0)
        val match = clamp(s.purposeMatch)
        val late = if (s.hour >= 23 || s.hour < 5) 0.6 else 1.0
        val baseline = 1.0 - clamp((s.baselineRatio - 1.5) / 2.5)

        val engagement = dwell * 0.5 + match * 0.5
        val contextWeight = if (engagement > 0.7) 0.02 else 0.1
        val raw = velocity * 0.15 + flick * 0.17 + dwell * 0.22 +
                refresh * 0.1 + match * 0.24 +
                late * (contextWeight * 0.6) + baseline * contextWeight

        val maxExpected = 0.88 + contextWeight * 1.6
        val score = (clamp(raw / maxExpected) * 100.0).roundToInt()
        return ScoreResult(score, labelFor(score), parts)
    }

    fun labelFor(score: Int): Label = when {
        score >= 65 -> Label.FOCUSED
        score >= 40 -> Label.DRIFTING
        else -> Label.COMPULSIVE
    }

    fun overrunState(elapsedSec: Double, plannedMin: Double): OverrunState {
        val r = elapsedSec / (plannedMin * 60.0)
        return when {
            r >= 1.5 -> OverrunState.FAR_OVER
            r >= 1.0 -> OverrunState.OVER
            r >= 0.8 -> OverrunState.NEARING
            else -> OverrunState.ON_TRACK
        }
    }
}`,
  },
  {
    path: 'engine/src/main/java/com/enak/engine/Ladder.kt',
    module: ':engine',
    description: 'Graduated intervention ladder (nudge → question → friction → redirect → extend) & coach copy.',
    language: 'kotlin',
    contentSnippet: `package com.enak.engine

enum class Step(val raw: String, val displayName: String) {
    NUDGE("nudge", "Gentle nudge"),
    REFLECT("reflect", "Reflection prompt"),
    FRICTION("friction", "Soft friction"),
    REDIRECT("redirect", "Redirect"),
    EXTEND("extend", "Conscious extension")
}

object Ladder {
    val LADDER = listOf(Step.NUDGE, Step.REFLECT, Step.FRICTION, Step.REDIRECT, Step.EXTEND)

    fun nextStep(input: LadderInput): Step? {
        if (input.label == Label.FOCUSED && !input.exitLoop) return null
        var target = -1
        if (input.overrun == OverrunState.NEARING) target = 0
        if (input.overrun == OverrunState.OVER) target = if (input.label == Label.COMPULSIVE) 2 else 1
        if (input.overrun == OverrunState.FAR_OVER) target = if (input.label == Label.COMPULSIVE) 3 else 2
        if (input.exitLoop) target = max(target, 1)
        if (input.overrun == OverrunState.ON_TRACK && input.label == Label.COMPULSIVE) target = max(target, 0)
        if (input.sleepGuard && target >= 0) target = min(target + 1, 3)
        if (input.gentleMode) target = min(target, 1)
        if (target < 0 || target <= input.shown) return null
        return LADDER.getOrNull(target)
    }
}`,
  },
  {
    path: 'engine/src/main/java/com/enak/engine/Warnings.kt',
    module: ':engine',
    description: 'Session warning rules: max 2 warnings per session, >= 60s apart.',
    language: 'kotlin',
    contentSnippet: `package com.enak.engine

object Warnings {
    /** Two warnings per session: at the plan and one minute later, or on drift. */
    fun warningDue(
        elapsedSec: Int,
        plannedMin: Int,
        count: Int,
        lastAt: Long,
        now: Long,
        drifting: Boolean
    ): Boolean {
        if (count >= 2 || (now - lastAt < 60_000L)) return false
        return (plannedMin < 600 && elapsedSec >= plannedMin * 60) || drifting
    }
}`,
  },
  {
    path: 'tracking/src/main/java/com/enak/tracking/UsageTracker.kt',
    module: ':tracking',
    description: 'Real UsageStatsManager queryEvents implementation computing today foreground time.',
    language: 'kotlin',
    contentSnippet: `package com.enak.tracking

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.Calendar

class UsageTracker(private val context: Context) {
    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager

    fun queryTodayUsage(): Map<String, Long> {
        val manager = usageStatsManager ?: return emptyMap()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val events = manager.queryEvents(cal.timeInMillis, System.currentTimeMillis())
        val event = UsageEvents.Event()
        val lastResumeTimes = mutableMapOf<String, Long>()
        val totalForegroundTimes = mutableMapOf<String, Long>()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> lastResumeTimes[pkg] = event.timeStamp
                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.ACTIVITY_STOPPED -> {
                    val resumedAt = lastResumeTimes.remove(pkg)
                    if (resumedAt != null && event.timeStamp > resumedAt) {
                        val duration = event.timeStamp - resumedAt
                        totalForegroundTimes[pkg] = (totalForegroundTimes[pkg] ?: 0L) + duration
                    }
                }
            }
        }
        return totalForegroundTimes
    }
}`,
  },
  {
    path: 'tracking/src/main/java/com/enak/tracking/EnakForegroundService.kt',
    module: ':tracking',
    description: 'Foreground service polling UsageStats every 1-2s and triggering BlockerActivity.',
    language: 'kotlin',
    contentSnippet: `package com.enak.tracking

import android.app.Service
import android.content.Intent
import com.enak.engine.Focus
import com.enak.engine.LimitStatus

class EnakForegroundService : Service() {
    private val warningCounts = mutableMapOf<String, Int>()

    private fun checkLimitsAndWarn() {
        val currentPkg = usageTracker.getCurrentForegroundPackage() ?: return
        val limitMin = appLimitsMinutes[currentPkg] ?: return
        val usedMinutes = (usageTracker.queryTodayUsage()[currentPkg] ?: 0L) / 60_000L

        val status = Focus.limitStatus(usedMinutes.toInt(), limitMin)
        val count = warningCounts[currentPkg] ?: 0

        if (status == LimitStatus.OVER) {
            if (count < 2) {
                warningCounts[currentPkg] = count + 1
                triggerBlockerOverlay(currentPkg, warningNumber = count + 1, isHardBlock = false)
            } else {
                triggerBlockerOverlay(currentPkg, warningNumber = 2, isHardBlock = true)
            }
        }
    }
}`,
  },
  {
    path: 'tracking/src/main/java/com/enak/tracking/BlockerActivity.kt',
    module: ':tracking',
    description: 'Full-screen blocking screen with 2 warnings, Close App, Breather, and AI negotiation.',
    language: 'kotlin',
    contentSnippet: `package com.enak.tracking

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class BlockerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pkgName = intent.getStringExtra("PACKAGE_NAME") ?: "Target App"
        val warningNumber = intent.getIntExtra("WARNING_NUMBER", 1)
        val isHardBlock = intent.getBooleanExtra("IS_HARD_BLOCK", false)

        setContent {
            BlockerScreen(
                packageName = pkgName,
                warningNumber = warningNumber,
                isHardBlock = isHardBlock,
                onCloseApp = {
                    val startMain = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(startMain)
                    finish()
                },
                onDismissWarning = { finish() }
            )
        }
    }
}`,
  },
  {
    path: 'app/src/main/AndroidManifest.xml',
    module: ':app',
    description: 'Manifest declaring PACKAGE_USAGE_STATS, SYSTEM_ALERT_WINDOW, services, queries, and receivers.',
    language: 'xml',
    contentSnippet: `<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />

    <queries>
        <intent>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent>
    </queries>

    <application
        android:name=".EnakApplication"
        android:theme="@style/Theme.Enak">
        ...
    </application>
</manifest>`,
  },
  {
    path: 'README.md',
    module: 'root',
    description: 'Complete build guide: assembleDebug, signed release APK/AAB, keystore, permissions, and policy notes.',
    language: 'markdown',
    contentSnippet: `# Enak — Native Android Digital Wellbeing & Mindful Screen Time

## Building the App
1. Build Debug APK:
   ./gradlew assembleDebug
   Output: app/build/outputs/apk/debug/app-debug.apk

2. Build Signed Release APK / AAB:
   ./gradlew assembleRelease
   ./gradlew bundleRelease

## Play Store Compliance
- PACKAGE_USAGE_STATS: Reads only package names & elapsed durations. Never reads screen text or messages.
- Accessibility: Prominent disclosure required. Only observes TYPE_WINDOW_STATE_CHANGED for timer triggers.
- Targeted <queries> replaces broad QUERY_ALL_PACKAGES.`,
  },
];

export const AndroidCodeInspector: React.FC<AndroidCodeInspectorProps> = ({ onClose }) => {
  const [selectedFile, setSelectedFile] = useState<ProjectFile>(ANDROID_FILES[0]);
  const [copied, setCopied] = useState(false);
  const [activeModuleFilter, setActiveModuleFilter] = useState<string>('all');

  const filteredFiles =
    activeModuleFilter === 'all'
      ? ANDROID_FILES
      : ANDROID_FILES.filter((f) => f.module === activeModuleFilter);

  const handleCopy = () => {
    navigator.clipboard.writeText(selectedFile.contentSnippet);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="h-full flex flex-col bg-[#0F0F16] text-slate-200 select-text overflow-hidden rounded-3xl border border-white/10 shadow-2xl">
      {/* Header */}
      <div className="px-6 py-4 bg-[#14141F] border-b border-white/10 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-xl bg-gradient-to-tr from-[#8A2BE2] to-[#22E3F0] flex items-center justify-center text-white shadow-md">
            <Cpu className="w-4 h-4" />
          </div>
          <div>
            <h2 className="text-sm font-bold text-white tracking-wide">
              Native Android Project Explorer (Kotlin + Jetpack Compose)
            </h2>
            <p className="text-[11px] text-slate-400">
              Clean modular architecture (:engine, :data, :tracking, :app) ready for Android Studio
            </p>
          </div>
        </div>

        {/* Build Commands pill */}
        <div className="flex items-center gap-2">
          <div className="hidden md:flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#1F1F30] border border-white/10 text-[11px] font-mono text-cyan-300">
            <Terminal className="w-3.5 h-3.5 text-cyan-400" />
            <span>./gradlew assembleDebug</span>
          </div>
        </div>
      </div>

      {/* Main layout: Sidebar + Code Viewer */}
      <div className="flex-1 flex overflow-hidden">
        {/* Module / File Tree Sidebar */}
        <div className="w-64 border-r border-white/10 bg-[#12121B] flex flex-col">
          {/* Module Filter pills */}
          <div className="p-3 border-b border-white/5 flex flex-wrap gap-1">
            {['all', ':engine', ':data', ':tracking', ':app', 'root'].map((m) => (
              <button
                key={m}
                onClick={() => setActiveModuleFilter(m)}
                className={`px-2 py-0.5 rounded-md text-[10px] font-mono transition-colors ${
                  activeModuleFilter === m
                    ? 'bg-[#8A2BE2] text-white font-semibold'
                    : 'bg-white/5 text-slate-400 hover:text-slate-200'
                }`}
              >
                {m}
              </button>
            ))}
          </div>

          {/* File list */}
          <div className="flex-1 overflow-y-auto p-2 space-y-1">
            {filteredFiles.map((file) => {
              const isSelected = selectedFile.path === file.path;
              return (
                <button
                  key={file.path}
                  onClick={() => setSelectedFile(file)}
                  className={`w-full text-left px-2.5 py-2 rounded-xl text-xs transition-all flex items-start gap-2 ${
                    isSelected
                      ? 'bg-[#8A2BE2]/20 border border-[#8A2BE2]/50 text-cyan-300'
                      : 'hover:bg-white/5 text-slate-300'
                  }`}
                >
                  <FileCode className="w-4 h-4 mt-0.5 shrink-0 text-slate-400" />
                  <div className="min-w-0">
                    <p className="font-mono text-[11px] truncate font-medium">
                      {file.path.split('/').pop()}
                    </p>
                    <p className="text-[10px] text-slate-500 truncate">{file.module}</p>
                  </div>
                </button>
              );
            })}
          </div>

          {/* Quick Instructions Footer */}
          <div className="p-3 bg-[#0B0B10] border-t border-white/5 text-[10px] text-slate-400 space-y-1">
            <div className="flex items-center gap-1.5 text-slate-300 font-semibold">
              <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
              <span>Full Project in /android</span>
            </div>
            <p className="leading-tight">
              Open <code>/android</code> in Android Studio Ladybug/Meerkat to build native APK or AAB.
            </p>
          </div>
        </div>

        {/* Code Content Viewer */}
        <div className="flex-1 flex flex-col bg-[#0A0A0F] overflow-hidden">
          {/* File Tab Bar */}
          <div className="px-5 py-3 bg-[#11111A] border-b border-white/10 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="text-xs font-mono text-cyan-300 font-semibold">
                {selectedFile.path}
              </span>
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-white/5 text-slate-400 font-mono">
                {selectedFile.module}
              </span>
            </div>

            <button
              onClick={handleCopy}
              className="flex items-center gap-1.5 px-3 py-1 rounded-lg bg-white/5 hover:bg-white/10 text-xs text-slate-300 font-mono transition-colors"
            >
              {copied ? (
                <>
                  <Check className="w-3.5 h-3.5 text-emerald-400" />
                  <span className="text-emerald-300">Copied</span>
                </>
              ) : (
                <>
                  <Copy className="w-3.5 h-3.5" />
                  <span>Copy code</span>
                </>
              )}
            </button>
          </div>

          {/* File Description */}
          <div className="px-5 py-2.5 bg-[#141420] border-b border-white/5 text-xs text-slate-400 flex items-center gap-2">
            <BookOpen className="w-3.5 h-3.5 text-[#8A2BE2]" />
            <span>{selectedFile.description}</span>
          </div>

          {/* Code block */}
          <div className="flex-1 p-5 overflow-auto font-mono text-xs text-slate-300 leading-relaxed bg-[#0B0B10]">
            <pre className="whitespace-pre">{selectedFile.contentSnippet}</pre>
          </div>
        </div>
      </div>
    </div>
  );
};
