package com.ruta3.app;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "Ruta3Settings")
public class Ruta3SettingsPlugin extends Plugin {
    @PluginMethod
    public void getSettings(PluginCall call) {
        JSObject result = new JSObject();
        result.put("minPerKm", Ruta3Settings.getMinPerKm(getContext()));
        result.put("minPerHour", Ruta3Settings.getMinPerHour(getContext()));
        call.resolve(result);
    }

    @PluginMethod
    public void saveSettings(PluginCall call) {
        int minPerKm = call.getInt("minPerKm", Ruta3Settings.getMinPerKm(getContext()));
        int minPerHour = call.getInt("minPerHour", Ruta3Settings.getMinPerHour(getContext()));
        Ruta3Settings.save(getContext(), minPerKm, minPerHour);
        call.resolve();
    }
}
