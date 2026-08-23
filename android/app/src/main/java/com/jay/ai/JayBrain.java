package com.jay.ai;

import android.content.Context;

import java.util.Locale;

public class JayBrain {

    private final Context context;
    private final JayDatabase database;

    public JayBrain(Context context) {
        this.context = context.getApplicationContext();
        this.database = new JayDatabase(this.context);
    }

    public String think(String input) {

        if (input == null || input.trim().isEmpty()) {
            return "I'm here, Sir. Tell me what you would like me to do.";
        }

        String text = input.trim().toLowerCase(Locale.ROOT);

        // Greetings — English, Kiswahili and Sheng
        if (containsAny(text,
                "hello",
                "hi",
                "hey",
                "habari",
                "mambo",
                "niaje",
                "sasa")) {

            return "Hello, Sir. I'm Jay. How can I help you?";
        }

        // Identity
        if (containsAny(text,
                "who are you",
                "what are you",
                "wewe ni nani",
                "jay ni nani",
                "jay ni nani wewe")) {

            return "I'm Jay, your AI assistant. I can help you with phone controls, memory, business tasks and more.";
        }

        // Offline status
        if (containsAny(text,
                "offline",
                "no internet",
                "hakuna internet",
                "bila internet")) {

            return "Yes, Sir. I can work in local mode without an internet connection.";
        }

        // Memory
        if (containsAny(text,
                "remember",
                "kumbuka",
                "save this",
                "hifadhi hii")) {

            database.saveMemory("last_request", input);

            return "Sure, Sir. I've saved that in my local memory.";
        }

        // Recall
        if (containsAny(text,
                "what did i tell you",
                "what did i ask",
                "unakumbuka nini",
                "ulikumbuka nini")) {

            String memory =
                    database.getMemory("last_request");

            if (memory == null) {
                return "I don't have anything saved in that memory yet, Sir.";
            }

            return "I remember you said: " + memory;
        }

        // Settings
        if (containsAny(text,
                "open settings",
                "fungua settings",
                "fungua mipangilio",
                "settings",
                "mipangilio")) {

            return "OPEN_SETTINGS";
        }

        // Phone
        if (containsAny(text,
                "open phone",
                "open dialer",
                "fungua simu",
                "fungua dialer",
                "fungua phone")) {

            return "OPEN_PHONE";
        }

        // Calendar
        if (containsAny(text,
                "open calendar",
                "fungua calendar",
                "fungua kalenda",
                "kalenda")) {

            return "OPEN_CALENDAR";
        }

        // Business / inventory
        if (containsAny(text,
                "inventory",
                "stock",
                "stock iko",
                "inventory iko",
                "parts",
                "bidhaa",
                "spares")) {

            return "OPEN_INVENTORY";
        }

        // Repairs
        if (containsAny(text,
                "repair",
                "repairs",
                "repair job",
                "matengenezo",
                "phone repair",
                "karabati",
                "ukarabati")) {

            return "OPEN_REPAIRS";
        }

        // Language
        if (containsAny(text,
                "kiswahili",
                "swahili",
                "sheng")) {

            return "I understand English, Kiswahili and Sheng, Sir.";
        }

        // Unknown request
        return "ONLINE_REQUIRED";
    }

    /**
     * Sends an unknown request to Jay's cloud backend.
     *
     * This is asynchronous because network operations
     * must not block the Android UI thread.
     */
    public void askOnline(
            String input,
            JayApiClient.Callback callback) {

        if (input == null ||
                input.trim().isEmpty()) {

            if (callback != null) {
                callback.onError(
                        "Empty message."
                );
            }

            return;
        }

        if (callback == null) {
            return;
        }

        JayApiClient apiClient =
                new JayApiClient();

        apiClient.chat(
                input.trim(),
                new JayApiClient.Callback() {

                    @Override
                    public void onSuccess(
                            String reply) {

                        callback.onSuccess(reply);
                        apiClient.shutdown();
                    }

                    @Override
                    public void onError(
                            String error) {

                        callback.onError(error);
                        apiClient.shutdown();
                    }
                }
        );
    }

    private boolean containsAny(
            String text,
            String... words) {

        for (String word : words) {

            if (text.contains(word)) {
                return true;
            }
        }

        return false;
    }
        }
