package com.jay.ai.security;

public class JaySecurityAudit {

    private final long timestamp;
    private final JayAction.Type actionType;
    private final JaySecurityResult.Status status;
    private final String message;

    public JaySecurityAudit(
            long timestamp,
            JayAction.Type actionType,
            JaySecurityResult.Status status,
            String message) {

        this.timestamp = timestamp;
        this.actionType = actionType;
        this.status = status;
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public JayAction.Type getActionType() {
        return actionType;
    }

    public JaySecurityResult.Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
