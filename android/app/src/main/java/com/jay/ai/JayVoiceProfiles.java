package com.jay.ai;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Ten selectable Jay voice profiles. Uses installed TTS voices and safe fallbacks. */
public final class JayVoiceProfiles {
    public static final String[] NAMES = {
            "Jay Deep", "Jay Calm", "Jay Command", "Jay Smooth", "Jay Future",
            "Jay Aurora", "Jay Nova", "Jay Grace", "Jay Luna", "Jay Echo"
    };
    public static final boolean[] FEMALE = {
            false, false, false, false, false, true, true, true, true, true
    };
    private static final String PREF = "jay_voice_profile";

    private JayVoiceProfiles() { }

    public static int getSelected(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE).getInt("index", 0);
    }

    public static void setSelected(Context c, int index) {
        if (index < 0 || index >= NAMES.length) return;
        c.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putInt("index", index).apply();
    }

    public static void apply(Context c, TextToSpeech tts, int index) {
        if (tts == null || index < 0 || index >= NAMES.length) return;
        try {
            List<Voice> candidates = new ArrayList<>();
            for (Voice v : tts.getVoices()) {
                Locale l = v.getLocale();
                if (l != null && "en".equalsIgnoreCase(l.getLanguage())) candidates.add(v);
            }
            Voice chosen = null;
            for (Voice v : candidates) {
                String n = v.getName() == null ? "" : v.getName().toLowerCase(Locale.ROOT);
                String f = v.getFeatures() == null ? "" : v.getFeatures().toString().toLowerCase(Locale.ROOT);
                boolean female = f.contains("gender=female") || f.contains("gender:female") || n.contains("female");
                boolean male = f.contains("gender=male") || f.contains("gender:male") || n.contains("male");
                if (FEMALE[index] ? female : male) { chosen = v; break; }
            }
            if (chosen == null && !candidates.isEmpty()) chosen = candidates.get(Math.min(index, candidates.size() - 1));
            if (chosen != null) tts.setVoice(chosen);
            tts.setPitch(FEMALE[index] ? 1.0f : 0.65f);
            tts.setSpeechRate(0.90f);
        } catch (Exception ignored) { }
    }
}
