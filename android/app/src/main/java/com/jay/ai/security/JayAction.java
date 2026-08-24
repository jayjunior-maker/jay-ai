package com.jay.ai.security;

public class JayAction {

    public enum Type {
        OPEN_APP,
        OPEN_SETTINGS,
        CAMERA,
        MICROPHONE,
        SCREEN_READ,
        LOCATION,
        READ_CONTACTS,
        READ_MESSAGES,
        SEND_MESSAGE,
        MAKE_CALL,
        CHANGE_SETTING,
        CREATE_FILE,
        DELETE_FILE,
        DELETE_DATA,
        CLEAR_DATA,
        UNINSTALL_APP,
        UNKNOWN
    }

    private final Type type;
    private final String description;

    public JayAction(Type type, String description) {
        this.type = type;
        this.description = description;
    }

    public Type getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDestructive() {
        return type == Type.DELETE_FILE
                || type == Type.DELETE_DATA
                || type == Type.CLEAR_DATA
                || type == Type.UNINSTALL_APP;
    }

    public boolean requiresConfirmation() {
        return isDestructive()
                || type == Type.SEND_MESSAGE
                || type == Type.MAKE_CALL
                || type == Type.CHANGE_SETTING;
    }

    public boolean requiresPermission() {
        return type == Type.CAMERA
                || type == Type.MICROPHONE
                || type == Type.LOCATION
                || type == Type.READ_CONTACTS
                || type == Type.READ_MESSAGES
                || type == Type.SEND_MESSAGE
                || type == Type.MAKE_CALL;
    }
  }
