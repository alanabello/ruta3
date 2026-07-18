package com.ruta3.app;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        registerPlugin(Ruta3SettingsPlugin.class);
        super.onCreate(savedInstanceState);
        OverlayLauncher.startOverlay(this);
        if (!AccessibilityLauncher.isEnabled(this)) {
            AccessibilityLauncher.requestEnable(this);
        }
    }
}
