package com.jay.ai;

import android.content.Context;

import java.util.Locale;

public class JayBrain {

    private final Context context;
    private final JayDatabase database;
    private final JayApiClient apiClient;
    private final JayLocalCommandManager localCommands;

    public JayBrain(Context context) {

        this.context = context.getApplicationContext();
        this.database = new JayDatabase(context);
        this.apiClient = new JayApiClient();
        this.localCommands = new JayLocalCommandManager(this.context);
    }

    public String think(String input) {

        if (input == null || input.trim().isEmpty()) {
            return "I'm here. Tell me what you would like me to do, Sir.";
        }

        // Device actions must be handled locally before any online fallback.
        // This prevents commands such as time, battery, calls, apps and camera
        // from becoming fake 404 'online brain' failures.
        String localResponse = localCommands.handle(input);
        if (localResponse != null && !localResponse.trim().isEmpty()) {
            return localResponse;
        }

        String text = input.trim().toLowerCase(Locale.ROOT);

        // =========================
        // GREETINGS
        // =========================
        if (containsAny(text, "hello", "hi", "hey", "habari", "mambo", "niaje", "sasa")) {
            return "Hello, Sir. Jay is online and ready.";
        }

        // =========================
        // IDENTITY
        // =========================
        if (containsAny(text, "who are you", "what are you", "wewe ni nani",
                "jay ni nani", "jay ni nani wewe")) {
            return "I'm Jay, your personal AI assistant, Sir.";
        }

        // =========================
        // OFFLINE
        // =========================
        if (containsAny(text, "offline", "no internet", "hakuna internet", "bila internet")) {
            return "Yes, Sir. I can work in local mode without an internet connection.";
        }

        // =========================
        // MEMORY
        // =========================
        if (containsAny(text, "remember", "kumbuka", "save this", "hifadhi hii")) {
            database.saveMemory("last_request", input);
            return "Sure, Sir. I've saved that in my local memory.";
        }

        // =========================
        // RECALL
        // =========================
        if (containsAny(text, "what did i tell you", "what did i ask",
                "unakumbuka nini", "ulikumbuka nini")) {
            String memory = database.getMemory("last_request");
            if (memory == null) {
                return "I don't have anything saved in that memory yet, Sir.";
            }
            return "I remember you said: " + memory;
        }

        // =========================
        // SETTINGS
        // =========================
        if (containsAny(text, "open settings", "fungua settings",
                "fungua mipangilio", "settings", "mipangilio")) {
            return "OPEN_SETTINGS";
        }

        // =========================
        // PHONE / DIALER
        // =========================
        if (containsAny(text, "open phone", "open dialer", "fungua simu",
                "fungua dialer", "fungua phone")) {
            return "OPEN_PHONE";
        }

        // =========================
        // CALENDAR
        // =========================
        if (containsAny(text, "open calendar", "fungua calendar",
                "fungua kalenda", "kalenda")) {
            return "OPEN_CALENDAR";
        }

        // =========================
        // INVENTORY
        // =========================
        if (containsAny(text, "inventory", "stock", "stock iko", "inventory iko",
                "parts", "bidhaa", "spares")) {
            return "OPEN_INVENTORY";
        }

        // =========================
        // REPAIRS
        // =========================
        if (containsAny(text, "repair", "repairs", "repair job", "matengenezo",
                "phone repair", "karabati", "ukarabati")) {
            return "OPEN_REPAIRS";
        }

        // =========================
        // LANGUAGE
        // =========================
        if (containsAny(text, "kiswahili", "swahili", "sheng")) {
            return "I understand English, Kiswahili and Sheng, Sir.";
        }

        // =========================
        // UNKNOWN -> ONLINE FALLBACK
        // =========================
        return "ONLINE_REQUIRED";
    }

    public void askOnline(String message, JayApiClient.Callback callback) {

        if (message == null || message.trim().isEmpty()) {
            callback.onError("Empty message.");
            return;
        }

        apiClient.chat(message.trim(), callback);
    }

    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
