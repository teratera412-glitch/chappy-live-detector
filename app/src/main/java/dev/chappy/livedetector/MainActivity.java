package dev.chappy.livedetector;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.AudioPlaybackConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * STEP 2-C: Playback state observer. No microphone, recording or network access.
 * This app counts playback configurations visible to its Android process.
 */
public class MainActivity extends Activity {
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final StringBuilder history = new StringBuilder();
    private final SimpleDateFormat clock = new SimpleDateFormat("HH:mm:ss.SSS", Locale.JAPAN);

    private AudioManager audioManager;
    private TextView countView;
    private TextView lastView;
    private TextView historyView;
    private AudioManager.AudioPlaybackCallback callback;
    private int lastCount = -1;
    private int eventCount = 0;
    private boolean registered = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        buildScreen();
        addLog("起動。録音もマイク使用もしません。");
        addLog("Liveをバックグラウンドにして、この画面を前面表示してください。");
        callback = new AudioManager.AudioPlaybackCallback() {
            @Override
            public void onPlaybackConfigChanged(List<AudioPlaybackConfiguration> configs) {
                updateStatus(configs, true);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (!registered) {
                audioManager.registerAudioPlaybackCallback(callback, mainHandler);
                registered = true;
                addLog("再生状態の監視を開始");
            }
            updateStatus(audioManager.getActivePlaybackConfigurations(), false);
        } catch (SecurityException | IllegalStateException e) {
            addLog("監視エラー: " + e.getClass().getSimpleName());
        }
    }

    @Override
    protected void onPause() {
        if (registered) {
            audioManager.unregisterAudioPlaybackCallback(callback);
            registered = false;
        }
        super.onPause();
    }

    private void updateStatus(List<AudioPlaybackConfiguration> configs, boolean fromCallback) {
        // Android returns a list of currently active playback configurations.
        // isActive()/getPlayerState() are not public APIs on this class.
        int visible = configs == null ? 0 : configs.size();
        int active = visible;
        countView.setText("再生中の構成数: " + active);
        boolean changed = active != lastCount;
        lastCount = active;
        if (fromCallback) eventCount++;
        lastView.setText("取得した構成: " + visible + " / 通知回数: " + eventCount + "回");
        if (fromCallback || changed) {
            addLog((fromCallback ? "通知" : "初期状態") + ": 再生中=" + active +
                    " / 構成=" + visible + (changed ? " [数値変化]" : " [数値同じ]"));
        }
    }

    private void addLog(String line) {
        history.append(clock.format(new Date())).append("  ").append(line).append('\n');
        if (historyView != null) historyView.setText(history.toString());
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(14, 20, 31));
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(18);
        layout.setPadding(pad, dp(24), pad, dp(32));
        scroll.addView(layout);

        TextView title = addText(layout, "CHAPPY Live Detector", 24, Color.WHITE);
        title.setTypeface(null, Typeface.BOLD);
        addText(layout, "STEP 2-C  |  Android 再生状態テスト", 14, 0xffa7b8cb);
        addGap(layout, 18);
        countView = addText(layout, "再生中の構成数: --", 25, 0xff67e8b9);
        lastView = addText(layout, "取得した構成: -- / 通知回数: --", 14, 0xffd0dae7);
        addGap(layout, 20);
        addText(layout, "【使い方】", 17, Color.WHITE).setTypeface(null, Typeface.BOLD);
        addText(layout,
                "① ChatGPT Liveを開始する\n② ホームに戻り、このアプリを開く\n③ CHAPPY発話中と無言中の数字を比較する\n④ 下のボタンで観察時刻を記録する",
                15, 0xffdae6ef);
        addGap(layout, 10);
        addButton(layout, "🗣 CHAPPYが話している（時刻マーク）", () -> addLog("観察マーク: CHAPPY発話中 / 構成数=" + lastCount));
        addButton(layout, "🤫 CHAPPYが無言（時刻マーク）", () -> addLog("観察マーク: 無言 / 構成数=" + lastCount));
        addButton(layout, "ログをコピー", () -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("CHAPPY detector log", history.toString()));
            Toast.makeText(this, "コピーしました", Toast.LENGTH_SHORT).show();
        });
        addGap(layout, 12);
        addText(layout, "【状態ログ】", 17, Color.WHITE).setTypeface(null, Typeface.BOLD);
        historyView = addText(layout, "", 13, 0xffcbd5e1);
        historyView.setTypeface(Typeface.MONOSPACE);
        addGap(layout, 14);
        addText(layout,
                "注意：この数値は『CHAPPYが話している』ことを直接意味しません。ほかのアプリの再生も含まれます。" +
                "発話・無言で変化しなければ、この方式は不適合の可能性があります。" +
                "監視はこの画面が表示されている間だけ動作します。",
                13, 0xffffd18a);
        setContentView(scroll);
    }

    private void addButton(LinearLayout parent, String label, Runnable action) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(14);
        button.setAllCaps(false);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(54));
        params.topMargin = dp(6);
        parent.addView(button, params);
        button.setOnClickListener(v -> action.run());
    }

    private TextView addText(LinearLayout parent, String text, int sp, int color) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(sp);
        view.setTextColor(color);
        parent.addView(view, new LinearLayout.LayoutParams(-1, -2));
        return view;
    }

    private void addGap(LinearLayout parent, int dp) {
        View spacer = new View(this);
        parent.addView(spacer, new LinearLayout.LayoutParams(1, dp(dp)));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
