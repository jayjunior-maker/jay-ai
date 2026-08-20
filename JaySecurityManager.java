package com.jay.ai;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class JaySecurityManager {

    private static final String PREFS_NAME = "jay_security";
    private static final String ADMIN_AUTHORIZED = "admin_authorized";
    private static final String GUEST_MODE = "guest_mode";

    private final Context context;
    private final SharedPreferences preferences;
    private final JayBiometricManager biometricManager;

    public JaySecurityManager(Activity activity) {
        this.context = activity;

        preferences = context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );

        biometricManager =
                new JayBiometricManager(activity);
    }

    // ---------------------------------------------------------
    // ADMIN STATUS
    // ---------------------------------------------------------

    public boolean isAdminAuthorized() {
        return preferences.getBoolean(
                ADMIN_AUTHORIZED,
                false
        );
    }

    private void setAdminAuthorized(
            boolean authorized
    ) {
        preferences.edit()
                .putBoolean(
                        ADMIN_AUTHORIZED,
                        authorized
                )
                .apply();
    }

    // ---------------------------------------------------------
    // GUEST STATUS
    // ---------------------------------------------------------

    public boolean isGuestMode() {
        return preferences.getBoolean(
                GUEST_MODE,
                false
        );
    }

    private void setGuestMode(
            boolean enabled
    ) {
        preferences.edit()
                .putBoolean(
                        GUEST_MODE,
                        enabled
                )
                .apply();
    }

    // ---------------------------------------------------------
    // REAL ADMIN AUTHENTICATION
    // ---------------------------------------------------------

    public void authenticateAdmin(
            final AuthorizationCallback callback
    ) {

        if (!biometricManager
                .isAuthenticationAvailable()) {

            Toast.makeText(
                    context,
                    "No supported biometric authentication is available.",
                    Toast.LENGTH_LONG
            ).show();

            if (callback != null) {
                callback.onDenied();
            }

            return;
        }

        biometricManager.authenticate(
                new JayBiometricManager.AuthenticationCallback() {

                    @Override
                    public void onSuccess() {

                        setAdminAuthorized(true);
                        setGuestMode(false);

                        if (callback != null) {
                            callback.onAuthorized();
                        }
                    }

                    @Override
                    public void onFailed() {

                        if (callback != null) {
                            callback.onDenied();
                        }
                    }

                    @Override
                    public void onError(
                            int errorCode,
                            String message
                    ) {

                        if (callback != null) {
                            callback.onDenied();
                        }
                    }
                }
        );
    }

    // ---------------------------------------------------------
    // ENTER GUEST MODE
    // ---------------------------------------------------------

    public void enterGuestMode() {

        setAdminAuthorized(false);
        setGuestMode(true);
    }

    // ---------------------------------------------------------
    // EXIT GUEST MODE
    // ---------------------------------------------------------

    public void exitGuestMode() {

        setGuestMode(false);
    }

    // ---------------------------------------------------------
    // RESTRICTED MODE
    // ---------------------------------------------------------

    public void enterRestrictedMode() {

        setAdminAuthorized(false);
        setGuestMode(true);
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
    // RESET
    // ---------------------------------------------------------

    public void resetSecurity() {

        preferences.edit()
                .clear()
                .apply();
    }

    // ---------------------------------------------------------
    // CALLBACK
    // ---------------------------------------------------------

    public interface AuthorizationCallback {

        void onAuthorized();

        void onDenied();
    }
}
