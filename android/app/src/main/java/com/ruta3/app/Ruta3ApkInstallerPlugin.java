package com.ruta3.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.content.FileProvider;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

@CapacitorPlugin(name = "Ruta3ApkInstaller")
public class Ruta3ApkInstallerPlugin extends Plugin {
    private static final String APK_ASSET_PATH = "public/downloads/ruta3.apk";
    private static final String APK_MIME_TYPE = "application/vnd.android.package-archive";

    @PluginMethod
    public void installBundledApk(PluginCall call) {
        Context context = getContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.getPackageManager().canRequestPackageInstalls()) {
            Intent settingsIntent = new Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + context.getPackageName())
            );
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(settingsIntent);

            JSObject result = new JSObject();
            result.put("needsPermission", true);
            call.resolve(result);
            return;
        }

        try {
            File apkFile = copyApkToCache(context);
            Uri apkUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    apkFile
            );

            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setDataAndType(apkUri, APK_MIME_TYPE);
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(installIntent);

            JSObject result = new JSObject();
            result.put("started", true);
            call.resolve(result);
        } catch (Exception exception) {
            call.reject("No se pudo iniciar la instalacion del APK", exception);
        }
    }

    private File copyApkToCache(Context context) throws Exception {
        File outputDir = new File(context.getCacheDir(), "downloads");
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IllegalStateException("No se pudo crear el directorio de descarga");
        }

        File outputFile = new File(outputDir, "ruta3.apk");
        try (
                InputStream input = context.getAssets().open(APK_ASSET_PATH);
                OutputStream output = new FileOutputStream(outputFile)
        ) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        }

        return outputFile;
    }
}
