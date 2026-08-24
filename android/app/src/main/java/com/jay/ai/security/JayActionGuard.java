package com.jay.ai.security;

import android.content.Context;

public class JayActionGuard {

    private final Context context;

    public JayActionGuard(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Evaluates an action and returns the security decision.
     */
    public JaySecurityResult evaluate(JayAction action) {

        if (action == null) {
            return new JaySecurityResult(
                    JaySecurityResult.Status.BLOCKED,
                    "Sir, I could not identify that action."
            );
        }

        /*
         * Destructive actions always receive the highest
         * protection level and require the 5-second delay.
         */
        if (action.isDestructive()) {
            return new JaySecurityResult(
                    JaySecurityResult.Status.NEEDS_FIVE_SECOND_DELAY,
                    "Sir, this action can permanently change or delete data. "
                            + "A 5-second safety confirmation is required."
            );
        }

        /*
         * Actions involving Android permissions must have
         * the required permission before Jay can continue.
         */
        if (action.requiresPermission()) {
            return new JaySecurityResult(
                    JaySecurityResult.Status.NEEDS_PERMISSION,
                    "Sir, this action requires the appropriate Android permission."
            );
        }

        /*
         * Important actions such as calls, messages, and
         * settings changes require explicit confirmation.
         */
        if (action.requiresConfirmation()) {
            return new JaySecurityResult(
                    JaySecurityResult.Status.NEEDS_CONFIRMATION,
                    "Sir, I need your confirmation before performing this action."
            );
        }

        return new JaySecurityResult(
                JaySecurityResult.Status.ALLOWED,
                "Action approved."
        );
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
