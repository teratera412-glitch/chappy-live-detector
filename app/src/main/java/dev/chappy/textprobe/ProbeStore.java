package dev.chappy.textprobe;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fixed 10-second event measurements, with a 6-second preparation delay.
 * Counts only event types, flags and timestamps from the official ChatGPT package.
 * Never reads event text or accessibility node hierarchy.
 */
final class ProbeStore {
    private static final String NAME = "probe_stats_v03";
    private static final int PREP_MS = 6000;
    private static final int WINDOW_MS = 10000;
    private static final int LAST_LOG_LIMIT = 35;

    private ProbeStore() { }

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    private static String clock(long wall) {
        return new SimpleDateFormat("HH:mm:ss.SSS", Locale.JAPAN).format(new Date(wall));
    }

    static void start(Context c, String testName) {
        long now = SystemClock.uptimeMillis();
        long begin = now + PREP_MS;
        long end = begin + WINDOW_MS;
        long beginWall = System.currentTimeMillis() + PREP_MS;
        prefs(c).edit().clear()
                .putString("name", testName)
                .putLong("startUp", begin)
                .putLong("stopUp", end)
                .putLong("startWall", beginWall)
                .putLong("finishWall", beginWall + WINDOW_MS)
                .apply();
    }

    static void append(Context c, int eventType, int flags) {
        int kind;
        if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            kind = 1;
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            kind = (flags & AccessibilityEvent.CONTENT_CHANGE_TYPE_TEXT) != 0 ? 2 : 3;
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            kind = 4;
        } else {
            return;
        }
        SharedPreferences p = prefs(c);
        long start = p.getLong("startUp", -1);
        long now = SystemClock.uptimeMillis();
        if (start < 0 || now < start || now >= p.getLong("stopUp", -1)) {
            return; // The preparation delay and anything after 10s are excluded.
        }
        int sec = (int) ((now - start) / 1000);
        String prefix = "sec" + sec + "_";
        String key = kind == 1 ? "view" : kind == 2 ? "text"
                : kind == 3 ? "other" : "window";
        int flagCategory = (flags & 64) != 0 ? 64 : (flags == 0 ? 0 : 1);
        String flagKey = "flag" + flagCategory;

        String tail = p.getString("tail", "");
        String[] lines = tail.isEmpty() ? new String[0] : tail.split("\n");
        StringBuilder newTail = new StringBuilder();
        for (int i = Math.max(0, lines.length - LAST_LOG_LIMIT + 1); i < lines.length; i++) {
            newTail.append(lines[i]).append('\n');
        }
        newTail.append(clock(System.currentTimeMillis())).append(" sec=").append(sec)
                .append(' ').append(key).append(" flags=").append(flags).append('\n');

        SharedPreferences.Editor edit = p.edit()
                .putInt("total", p.getInt("total", 0) + 1)
                .putInt(prefix + "total", p.getInt(prefix + "total", 0) + 1)
                .putInt(prefix + key, p.getInt(prefix + key, 0) + 1)
                .putInt(prefix + flagKey, p.getInt(prefix + flagKey, 0) + 1)
                .putString("tail", newTail.toString())
                .putLong("lastWall", System.currentTimeMillis());
        if (p.getLong("firstWall", 0) == 0) {
            edit.putLong("firstWall", System.currentTimeMillis());
        }
        edit.apply();
    }

    static String report(Context c) {
        SharedPreferences p = prefs(c);
        String name = p.getString("name", "");
        if (name.isEmpty()) {
            return "CHAPPY Text Probe v0.3\n未測定です。試験ボタンを押してください。";
        }
        long now = SystemClock.uptimeMillis();
        long start = p.getLong("startUp", 0);
        long finish = p.getLong("stopUp", 0);
        String state = now < start ? "準備時間（まだ集計しません）"
                : now < finish ? "測定中（終了まで " + ((finish - now + 999) / 1000) + "秒）"
                : "測定終了";
        StringBuilder s = new StringBuilder();
        s.append("CHAPPY Text Probe v0.3\n")
         .append("対象: com.openai.chatgpt\n")
         .append("会話本文・画面画像は取得／保存しません\n")
         .append("試験: ").append(name).append('\n')
         .append("状態: ").append(state).append('\n')
         .append("準備: 6秒 / 測定: 10秒\n")
         .append("開始: ").append(clock(p.getLong("startWall", 0))).append('\n')
         .append("終了: ").append(clock(p.getLong("finishWall", 0))).append('\n')
         .append("受信総数: ").append(p.getInt("total", 0)).append("\n\n")
         .append("秒 | 合計 | 文字 | その他 | flag64 | flag0\n");
        int texts = 0, others = 0, flag64 = 0;
        for (int sec = 0; sec < 10; sec++) {
            String pre = "sec" + sec + "_";
            int text = p.getInt(pre + "text", 0) + p.getInt(pre + "view", 0);
            int other = p.getInt(pre + "other", 0);
            int flags64 = p.getInt(pre + "flag64", 0);
            int flags0 = p.getInt(pre + "flag0", 0);
            texts += text;
            others += other;
            flag64 += flags64;
            s.append(sec).append("   ")
             .append(p.getInt(pre + "total", 0)).append("     ")
             .append(text).append("     ")
             .append(other).append("       ")
             .append(flags64).append("       ")
             .append(flags0).append('\n');
        }
        s.append("\n文字関連合計=").append(texts)
         .append(" / その他=").append(others)
         .append(" / flags64=").append(flag64).append('\n');
        if (p.getLong("firstWall", 0) > 0) {
            s.append("最初の通知=").append(clock(p.getLong("firstWall", 0))).append('\n')
             .append("最後の通知=").append(clock(p.getLong("lastWall", 0))).append('\n');
        } else {
            s.append("測定窓内のイベントなし\n");
        }
        s.append("\n直近").append(LAST_LOG_LIMIT).append("件（全件ではありません）:\n")
         .append(p.getString("tail", ""));
        return s.toString();
    }
}
