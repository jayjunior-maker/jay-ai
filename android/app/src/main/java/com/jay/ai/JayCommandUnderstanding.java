package com.jay.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts natural-language requests into structured intent information.
 * This is deliberately separate from execution: understanding must never execute an action by itself.
 */
public final class JayCommandUnderstanding {
    public enum Intent {
        UNKNOWN, OPEN_APP, OPEN_SETTINGS, PLAY_MUSIC, STOP_MUSIC, PAUSE_MUSIC,
        RESUME_MUSIC, NEXT_MUSIC, PREVIOUS_MUSIC, SEND_MESSAGE, CALL_CONTACT,
        SET_ALARM, CANCEL_ALARM, NETWORK_STATUS, DEVICE_NAVIGATION, CLEAR_CHAT,
        CHECK_UPDATE, MPESA_BALANCE
    }

    public static final class Result {
        public final Intent intent;
        public final String target;
        public final String message;
        public final List<String> steps;

        Result(Intent intent, String target, String message, List<String> steps) {
            this.intent = intent;
            this.target = target;
            this.message = message;
            this.steps = steps;
        }
    }

    private JayCommandUnderstanding() {}

    public static Result understand(String input) {
        String s = normalize(input);
        if (s.isEmpty()) return unknown();

        if (isAny(s, "clear chat", "delete chat history", "clear conversation"))
            return result(Intent.CLEAR_CHAT, "", "", null);
        if (isAny(s, "check for updates", "check jay update", "update jay", "is jay up to date"))
            return result(Intent.CHECK_UPDATE, "Jay", "", null);
        if (isAny(s, "m-pesa balance", "mpesa balance", "m pesa balance", "check my mpesa"))
            return result(Intent.MPESA_BALANCE, "M-PESA", "", null);

        JayMusicCommandMatcher.Action music = JayMusicCommandMatcher.match(s);
        switch (music) {
            case STOP: return result(Intent.STOP_MUSIC, "music", "", null);
            case PAUSE: return result(Intent.PAUSE_MUSIC, "music", "", null);
            case RESUME: return result(Intent.RESUME_MUSIC, "music", "", null);
            case NEXT: return result(Intent.NEXT_MUSIC, "music", "", null);
            case PREVIOUS: return result(Intent.PREVIOUS_MUSIC, "music", "", null);
            case PLAY:
                return result(Intent.PLAY_MUSIC, extractMusicTarget(s), "", null);
            default: break;
        }

        Matcher msg = Pattern.compile("^(?:open\\s+)?whatsapp\\s+and\\s+(?:send|text|message)\\s+(.+?)\\s+(?:saying|that)\\s+(.+)$").matcher(s);
        if (msg.find()) return result(Intent.SEND_MESSAGE, msg.group(1).trim(), msg.group(2).trim(), null);

        Matcher call = Pattern.compile("^(?:please\\s+)?(?:call|phone|ring)\\s+(.+)$").matcher(s);
        if (call.find()) return result(Intent.CALL_CONTACT, call.group(1).trim(), "", null);

        Matcher alarm = Pattern.compile("^(?:set|create)\\s+(?:an?\\s+)?alarm\\s+(?:for|at)\\s+(.+)$").matcher(s);
        if (alarm.find()) return result(Intent.SET_ALARM, alarm.group(1).trim(), "", null);
        Matcher cancelAlarm = Pattern.compile("^(?:cancel|turn off|delete)\\s+(?:the\\s+)?alarm(?:\\s+(?:for|at)\\s+(.+))?$").matcher(s);
        if (cancelAlarm.find()) return result(Intent.CANCEL_ALARM, cancelAlarm.group(1) == null ? "" : cancelAlarm.group(1).trim(), "", null);

        if (isAny(s, "internet status", "network status", "am i connected", "is internet connected", "check my internet"))
            return result(Intent.NETWORK_STATUS, "network", "", null);

        if (s.startsWith("open settings") || s.equals("settings")) {
            List<String> steps = navigationSteps(s.substring(s.indexOf("settings") + "settings".length()).trim());
            return result(Intent.OPEN_SETTINGS, "Settings", "", steps);
        }
        if (s.startsWith("open ")) return result(Intent.OPEN_APP, s.substring(5).trim(), "", null);
        if (s.startsWith("go to ") || s.startsWith("navigate to "))
            return result(Intent.DEVICE_NAVIGATION, s.replaceFirst("^(go to|navigate to)\\s+", "").trim(), "", null);

        return unknown();
    }

    private static String extractMusicTarget(String s) {
        String[] prefixes = {"play music", "play the music", "play a song", "play song", "cheza ngoma", "cheza mziki", "play mziki"};
        for (String p : prefixes) if (s.startsWith(p)) return s.substring(p.length()).trim();
        return "";
    }

    private static List<String> navigationSteps(String tail) {
        List<String> steps = new ArrayList<>();
        if (tail.isEmpty()) return steps;
        for (String part : tail.split("\\s+(?:then|and)\\s+|\\s*,\\s*")) {
            String x = part.trim();
            if (!x.isEmpty()) steps.add(x);
        }
        return steps;
    }

    private static String normalize(String input) {
        if (input == null) return "";
        String s = input.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
        if (s.startsWith("jay ")) s = s.substring(4).trim();
        if (s.equals("jay")) return "";
        return s;
    }

    private static boolean isAny(String s, String... values) {
        for (String v : values) if (s.equals(v) || s.contains(v)) return true;
        return false;
    }

    private static Result result(Intent intent, String target, String message, List<String> steps) {
        return new Result(intent, target, message, steps == null ? new ArrayList<String>() : steps);
    }

    private static Result unknown() { return result(Intent.UNKNOWN, "", "", null); }
}
