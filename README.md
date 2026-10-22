# CHAPPY Text Probe (STEP 2-D)

**Branch:** `experiment/text-event-detector-v1`

This is a metadata-only accessibility event probe for Android.
It observes events from `com.openai.chatgpt`, records type/timestamp
and an aggregate count locally, and never accesses or stores
message text, screen images, audio or passwords.
No network permission; no microphone or recording permission.

## Goal
Determine if the official ChatGPT Live app emits accessibility text/content
update events in the foreground and after it moves behind the browser/Miko UI.
This is *not* proof of voice activity detection or lip-sync.

## Successful build base
Uses the Gradle/Android SDK GitHub Actions setup proven by CHAPPY Live Detector
Run #5 on the `main` branch. The known-good Detector version remains
unchanged on `main`.

## User test
1. Install the `CHAPPY-Text-Probe-debug-APK` artifact from this branch.
2. Open the app and tap Android Accessibility settings.
3. Enable CHAPPY Text Probe explicitly; Android 13+ may restrict sideloaded
   app accessibility toggles. The user must decide whether to allow it.
4. Return, reset results, switch to ChatGPT Live foreground, talk ~10s, return,
   and inspect the counters.
5. Repeat with ChatGPT Live behind Miko/browser if the foreground test responds.
6. Turn off the accessibility service after testing.

This service only receives accessibility events permitted by Android. Even
if text/content events are detected, they are not necessarily speech events.
