package com.jay.ai;

import android.content.Context;
import java.util.Locale;

/** Local, consent-aware learning layer for Jay. */
public final class JayLearningManager {
    private final JayDatabase database;

    public JayLearningManager(Context context) {
        this.database = new JayDatabase(context.getApplicationContext());
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

    /** Records a repeated command without silently declaring it a permanent preference. */
    public void observeCommand(String command) {
        if (command == null || command.trim().isEmpty()) return;
        String normalized = command.trim().toLowerCase(Locale.ROOT);
        String key = "pattern_" + normalized;
        String existing = remember(key);
        int count = 1;
        if (existing != null) {
            try { count = Integer.parseInt(existing) + 1; } catch (NumberFormatException ignored) { }
        }
        learn(key, String.valueOf(count));
    }

    /** Returns a question only after the same command/pattern has been observed repeatedly. */
    public String questionForObservedPattern(String message) {
        if (message == null || message.trim().isEmpty()) return null;
        String normalized = message.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("remember ") || normalized.startsWith("learn ")) return null;
        String countText = remember("pattern_" + normalized);
        if (countText == null) return null;
        try {
            int count = Integer.parseInt(countText);
            if (count == 3) return "Sir, I noticed you do this repeatedly. Would you like me to remember it as a preference or routine?";
        } catch (NumberFormatException ignored) { }
        return null;
    }

    public String learningResponse() {
        return "Understood, Sir. I'll remember that locally.";
    }
}
