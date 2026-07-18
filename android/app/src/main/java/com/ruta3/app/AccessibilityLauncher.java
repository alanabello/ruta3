package com.ruta3.app;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

public class AccessibilityLauncher {
    private AccessibilityLauncher() {}

    public static boolean isEnabled(Context context) {
        String expected = context.getPackageName() + "/" + Ruta3AccessibilityService.class.getName();
        String enabledServices = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );
        if (enabledServices == null) return false;

        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
        splitter.setString(enabledServices);
        while (splitter.hasNext()) {
            String service = splitter.next();
            if (expected.equalsIgnoreCase(service)) {
                return true;
            }
        }
        return false;
    }

    public static void requestEnable(Context context) {
        Toast.makeText(context, "Activa Ruta3 en Accesibilidad para leer ofertas", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
