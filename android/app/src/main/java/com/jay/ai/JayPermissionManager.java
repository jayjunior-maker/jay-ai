package com.jay.ai;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

/** Centralizes runtime permission checks for Jay's Android capabilities. */
public final class JayPermissionManager {
    public static final int REQUEST_MICROPHONE = 4101;
    public static final int REQUEST_CAMERA = 4102;

    private JayPermissionManager() { }

    public static boolean hasMicrophone(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                context.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasCamera(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                context.checkSelfPermission(Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestMicrophone(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasMicrophone(activity)) {
            activity.requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE);
        }
    }

    public static void requestCamera(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasCamera(activity)) {
            activity.requestPermissions(
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }
    }
}
