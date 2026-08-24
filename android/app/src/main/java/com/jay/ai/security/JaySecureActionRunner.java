package com.jay.ai.security;

public class JaySecureActionRunner {

    private final JayActionGuard actionGuard;
    private final JayActionExecutor actionExecutor;

    public JaySecureActionRunner(
            JayActionGuard actionGuard,
            JayActionExecutor actionExecutor) {

        this.actionGuard = actionGuard;
        this.actionExecutor = actionExecutor;
    }

    /**
     * Evaluates an action before allowing it to reach
     * the actual action executor.
     */
    public JaySecurityResult checkAction(JayAction action) {

        return actionGuard.evaluate(action);
    }

    /**
     * Executes an action only when the security system
     * says that it is allowed.
     */
    public boolean executeIfAllowed(JayAction action) {

        JaySecurityResult result = actionGuard.evaluate(action);

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
     * Returns the security guard used by this runner.
     */
    public JayActionGuard getActionGuard() {
        return actionGuard;
    }

    /**
     * Returns the action executor used by this runner.
     */
    public JayActionExecutor getActionExecutor() {
        return actionExecutor;
    }
          }
