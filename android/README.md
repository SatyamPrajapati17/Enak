# Enak — Native Android Digital Wellbeing & Mindful Screen Time

Enak is a production-grade native Android application built with Kotlin and Jetpack Compose (Material 3). It asks users what they want to do and for how long before a session, detects when actual usage drifts from that intention, and responds with graduated, personal interventions instead of abrupt, punitive blocking.

Long screen time is not treated as bad by default: only **loss of intentionality** matters.

---

## Architecture Overview

The project is structured into clear modular boundaries:
- `:engine` — Pure Kotlin module (zero Android dependencies). Houses intentionality scoring (0–100), the graduated intervention ladder, Thompson-sampling bandit engine, session patterns, warning rules, and focus streak/quiet hour calculations. Fully unit-testable on standard JVM.
- `:data` — Room database (`EnakDatabase`), DAOs for sessions and daily app usage, and Jetpack DataStore Preferences for user settings.
- `:tracking` — Android device-level services: `UsageStatsManager` foreground tracking, `EnakForegroundService` (with `specialUse`/`dataSync`), optional `EnakAccessibilityService`, `BlockerActivity` overlay, and `BootReceiver`.
- `:app` — Jetpack Compose presentation layer, Hilt dependency injection, Navigation Compose, and theme styling (Jet Black `#0B0B10`, Royal Amethyst `#8A2BE2`, Electric Cyan `#22E3F0`).

---

## Building the App

### Prerequisites
- Android Studio Ladybug / Meerkat (or newer)
- JDK 17+
- Android SDK 35 (minSdk 26)

### 1. Build Debug APK
To assemble the debug APK:
```bash
./gradlew assembleDebug
```
The output APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

### 2. Run All Unit & Instrumented Tests
To verify all engine tests (with identical mathematical coverage to the test suite):
```bash
./gradlew test
```
To run the instrumented Compose blocker flow test on an emulator/device:
```bash
./gradlew connectedAndroidTest
```

### 3. Build Signed Release APK & Android App Bundle (AAB)
To generate a production release keystore:
```bash
keytool -genkey -v -keystore release.keystore -alias enak_key -keyalg RSA -keysize 2048 -validity 10000
```

Export keystore credentials into environment variables:
```bash
export KEYSTORE_PATH="/path/to/release.keystore"
export KEYSTORE_PASSWORD="your-secure-store-password"
export KEY_ALIAS="enak_key"
export KEY_PASSWORD="your-secure-key-password"
```

To build a signed Release APK:
```bash
./gradlew assembleRelease
```
Output: `app/build/outputs/apk/release/app-release.apk`

To build an Android App Bundle (AAB) for Google Play:
```bash
./gradlew bundleRelease
```
Output: `app/build/outputs/bundle/release/app-release.aab`

---

## Permissions Explained

| Permission | Android Level | Reason / Usage in Enak |
|---|---|---|
| `PACKAGE_USAGE_STATS` | Special Access | Computes foreground minutes spent in target apps today via `UsageStatsManager.queryEvents()` without inspecting content. |
| `SYSTEM_ALERT_WINDOW` | Special Access | Displays the mindful intervention overlay over other running applications when daily boundaries are reached. |
| `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_SPECIAL_USE` | Normal / Runtime | Keeps Enak's intentionality shield active and responsive while apps are in use. |
| `RECEIVE_BOOT_COMPLETED` | Normal | Automatically re-activates background limit protection following a device reboot. |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Special | Ensures timers and blocker services survive Android Doze power management. |
| `POST_NOTIFICATIONS` | Runtime (33+) | Sends gentle check-in reminders, daily intentionality recaps, and 80% boundary alerts. |
| `RECORD_AUDIO` | Runtime | Allows voice input when conversing with the mindful AI coach. |

---

## Google Play Store Policy Declarations

### 1. Usage Access (`PACKAGE_USAGE_STATS`)
- **Category:** Digital Wellbeing & Screen Time Management
- **User Disclosure:** Enak reads only the package name and elapsed foreground timestamps of installed apps to compute daily usage. It never accesses, records, or transmits application data, user input, or in-app screens.

### 2. Accessibility Service (`EnakAccessibilityService`)
- **Declaration:** An optional feature for instantaneous foreground window transition detection.
- **Prominent Disclosure Screen:** Before enablement, Enak presents a prominent disclosure explaining that Accessibility is utilized solely for detecting window state transitions (`TYPE_WINDOW_STATE_CHANGED`) to trigger configured timer overlays.
- **Strict Compliance:** Enak explicitly does NOT read screen text, modify user settings, disable hardware controls, or intercept keystrokes.

### 3. Package Visibility (`<queries>` vs `QUERY_ALL_PACKAGES`)
- In accordance with Google Play's high-risk permission policy, Enak does **not** declare the broad `QUERY_ALL_PACKAGES` permission. Instead, it utilizes targeted `<queries>` matching `android.intent.action.MAIN` with `category.LAUNCHER` to discover user-launchable apps that the user may want to set boundaries for.
