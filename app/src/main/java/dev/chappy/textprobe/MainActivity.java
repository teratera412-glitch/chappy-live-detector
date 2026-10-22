package dev.chappy.textprobe;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/** On-device readout for the metadata-only Accessibility event experiment. */
public final class MainActivity extends Activity {
    private static final int LIGHT = 0xffd6e4f0;
    private TextView statusView;
    private TextView resultsView;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        buildScreen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private boolean isServiceEnabled() {
        AccessibilityManager manager = (AccessibilityManager)
                getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (manager == null) return false;
        List<AccessibilityServiceInfo> list =
                manager.getEnabledAccessibilityServiceList(
                        AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        String serviceClass = TextEventService.class.getName();
        for (AccessibilityServiceInfo info : list) {
            if (info.getResolveInfo() != null
                    && info.getResolveInfo().serviceInfo != null
                    && getPackageName().equals(info.getResolveInfo().serviceInfo.packageName)
                    && serviceClass.equals(info.getResolveInfo().serviceInfo.name)) {
                return true;
            }
        }
        return false;
    }

    private void refresh() {
        boolean enabled = isServiceEnabled();
        statusView.setText(enabled
                ? "監視権限: ON  ✅（ChatGPTイベントを収集）"
                : "監視権限: OFF  ⚠️（Android設定で有効にする）");
        statusView.setTextColor(enabled ? 0xff66e9b9 : 0xffffd18a);
        resultsView.setText(ProbeStore.report(this));
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0xff101725);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(16), dp(24), dp(16), dp(36));
        scroll.addView(layout);

        addText(layout, "CHAPPY Text Probe", 25, Color.WHITE).setTypeface(null, Typeface.BOLD);
        addText(layout, "STEP 2-D | 公式Liveの文字更新検知", 14, 0xff9fb4cb);
        gap(layout, 16);
        statusView = addText(layout, "監視権限を確認中…", 17, Color.WHITE);
        gap(layout, 10);
        addButton(layout, "① Androidのアクセシビリティ設定を開く", () -> {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        });
        addButton(layout, "② 結果をリセット（測定前）", () -> {
            ProbeStore.reset(this);
            refresh();
            Toast.makeText(this, "カウンタをリセットしました", Toast.LENGTH_SHORT).show();
        });
        addButton(layout, "③ 結果を更新（ChatGPTから戻ったら）", this::refresh);
        addButton(layout, "④ 結果をコピー", () -> {
            ClipboardManager manager = (ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);
            manager.setPrimaryClip(ClipData.newPlainText(
                    "CHAPPY Text Probe results", ProbeStore.report(this)));
            Toast.makeText(this, "結果をコピーしました", Toast.LENGTH_SHORT).show();
        });
        gap(layout, 15);
        addText(layout, "【最初のテスト】", 17, Color.WHITE).setTypeface(null, Typeface.BOLD);
        addText(layout,
                "1. 設定から「CHAPPY Text Probe」のアクセシビリティを自分でONにする。\n"
                        + "2. この画面に戻り「結果をリセット」を押す。\n"
                        + "3. ChatGPT Liveを前面に出して数秒話す。\n"
                        + "4. 会話を終えて戻り「結果を更新」。数字を確認する。\n"
                        + "5. 次の段階で、Mikoを前面に出した状態でも比較する。",
                15, LIGHT);
        gap(layout, 15);
        addText(layout, "【イベント統計】", 17, Color.WHITE).setTypeface(null, Typeface.BOLD);
        resultsView = addText(layout, "未取得", 13, 0xff6be8bb);
        resultsView.setTypeface(Typeface.MONOSPACE);
        gap(layout, 15);
        addText(layout,
                "【重要】Androidのアクセシビリティは画面に関する強い権限です。"
                        + "本アプリの実装はcom.openai.chatgptのイベント種類と時刻だけを保存し、"
                        + "文字本文、パスワード、画面画像、音声は取得・送信しません。"
                        + "ネットワーク権限もありません。計測が終わったら設定からOFFにしてください。"
                        + "イベント発生＝CHAPPYの発話とは限りません。",
                13, 0xffffcf8b);
        setContentView(scroll);
    }

    private TextView addText(LinearLayout root, String text, int sp, int color) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setTextIsSelectable(false);
        root.addView(view, new LinearLayout.LayoutParams(-1, -2));
        return view;
    }

    private void addButton(LinearLayout root, String title, Runnable action) {
        Button button = new Button(this);
        button.setText(title);
        button.setAllCaps(false);
        button.setTextSize(14);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(56));
        params.topMargin = dp(7);
        root.addView(button, params);
        button.setOnClickListener(v -> action.run());
    }

    private void gap(LinearLayout root, int amount) {
        View spacer = new View(this);
        root.addView(spacer, new LinearLayout.LayoutParams(1, dp(amount)));
    }

    private int dp(int count) {
        return Math.round(count * getResources().getDisplayMetrics().density);
    }
}
