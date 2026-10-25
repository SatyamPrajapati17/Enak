# Privacy Policy for Enak

**Last Updated:** October 2026

Enak ("we", "our", or "us") is dedicated to protecting your privacy. This Privacy Policy details our practices concerning data collection, usage, and disclosure when you use the Enak Android application.

## 1. Local-First Philosophy
Enak is built on a local-first architecture. All session histories, goal definitions, intentionality scores, reflection journals, and app usage logs remain stored exclusively on your device in a local Room database and DataStore preferences. We do not sell, rent, or monetize your personal data.

## 2. Permissions & Data Collection

### a. Package Usage Stats (`PACKAGE_USAGE_STATS`)
- **What is collected:** Timestamps of app foreground resume and pause events, alongside application package identifiers (e.g., `com.instagram.android`).
- **Purpose:** To calculate total minutes spent per app today, evaluate your configured daily boundaries, and alert you before overruns occur.
- **Handling:** All usage statistics are aggregated on-device. No usage metrics are transmitted to any remote server without your explicit manual JSON export action.

### b. Accessibility Service (`EnakAccessibilityService`)
- **What is observed:** The package name of the active foreground window (`TYPE_WINDOW_STATE_CHANGED`).
- **Purpose:** Instantaneous enforcement of your mindful timers and focus modes.
- **Explicit Exclusions:** Enak never accesses, logs, reads, or transmits on-screen text, typed passwords, form submissions, payment details, or personal conversations.

### c. Audio Recording (`RECORD_AUDIO`)
- **Purpose:** Used strictly for interactive voice chats with the AI coach when you press the microphone button.
- **Handling:** Spoken audio is converted to text either via device-level speech recognition or processed ephemerally. Audio recordings are never retained or stored on permanent media.

### d. Overlay Permission (`SYSTEM_ALERT_WINDOW`)
- **Purpose:** Renders the mindful intervention shield over target apps when daily limits are exhausted.

## 3. Artificial Intelligence & Third-Party APIs
Conversations with the AI Coach and time negotiations are processed through Google Gemini API. Only the immediate prompt, declared session purpose, and high-level statistical summaries are provided to the model. Data shared with the AI is never used to train public models.

## 4. User Control & Data Deletion
You retain complete sovereignty over your data:
- **Export Data:** You can export all local records at any time as a structured JSON file.
- **Erase All Data:** The Settings menu contains a permanent "Delete Everything" action that immediately wipes the local Room database and DataStore.

## 5. Contact
For questions regarding this Privacy Policy, please contact privacy@enak.app.
