# Enak — Mindful Digital Wellbeing & Intentional Screen Time

<div align="center">

[![Android](https://img.shields.io/badge/Platform-Android_8.0+_--_15_(API_26--35)-brightgreen?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin_2.0-purple?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack_Compose_Material_3-blue?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean_Architecture_--_MVVM-orange)]()
[![Local First](https://img.shields.io/badge/Privacy-100%25_Local--First-success)]()

**Scroll with a reason. Protect your attention with graduated, mindful interventions instead of abrupt, punitive blocking.**

[Quick Install](#quick-install--apk-downloads) • [Architecture](#architecture-overview) • [Design System](#design-system--vibe-engine) • [Native Features](#native-android-capabilities) • [Permissions](#permissions-architecture) • [Building](#building-from-source)

</div>

---

## Overview

**Enak** is a native Android application built with **Kotlin and Jetpack Compose (Material 3)**.

Most screen-time apps treat screen time as inherently bad. Enak operates on a different philosophy: **long screen time is not bad by default — only loss of intentionality matters**. Reading an e-book or following a programming course for 2 hours is valuable; mindlessly switching between social feeds and doomscrolling short-form video reels is not.

Enak asks you what you want to do and for how long before a session, detects when your usage drifts from that intention, and responds with personal, graduated interventions.

---

## Key Highlights

- **Distraction Blocker & Focus Shield**: Continuously blocks selected distracting applications (Instagram, YouTube, TikTok, X/Twitter, Reddit) while Focus Mode is active.
- **Theme Changing as per Vibe**: Live, dynamic recoloring across the app to match your mindset:
  - 🟣 **Royal Amethyst** (`#8A2BE2` & `#22E3F0`) — Deep focus & contemplation
  - 🔷 **Cyan Clarity** (`#22E3F0` & `#8A2BE2`) — High alertness & crisp energy
  - 🟢 **Emerald Zen** (`#10B981` & `#06D6A0`) — Calming nature & balance
  - 🌅 **Sunset Rose** (`#F43F5E` & `#F59E0B`) — Warm twilight & evening wind-down
- **Mindful AI Coach**: Grounded, non-judgmental guidance with voice output (**Text-to-Speech**) and customized personas (*Calm Zen*, *Direct Strict*, *Friendly*, *Philosophical Monk*).
- **Sequential Permissions Onboarding**: Guides you step-by-step through granting system permissions with live auto-detection on `onResume()`. No mandatory login or account creation required.
- **Graduated Interventions**: Thompson-sampling multi-armed bandit optimizes which intervention works best for you (Breathing pause, Reflection question, Soft banner, or Coach negotiation).
- **The Mindful Garden**: A botanical plant that blossoms as you complete intentional sessions and gently withers on days with compulsive drift.
- **One-Shot Confetti Engine**: Lightweight Canvas-based cyan and amethyst particle celebration upon completing an intentional session.

---

## Quick Install & APK Downloads

Pre-built ready-to-install APKs are provided directly in the repository:

| Build | File Path | Size | Description |
|---|---|---|---|
| 🚀 **Release (Recommended)** | [`apk/enak-release.apk`](apk/enak-release.apk) | **1.4 MB** | Production-signed with 2048-bit RSA key, optimized with R8/ProGuard |
| 🛠️ **Debug Build** | [`apk/enak-debug.apk`](apk/enak-debug.apk) | **18.3 MB** | Contains complete debug symbol tables and verbose logs |

### Resolving Google Play Protect ("App Blocked to Protect Your Device")

Because Enak uses deep device-level features (**Usage Access**, **Accessibility Service**, **Device Admin**) and is sideloaded directly rather than installed from Google Play, Google Play Protect may display a prompt:

> *"Google Play Protect: App blocked to protect your device"*

**How to Install:**
1. **Method A (Direct Tap)**: On the Play Protect popup, tap **"More details"**, then tap **"Install anyway"**.
2. **Method B**:
   - Open **Google Play Store** $\rightarrow$ Tap your **Profile icon** (top right) $\rightarrow$ **Play Protect**.
   - Tap the **Settings Gear (⚙️)** in the top right.
   - Temporarily toggle off **"Scan apps with Play Protect"**.
   - Install `enak-release.apk` from your phone's File Manager.
   - Re-enable Play Protect scanning.

### Android 13, 14 & 15 "Restricted Setting" Fix (Accessibility & Notifications)

On Android 13+, Google automatically disables Accessibility Services and Notification Access for sideloaded APKs, showing a popup:
> *"Restricted setting: For your security, this setting is currently unavailable."*

**How to unlock in 3 quick steps:**
1. Open your phone's **Settings** $\rightarrow$ **Apps** $\rightarrow$ **All Apps** $\rightarrow$ Tap **Enak** (or tap *"App Info"* directly from Enak's Permissions Center).
2. Tap the **three vertical dots (⋮)** in the top-right corner.
3. Tap **"Allow restricted settings"** $\rightarrow$ Authenticate with your phone's PIN/Fingerprint.
4. Return to **Enak** $\rightarrow$ Tap **Accessibility** or **Notification Access**; the toggle will now be fully interactive!

### Fresh Installs & Data Isolation
- **No Old Stale Data**: Cloud backup is disabled (`android:allowBackup="false"`). Each re-installation starts completely fresh from scratch with no ghost sessions or stale permissions from previous installs.
- **One-Tap Reset**: A dedicated **"Start From Scratch (Reset All Data)"** button in **Settings** allows wiping all Room database records and DataStore preferences anytime.

---

## Architecture Overview

The codebase is engineered with strict modular boundaries and Clean Architecture principles:

```
enak/
├── :engine      # Pure Kotlin module (Zero Android dependencies).
│                # Houses Thompson bandit math, intentionality scoring (0–100),
│                # ladder interventions, pattern detection, and quiet hour algorithms.
├── :data        # Room Database (EnakDatabase), DAOs (sessions, daily usage),
│                # and Jetpack DataStore Preferences repository.
├── :tracking    # Android system-level services:
│                # - UsageStatsManager real-time tracking
│                # - EnakForegroundService (specialUse / dataSync)
│                # - DistractionBlocker live app interceptor
│                # - EnakAccessibilityService (instant window transitions)
│                # - EnakDeviceAdminReceiver (Strict Mode anti-tamper)
│                # - EnakNotificationListenerService (alert muting)
│                # - BlockerActivity (full-screen mindful intervention overlay)
└── :app         # Jetpack Compose UI (Material 3), Navigation Compose,
                 # Hilt dependency injection, theme vibe engine, and screen flows.
```

---

## Design System & Vibe Engine

- **Base Theme**: Jet Black (`#0B0B10`) background, Dark Surface (`#13131D`), Card Surface (`#1B1B26`), Glass borders (`12% white`).
- **Typography**: Clean typographic hierarchy with Poppins/System font scaling:
  - Screen greeting: `28sp bold`
  - Headline: `20sp semibold`
  - Stat counters: `32–42sp bold`
  - Body: `14–15sp regular`
- **Theme Modes**: Choose between **Dark Theme**, **Light Theme** (Clean porcelain `#F7F8FC` with crisp dark typography), or **System Default** directly in Settings.
- **Dynamic Vibe Switcher**: Pick your vibe at any time from the Today dashboard or Settings screen to dynamically shift color schemes, gradients, and glowing accents across both Dark and Light modes.

---

## Native Android Capabilities

### 1. Distraction Blocker & Focus Shield
When **Distraction Blocker** is active (or when an app exceeds its daily boundary):
- Enak intercepts the target package name via `EnakAccessibilityService` and `EnakForegroundService`.
- Displays the live `BlockerActivity` overlay with the 3 direct actions:
  - 🏠 **"Exit [App] (Go Home)"**: Closes the distracting app immediately and returns to Android home.
  - ⏱️ **"Put Timer for a While (+5m)"**: Temporarily snoozes the blocker for 5 intentional minutes before re-engaging.
  - ⚡ **"Back to Enak"**: Navigates straight back to the Enak dashboard to start a focused session.
  - 🧘 **"Take a 60s Breather"**: Smooth circular box breathing exercise to reset dopamine cravings.
- Distracting apps remain shielded continuously until toggled off from Enak's Digital Wellbeing tab.

### 2. Real Usage Access Tracking
Uses Android's `UsageStatsManager.queryAndAggregateUsageStats()` to calculate actual foreground minutes spent per application today. Enak displays real screen time instead of mock values and never inspects personal user content.

### 3. Curated Mindful Learning Intentions
Directly embedded high-impact learning intentions with one-tap YouTube playback and automatic session goal configuration:
- **Learn AI in 15 Minutes**: Core fundamentals of deep learning & LLMs
- **English Learning Session**: Spoken fluency, pronunciation, and expression
- **How to Start Fitness**: Beginner home workouts & posture habits
- **Web Developer Roadmap**: Full-stack web technologies & system design in 2026

### 4. Interactive Mindful Lab & Thompson Sampling
- **The Mindful Garden**: Visual blooming plant with 5 growth stages (*Seedling*, *Zen Fern*, *Lotus*, *Bamboo*, *Ancient Bonsai*). Interactive watering and weed pruning controls.
- **Thompson Sampling Playground**: Real-time multi-armed bandit algorithm optimizing interventions (*Breather*, *Anchor Question*, *Soft Banner*, *AI Negotiation*) with live preview testing and 20-trial Monte Carlo simulation.

### 5. Multi-Tiered Milestones & Streaks
- **7-Day Streak Calendar**: Visual tracking with active daily badges.
- **Tiered Milestones**: 3-Day Foundation (🥉), 7-Day Consistency (🥈), 14-Day Mastery (🥇), and 30-Day Digital Sovereign (💎) with progress bars and unlocked garden species.

### 6. Strict Mode (Device Administrator)
Prevents compulsive uninstallation or force-quitting through `EnakDeviceAdminReceiver` during active focus sessions.

### 7. Background Persistence
Exempt from aggressive Android Doze battery optimizations via `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, with auto-resumption following reboot via `BootReceiver`.

---

## Permissions Architecture

| Permission | Category | Purpose in Enak |
|---|---|---|
| `android.permission.PACKAGE_USAGE_STATS` | Special Access | Computes foreground minutes spent in target apps today. |
| `android.permission.SYSTEM_ALERT_WINDOW` | Special Access | Renders the gentle intervention overlay over distracting apps. |
| `android.permission.BIND_ACCESSIBILITY_SERVICE` | Accessibility | Detects foreground app launches instantly with zero battery drain. |
| `android.permission.BIND_DEVICE_ADMIN` | System Admin | Powers Strict Mode to prevent impulsively turning off timers. |
| `android.permission.BIND_NOTIFICATION_LISTENER_SERVICE` | Notification Access | Silences notifications from distracting apps during active focus. |
| `android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Battery | Prevents Android Doze from killing background timers. |
| `android.permission.FOREGROUND_SERVICE` | Foreground | Keeps the mindful shield responsive during active device usage. |
| `android.permission.POST_NOTIFICATIONS` | Runtime (33+) | Sends gentle check-ins and session end reminders. |
| `android.permission.RECORD_AUDIO` | Runtime | Allows voice input when speaking with the Mindful AI Coach. |

---

## Google Play Store Policy Declarations

1. **Usage Access (`PACKAGE_USAGE_STATS`)**:
   - Enak queries package names and elapsed timestamps exclusively to tally daily screen minutes. Enak never accesses, records, or transmits in-app screens or user inputs.
2. **Accessibility Service (`EnakAccessibilityService`)**:
   - Accessibility is utilized solely for detecting window transition events (`TYPE_WINDOW_STATE_CHANGED`) to trigger configured timer overlays.
   - Enak explicitly **does NOT** read screen text, intercept keystrokes, or modify device settings.
3. **Targeted Package Visibility (`<queries>`)**:
   - Enak does **not** request broad `QUERY_ALL_PACKAGES` access. It uses targeted `<queries>` matching `android.intent.action.MAIN` with category `LAUNCHER` to discover user apps.

---

## Building from Source

### Prerequisites
- Android Studio Ladybug / Meerkat (or newer)
- JDK 17 (Eclipse Temurin 17 recommended)
- Android SDK 35 (minSdk 26)

### 1. Build Debug APK
```bash
cd android
./gradlew assembleDebug
```
Output located at: `app/build/outputs/apk/debug/app-debug.apk`

### 2. Build Signed Release APK
Generate a release keystore (already pre-configured in project):
```bash
cd android
./gradlew assembleRelease
```
Output located at: `app/build/outputs/apk/release/app-release.apk`

### 3. Run Unit Tests
```bash
cd android
./gradlew test
```

---

## Privacy & Local-First Guarantee

- **Zero Cloud Data Transmission**: All session logs, screen statistics, and bandit weights remain strictly on your local device in Room Database and DataStore.
- **Export & Delete**: Export your data anytime as a formatted JSON document, or purge all records with a single tap in Settings.

---

<div align="center">

Crafted with care for mindful, intentional digital wellbeing.

</div>
