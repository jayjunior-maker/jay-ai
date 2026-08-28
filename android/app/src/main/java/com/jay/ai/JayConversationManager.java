package com.jay.ai;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Offline-first conversational personality for Jay. */
public class JayConversationManager {
    public String getTimeGreeting() {
        int hour = Integer.parseInt(new SimpleDateFormat("HH", Locale.getDefault()).format(new Date()));
        if (hour >= 5 && hour < 12) return "Good morning";
        if (hour >= 12 && hour < 18) return "Good afternoon";
        return "Good evening";
    }
    public String getStartupGreeting(boolean online) {
        String greeting = getTimeGreeting();
        return online ? greeting + ". I'm online and ready. What would you like to do?" : greeting + ". I'm offline right now, but my local systems are still available. What can I help you with?";
    }
    public String friendlyAcknowledgement() {
        String[] r={"Absolutely.","Got it.","On it.","Sure thing.","Understood.","I'm on it."}; return r[(int)(Math.random()*r.length)];
    }
    public String friendlyUnknown() {
        String[] r={"I'm not sure about that yet.","I don't have a good answer for that locally yet.","I'm still learning that part.","I can't confidently answer that offline yet."}; return r[(int)(Math.random()*r.length)];
    }
}
