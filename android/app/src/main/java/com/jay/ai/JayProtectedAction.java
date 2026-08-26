package com.jay.ai;

/** Small pipeline that puts a safety confirmation in front of sensitive actions. */
public final class JayProtectedAction {
    public interface Action { void execute(); }
    public interface Listener {
        void onWaiting();
        void onConfirmed();
    }

    private final JayActionGuard guard;

    public JayProtectedAction(JayActionGuard guard) {
        this.guard = guard;
    }

    public void request(Action action, Listener listener) {
        if (listener != null) listener.onWaiting();
        guard.requestConfirmation(new JayActionGuard.Callback() {
            @Override public void onConfirmed() {
                if (listener != null) listener.onConfirmed();
                if (action != null) action.execute();
            }

            @Override public void onCancelled() { }
        });
    }

    public void cancel() { guard.cancel(); }
}
