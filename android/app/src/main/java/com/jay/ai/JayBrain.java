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

        String localResponse = localCommands.handle(input);
        if (localResponse != null && !localResponse.trim().isEmpty()) {
            return localResponse;
        }

        String text = input.trim().toLowerCase(Locale.ROOT);

        if (containsAny(text, "hello", "hi", "hey", "habari", "mambo", "niaje", "sasa")) {
            return "Hello, Sir. Jay is ready.";
        }

        if (containsAny(text, "who are you", "what are you", "wewe ni nani",
                "jay ni nani", "jay ni nani wewe")) {
            return "I'm Jay, your personal AI assistant, Sir.";
        }

        if (containsAny(text, "what can you do", "what can u do", "what do you do",
                "explain what you can do", "what are your capabilities", "unaweza kufanya nini",
                "unaweza kufanya nini wewe")) {
            return "Sir, I can work locally with your phone for supported actions such as opening apps, checking the time, battery and device information, opening the camera, and calling contacts when the required Android permissions are granted. I can also use local memory and conversation features. For questions that need broader knowledge, I can use Jay's online brain when the server is available.";
        }

        if (containsAny(text, "change background", "change the background", "background",
                "badilisha background", "badilisha mandharinyuma")) {
            return "Sir, the background editor is not connected yet, so I won't pretend that I changed it. Open Settings to configure Jay's interface when that option is available.";
        }

        if (containsAny(text, "offline", "no internet", "hakuna internet", "bila internet")) {
            return "Yes, Sir. I can work in local mode without an internet connection.";
        }

        if (containsAny(text, "remember", "kumbuka", "save this", "hifadhi hii")) {
            database.saveMemory("last_request", input);
            return "Sure, Sir. I've saved that in my local memory.";
        }

        if (containsAny(text, "what did i tell you", "what did i ask",
                "unakumbuka nini", "ulikumbuka nini")) {
            String memory = database.getMemory("last_request");
            if (memory == null) {
                return "I don't have anything saved in that memory yet, Sir.";
            }
            return "I remember you said: " + memory;
        }

        if (containsAny(text, "open settings", "fungua settings",
                "fungua mipangilio", "settings", "mipangilio")) {
            return "OPEN_SETTINGS";
        }

        if (containsAny(text, "open phone", "open dialer", "fungua simu",
                "fungua dialer", "fungua phone")) {
            return "OPEN_PHONE";
        }

        if (containsAny(text, "open calendar", "fungua calendar",
                "fungua kalenda", "kalenda")) {
            return "OPEN_CALENDAR";
        }

        if (containsAny(text, "inventory", "stock", "stock iko", "inventory iko",
                "parts", "bidhaa", "spares")) {
            return "OPEN_INVENTORY";
        }

        if (containsAny(text, "repair", "repairs", "repair job", "matengenezo",
                "phone repair", "karabati", "ukarabati")) {
            return "OPEN_REPAIRS";
        }

        if (containsAny(text, "kiswahili", "swahili", "sheng")) {
            return "I understand English, Kiswahili and Sheng, Sir.";
        }

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
