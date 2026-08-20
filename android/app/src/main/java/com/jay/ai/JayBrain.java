package com.jay.ai;

import android.content.Context;

import java.util.Locale;

public class JayBrain {

    private final Context context;
    private final JayDatabase database;

    public JayBrain(Context context) {
        this.context = context;
        this.database = new JayDatabase(context);
    }

    public String think(String input) {

        if (input == null || input.trim().isEmpty()) {
            return "Niko hapa. Niambie unataka nifanye nini? 😊";
        }

        String text = input.trim().toLowerCase(Locale.ROOT);

        // Greetings
        if (containsAny(text,
                "hello",
                "hi",
                "hey",
                "habari",
                "mambo",
                "niaje",
                "sasa")) {

            return "Habari! 😄 Mimi ni Jay. Niko hapa kukusaidia.";
        }

        // Identity
        if (containsAny(text,
                "who are you",
                "what are you",
                "wewe ni nani",
                "jay ni nani")) {

            return "Mimi ni Jay 🤖 — local AI assistant yako. Naweza kufanya kazi hata bila internet.";
        }

        // Offline status
        if (containsAny(text,
                "offline",
                "no internet",
                "hakuna internet",
                "bila internet")) {

            return "Ndiyo 👍 Ninaweza kutumia local brain na local memory bila internet.";
        }

        // Memory test
        if (containsAny(text,
                "remember",
                "kumbuka",
                "save this",
                "hifadhi hii")) {

            database.saveMemory("last_request", input);

            return "Sawa 👍 Nimehifadhi hiyo kwenye memory yangu ya ndani.";
        }

        // Recall
        if (containsAny(text,
                "what did i tell you",
                "what did i ask",
                "unakumbuka nini",
                "ulikumbuka nini")) {

            String memory = database.getMemory("last_request");

            if (memory == null) {
                return "Bado sina memory hiyo.";
            }

            return "Nakumbuka ulisema: " + memory;
        }

        // Settings
        if (containsAny(text,
                "open settings",
                "fungua settings",
                "settings")) {

            return "OPEN_SETTINGS";
        }

        // Phone
        if (containsAny(text,
                "open phone",
                "open dialer",
                "fungua simu",
                "fungua dialer")) {

            return "OPEN_PHONE";
        }

        // Calendar
        if (containsAny(text,
                "open calendar",
                "fungua calendar",
                "kalenda")) {

            return "OPEN_CALENDAR";
        }

        // Business
        if (containsAny(text,
                "inventory",
                "stock",
                "stock iko",
                "inventory iko",
                "parts")) {

            return "OPEN_INVENTORY";
        }

        // Technician
        if (containsAny(text,
                "repair",
                "repairs",
                "repair job",
                "matengenezo",
                "phone repair")) {

            return "OPEN_REPAIRS";
        }

        // Languages
        if (containsAny(text,
                "kiswahili",
                "swahili")) {

            return "Sawa 🇰🇪 Tunaweza kuongea Kiswahili.";
        }

        if (containsAny(text,
                "sheng")) {

            return "Kabisa bana 😎 Jay anaelewa Sheng pia.";
        }

        // Generic local response
        return "Nimekusikia 👍 Mimi ni Jay na niko kwenye local mode. "
                + "Kwa sasa naweza kukusaidia na commands, memory, "
                + "phone controls na business functions bila internet.";
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
