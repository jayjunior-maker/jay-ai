package com.jay.ai;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class JayBiometricManager {

    private final Activity activity;
    private final Executor executor;

    public JayBiometricManager(Activity activity) {
        this.activity = activity;
        this.executor = ContextCompat.getMainExecutor(activity);
    }

    // -----------------------------------------------------
    // CHECK WHETHER BIOMETRIC AUTHENTICATION IS AVAILABLE
    // -----------------------------------------------------

    public boolean isAuthenticationAvailable() {

        BiometricManager manager =
                BiometricManager.from(activity);

        int result = manager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
                        | BiometricManager.Authenticators.DEVICE_CREDENTIAL
        );

        return result == BiometricManager.BIOMETRIC_SUCCESS;
    }

    // -----------------------------------------------------
    // AUTHENTICATION CALLBACK
    // -----------------------------------------------------

    public interface AuthenticationCallback {

        void onAuthenticationSucceeded();

        void onAuthenticationFailed();
    }

    // -----------------------------------------------------
    // START AUTHENTICATION
    // -----------------------------------------------------

    public void authenticate(
            final AuthenticationCallback callback) {

        BiometricPrompt.AuthenticationCallback biometricCallback =
                new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {

                        super.onAuthenticationSucceeded(result);

                        if (callback != null) {
                            callback.onAuthenticationSucceeded();
                        }
                    }

                    @Override
                    public void onAuthenticationError(
                            int errorCode,
                            @NonNull CharSequence errString) {

                        super.onAuthenticationError(
                                errorCode,
                                errString
                        );

                        if (callback != null) {
                            callback.onAuthenticationFailed();
                        }
                    }

                    @Override
                    public void onAuthenticationFailed() {

                        super.onAuthenticationFailed();

                        if (callback != null) {
                            callback.onAuthenticationFailed();
                        }
                    }
                };

        BiometricPrompt biometricPrompt =
                new BiometricPrompt(
                        activity,
                        executor,
                        biometricCallback
                );

        BiometricPrompt.PromptInfo promptInfo =
                new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Jay Admin Security")
                        .setSubtitle("Verify your identity")
                        .setDescription(
                                "Use your fingerprint, face authentication, " +
                                "or device security to verify that you are Jay's Admin."
                        )
                        .setAllowedAuthenticators(
                                BiometricManager.Authenticators.BIOMETRIC_STRONG
                                        | BiometricManager.Authenticators.DEVICE_CREDENTIAL
                        )
                        .build();

        biometricPrompt.authenticate(promptInfo);
    }
        }
