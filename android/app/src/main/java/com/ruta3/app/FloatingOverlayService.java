package com.ruta3.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FloatingOverlayService extends Service {
    public static final String ACTION_SHOW_OFFER = "com.ruta3.app.SHOW_OFFER";
    public static final String ACTION_APP_CHANGED = "com.ruta3.app.APP_CHANGED";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_VALUE = "value";
    public static final String EXTRA_NOTE = "note";
    public static final String EXTRA_MEETS = "meets";
    public static final String EXTRA_PACKAGE_NAME = "packageName";
    public static final String EXTRA_APP_NAME = "appName";

    private static final String CHANNEL_ID = "ruta3_overlay";
    private static final int NOTIFICATION_ID = 33;

    private WindowManager windowManager;
    private LinearLayout overlayLayout;
    private WindowManager.LayoutParams overlayParams;
    private TextView titleView;
    private TextView valueView;
    private TextView noteView;
    private GradientDrawable bubble;
    private String currentApp = "Sin app detectada";

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(NOTIFICATION_ID, buildNotification("Overlay activo"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            stopSelf();
            return;
        }

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
        noteView.setText(currentApp);
        noteView.setTextColor(Color.parseColor("#9a9da6"));
        noteView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);

        overlayLayout.addView(titleView);
        overlayLayout.addView(valueView);
        overlayLayout.addView(noteView);

        overlayParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );

        overlayParams.gravity = Gravity.TOP | Gravity.START;
        overlayParams.x = dp(16);
        overlayParams.y = dp(110);

        enableDrag();
        windowManager.addView(overlayLayout, overlayParams);
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
        } else if (intent != null && ACTION_APP_CHANGED.equals(intent.getAction())) {
            updateCurrentApp(
                    intent.getStringExtra(EXTRA_APP_NAME),
                    intent.getStringExtra(EXTRA_PACKAGE_NAME)
            );
        }
        return START_STICKY;
    }

    private Notification buildNotification(String text) {
        ensureNotificationChannel();

        Intent openAppIntent = new Intent(this, MainActivity.class);
        PendingIntent openAppPendingIntent = PendingIntent.getActivity(
                this,
                0,
                openAppIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_IMMUTABLE
                        : 0
        );

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);

        return builder
                .setSmallIcon(R.drawable.ic_stat_ruta3)
                .setContentTitle("Ruta3")
                .setContentText(text)
                .setContentIntent(openAppPendingIntent)
                .setOngoing(true)
                .build();
    }

    private void ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null || manager.getNotificationChannel(CHANNEL_ID) != null) return;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Overlay Ruta3",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Mantiene activa la vista flotante de Ruta3");
        manager.createNotificationChannel(channel);
    }

    private void enableDrag() {
        overlayLayout.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (overlayParams == null || windowManager == null) return false;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = overlayParams.x;
                        initialY = overlayParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        overlayParams.x = initialX + Math.round(event.getRawX() - initialTouchX);
                        overlayParams.y = initialY + Math.round(event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(overlayLayout, overlayParams);
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private void updateOffer(String title, String value, String note, boolean meets) {
        if (titleView == null || valueView == null || noteView == null) return;

        titleView.setText(title == null ? "Oferta detectada" : title);
        valueView.setText(value == null ? "--" : value);
        noteView.setText(note == null ? "" : note);
        noteView.setTextColor(Color.parseColor(meets ? "#7fd858" : "#ff6459"));
        bubble.setStroke(dp(1), Color.parseColor(meets ? "#4f8f36" : "#8f3630"));
    }

    private void updateCurrentApp(String appName, String packageName) {
        if (titleView == null || valueView == null || noteView == null) return;

        currentApp = appName == null || appName.isEmpty() ? packageName : appName;
        titleView.setText("App abierta");
        valueView.setText(currentApp == null || currentApp.isEmpty() ? "Desconocida" : currentApp);
        noteView.setText(packageName == null ? "" : packageName);
        noteView.setTextColor(Color.parseColor("#9a9da6"));
        bubble.setStroke(dp(1), Color.parseColor("#2c303a"));
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
