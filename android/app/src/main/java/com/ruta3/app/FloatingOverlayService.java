package com.ruta3.app;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FloatingOverlayService extends Service {
    private WindowManager windowManager;
    private LinearLayout overlayLayout;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        overlayLayout = new LinearLayout(this);
        overlayLayout.setOrientation(LinearLayout.VERTICAL);
        overlayLayout.setBackgroundColor(Color.parseColor("#1c1f26"));
        overlayLayout.setPadding(dp(12), dp(10), dp(12), dp(10));
        overlayLayout.setMinimumWidth(dp(200));
        overlayLayout.setMinimumHeight(dp(84));

        TextView title = new TextView(this);
        title.setText("Resultado rápido");
        title.setTextColor(Color.parseColor("#9a9da6"));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);

        TextView value = new TextView(this);
        value.setText("$0");
        value.setTextColor(Color.parseColor("#f2f1ea"));
        value.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        value.setTypeface(value.getTypeface(), android.graphics.Typeface.BOLD);

        TextView note = new TextView(this);
        note.setText("Cumple tus requisitos");
        note.setTextColor(Color.parseColor("#7fd858"));
        note.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);

        overlayLayout.addView(title);
        overlayLayout.addView(value);
        overlayLayout.addView(note);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.END;
        params.x = dp(16);
        params.y = dp(120);

        windowManager.addView(overlayLayout, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayLayout != null && windowManager != null) {
            windowManager.removeView(overlayLayout);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        ));
    }
}
