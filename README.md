# CHAPPY Live Detector (STEP 2-C)

Small Android diagnostic app for the **Project with CHAPPY** official ChatGPT Live voice path. Independent of existing Avatar 0 / Miko / CHAPPY Brain Worker assets.

## What this prototype does

- Shows count of playback configurations reported as active by Android AudioManager.
- Logs playback-configuration notifications and gives manual timestamp markers for "CHAPPY speaking" vs. "silent".
- Uses **no microphone, audio capture, network, or accessibility permissions**.
- Does not capture, record, transcribe, or modify ChatGPT audio.
- **Foreground-only diagnostic**: checks while its own screen is visible.

Android playback activity is **not** a direct indicator of spoken audio. ChatGPT Live may keep a player active through silent periods, and other apps contribute events.

## Build (GitHub Actions)

1. Open **Actions** -> **Build Android detector APK** in this repository.
2. Look for the workflow triggered by the push, or press **Run workflow**.
3. After a green-success run, open that run, download artifact **CHAPPY-Live-Detector-debug-APK**, extract the ZIP to get `app-debug.apk`.
4. Install it on Android after verifying it comes from this repository.

Public repo + GitHub-hosted standard runner is intended to stay on the free GitHub Actions path; paid runners and secrets are not used.

## Test

1. Start official ChatGPT Live.
2. Return to Home, open **CHAPPY Live Detector**, leaving Live in the background.
3. Compare the count and callback log during CHAPPY speech, silence, and your speech.
4. Tap the appropriate marker during each interval, copy the log and share results.
5. If needed, test with Live fully stopped as a baseline.

Success criterion for a future mouth-animation trigger: **repeatable state transition at speech start/end**. A static active count is not success.

## Build config

- Android minSdk 26 / compileSdk 35 / targetSdk 35
- Java 17, Gradle 8.11.1, Android Gradle Plugin 8.9.2
- Standard GitHub-hosted Ubuntu 24.04
