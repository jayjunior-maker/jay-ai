package com.jay.ai.security;

import android.content.Context;

public class JayActionGuard {

    private final Context context;

    public JayActionGuard(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Determines whether an action requires the 5-second safety delay.
     */
    public boolean requiresFiveSecondConfirmation(JayAction action) {

        if (action == null) {
            return false;
        }

        return action.isDestructive();
    }

    /**
     * Determines whether an action requires explicit confirmation.
     */
    public boolean requiresConfirmation(JayAction action) {

        if (action == null) {
            return false;
        }

        return action.requiresConfirmation();
    }

    /**
     * Determines whether an action requires an Android permission.
     */
    public boolean requiresPermission(JayAction action) {

        if (action == null) {
            return false;
        }

        return action.requiresPermission();
    }

    /**
     * Returns a security message for Jay.
     */
    public String getSecurityMessage(JayAction action) {

        if (action == null) {
            return "Sir, I could not identify that action.";
        }

        if (action.isDestructive()) {
            return "Sir, this action can permanently change or delete data. "
                    + "A 5-second safety confirmation is required.";
        }

        if (action.requiresConfirmation()) {
            return "Sir, I need your confirmation before performing this action.";
        }

        if (action.requiresPermission()) {
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
