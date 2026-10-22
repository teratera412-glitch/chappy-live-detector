package dev.chappy.textprobe;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

/**
 * Diagnostic only. Records no text, node hierarchy or other personal data.
 * Restricted to the official ChatGPT Android package in XML and Java.
 */
public final class TextEventService extends AccessibilityService {
    private static final String CHATGPT_PACKAGE = "com.openai.chatgpt";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) {
            return;
        }
        if (!CHATGPT_PACKAGE.contentEquals(event.getPackageName())) {
            return;
        }
        ProbeStore.append(this, event.getEventType(), event.getContentChangeTypes());
    }

    @Override
    public void onInterrupt() {
        // No active interaction, gestures, screenshot or window introspection.
    }
}
