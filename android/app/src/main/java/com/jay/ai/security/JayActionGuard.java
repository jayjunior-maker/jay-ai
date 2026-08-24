package com.jay.ai.security;

import android.content.Context;

public class JayActionGuard {

    private final Context context;
    private final JayFiveSecondGuard fiveSecondGuard;

    public JayActionGuard(Context context) {
        this.context = context.getApplicationContext();
        this.fiveSecondGuard = new JayFiveSecondGuard();
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

        if (action.isDestructive()) {
            return new JaySecurityResult(
                    JaySecurityResult.Status.NEEDS_FIVE_SECOND_DELAY,
                    "Sir, this action can permanently change or delete data. "
                            + "A 5-second safety confirmation is required."
            );
        }

        if (action.requiresPermission()) {
            return new JaySecurityResult(
                    JaySecurityResult.Status.NEEDS_PERMISSION,
                    "Sir, this action requires the appropriate Android permission."
            );
        }

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
     * Starts the 5-second safety countdown for a destructive action.
     */
    public void startFiveSecondGuard(
            JayFiveSecondGuard.ConfirmationListener listener) {

        fiveSecondGuard.startConfirmation(listener);
    }

    /**
     * Cancels the active 5-second countdown.
     */
    public void cancelFiveSecondGuard() {
        fiveSecondGuard.cancel();
    }

    /**
     * Checks whether the 5-second countdown is currently active.
     */
    public boolean isFiveSecondGuardActive() {
        return fiveSecondGuard.isWaitingForConfirmation();
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
