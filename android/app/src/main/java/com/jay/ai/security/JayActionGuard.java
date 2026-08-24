package com.jay.ai.security;

import android.content.Context;

public class JayActionGuard {

    private final Context context;

    public JayActionGuard(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Checks whether an action is considered dangerous.
     */
    public boolean requiresFiveSecondConfirmation(String action) {

        if (action == null) {
            return false;
        }

        String normalizedAction = action.toLowerCase().trim();

        return normalizedAction.contains("delete")
                || normalizedAction.contains("remove")
                || normalizedAction.contains("erase")
                || normalizedAction.contains("clear data")
                || normalizedAction.contains("uninstall")
                || normalizedAction.contains("factory reset")
                || normalizedAction.contains("reset device");
    }

    /**
     * Checks whether an action requires explicit user confirmation.
     */
    public boolean requiresConfirmation(String action) {

        if (action == null) {
            return false;
        }

        String normalizedAction = action.toLowerCase().trim();

        return normalizedAction.contains("send message")
                || normalizedAction.contains("send sms")
                || normalizedAction.contains("make call")
                || normalizedAction.contains("place call")
                || normalizedAction.contains("change setting")
                || normalizedAction.contains("change settings")
                || requiresFiveSecondConfirmation(normalizedAction);
    }

    /**
     * Checks whether the action is potentially sensitive.
     */
    public boolean isSensitiveAction(String action) {

        if (action == null) {
            return false;
        }

        String normalizedAction = action.toLowerCase().trim();

        return normalizedAction.contains("camera")
                || normalizedAction.contains("microphone")
                || normalizedAction.contains("screen")
                || normalizedAction.contains("contacts")
                || normalizedAction.contains("messages")
                || normalizedAction.contains("sms")
                || normalizedAction.contains("location")
                || normalizedAction.contains("files");
    }

    /**
     * Returns a human-readable security message for Jay.
     */
    public String getSecurityMessage(String action) {

        if (requiresFiveSecondConfirmation(action)) {
            return "Sir, this action can delete or permanently change data. "
                    + "Please wait for the 5-second safety confirmation.";
        }

        if (requiresConfirmation(action)) {
            return "Sir, I need your confirmation before performing this action.";
        }

        if (isSensitiveAction(action)) {
            return "Sir, this action requires the appropriate Android permission.";
        }

        return "Action approved.";
    }

    /**
     * Returns the application context.
     */
    public Context getContext() {
        return context;
    }
    }
