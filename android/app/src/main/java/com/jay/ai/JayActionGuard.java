package com.jay.ai;

import android.os.Handler;
import android.os.Looper;

/** Safety gate for actions that can change or delete device data/state. */
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

    public synchronized boolean isWaiting() { return waiting; }
    public synchronized boolean isAuthorizationRequested() { return authorizationRequested; }

    /** Starts a five-second safety window; it never executes automatically. */
    public synchronized void requestConfirmation(Callback newCallback) {
        cancelPending();
        if (newCallback == null) return;

        callback = newCallback;
        authorizationRequested = true;
        waiting = true;

        final Callback requestCallback = newCallback;
        timeout = () -> {
            synchronized (JayActionGuard.this) {
                if (callback != requestCallback || !authorizationRequested) return;
                callback = null;
                authorizationRequested = false;
                waiting = false;
                timeout = null;
            }
            requestCallback.onCancelled();
        };
        handler.postDelayed(timeout, CONFIRMATION_DELAY_MS);
    }

    /** Execute only after explicit approval while the request is active. */
    public void approve() {
        Callback cb;
        synchronized (this) {
            if (!authorizationRequested || !waiting || callback == null) return;
            cancelTimeoutLocked();
            cb = callback;
            callback = null;
            authorizationRequested = false;
            waiting = false;
        }
        cb.onConfirmed();
    }

    /** Explicitly deny the pending action. */
    public void deny() { cancelAndNotify(); }

    /** Cancel the pending action without executing it. */
    public void cancel() { cancelAndNotify(); }

    private void cancelAndNotify() {
        Callback cb;
        synchronized (this) {
            if (!authorizationRequested || callback == null) return;
            cancelTimeoutLocked();
            cb = callback;
            callback = null;
            authorizationRequested = false;
            waiting = false;
        }
        cb.onCancelled();
    }

    private synchronized void cancelPending() {
        cancelTimeoutLocked();
        callback = null;
        authorizationRequested = false;
        waiting = false;
    }

    private void cancelTimeoutLocked() {
        if (timeout != null) {
            handler.removeCallbacks(timeout);
            timeout = null;
        }
    }
}
