package com.jay.ai.security;

import android.content.Context;

public class JayActionGuard {

    private final Context context;
    private final JayFiveSecondGuard fiveSecondGuard;
    private final JayActionPolicy actionPolicy;
    private final JaySecurityAuditLogger auditLogger;

    public JayActionGuard(Context context) {
        this.context = context.getApplicationContext();
        this.fiveSecondGuard = new JayFiveSecondGuard();
        this.actionPolicy = new JayActionPolicy();
        this.auditLogger = new JaySecurityAuditLogger();
    }

    /**
     * Evaluates an action and records the security decision.
     */
    public JaySecurityResult evaluate(JayAction action) {

        JaySecurityResult result;

        if (action == null || action.getType() == null) {

            result = new JaySecurityResult(
                    JaySecurityResult.Status.BLOCKED,
                    "Sir, I could not identify that action."
            );

            recordAudit(null, result);
            return result;
        }

        JayAction.Type type = action.getType();

        if (actionPolicy.isDestructive(type)) {

            result = new JaySecurityResult(
                    JaySecurityResult.Status.NEEDS_FIVE_SECOND_DELAY,
                    "Sir, this action can permanently change or delete data. "
                            + "A 5-second safety confirmation is required."
            );

            recordAudit(action, result);
            return result;
        }

        if (actionPolicy.requiresPermission(type)) {

            result = new JaySecurityResult(
                    JaySecurityResult.Status.NEEDS_PERMISSION,
                    "Sir, this action requires the appropriate Android permission."
            );

            recordAudit(action, result);
            return result;
        }

        if (actionPolicy.requiresConfirmation(type)) {

            result = new JaySecurityResult(
                    JaySecurityResult.Status.NEEDS_CONFIRMATION,
                    "Sir, I need your confirmation before performing this action."
            );

            recordAudit(action, result);
            return result;
        }

        result = new JaySecurityResult(
                JaySecurityResult.Status.ALLOWED,
                "Action approved."
        );

        recordAudit(action, result);
        return result;
    }

    /**
     * Records a security decision.
     */
    private void recordAudit(
            JayAction action,
            JaySecurityResult result) {

        JayAction.Type type = null;

        if (action != null) {
            type = action.getType();
        }

        JaySecurityAudit audit = new JaySecurityAudit(
                System.currentTimeMillis(),
                type,
                result.getStatus(),
                result.getMessage()
        );

        auditLogger.log(audit);
    }

    /**
     * Starts the 5-second safety countdown for a specific action.
     */
    public void startFiveSecondGuard(
            JayAction action,
            JayFiveSecondGuard.ConfirmationListener listener) {

        if (action == null) {
            return;
        }

        if (!actionPolicy.isDestructive(action.getType())) {
            return;
        }

        fiveSecondGuard.startConfirmation(
                action,
                listener
        );
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
     * Returns the action currently waiting for confirmation.
     */
    public JayAction getPendingFiveSecondAction() {
        return fiveSecondGuard.getPendingAction();
    }

    public boolean requiresFiveSecondConfirmation(JayAction action) {

        if (action == null || action.getType() == null) {
            return false;
        }

        return actionPolicy.isDestructive(action.getType());
    }

    public boolean requiresConfirmation(JayAction action) {

        if (action == null || action.getType() == null) {
            return false;
        }

        return actionPolicy.requiresConfirmation(action.getType());
    }

    public boolean requiresPermission(JayAction action) {

        if (action == null || action.getType() == null) {
            return false;
        }

        return actionPolicy.requiresPermission(action.getType());
    }

    public boolean isSensitiveAction(JayAction action) {

        if (action == null || action.getType() == null) {
            return false;
        }

        return actionPolicy.isSensitive(action.getType());
    }

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

    public JaySecurityAuditLogger getAuditLogger() {
        return auditLogger;
    }

    public JayActionPolicy getActionPolicy() {
        return actionPolicy;
    }

    public Context getContext() {
        return context;
    }
    }
