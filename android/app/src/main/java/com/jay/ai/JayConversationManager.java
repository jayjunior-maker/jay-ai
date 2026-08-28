package com.jay.ai;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Handles Jay's conversational personality and time-aware greetings.
 * Offline-first: this class never requires an internet connection.
 */
public class JayConversationManager {

    public String getTimeGreeting() {
        int hour = Integer.parseInt(
                new SimpleDateFormat("HH", Locale.getDefault())
                        .format(new Date())
        );

        if (hour >= 5 && hour < 12) {
            return "Good morning";
        }

        if (hour >= 12 && hour < 18) {
            return "Good afternoon";
        }

        if (hour >= 18 && hour < 22) {
            return "Good evening";
        }

        return "Good evening";
    }

    public String getStartupGreeting(boolean online) {
        String greeting = getTimeGreeting();

        if (online) {
            return greeting
                    + ", Sir. I'm online and ready. What would you like to do?";
        }

        return greeting
                + ", Sir. I'm offline right now, but my local systems are still available. What can I help you with?";
    }

    public String friendlyAcknowledgement() {
        String[] responses = {
                "Absolutely, Sir.",
                "Got it, Sir.",
                "On it, Sir.",
                "Sure thing, Sir.",
                "Understood, Sir.",
                "I'm on it."
        };

        int index = (int) (Math.random() * responses.length);
        return responses[index];
    }

    public String friendlyUnknown() {
        String[] responses = {
                "I'm not sure about that yet, Sir.",
                "I don't have a good answer for that locally yet, Sir.",
                "I'm still learning that part, Sir.",
                "I can't confidently answer that offline yet, Sir."
        };

        int index = (int) (Math.random() * responses.length);
        return responses[index];
    }
}
