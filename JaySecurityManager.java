package com.jay.ai;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class JaySecurityManager {

    private final Context context;
    private final SharedPreferences preferences;

    private static final String PREFS_NAME = "jay_security";
    private static final String KEY_MODE = "security_mode";

    public static final String MODE_LOCKED = "LOCKED";
    public static final String MODE_ADMIN = "ADMIN";
    public static final String MODE_GUEST = "GUEST";

    public JaySecurityManager(Context context) {
        this.context = context.getApplicationContext();

        preferences = this.context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );
    }

    public interface AuthorizationCallback {
        void onAuthorized();
        void onDenied();
    }

    public void authenticateAdmin(
            AuthorizationCallback callback
    ) {

        if (!(context instanceof Activity)) {
            if (callback != null) {
                callback.onDenied();
            }
            return;
        }

        JayBiometricManager biometricManager =
                new JayBiometricManager((Activity) context);

        biometricManager.authenticate(
                new JayBiometricManager.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationSucceeded() {
                        setAdminMode();

                        if (callback != null) {
                            callback.onAuthorized();
                        }
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        setLockedMode();

                        if (callback != null) {
                            callback.onDenied();
                        }
                    }
                }
        );
    }

    public void setAdminMode() {
        preferences.edit()
                .putString(KEY_MODE, MODE_ADMIN)
                .apply();
    }

    public void enterGuestMode() {
        preferences.edit()
                .putString(KEY_MODE, MODE_GUEST)
                .apply();
    }

    public void enterRestrictedMode() {
        setLockedMode();
    }

    public void lock() {
        setLockedMode();
    }

    private void setLockedMode() {
        preferences.edit()
                .putString(KEY_MODE, MODE_LOCKED)
                .apply();
    }

    public String getSecurityStatus() {
        return preferences.getString(
                KEY_MODE,
                MODE_LOCKED
        );
    }

    public boolean isAdmin() {
        return MODE_ADMIN.equals(getSecurityStatus());
    }

    public boolean isGuest() {
        return MODE_GUEST.equals(getSecurityStatus());
    }

    public boolean isLocked() {
        return MODE_LOCKED.equals(getSecurityStatus());
    }

    public void resetSecurity() {
        preferences.edit()
                .clear()
                .apply();
    }
  }
