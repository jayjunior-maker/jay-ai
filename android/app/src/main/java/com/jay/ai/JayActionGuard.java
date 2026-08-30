package com.jay.ai;

import android.os.Handler;
import android.os.Looper;

/**
 * Safety gate for actions that can change or delete device data/state.
 *
 * The five-second delay is NOT user authorization.  A caller must explicitly
 * call approve() after the user has said yes.  If the user says no, call deny().
 */
public final class JayActionGuard {
    public interface Callback {
        void onConfirmed();
        void onCancelled();
    }

    private static final long CONFIRMATION_DELAY_MS = 5000L;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timeout;
    private Callback callback;
    private boolean waiting;
    private boolean authorizationRequested;

    public synchronized boolean isWaiting() {
        return waiting;
    }

    public synchronized boolean isAuthorizationRequested() {
        return authorizationRequested;
    }

    /**
     * Starts the safety window. This never executes the action automatically.
     * The caller must obtain explicit user approval and then call approve().
     */
    public synchronized void requestConfirmation(Callback callback) {
        cancelPending(false);
        this.callback = callback;
        this.authorizationRequested = true;
        this.waiting = true;

        timeout = () -> {
            synchronized (JayActionGuard.this) {
                waiting = false;
                timeout = null;
            }
            // The action remains blocked until approve() is explicitly called.
        };
        handler.postDelayed(timeout, CONFIRMATION_DELAY_MS);
    }

    /** Execute the guarded action only after explicit user approval. */
    public void approve() {
        Callback cb;
        synchronized (this) {
            if (!authorizationRequested) return;
            cancelTimeout();
            authorizationRequested = false;
            waiting = false;
            cb = callback;
            callback = null;
        }
        if (cb != null) cb.onConfirmed();
    }

    /** Explicitly deny the pending action. */
    public void deny() {
        Callback cb;
        synchronized (this) {
            if (!authorizationRequested) return;
            cancelTimeout();
            authorizationRequested = false;
            waiting = false;
            cb = callback;
            callback = null;
        }
        if (cb != null) cb.onCancelled();
    }

    public void cancel() {
        Callback cb;
        synchronized (this) {
            if (!authorizationRequested) return;
            cancelTimeout();
            authorizationRequested = false;
            waiting = false;
            cb = callback;
            callback = null;
        }
        if (cb != null) cb.onCancelled();
    }

    private synchronized void cancelTimeout() {
        if (timeout != null) {
            handler.removeCallbacks(timeout);
            timeout = null;
        }
    }

    private synchronized void cancelPending(boolean notify) {
        if (timeout != null) {
            handler.removeCallbacks(timeout);
            timeout = null;
        }
        Callback cb = callback;
        boolean wasAuthorized = authorizationRequested;
        callback = null;
        authorizationRequested = false;
        waiting = false;
        if (notify && wasAuthorized && cb != null) {
            cb.onCancelled();
        }
    }
}
