package com.jay.ai.security;

import android.os.CountDownTimer;

public class JayFiveSecondGuard {

    public interface ConfirmationListener {
        void onCountdownTick(int secondsRemaining);
        void onConfirmed(JayAction action);
        void onCancelled(JayAction action);
    }

    private static final long COUNTDOWN_MS = 5000L;
    private static final long TICK_MS = 1000L;

    private CountDownTimer countDownTimer;
    private boolean waitingForConfirmation = false;
    private JayAction pendingAction;
    private ConfirmationListener pendingListener;

    public synchronized void startConfirmation(
            JayAction action,
            ConfirmationListener listener) {

        if (waitingForConfirmation || action == null) {
            return;
        }

        pendingAction = action;
        pendingListener = listener;
        waitingForConfirmation = true;

        countDownTimer = new CountDownTimer(
                COUNTDOWN_MS,
                TICK_MS) {

            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining =
                        (int) Math.ceil(
                                millisUntilFinished / 1000.0
                        );

                ConfirmationListener currentListener;

                synchronized (JayFiveSecondGuard.this) {
                    currentListener = pendingListener;
                }

                if (currentListener != null) {
                    currentListener.onCountdownTick(
                            secondsRemaining
                    );
                }
            }

            @Override
            public void onFinish() {
                JayAction confirmedAction;
                ConfirmationListener currentListener;

                synchronized (JayFiveSecondGuard.this) {
                    confirmedAction = pendingAction;
                    currentListener = pendingListener;

                    waitingForConfirmation = false;
                    pendingAction = null;
                    pendingListener = null;
                    countDownTimer = null;
                }

                if (currentListener != null &&
                        confirmedAction != null) {
                    currentListener.onConfirmed(
                            confirmedAction
                    );
                }
            }
        }.start();
    }

    public synchronized void cancel() {
        JayAction cancelledAction = pendingAction;
        ConfirmationListener currentListener = pendingListener;

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        pendingAction = null;
        pendingListener = null;
        waitingForConfirmation = false;

        if (currentListener != null &&
                cancelledAction != null) {
            currentListener.onCancelled(
                    cancelledAction
            );
        }
    }

    public synchronized JayAction getPendingAction() {
        return pendingAction;
    }

    public synchronized boolean isWaitingForConfirmation() {
        return waitingForConfirmation;
    }
}
