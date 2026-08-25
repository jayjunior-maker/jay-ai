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

    public JaySecurityResult evaluate(JayAction action) {
        JaySecurityResult result;

        if (action == null || action.getType() == null) {
            result = new JaySecurityResult(
                    JaySecurityResult.Status.BLOCKED,
                    "Sir, I could not identify that action."
            );
            recordAudit(action, result);
            return result;
        }

        JayAction.Type type = action.getType();

        if (actionPolicy.isDestructive(type)) {
            result = new JaySecurityResult(
                    JaySecurityResult.Status.NEEDS_FIVE_SECOND_DELAY,
                    "Sir, this action can permanently change or delete data. A 5-second safety confirmation is required."
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

    private void recordAudit(JayAction action, JaySecurityResult result) {
        JayAction.Type type = action == null ? null : action.getType();

        JaySecurityAudit audit = new JaySecurityAudit(
                System.currentTimeMillis(),
                type,
                result.getStatus(),
                result.getMessage()
        );

        auditLogger.log(audit);
    }

    public void startFiveSecondGuard(
            JayAction action,
            JayFiveSecondGuard.ConfirmationListener listener) {

        if (action == null || action.getType() == null) {
            return;
        }

        if (!actionPolicy.isDestructive(action.getType())) {
            return;
        }

        fiveSecondGuard.startConfirmation(action, listener);
    }

    public void cancelFiveSecondGuard() {
        fiveSecondGuard.cancel();
    }

    public boolean isFiveSecondGuardActive() {
        return fiveSecondGuard.isWaitingForConfirmation();
    }

    public JayAction getPendingFiveSecondAction() {
        return fiveSecondGuard.getPendingAction();
    }

    public boolean requiresFiveSecondConfirmation(JayAction action) {
        return action != null
                && action.getType() != null
                && actionPolicy.isDestructive(action.getType());
    }

    public boolean requiresConfirmation(JayAction action) {
        return action != null
                && action.getType() != null
                && actionPolicy.requiresConfirmation(action.getType());
    }

    public boolean requiresPermission(JayAction action) {
        return action != null
                && action.getType() != null
                && actionPolicy.requiresPermission(action.getType());
    }

    public boolean isSensitiveAction(JayAction action) {
        return action != null
                && action.getType() != null
                && actionPolicy.isSensitive(action.getType());
    }

    public String getSecurityMessage(JayAction action) {
        if (action == null || action.getType() == null) {
            return "Sir, I could not identify that action.";
        }

        JayAction.Type type = action.getType();

        if (actionPolicy.isDestructive(type)) {
            return "Sir, this action can permanently change or delete data. A 5-second safety confirmation is required.";
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
