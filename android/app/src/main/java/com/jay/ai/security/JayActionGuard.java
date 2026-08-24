package com.jay.ai.security;

import android.content.Context;

public class JayActionGuard {

    private final Context context;
    private final JayFiveSecondGuard fiveSecondGuard;
    private final JayActionPolicy actionPolicy;

    public JayActionGuard(Context context) {
        this.context = context.getApplicationContext();
        this.fiveSecondGuard = new JayFiveSecondGuard();
        this.actionPolicy = new JayActionPolicy();
    }

    /**
     * Evaluates an action and returns the security decision.
     */
    public JaySecurityResult evaluate(JayAction action) {

        if (action == null || action.getType() == null) {
            return new JaySecurityResult(
                    JaySecurityResult.Status.BLOCKED,
                    "Sir, I could not identify that action."
            );
        }

        JayAction.Type type = action.getType();

        if (actionPolicy.isDestructive(type)) {
            return new JaySecurityResult(
                    JaySecurityResult.Status.NEEDS_FIVE_SECOND_DELAY,
                    "Sir, this action can permanently change or delete data. "
                            + "A 5-second safety confirmation is required."
            );
        }

        if (actionPolicy.requiresPermission(type)) {
            return new JaySecurityResult(
                    JaySecurityResult.Status.NEEDS_PERMISSION,
                    "Sir, this action requires the appropriate Android permission."
            );
        }

        if (actionPolicy.requiresConfirmation(type)) {
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
     * Starts the 5-second safety countdown.
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
     * Checks whether the 5-second countdown is active.
     */
    public boolean isFiveSecondGuardActive() {
        return fiveSecondGuard.isWaitingForConfirmation();
    }

    /**
     * Determines whether an action requires the 5-second delay.
     */
    public boolean requiresFiveSecondConfirmation(JayAction action) {

        if (action == null || action.getType() == null) {
            return false;
        }

        return actionPolicy.isDestructive(action.getType());
    }

    /**
     * Determines whether an action requires confirmation.
     */
    public boolean requiresConfirmation(JayAction action) {

        if (action == null || action.getType() == null) {
            return false;
        }

        return actionPolicy.requiresConfirmation(action.getType());
    }

    /**
     * Determines whether an action requires permission.
     */
    public boolean requiresPermission(JayAction action) {

        if (action == null || action.getType() == null) {
            return false;
        }

        return actionPolicy.requiresPermission(action.getType());
    }

    /**
     * Determines whether an action is sensitive.
     */
    public boolean isSensitiveAction(JayAction action) {

        if (action == null || action.getType() == null) {
            return false;
        }

        return actionPolicy.isSensitive(action.getType());
    }

    /**
     * Returns a security message for Jay.
     */
    public String getSecurityMessage(JayAction action) {

        if (action == null || action.getType() == null) {
            return "Sir, I could not identify that action.";
        }

        JayAction.Type type = action.getType();

        if (actionPolicy.isDestructive(type)) {
            return "Sir, this action can permanently change or delete data. "
                    + "A 5-second safety confirmation is required.";
        }

        if (actionPolicy.requiresPermission(type)) {
            return "Sir, this action requires the appropriate Android permission.";
        }

        if (actionPolicy.requiresConfirmation(type)) {
            return "Sir, I need your confirmation before performing this action.";
        }

        return "Action approved.";
    }

    /**
     * Returns the application context.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Returns the action policy.
     */
    public JayActionPolicy getActionPolicy() {
        return actionPolicy;
    }
            }
