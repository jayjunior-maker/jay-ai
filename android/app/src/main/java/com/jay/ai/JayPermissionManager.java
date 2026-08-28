package com.jay.ai;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

/** Centralizes Jay's first-launch and runtime permission flow. */
public final class JayPermissionManager {
    public static final int REQUEST_SETUP = 4100;
    public static final int REQUEST_MICROPHONE = 4101;
    public static final int REQUEST_CAMERA = 4102;
    public static final int REQUEST_CONTACTS = 4103;
    public static final int REQUEST_PHONE = 4104;
    public static final int REQUEST_LOCATION = 4105;
    public static final int REQUEST_SMS = 4106;
    public static final int REQUEST_NOTIFICATIONS = 4107;

    private JayPermissionManager() { }

    public static boolean has(Context context, String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasMicrophone(Context context) { return has(context, Manifest.permission.RECORD_AUDIO); }
    public static boolean hasCamera(Context context) { return has(context, Manifest.permission.CAMERA); }
    public static boolean hasContacts(Context context) { return has(context, Manifest.permission.READ_CONTACTS); }
    public static boolean hasPhone(Context context) { return has(context, Manifest.permission.CALL_PHONE); }
    public static boolean hasLocation(Context context) {
        return has(context, Manifest.permission.ACCESS_FINE_LOCATION) || has(context, Manifest.permission.ACCESS_COARSE_LOCATION);
    }
    public static boolean hasSms(Context context) { return has(context, Manifest.permission.READ_SMS); }

    /** Core permissions Jay can request during first-launch setup. */
    public static String[] requiredForSetup() {
        return new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
    }

    /** SMS is intentionally optional because it exposes private messages such as M-Pesa notifications. */
    public static String[] optionalSms() { return new String[]{Manifest.permission.READ_SMS}; }

    public static void request(Activity activity, String permission, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !has(activity, permission)) {
            activity.requestPermissions(new String[]{permission}, requestCode);
        }
    }

    public static void requestMicrophone(Activity activity) { request(activity, Manifest.permission.RECORD_AUDIO, REQUEST_MICROPHONE); }
    public static void requestCamera(Activity activity) { request(activity, Manifest.permission.CAMERA, REQUEST_CAMERA); }
    public static void requestContacts(Activity activity) { request(activity, Manifest.permission.READ_CONTACTS, REQUEST_CONTACTS); }
    public static void requestPhone(Activity activity) { request(activity, Manifest.permission.CALL_PHONE, REQUEST_PHONE); }
    public static void requestLocation(Activity activity) { request(activity, Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_LOCATION); }
    public static void requestSms(Activity activity) { request(activity, Manifest.permission.READ_SMS, REQUEST_SMS); }

    /** Core setup is complete when Jay has the permissions required for its main device features. */
    public static boolean setupComplete(Context context) {
        return hasMicrophone(context) && hasCamera(context) && hasContacts(context) && hasPhone(context) && hasLocation(context);
    }
}
