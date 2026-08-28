package com.jay.ai;

import android.content.Context;
import android.content.Intent;
import android.provider.AlarmClock;

/** Safe Android alarm integration. */
public final class JayAlarmManager {
    private final Context context;

    public JayAlarmManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public String setAlarm(int hour, int minute, String message) {
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            return "Invalid alarm time, Sir.";
        }
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                .putExtra(AlarmClock.EXTRA_HOUR, hour)
                .putExtra(AlarmClock.EXTRA_MINUTES, minute)
                .putExtra(AlarmClock.EXTRA_MESSAGE, message == null ? "Jay alarm" : message)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            return "I couldn't find an alarm application on this phone, Sir.";
        }
        try {
            context.startActivity(intent);
            return "Opening the alarm setup, Sir.";
        } catch (Exception e) {
            return "I couldn't open the alarm setup, Sir.";
        }
    }

    public String showAlarms() {
        return launch(AlarmClock.ACTION_SHOW_ALARMS, "Opening your alarms, Sir.");
    }

    private String launch(String action, String success) {
        Intent intent = new Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            return "The alarm screen isn't available on this phone, Sir.";
        }
        try {
            context.startActivity(intent);
            return success;
        } catch (Exception e) {
            return "I couldn't open the alarm screen, Sir.";
        }
    }
}
