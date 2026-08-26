package com.jay.ai;

import android.os.Handler;
import android.os.Looper;

/** Safety gate for actions that can change or delete device data/state. */
public final class JayActionGuard {
    public interface Callback { void onConfirmed(); void onCancelled(); }

    private static final long CONFIRMATION_DELAY_MS = 5000L;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pending;
    private boolean waiting;

    public synchronized boolean isWaiting() { return waiting; }

    public synchronized void requestConfirmation(Callback callback) {
        cancelPending(false);
        waiting = true;
        pending = () -> {
            synchronized (JayActionGuard.this) { waiting = false; pending = null; }
            if (callback != null) callback.onConfirmed();
        };
        handler.postDelayed(pending, CONFIRMATION_DELAY_MS);
    }

    public synchronized void cancel() {
        cancelPending(true);
    }

    private void cancelPending(boolean notify) {
        if (pending != null) {
            handler.removeCallbacks(pending);
            pending = null;
        }
        boolean wasWaiting = waiting;
        waiting = false;
        if (notify && wasWaiting) {
            // Cancellation is intentionally silent here; UI may provide its own feedback.
        }
    }
}
