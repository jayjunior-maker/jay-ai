package com.jay.ai;

import android.content.Context;
import java.util.Locale;

/** Local, user-controlled learning layer for Jay. */
public final class JayLearningManager {
    private final Context context;
    private final JayDatabase database;

    public JayLearningManager(Context context) {
        this.context = context.getApplicationContext();
        this.database = new JayDatabase(this.context);
    }

    public void learn(String key, String value) {
        if (key == null || value == null || key.trim().isEmpty() || value.trim().isEmpty()) return;
        database.saveMemory(key.trim().toLowerCase(Locale.ROOT), value.trim());
    }

    public String remember(String key) {
        if (key == null || key.trim().isEmpty()) return null;
        return database.getMemory(key.trim().toLowerCase(Locale.ROOT));
    }

    public boolean learnFromCommand(String message) {
        if (message == null) return false;
        String text = message.trim();
        String lower = text.toLowerCase(Locale.ROOT);
        String[] prefixes = {"remember that ", "remember ", "learn that ", "learn "};
        for (String prefix : prefixes) {
            if (lower.startsWith(prefix)) {
                String fact = text.substring(prefix.length()).trim();
                if (!fact.isEmpty()) {
                    learn("learned_" + System.currentTimeMillis(), fact);
                    return true;
                }
            }
        }
        return false;
    }

    public String learningResponse() {
        return "Understood, Sir. I'll remember that locally.";
    }

    public String questionForRepeatedPattern(String message) {
        if (message == null || message.trim().isEmpty()) return null;
        String lower = message.toLowerCase(Locale.ROOT);
        if (lower.contains("remember") || lower.contains("learn")) return null;
        return null;
    }
}
