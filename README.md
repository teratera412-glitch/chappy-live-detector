# CHAPPY Text Probe v0.3 — Event source diagnostic

**Branch**: `experiment/text-event-timing-v03`

Purpose: determine whether ChatGPT accessibility change events correlate with
Live speech or instead with UI animations, opening/closing Live or window changes.

Preservation: `main` holds the original playback Detector; `experiment/text-event-detector-v1`
holds the successful v0.2. Neither branch is altered by this version.

## Measurement

Tap one of the test buttons. The app gives 6 seconds to switch to the indicated
ChatGPT view, then records exactly 10 seconds of accessibility event *metadata*
and saves per-second counts. The 6-second preparation period and subsequent
events (including returning to Probe) are excluded.

A. Standard ChatGPT screen without Live.
B. Live with text visible, remain silent.
C. Live with text visible, CHAPPY speaks during 10s measurement.
D. Live end operation during 10s measurement (record separately).

Return to Probe, refresh and copy result before starting the next test. The
app keeps only the latest measurement. Watch out for the 6s preparation delay.

Timestamps now use Java SimpleDateFormat `HH:mm:ss.SSS`. All per-second
windows use monotonic `SystemClock.uptimeMillis()` to avoid confusing time
changes with event timing. Last 35 events are retained only as illustrative
examples, while 10 per-second aggregate counts cover the full measurement.

## Privacy / limitations

Target package is restricted to `com.openai.chatgpt`. This test does not
access text nodes, message bodies, screenshots, microphone or network.
It needs user-enabled Android Accessibility permission, which should be turned
off after testing. Event timing is not proof that the signal identifies speech.
