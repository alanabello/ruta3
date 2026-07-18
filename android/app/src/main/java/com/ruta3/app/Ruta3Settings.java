package com.ruta3.app;

import android.content.Context;
import android.content.SharedPreferences;

public class Ruta3Settings {
    private static final String PREFS = "ruta3_settings";
    private static final String KEY_MIN_PER_KM = "minPerKm";
    private static final String KEY_MIN_PER_HOUR = "minPerHour";
    private static final int DEFAULT_MIN_PER_KM = 400;
    private static final int DEFAULT_MIN_PER_HOUR = 6000;

    private Ruta3Settings() {}

    public static int getMinPerKm(Context context) {
        return prefs(context).getInt(KEY_MIN_PER_KM, DEFAULT_MIN_PER_KM);
    }

    public static int getMinPerHour(Context context) {
        return prefs(context).getInt(KEY_MIN_PER_HOUR, DEFAULT_MIN_PER_HOUR);
    }

    public static void save(Context context, int minPerKm, int minPerHour) {
        prefs(context)
                .edit()
                .putInt(KEY_MIN_PER_KM, minPerKm > 0 ? minPerKm : DEFAULT_MIN_PER_KM)
                .putInt(KEY_MIN_PER_HOUR, minPerHour > 0 ? minPerHour : DEFAULT_MIN_PER_HOUR)
                .apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
