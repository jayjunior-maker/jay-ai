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

    /**
     * Checks whether a specific Android permission is granted.
     */
    public boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(
                context,
                permission
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Checks camera permission.
     */
    public boolean hasCameraPermission() {
        return hasPermission(Manifest.permission.CAMERA);
    }

    /**
     * Checks microphone permission.
     */
    public boolean hasMicrophonePermission() {
        return hasPermission(Manifest.permission.RECORD_AUDIO);
    }

    /**
     * Checks location permission.
     */
    public boolean hasLocationPermission() {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                || hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    /**
     * Checks contacts permission.
     */
    public boolean hasContactsPermission() {
        return hasPermission(Manifest.permission.READ_CONTACTS);
    }

    /**
     * Checks phone permission.
     */
    public boolean hasPhonePermission() {
        return hasPermission(Manifest.permission.READ_PHONE_STATE);
    }

    /**
     * Checks SMS permission.
     */
    public boolean hasSmsPermission() {
        return hasPermission(Manifest.permission.READ_SMS);
    }

    /**
     * Requests camera permission.
     */
    public void requestCameraPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA
        );
    }

    /**
     * Requests microphone permission.
     */
    public void requestMicrophonePermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_MICROPHONE
        );
    }

    /**
     * Requests location permissions.
     */
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

    /**
     * Requests contacts permission.
     */
    public void requestContactsPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.READ_CONTACTS},
                REQUEST_CONTACTS
        );
    }

    /**
     * Requests phone permission.
     */
    public void requestPhonePermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.READ_PHONE_STATE},
                REQUEST_PHONE
        );
    }

    /**
     * Requests SMS permission.
     */
    public void requestSmsPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.READ_SMS},
                REQUEST_SMS
        );
    }
                                 }
