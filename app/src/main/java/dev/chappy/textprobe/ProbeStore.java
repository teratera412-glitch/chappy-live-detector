package dev.chappy.textprobe;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateFormat;
import android.view.accessibility.AccessibilityEvent;

/**
 * Stores only event metadata and time. Never stores message text,
 * accessibility node contents, clipboard contents, or screenshots.
 */
final class ProbeStore {
    private static final String NAME = "probe_stats_v1";
    private static final int MAX_LOG_LINES = 85;

    private ProbeStore() { }

    static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    static void append(Context context, int type, int contentChanges) {
        SharedPreferences p = prefs(context);
        String group;
        String key;
        if (type == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            group = "VIEW_TEXT_CHANGED";
            key = "view_text";
        } else if (type == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            if ((contentChanges & AccessibilityEvent.CONTENT_CHANGE_TYPE_TEXT) != 0) {
                group = "CONTENT_TEXT";
                key = "content_text";
            } else {
                group = "CONTENT_OTHER";
                key = "content_other";
            }
        } else if (type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            group = "WINDOW_STATE";
            key = "window_state";
        } else {
            return;
        }
        long now = System.currentTimeMillis();
        String line = DateFormat.format("HH:mm:ss.SSS", now)
                + " " + group + " flags=" + contentChanges;
        String old = p.getString("log", "");
        String[] items = old.isEmpty() ? new String[0] : old.split("\n");
        StringBuilder next = new StringBuilder();
        for (int i = Math.max(0, items.length - MAX_LOG_LINES + 1); i < items.length; i++) {
            next.append(items[i]).append('\n');
        }
        next.append(line).append('\n');
        p.edit()
                .putInt("total", p.getInt("total", 0) + 1)
                .putInt(key, p.getInt(key, 0) + 1)
                .putLong("last", now)
                .putString("log", next.toString())
                .apply();
    }

    static void reset(Context context) {
        prefs(context).edit().clear().apply();
    }

    static String report(Context context) {
        SharedPreferences p = prefs(context);
        return "CHAPPY Text Probe v0.2\n"
                + "対象: com.openai.chatgpt のAccessibilityイベントのみ\n"
                + "会話本文は取得・保存しません\n"
                + "TOTAL=" + p.getInt("total", 0) + "\n"
                + "VIEW_TEXT_CHANGED=" + p.getInt("view_text", 0) + "\n"
                + "CONTENT_TEXT=" + p.getInt("content_text", 0) + "\n"
                + "CONTENT_OTHER=" + p.getInt("content_other", 0) + "\n"
                + "WINDOW_STATE=" + p.getInt("window_state", 0) + "\n\n"
                + "最近のイベント（最大" + MAX_LOG_LINES + "件）:\n"
                + p.getString("log", "");
    }
}
