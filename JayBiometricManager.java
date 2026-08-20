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

    public boolean isAuthenticationAvailable() {
        BiometricManager manager =
                BiometricManager.from(activity);

        int result = manager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
                        | BiometricManager.Authenticators.DEVICE_CREDENTIAL
        );

        return result == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public void authenticate(
            final AuthenticationCallback callback) {

        BiometricPrompt.AuthenticationCallback authCallback =
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
                            @NonNull CharSequence errorString) {

                        if (callback != null) {
                            callback.onError(
                                    errorCode,
                                    errorString.toString()
                            );
                        }
                    }
                };

        BiometricPrompt prompt =
                new BiometricPrompt(
                        activity,
                        executor,
                        authCallback
                );

        BiometricPrompt.PromptInfo info =
                new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Jay Admin Authentication")
                        .setSubtitle("Verify your identity")
                        .setDescription(
                                "Authenticate to enter Jay Admin Mode."
                        )
                        .setNegativeButtonText("Cancel")
                        .build();

        prompt.authenticate(info);
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
