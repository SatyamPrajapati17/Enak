package com.enak.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

data class UserSettings(
    val name: String = "Traveler",
    val personality: String = "calm", // calm, strict, friendly, monk
    val language: String = "en", // en, hinglish, hi
    val weeklyGoalHours: Int = 14,
    val quietHoursStart: Int = 22,
    val quietHoursEnd: Int = 7,
    val gentleMode: Boolean = false,
    val sleepGuard: Boolean = true,
    val isOnboarded: Boolean = false,
    val vibe: String = "royal", // royal, cyan, emerald, sunset
    val focusModeActive: Boolean = false,
    val strictModeEnabled: Boolean = false,
    val themeMode: String = "dark" // "dark", "light", "system"
)

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val NAME = stringPreferencesKey("name")
        val PERSONALITY = stringPreferencesKey("personality")
        val LANGUAGE = stringPreferencesKey("language")
        val WEEKLY_GOAL_HOURS = intPreferencesKey("weekly_goal_hours")
        val QUIET_START = intPreferencesKey("quiet_start")
        val QUIET_END = intPreferencesKey("quiet_end")
        val GENTLE_MODE = booleanPreferencesKey("gentle_mode")
        val SLEEP_GUARD = booleanPreferencesKey("sleep_guard")
        val IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
        val VIBE = stringPreferencesKey("vibe")
        val FOCUS_MODE = booleanPreferencesKey("focus_mode")
        val STRICT_MODE = booleanPreferencesKey("strict_mode")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val BLOCKED_APPS = stringSetPreferencesKey("blocked_apps")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            name = prefs[Keys.NAME] ?: "Traveler",
            personality = prefs[Keys.PERSONALITY] ?: "calm",
            language = prefs[Keys.LANGUAGE] ?: "en",
            weeklyGoalHours = prefs[Keys.WEEKLY_GOAL_HOURS] ?: 14,
            quietHoursStart = prefs[Keys.QUIET_START] ?: 22,
            quietHoursEnd = prefs[Keys.QUIET_END] ?: 7,
            gentleMode = prefs[Keys.GENTLE_MODE] ?: false,
            sleepGuard = prefs[Keys.SLEEP_GUARD] ?: true,
            isOnboarded = prefs[Keys.IS_ONBOARDED] ?: false,
            vibe = prefs[Keys.VIBE] ?: "royal",
            focusModeActive = prefs[Keys.FOCUS_MODE] ?: false,
            strictModeEnabled = prefs[Keys.STRICT_MODE] ?: false,
            themeMode = prefs[Keys.THEME_MODE] ?: "dark"
        )
    }

    suspend fun updateOnboarding(
        name: String,
        personality: String,
        language: String,
        weeklyGoalHours: Int,
        quietStart: Int,
        quietEnd: Int
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NAME] = name
            prefs[Keys.PERSONALITY] = personality
            prefs[Keys.LANGUAGE] = language
            prefs[Keys.WEEKLY_GOAL_HOURS] = weeklyGoalHours
            prefs[Keys.QUIET_START] = quietStart
            prefs[Keys.QUIET_END] = quietEnd
            prefs[Keys.IS_ONBOARDED] = true
        }
    }

    suspend fun setVibe(vibe: String) {
        context.dataStore.edit { it[Keys.VIBE] = vibe }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setFocusMode(active: Boolean) {
        context.dataStore.edit { it[Keys.FOCUS_MODE] = active }
    }

    suspend fun setStrictMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.STRICT_MODE] = enabled }
    }

    suspend fun setPersonality(p: String) {
        context.dataStore.edit { it[Keys.PERSONALITY] = p }
    }

    suspend fun setName(name: String) {
        context.dataStore.edit { it[Keys.NAME] = name }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = lang }
    }

    suspend fun setGentleMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.GENTLE_MODE] = enabled }
    }

    val blockedAppsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.BLOCKED_APPS] ?: emptySet()
    }

    suspend fun setAppBlocked(packageName: String, blocked: Boolean) {
        context.dataStore.edit { prefs ->
            val current = (prefs[Keys.BLOCKED_APPS] ?: emptySet()).toMutableSet()
            if (blocked) {
                current.add(packageName)
            } else {
                current.remove(packageName)
            }
            prefs[Keys.BLOCKED_APPS] = current
        }
    }

    suspend fun setBlockedApps(packages: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BLOCKED_APPS] = packages
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
