package com.jay.ai.security;

public class JayActionPolicy {

    /**
     * Returns true if the action is considered destructive.
     */
    public boolean isDestructive(JayAction.Type type) {

        if (type == null) {
            return false;
        }

        return type == JayAction.Type.DELETE_FILE
                || type == JayAction.Type.DELETE_DATA
                || type == JayAction.Type.CLEAR_DATA
                || type == JayAction.Type.UNINSTALL_APP;
    }

    /**
     * Returns true if the action requires explicit confirmation.
     */
    public boolean requiresConfirmation(JayAction.Type type) {

        if (type == null) {
            return false;
        }

        return isDestructive(type)
                || type == JayAction.Type.SEND_MESSAGE
                || type == JayAction.Type.MAKE_CALL
                || type == JayAction.Type.CHANGE_SETTING;
    }

    /**
     * Returns true if the action requires an Android permission.
     */
    public boolean requiresPermission(JayAction.Type type) {

        if (type == null) {
            return false;
        }

        return type == JayAction.Type.CAMERA
                || type == JayAction.Type.MICROPHONE
                || type == JayAction.Type.LOCATION
                || type == JayAction.Type.READ_CONTACTS
                || type == JayAction.Type.READ_MESSAGES
                || type == JayAction.Type.SEND_MESSAGE
                || type == JayAction.Type.MAKE_CALL;
    }

    /**
     * Returns true if the action is sensitive.
     */
    public boolean isSensitive(JayAction.Type type) {

        if (type == null) {
            return false;
        }

        return requiresPermission(type)
                || type == JayAction.Type.SCREEN_READ
                || type == JayAction.Type.OPEN_SETTINGS;
    }
          }
