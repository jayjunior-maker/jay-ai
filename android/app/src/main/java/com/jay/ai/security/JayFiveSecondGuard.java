package com.jay.ai.security;

import android.os.CountDownTimer;

public class JayFiveSecondGuard {

    public interface ConfirmationListener {
        void onCountdownTick(int secondsRemaining);
        void onConfirmed(JayAction action);
        void onCancelled(JayAction action);
    }

    private CountDownTimer countDownTimer;
    private boolean waitingForConfirmation = false;
    private JayAction pendingAction;

    /**
     * Starts the 5-second safety countdown for a specific action.
     */
    public void startConfirmation(
            JayAction action,
            final ConfirmationListener listener) {

        if (waitingForConfirmation || action == null) {
            return;
        }

        pendingAction = action;
        waitingForConfirmation = true;

        countDownTimer = new CountDownTimer(5000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                int secondsRemaining =
                        (int) Math.ceil(
                                millisUntilFinished / 1000.0
                        );

                if (listener != null) {
                    listener.onCountdownTick(secondsRemaining);
                }
            }

            @Override
            public void onFinish() {

                JayAction confirmedAction = pendingAction;

                waitingForConfirmation = false;
                pendingAction = null;
                countDownTimer = null;

                if (listener != null) {
                    listener.onConfirmed(confirmedAction);
                }
            }

        }.start();
    }

    /**
     * Cancels the current countdown.
     */
    public void cancel() {

        JayAction cancelledAction = pendingAction;

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        pendingAction = null;
        waitingForConfirmation = false;
    }

    /**
     * Returns the action currently waiting for confirmation.
     */
    public JayAction getPendingAction() {
        return pendingAction;
    }

    /**
     * Returns true while the safety countdown is active.
     */
    public boolean isWaitingForConfirmation() {
        return waitingForConfirmation;
    }
                    }
