package com.jay.ai;

import android.content.Context;

/** Persistent local settings for Jay's chat/history and phone-control preferences. */
public final class JayFeatureSettings {
    private static final String PREF = "jay_feature_settings";
    private JayFeatureSettings() {}

    public static boolean keepHistory(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE).getBoolean("keep_history", true);
    }

    public static void setKeepHistory(Context c, boolean enabled) {
        c.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putBoolean("keep_history", enabled).apply();
    }
}
