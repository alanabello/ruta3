package com.ruta3.app;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class Ruta3AccessibilityService extends AccessibilityService {
    private static final long MIN_UPDATE_INTERVAL_MS = 900;
    private String lastSignature = "";
    private String lastPackageName = "";
    private long lastUpdateAt = 0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        String packageName = event.getPackageName() == null ? "" : event.getPackageName().toString();
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            publishCurrentApp(packageName);
        }

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        StringBuilder text = new StringBuilder();
        collectText(root, text);

        RideOffer offer = RideOfferParser.parse(
                packageName,
                text.toString(),
                Ruta3Settings.getMinPerKm(this),
                Ruta3Settings.getMinPerHour(this)
        );
        if (offer == null) return;

        String signature = offer.platform + "|" + offer.amount + "|" + offer.totalMin + "|" + offer.totalKm;
        long now = System.currentTimeMillis();
        if (signature.equals(lastSignature) && now - lastUpdateAt < MIN_UPDATE_INTERVAL_MS) {
            return;
        }

        lastSignature = signature;
        lastUpdateAt = now;
        publishOffer(offer);
    }

    @Override
    public void onInterrupt() {
    }

    private void collectText(AccessibilityNodeInfo node, StringBuilder out) {
        if (node == null) return;
        CharSequence text = node.getText();
        CharSequence description = node.getContentDescription();
        if (text != null && text.length() > 0) {
            out.append(text).append(' ');
        }
        if (description != null && description.length() > 0) {
            out.append(description).append(' ');
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                collectText(child, out);
            }
        }
    }

    private void publishOffer(RideOffer offer) {
        Intent intent = new Intent(this, FloatingOverlayService.class);
        intent.setAction(FloatingOverlayService.ACTION_SHOW_OFFER);
        intent.putExtra(FloatingOverlayService.EXTRA_TITLE, offer.meets ? "Buen viaje" : "No conviene");
        intent.putExtra(FloatingOverlayService.EXTRA_VALUE, "$" + Math.round(offer.perKm) + "/km");
        intent.putExtra(
                FloatingOverlayService.EXTRA_NOTE,
                offer.platform + " - " + Math.round(offer.totalMin) + " min - " +
                        formatOneDecimal(offer.totalKm) + " km - $" + Math.round(offer.perHour) + "/h"
        );
        intent.putExtra(FloatingOverlayService.EXTRA_MEETS, offer.meets);
        OverlayLauncher.startOverlayService(this, intent);
    }

    private void publishCurrentApp(String packageName) {
        if (packageName == null || packageName.isEmpty() || packageName.equals(lastPackageName)) {
            return;
        }

        lastPackageName = packageName;

        Intent intent = new Intent(this, FloatingOverlayService.class);
        intent.setAction(FloatingOverlayService.ACTION_APP_CHANGED);
        intent.putExtra(FloatingOverlayService.EXTRA_PACKAGE_NAME, packageName);
        intent.putExtra(FloatingOverlayService.EXTRA_APP_NAME, getAppLabel(packageName));
        OverlayLauncher.startOverlayService(this, intent);
    }

    private String getAppLabel(String packageName) {
        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo info = packageManager.getApplicationInfo(packageName, 0);
            CharSequence label = packageManager.getApplicationLabel(info);
            return label == null ? packageName : label.toString();
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }

    private String formatOneDecimal(double value) {
        return String.format(java.util.Locale.US, "%.1f", value);
    }
}
