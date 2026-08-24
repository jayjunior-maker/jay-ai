package com.jay.ai.security;

import android.os.CountDownTimer;

public class JayFiveSecondGuard {

    public interface ConfirmationListener {
        void onCountdownTick(int secondsRemaining);
        void onConfirmed();
        void onCancelled();
    }

    private CountDownTimer countDownTimer;
    private boolean waitingForConfirmation = false;

    /**
     * Starts the 5-second safety countdown.
     */
    public void startConfirmation(final ConfirmationListener listener) {

        if (waitingForConfirmation) {
            return;
        }

        waitingForConfirmation = true;

        countDownTimer = new CountDownTimer(5000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining =
                        (int) Math.ceil(millisUntilFinished / 1000.0);

                if (listener != null) {
                    listener.onCountdownTick(secondsRemaining);
                }
            }

            @Override
            public void onFinish() {
                waitingForConfirmation = false;

                if (listener != null) {
                    listener.onConfirmed();
                }
            }

        }.start();
    }

    /**
     * Cancels the current countdown.
     */
    public void cancel() {

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        waitingForConfirmation = false;
    }

    /**
     * Returns true while the safety countdown is active.
     */
    public boolean isWaitingForConfirmation() {
        return waitingForConfirmation;
    }
}
