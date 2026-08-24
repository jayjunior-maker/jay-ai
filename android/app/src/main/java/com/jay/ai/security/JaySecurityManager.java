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

    /**
     * Checks an action before Jay attempts to perform it.
     */
    public JaySecurityResult checkAction(JayAction action) {

        if (securityLock.isLocked()) {

            return new JaySecurityResult(
                    JaySecurityResult.Status.BLOCKED,
                    "Sir, Jay's protected actions are currently locked."
            );
        }

        return actionGuard.evaluate(action);
    }

    /**
     * Checks whether the required Android permission exists.
     */
    public boolean hasRequiredPermission(JayAction action) {

        return permissionManager.hasRequiredPermission(action);
    }

    /**
     * Requests the Android permission needed for an action.
     */
    public boolean requestPermission(
            Activity activity,
            JayAction action) {

        if (securityLock.isLocked()) {
            return false;
        }

        String permission =
                permissionManager.getRequiredPermission(action);

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

    /**
     * Starts the 5-second safety countdown.
     */
    public void startFiveSecondGuard(
            JayFiveSecondGuard.ConfirmationListener listener) {

        if (securityLock.isLocked()) {
            return;
        }

        actionGuard.startFiveSecondGuard(listener);
    }

    /**
     * Cancels the active 5-second countdown.
     */
    public void cancelFiveSecondGuard() {
        actionGuard.cancelFiveSecondGuard();
    }

    /**
     * Checks whether the 5-second guard is active.
     */
    public boolean isFiveSecondGuardActive() {
        return actionGuard.isFiveSecondGuardActive();
    }

    /**
     * Locks protected Jay actions.
     */
    public void lock() {
        securityLock.lock();
    }

    /**
     * Unlocks protected Jay actions.
     */
    public void unlock() {
        securityLock.unlock();
    }

    /**
     * Returns whether protected actions are locked.
     */
    public boolean isLocked() {
        return securityLock.isLocked();
    }

    /**
     * Returns the security audit history.
     */
    public JaySecurityAuditLogger getAuditLogger() {
        return auditLogger;
    }

    /**
     * Returns the action guard.
     */
    public JayActionGuard getActionGuard() {
        return actionGuard;
    }

    /**
     * Returns the permission manager.
     */
    public JayPermissionManager getPermissionManager() {
        return permissionManager;
    }

    /**
     * Returns the security lock.
     */
    public JaySecurityLock getSecurityLock() {
        return securityLock;
    }
          }
