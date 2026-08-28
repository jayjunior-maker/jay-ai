package com.jay.ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

/** Stores Jay's local visual appearance preferences. */
public final class JayAppearanceManager {
    private static final String PREFS = "jay_appearance";
    private static final String KEY_BACKGROUND = "background_color";
    private static final int DEFAULT_BACKGROUND = Color.rgb(8, 8, 25);

    private JayAppearanceManager() { }

    public static int getBackgroundColor(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getInt(KEY_BACKGROUND, DEFAULT_BACKGROUND);
    }

    public static boolean setBackgroundColor(Context context, String requested) {
        if (requested == null) return false;
        String value = requested.trim().toLowerCase(java.util.Locale.ROOT);
        int color;
        if (value.startsWith("#")) {
            try { color = Color.parseColor(value); }
            catch (IllegalArgumentException e) { return false; }
        } else {
            if ("black".equals(value)) color = Color.BLACK;
            else if ("blue".equals(value)) color = Color.rgb(8, 20, 55);
            else if ("purple".equals(value)) color = Color.rgb(28, 10, 55);
            else if ("red".equals(value)) color = Color.rgb(55, 8, 15);
            else if ("green".equals(value)) color = Color.rgb(5, 40, 20);
            else if ("cyan".equals(value)) color = Color.rgb(5, 40, 45);
            else if ("dark".equals(value) || "default".equals(value)) color = DEFAULT_BACKGROUND;
            else return false;
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_BACKGROUND, color).apply();
        return true;
    }

    public static void reset(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY_BACKGROUND).apply();
    }
}
