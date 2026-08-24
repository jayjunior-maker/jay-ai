package com.jay.ai.security;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class JayPermissionManager {

    public static final int REQUEST_CAMERA = 1001;
    public static final int REQUEST_MICROPHONE = 1002;
    public static final int REQUEST_LOCATION = 1003;
    public static final int REQUEST_CONTACTS = 1004;
    public static final int REQUEST_PHONE = 1005;
    public static final int REQUEST_SMS = 1006;

    private final Context context;

    public JayPermissionManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(
                context,
                permission
        ) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasCameraPermission() {
        return hasPermission(Manifest.permission.CAMERA);
    }

    public boolean hasMicrophonePermission() {
        return hasPermission(Manifest.permission.RECORD_AUDIO);
    }

    public boolean hasLocationPermission() {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                || hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    public boolean hasContactsPermission() {
        return hasPermission(Manifest.permission.READ_CONTACTS);
    }

    public boolean hasPhonePermission() {
        return hasPermission(Manifest.permission.READ_PHONE_STATE);
    }

    public boolean hasSmsPermission() {
        return hasPermission(Manifest.permission.READ_SMS);
    }

    /**
     * Returns the Android permission required for a Jay action.
     */
    public String getRequiredPermission(JayAction action) {

        if (action == null || action.getType() == null) {
            return null;
        }

        switch (action.getType()) {

            case CAMERA:
                return Manifest.permission.CAMERA;

            case MICROPHONE:
                return Manifest.permission.RECORD_AUDIO;

            case LOCATION:
                return Manifest.permission.ACCESS_FINE_LOCATION;

            case READ_CONTACTS:
                return Manifest.permission.READ_CONTACTS;

            case READ_MESSAGES:
                return Manifest.permission.READ_SMS;

            case SEND_MESSAGE:
                return Manifest.permission.SEND_SMS;

            case MAKE_CALL:
                return Manifest.permission.CALL_PHONE;

            default:
                return null;
        }
    }

    /**
     * Checks whether the permission required by an action is granted.
     */
    public boolean hasRequiredPermission(JayAction action) {

        String permission = getRequiredPermission(action);

        if (permission == null) {
            return true;
        }

        return hasPermission(permission);
    }

    public void requestCameraPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA
        );
    }

    public void requestMicrophonePermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_MICROPHONE
        );
    }

    public void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                REQUEST_LOCATION
        );
    }

    public void requestContactsPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.READ_CONTACTS},
                REQUEST_CONTACTS
        );
    }

    public void requestPhonePermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.READ_PHONE_STATE},
                REQUEST_PHONE
        );
    }

    public void requestSmsPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.READ_SMS},
                REQUEST_SMS
        );
    }
    }
