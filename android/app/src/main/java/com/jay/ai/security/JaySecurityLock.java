package com.jay.ai.security;

public class JaySecurityLock {

    private boolean locked = false;

    /**
     * Locks protected Jay actions.
     */
    public void lock() {
        locked = true;
    }

    /**
     * Unlocks protected Jay actions.
     */
    public void unlock() {
        locked = false;
    }

    /**
     * Returns true when protected actions are locked.
     */
    public boolean isLocked() {
        return locked;
    }
}
