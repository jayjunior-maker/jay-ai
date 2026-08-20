package com.jay.ai;

import android.app.Activity;
import android.content.Context;

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

    public boolean isAuthenticationAvailable() {

        BiometricManager biometricManager =
                BiometricManager.from(activity);

        int result =
                biometricManager.canAuthenticate(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG
                                | BiometricManager.Authenticators.DEVICE_CREDENTIAL
                );

        return result ==
                BiometricManager.BIOMETRIC_SUCCESS;
    }

    public void authenticate(
            final AuthenticationCallback callback) {

        BiometricPrompt.AuthenticationCallback
                authenticationCallback =
                new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {

                        if (callback != null) {
                            callback.onSuccess();
                        }
                    }

                    @Override
                    public void onAuthenticationFailed() {

                        if (callback != null) {
                            callback.onFailed();
                        }
                    }

                    @Override
                    public void onAuthenticationError(
                            int errorCode,
                            @NonNull CharSequence errString) {

                        if (callback != null) {
                            callback.onError(
                                    errorCode,
                                    errString.toString()
                            );
                        }
                    }
                };

        BiometricPrompt biometricPrompt =
                new BiometricPrompt(
                        activity,
                        executor,
                        authenticationCallback
                );

        BiometricPrompt.PromptInfo promptInfo =
                new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Jay Admin Authentication")
                        .setSubtitle(
                                "Verify that you are the Jay Admin"
                        )
                        .setDescription(
                                "Authentication is required to access Admin mode."
                        )
                        .setAllowedAuthenticators(
                                BiometricManager.Authenticators.BIOMETRIC_STRONG
                                        | BiometricManager.Authenticators.DEVICE_CREDENTIAL
                        )
                        .build();

        biometricPrompt.authenticate(promptInfo);
    }

    public interface AuthenticationCallback {

        void onSuccess();

        void onFailed();

        void onError(
                int errorCode,
                String message
        );
    }
}
