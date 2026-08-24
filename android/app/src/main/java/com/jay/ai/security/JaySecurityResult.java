package com.jay.ai.security;

public class JaySecurityResult {

    public enum Status {
        ALLOWED,
        NEEDS_PERMISSION,
        NEEDS_CONFIRMATION,
        NEEDS_FIVE_SECOND_DELAY,
        BLOCKED
    }

    private final Status status;
    private final String message;

    public JaySecurityResult(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public boolean isAllowed() {
        return status == Status.ALLOWED;
    }

    public boolean needsPermission() {
        return status == Status.NEEDS_PERMISSION;
    }

    public boolean needsConfirmation() {
        return status == Status.NEEDS_CONFIRMATION;
    }

    public boolean needsFiveSecondDelay() {
        return status == Status.NEEDS_FIVE_SECOND_DELAY;
    }

    public boolean isBlocked() {
        return status == Status.BLOCKED;
    }
          }
