package com.jay.ai;

import java.util.Locale;

/** Maps common English and Kiswahili/Sheng music commands to media actions. */
public final class JayMusicCommandMatcher {
    public enum Action { NONE, PLAY, PAUSE, RESUME, STOP, NEXT, PREVIOUS }
    private JayMusicCommandMatcher() {}

    public static Action match(String command) {
        if (command == null) return Action.NONE;
        String s = command.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        if (s.isEmpty()) return Action.NONE;
        if (containsAny(s, "stop music", "stop the music", "off music", "turn off music", "zima ngoma", "zima doba", "zima mziki", "zima music")) return Action.STOP;
        if (containsAny(s, "pause music", "pause the music", "simamisha ngoma", "simamisha mziki")) return Action.PAUSE;
        if (containsAny(s, "resume music", "resume the music", "continue music", "endelea na ngoma", "endelea na mziki")) return Action.RESUME;
        if (containsAny(s, "next song", "next music", "skip song", "skip track", "wimbo unaofuata", "song next")) return Action.NEXT;
        if (containsAny(s, "previous song", "previous music", "last song", "wimbo uliopita", "song previous")) return Action.PREVIOUS;
        if (containsAny(s, "play music", "play a song", "play song", "cheza ngoma", "cheza mziki", "play mziki")) return Action.PLAY;
        return Action.NONE;
    }

    private static boolean containsAny(String value, String... phrases) {
        for (String phrase : phrases) if (value.equals(phrase) || value.contains(phrase)) return true;
        return false;
    }
}
