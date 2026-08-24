package com.jay.ai.security;

import android.app.Activity;

public class JaySecureActionRunner {

    private final JayActionGuard actionGuard;
    private final JayActionExecutor actionExecutor;
    private final JayPermissionManager permissionManager;

    public JaySecureActionRunner(
            JayActionGuard actionGuard,
            JayActionExecutor actionExecutor,
            JayPermissionManager permissionManager) {

        this.actionGuard = actionGuard;
        this.actionExecutor = actionExecutor;
        this.permissionManager = permissionManager;
    }

    /**
     * Evaluates an action before allowing it to reach
     * the actual action executor.
     */
    public JaySecurityResult checkAction(JayAction action) {

        JaySecurityResult result = actionGuard.evaluate(action);

        /*
         * If the action requires an Android permission,
         * verify whether that permission has actually
         * been granted.
         */
        if (result.needsPermission()) {

            if (permissionManager.hasRequiredPermission(action)) {

                return new JaySecurityResult(
                        JaySecurityResult.Status.ALLOWED,
                        "Required Android permission is already granted."
                );
            }

            return result;
        }

        return result;
    }

    /**
     * Executes an action only when all security checks
     * have passed.
     */
    public boolean executeIfAllowed(JayAction action) {

        JaySecurityResult result = checkAction(action);

        if (result.isAllowed()) {
            actionExecutor.execute(action);
            return true;
        }

        actionExecutor.onActionBlocked(
                action,
                result.getMessage()
        );

        return false;
    }

    /**
     * Requests the Android permission required by an action.
     */
    public boolean requestPermission(
            Activity activity,
            JayAction action) {

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

    public JayActionGuard getActionGuard() {
        return actionGuard;
    }

    public JayActionExecutor getActionExecutor() {
        return actionExecutor;
    }

    public JayPermissionManager getPermissionManager() {
        return permissionManager;
    }
            }
