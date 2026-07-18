package com.ruta3.app;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FloatingOverlayService extends Service {
    public static final String ACTION_SHOW_OFFER = "com.ruta3.app.SHOW_OFFER";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_VALUE = "value";
    public static final String EXTRA_NOTE = "note";
    public static final String EXTRA_MEETS = "meets";

    private WindowManager windowManager;
    private LinearLayout overlayLayout;
    private TextView titleView;
    private TextView valueView;
    private TextView noteView;
    private GradientDrawable bubble;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        overlayLayout = new LinearLayout(this);
        overlayLayout.setOrientation(LinearLayout.VERTICAL);
        overlayLayout.setPadding(dp(12), dp(10), dp(12), dp(10));
        overlayLayout.setMinimumWidth(dp(200));
        overlayLayout.setMinimumHeight(dp(82));

        bubble = new GradientDrawable();
        bubble.setShape(GradientDrawable.RECTANGLE);
        bubble.setColor(Color.parseColor("#1c1f26"));
        bubble.setCornerRadius(dp(18));
        bubble.setStroke(dp(1), Color.parseColor("#2c303a"));
        overlayLayout.setBackground(bubble);

        titleView = new TextView(this);
        titleView.setText("Ruta3 activo");
        titleView.setTextColor(Color.parseColor("#9a9da6"));
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);

        valueView = new TextView(this);
        valueView.setText("Esperando oferta");
        valueView.setTextColor(Color.parseColor("#f2f1ea"));
        valueView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        valueView.setTypeface(valueView.getTypeface(), android.graphics.Typeface.BOLD);

        noteView = new TextView(this);
        noteView.setText("Abre Uber, Didi o InDrive");
        noteView.setTextColor(Color.parseColor("#9a9da6"));
        noteView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);

        overlayLayout.addView(titleView);
        overlayLayout.addView(valueView);
        overlayLayout.addView(noteView);

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
        params.y = dp(110);

        windowManager.addView(overlayLayout, params);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_SHOW_OFFER.equals(intent.getAction())) {
            updateOffer(
                    intent.getStringExtra(EXTRA_TITLE),
                    intent.getStringExtra(EXTRA_VALUE),
                    intent.getStringExtra(EXTRA_NOTE),
                    intent.getBooleanExtra(EXTRA_MEETS, false)
            );
        }
        return START_STICKY;
    }

    private void updateOffer(String title, String value, String note, boolean meets) {
        if (titleView == null || valueView == null || noteView == null) return;

        titleView.setText(title == null ? "Oferta detectada" : title);
        valueView.setText(value == null ? "--" : value);
        noteView.setText(note == null ? "" : note);
        noteView.setTextColor(Color.parseColor(meets ? "#7fd858" : "#ff6459"));
        bubble.setStroke(dp(1), Color.parseColor(meets ? "#4f8f36" : "#8f3630"));
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
