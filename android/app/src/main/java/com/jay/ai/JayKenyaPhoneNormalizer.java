package com.jay.ai;

import java.util.Locale;

/** Normalizes Kenyan local phone numbers before contact/WhatsApp lookup. */
public final class JayKenyaPhoneNormalizer {
    private JayKenyaPhoneNormalizer() {}

    public static String normalize(String input) {
        if (input == null) return null;
        String s = input.trim().replaceAll("[\\s().-]", "");
        if (s.startsWith("00")) s = "+" + s.substring(2);
        if (s.startsWith("+")) return digitsAfterPlus(s).length() >= 9 ? "+" + digitsAfterPlus(s) : null;
        if (s.startsWith("254")) return s.length() >= 11 ? "+" + s : null;
        if (s.startsWith("0") && s.length() == 10) return "+254" + s.substring(1);
        return null;
    }

    private static String digitsAfterPlus(String s) {
        return s.substring(1).replaceAll("\\D", "");
    }
}
