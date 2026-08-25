package com.jay.ai.security;

import android.app.Activity;
import android.content.Context;

public class JaySecurityManager {

    private final JayActionGuard actionGuard;
    private final JayPermissionManager permissionManager;
    private final JaySecurityLock securityLock;
    private final JaySecurityAuditLogger auditLogger;

    public JaySecurityManager(Context context) {
        actionGuard = new JayActionGuard(context);
        permissionManager = new JayPermissionManager(context);
        securityLock = new JaySecurityLock();
        auditLogger = actionGuard.getAuditLogger();
    }

    public JaySecurityResult checkAction(JayAction action) {
        if (securityLock.isLocked()) {
            return new JaySecurityResult(
                    JaySecurityResult.Status.BLOCKED,
                    "Sir, Jay's protected actions are currently locked."
            );
        }

        JaySecurityResult result = actionGuard.evaluate(action);

        if (result.needsPermission()
                && permissionManager.hasRequiredPermission(action)) {
            return new JaySecurityResult(
                    JaySecurityResult.Status.ALLOWED,
                    "Required Android permission is already granted."
            );
        }

        return result;
    }

    public boolean hasRequiredPermission(JayAction action) {
        return permissionManager.hasRequiredPermission(action);
    }

    public boolean requestPermission(Activity activity, JayAction action) {
        if (activity == null || action == null || securityLock.isLocked()) {
            return false;
        }

        String permission = permissionManager.getRequiredPermission(action);
        if (permission == null) {
            return false;
        }

        switch (action.getType()) {
            case CAMERA:
                permissionManager.requestCameraPermission(activity);
                return true;
            case MICROPHONE:
                permissionManager.requestMicrophonePermission(activity);
                return true;
            case LOCATION:
                permissionManager.requestLocationPermission(activity);
                return true;
            case READ_CONTACTS:
                permissionManager.requestContactsPermission(activity);
                return true;
            case READ_MESSAGES:
            case SEND_MESSAGE:
                permissionManager.requestSmsPermission(activity);
                return true;
            case MAKE_CALL:
                permissionManager.requestPhonePermission(activity);
                return true;
            default:
                return false;
        }
    }

    public void startFiveSecondGuard(
            JayAction action,
            JayFiveSecondGuard.ConfirmationListener listener) {
        if (securityLock.isLocked()) {
            return;
        }

        actionGuard.startFiveSecondGuard(action, listener);
    }

    public void cancelFiveSecondGuard() {
        actionGuard.cancelFiveSecondGuard();
    }

    public boolean isFiveSecondGuardActive() {
        return actionGuard.isFiveSecondGuardActive();
    }

    public JayAction getPendingFiveSecondAction() {
        return actionGuard.getPendingFiveSecondAction();
    }

    public void lock() {
        securityLock.lock();
    }

    public void unlock() {
        securityLock.unlock();
    }

    public boolean isLocked() {
        return securityLock.isLocked();
    }

    public JaySecurityAuditLogger getAuditLogger() {
        return auditLogger;
    }

    public JayActionGuard getActionGuard() {
        return actionGuard;
    }

    public JayPermissionManager getPermissionManager() {
        return permissionManager;
    }

    public JaySecurityLock getSecurityLock() {
        return securityLock;
    }
}
