package com.jay.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class JaySecurityManager {

    private static final String PREFS_NAME = "jay_security";
    private static final String ADMIN_AUTHORIZED = "admin_authorized";
    private static final String GUEST_MODE = "guest_mode";

    private final Context context;
    private final SharedPreferences preferences;
    private final Handler handler;

    public JaySecurityManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );
        this.handler = new Handler(Looper.getMainLooper());
    }

    // ---------------------------------------------------------
    // ADMIN STATUS
    // ---------------------------------------------------------

    public boolean isAdminAuthorized() {
        return preferences.getBoolean(ADMIN_AUTHORIZED, false);
    }

    public void setAdminAuthorized(boolean authorized) {
        preferences.edit()
                .putBoolean(ADMIN_AUTHORIZED, authorized)
                .apply();
    }

    // ---------------------------------------------------------
    // GUEST MODE
    // ---------------------------------------------------------

    public boolean isGuestMode() {
        return preferences.getBoolean(GUEST_MODE, false);
    }

    public void setGuestMode(boolean enabled) {
        preferences.edit()
                .putBoolean(GUEST_MODE, enabled)
                .apply();
    }

    // ---------------------------------------------------------
    // REQUEST ADMIN AUTHORIZATION
    // ---------------------------------------------------------

    public void requestAdminAuthorization(final AuthorizationCallback callback) {

        if (!(context instanceof Activity)) {
            Toast.makeText(
                    context,
                    "Jay requires Admin authorization",
                    Toast.LENGTH_SHORT
            ).show();

            if (callback != null) {
                callback.onDenied();
            }

            return;
        }

        Activity activity = (Activity) context;

        new AlertDialog.Builder(activity)
                .setTitle("Jay Security")
                .setMessage(
                        "Admin authorization is required before another person can use this phone."
                )
                .setPositiveButton(
                        "AUTHORIZE",
                        (dialog, which) -> {

                            setAdminAuthorized(true);
                            setGuestMode(false);

                            Toast.makeText(
                                    context,
                                    "Admin authorization granted",
                                    Toast.LENGTH_SHORT
                            ).show();

                            if (callback != null) {
                                callback.onAuthorized();
                            }
                        }
                )
                .setNegativeButton(
                        "DENY",
                        (dialog, which) -> {

                            setAdminAuthorized(false);
                            setGuestMode(true);

                            Toast.makeText(
                                    context,
                                    "Access denied",
                                    Toast.LENGTH_SHORT
                            ).show();

                            if (callback != null) {
                                callback.onDenied();
                            }
                        }
                )
                .setCancelable(false)
                .show();
    }

    // ---------------------------------------------------------
    // ENTER GUEST MODE
    // ---------------------------------------------------------

    public void enterGuestMode() {
        setGuestMode(true);
        setAdminAuthorized(false);

        Toast.makeText(
                context,
                "Jay Guest Mode enabled",
                Toast.LENGTH_SHORT
        ).show();
    }

    // ---------------------------------------------------------
    // EXIT GUEST MODE
    // ---------------------------------------------------------

    public void exitGuestMode() {
        setGuestMode(false);

        Toast.makeText(
                context,
                "Jay Admin Mode restored",
                Toast.LENGTH_SHORT
        ).show();
    }

    // ---------------------------------------------------------
    // RESET SECURITY STATE
    // ---------------------------------------------------------

    public void resetSecurity() {
        preferences.edit()
                .clear()
                .apply();
    }

    // ---------------------------------------------------------
    // SECURITY STATUS
    // ---------------------------------------------------------

    public String getSecurityStatus() {

        if (isAdminAuthorized()) {
            return "ADMIN";
        }

        if (isGuestMode()) {
            return "GUEST";
        }

        return "LOCKED";
    }

    // ---------------------------------------------------------
    // CALLBACK
    // ---------------------------------------------------------

    public interface AuthorizationCallback {

        void onAuthorized();

        void onDenied();
    }
}
