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

/** Diagnostic UI for four controlled 10-second comparison tests. */
public final class MainActivity extends Activity {
    private TextView statusView;
    private TextView reportView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildScreen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private boolean isServiceEnabled() {
        AccessibilityManager mgr = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (mgr == null) return false;
        List<AccessibilityServiceInfo> enabled =
                mgr.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo info : enabled) {
            if (info.getResolveInfo() != null && info.getResolveInfo().serviceInfo != null
                    && getPackageName().equals(info.getResolveInfo().serviceInfo.packageName)
                    && TextEventService.class.getName().equals(info.getResolveInfo().serviceInfo.name)) {
                return true;
            }
        }
        return false;
    }

    private void refresh() {
        boolean enabled = isServiceEnabled();
        statusView.setText(enabled ? "Accessibility監視: ON ✅" : "Accessibility監視: OFF ⚠️");
        statusView.setTextColor(enabled ? 0xff63e5b2 : 0xffffce80);
        reportView.setText(ProbeStore.report(this));
    }

    private void startTest(String mode) {
        if (!isServiceEnabled()) {
            Toast.makeText(this, "先にAccessibility監視を有効にしてください", Toast.LENGTH_LONG).show();
            return;
        }
        ProbeStore.start(this, mode);
        refresh();
        Toast.makeText(this, "6秒後から10秒間だけ測定します", Toast.LENGTH_LONG).show();
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0xff101725);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(22), dp(16), dp(30));
        scroll.addView(root);

        addText(root, "CHAPPY Text Probe v0.3", 24, Color.WHITE).setTypeface(null, Typeface.BOLD);
        addText(root, "STEP 2-D | イベント正体切り分け実験", 14, 0xffa5bbd1);
        gap(root, 13);
        statusView = addText(root, "監視状態を確認中", 17, 0xffffce80);
        addButton(root, "Android Accessibility設定", () ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        gap(root, 10);
        addText(root, "【共通操作】", 16, Color.WHITE).setTypeface(null, Typeface.BOLD);
        addText(root,
                "① 試験を1つ選ぶ\n"
                + "② 6秒の準備時間内にChatGPTの指定画面に移動\n"
                + "③ 続く10秒だけ指定条件を保つ\n"
                + "④ Probeに戻って「結果更新」→「コピー」\n"
                + "※別条件を開始すると前回の記録を上書きします。先にコピーしてね。",
                14, 0xffd4e1ef);
        gap(root, 10);
        addButton(root, "A｜通常チャット画面・Liveなし", () -> startTest("A_通常画面_Liveなし"));
        addButton(root, "B｜Live文字表示・10秒無言", () -> startTest("B_Live文字表示_無言"));
        addButton(root, "C｜Live文字表示・CHAPPY発話", () -> startTest("C_Live文字表示_発話"));
        addButton(root, "D｜Live終了の瞬間を計測", () -> startTest("D_Live終了操作"));
        gap(root, 9);
        addButton(root, "結果を更新", this::refresh);
        addButton(root, "結果をコピー", () -> {
            ClipboardManager mgr = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            mgr.setPrimaryClip(ClipData.newPlainText("CHAPPY v0.3 result", ProbeStore.report(this)));
            Toast.makeText(this, "コピーしました", Toast.LENGTH_SHORT).show();
        });
        gap(root, 13);
        addText(root, "【10秒間の1秒ごとの集計】", 16, Color.WHITE).setTypeface(null, Typeface.BOLD);
        reportView = addText(root, "", 13, 0xff67e8b7);
        reportView.setTypeface(Typeface.MONOSPACE);
        gap(root, 12);
        addText(root,
                "注意：com.openai.chatgptのイベント種類・フラグ・時刻のみ保存。"
                + "文字本文・画面内容・音声・画像は収集しません。"
                + "ネットワーク権限なし。検証後はAccessibility監視をOFFにしてください。"
                + "イベントが発生してもCHAPPYの発話を示すとは限りません。",
                13, 0xffffd18a);
        setContentView(scroll);
    }

    private TextView addText(LinearLayout parent, String text, int sizeSp, int color) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        parent.addView(view, new LinearLayout.LayoutParams(-1, -2));
        return view;
    }

    private void addButton(LinearLayout parent, String title, Runnable action) {
        Button button = new Button(this);
        button.setText(title);
        button.setAllCaps(false);
        button.setTextSize(14);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(52));
        p.topMargin = dp(6);
        parent.addView(button, p);
        button.setOnClickListener(v -> action.run());
    }

    private void gap(LinearLayout parent, int heightDp) {
        View spacer = new View(this);
        parent.addView(spacer, new LinearLayout.LayoutParams(1, dp(heightDp)));
    }

    private int dp(int n) {
        return Math.round(n * getResources().getDisplayMetrics().density);
    }
}
