package com.jay.ai.security;

public interface JayActionExecutor {

    /**
     * Executes an action after Jay's security checks
     * have approved it.
     */
    void execute(JayAction action);

    /**
     * Called when an action cannot be executed.
     */
    void onActionBlocked(JayAction action, String reason);
}
