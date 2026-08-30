package com.jay.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/** Central user-approval gate around local device actions. */
public final class JayDeviceAuthorizationBridge {
    private final Activity activity;
    private final JayLocalCommandManager executor;
    private final JayAuthorizationManager authorization = new JayAuthorizationManager();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String pendingAction;
    private int permissionRequestCode = 7801;

    public JayDeviceAuthorizationBridge(Activity activity, JayLocalCommandManager executor) {
        this.activity = activity;
        this.executor = executor;
    }

    public boolean shouldAuthorize(String action) {
        if (action == null) return false;
        String a = action.toLowerCase();
        return a.startsWith("open ") || a.startsWith("call ") || a.startsWith("sms ") ||
                a.startsWith("send sms ") || a.contains("whatsapp") || a.startsWith("set alarm") ||
                a.startsWith("create alarm") || a.startsWith("cancel alarm") || a.contains("brightness") ||
                a.contains("volume") || a.contains("torch") || a.contains("flashlight") ||
                a.contains("wallpaper") || a.contains("wifi") || a.contains("bluetooth") ||
                a.contains("airplane mode") || a.contains("location settings") || a.contains("camera") ||
                a.contains("media") || a.startsWith("play ") || a.startsWith("pause ") ||
                a.startsWith("resume ") || a.startsWith("next ") || a.startsWith("previous ");
    }

    public String request(String action) {
        if (!shouldAuthorize(action)) return executor.handle(action);
        pendingAction = action;
        authorization.begin(action);
        new AlertDialog.Builder(activity)
                .setTitle("Jay needs your permission")
                .setMessage("Sir, may I perform this device action?\n\n" + action)
                .setPositiveButton("Yes", (d, w) -> approve())
                .setNegativeButton("No", (d, w) -> deny())
                .setOnCancelListener(d -> deny())
                .show();
        return "Sir, may I " + action + "?";
    }

    private void approve() {
        final String action = authorization.approve();
        if (action == null) { deny(); return; }
        String permission = authorization.requiredPermission(action);
        if (permission != null && activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{permission}, permissionRequestCode);
            waitForAndroidPermission(action, permission, 0);
            return;
        }
        execute(action);
    }

    private void waitForAndroidPermission(final String action, final String permission, final int attempt) {
        if (activity.isFinishing()) { deny(); return; }
        if (activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            execute(action);
            return;
        }
        if (attempt >= 30) {
            authorization.clear(); pendingAction = null;
            Toast.makeText(activity, "Jay did not receive the required Android permission.", Toast.LENGTH_LONG).show();
            return;
        }
        handler.postDelayed(() -> waitForAndroidPermission(action, permission, attempt + 1), 1000L);
    }

    private void execute(String action) {
        String result;
        try {
            result = executor.handle(action);
        } catch (Exception e) {
            result = "Jay could not execute that action: " + e.getMessage();
        }
        authorization.clear();
        pendingAction = null;
        deliver(result == null ? "Jay could not complete that action." : result);
    }

    private void deny() {
        authorization.deny();
        pendingAction = null;
        deliver("Understood, Sir. I did not perform that action.");
    }

    private void deliver(String message) {
        try {
            java.lang.reflect.Method reply = activity.getClass().getDeclaredMethod("reply", String.class);
            reply.setAccessible(true);
            reply.invoke(activity, message);
        } catch (Exception ignored) {
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
        }
    }
}
