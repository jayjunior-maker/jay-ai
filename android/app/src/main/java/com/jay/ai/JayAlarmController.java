package com.jay.ai;

import android.app.AlarmClock;
import android.content.Context;
import android.content.Intent;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Creates alarms through Android's local Clock/Alarm service. No internet is required. */
public final class JayAlarmController {
    private final Context context;
    private static final Pattern TIME = Pattern.compile("(?:at\\s+)?(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?", Pattern.CASE_INSENSITIVE);

    public JayAlarmController(Context context) { this.context = context.getApplicationContext(); }

    public String setAlarm(String request) {
        if (request == null) return "Tell me the time for the alarm, Sir.";
        Matcher m = TIME.matcher(request.trim());
        if (!m.find()) return "I couldn't understand the alarm time, Sir. Try: set alarm at 4am.";
        int hour;
        int minute;
        try {
            hour = Integer.parseInt(m.group(1));
            minute = m.group(2) == null ? 0 : Integer.parseInt(m.group(2));
        } catch (NumberFormatException e) {
            return "I couldn't understand that alarm time, Sir.";
        }
        String meridiem = m.group(3);
        if (meridiem != null) {
            if (hour < 1 || hour > 12 || minute > 59) return "That alarm time is invalid, Sir.";
            if ("pm".equalsIgnoreCase(meridiem) && hour < 12) hour += 12;
            if ("am".equalsIgnoreCase(meridiem) && hour == 12) hour = 0;
        } else if (hour > 23 || minute > 59) {
            return "That alarm time is invalid, Sir.";
        }
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
        intent.putExtra(AlarmClock.EXTRA_HOUR, hour);
        intent.putExtra(AlarmClock.EXTRA_MINUTES, minute);
        intent.putExtra(AlarmClock.EXTRA_MESSAGE, "Jay alarm");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            return String.format(Locale.US, "Alarm set for %02d:%02d, Sir.", hour, minute);
        } catch (Exception e) {
            return "I couldn't open the phone's alarm service, Sir.";
        }
    }

    public String showAlarms() {
        Intent intent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            return "Opening your alarms, Sir.";
        } catch (Exception e) {
            return "I couldn't open the phone's alarm controls, Sir.";
        }
    }
}