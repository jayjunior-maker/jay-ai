package com.jay.ai;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Central authorization gate for Jay device actions.
 *
 * Policy:
 * 1) Jay identifies the action.
 * 2) Jay asks Sir for explicit approval.
 * 3) Only after approval are Android permissions requested.
 * 4) The caller executes the action and verifies the result.
 */
public final class JayAuthorizationManager {
    public enum Decision { ASK_USER, READY, DENIED, ANDROID_PERMISSION_NEEDED }

    private String pendingAction = null;

    public synchronized boolean hasPendingAction() {
        return pendingAction != null;
    }

    public synchronized String getPendingAction() {
        return pendingAction;
    }

    public synchronized Decision begin(String canonicalAction) {
        if (canonicalAction == null || canonicalAction.trim().isEmpty()) return Decision.DENIED;
        pendingAction = canonicalAction.trim();
        return Decision.ASK_USER;
    }

    public synchronized Decision deny() {
        pendingAction = null;
        return Decision.DENIED;
    }

    public synchronized String approve() {
        return pendingAction;
    }

    public synchronized void clear() {
        pendingAction = null;
    }

    /** Android permission required by the already-approved action, or null. */
    public String requiredPermission(String action) {
        if (action == null) return null;
        String a = action.toLowerCase();
        if (a.startsWith("call ") || a.startsWith("phone ")) return Manifest.permission.CALL_PHONE;
        if (a.startsWith("camera") || a.equals("open camera") || a.startsWith("open camera ")) return Manifest.permission.CAMERA;
        if (a.startsWith("location") || a.startsWith("get location")) return Manifest.permission.ACCESS_FINE_LOCATION;
        if (a.startsWith("sms ") || a.startsWith("send sms")) return Manifest.permission.SEND_SMS;
        if (a.startsWith("contact ") || a.startsWith("find contact")) return Manifest.permission.READ_CONTACTS;
        return null;
    }

    public boolean hasRequiredPermission(Context context, String action) {
        String permission = requiredPermission(action);
        return permission == null || context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    /** Request Android permission only after Jay's own approval has already been obtained. */
    public boolean requestRequiredPermission(Activity activity, String action, int requestCode) {
        String permission = requiredPermission(action);
        if (permission == null || hasRequiredPermission(activity, action)) return false;
        activity.requestPermissions(new String[]{permission}, requestCode);
        return true;
    }
}
