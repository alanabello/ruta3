package com.ruta3.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        registerPlugin(Ruta3ApkInstallerPlugin.class);
        registerPlugin(Ruta3SettingsPlugin.class);
        super.onCreate(savedInstanceState);
        OverlayLauncher.startOverlay(this);
        requestNotificationPermission();
        if (!AccessibilityLauncher.isEnabled(this)) {
            AccessibilityLauncher.requestEnable(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        OverlayLauncher.startOverlay(this);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
        }
    }
}
