package com.jay.ai;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import java.util.Locale;

/** Handles Kenyan phone numbers even when they are not saved in Contacts. */
public final class JayKenyaPhoneController {
    private final Context context;

    public JayKenyaPhoneController(Context context) {
        this.context = context.getApplicationContext();
    }

    public boolean looksLikeNumber(String value) {
        if (value == null) return false;
        String digits = value.replaceAll("[^0-9+]", "");
        return digits.matches("\\+?\\d{9,15}") || digits.matches("0\\d{8,9}");
    }

    public String normalizeKenyan(String value) {
        if (value == null) return null;
        String s = value.trim().replaceAll("[\\s().-]", "");
        if (s.startsWith("+254")) {
            String rest = s.substring(4);
            return rest.matches("\\d{9}") ? "+254" + rest : null;
        }
        if (s.startsWith("00254")) {
            String rest = s.substring(5);
            return rest.matches("\\d{9}") ? "+254" + rest : null;
        }
        if (s.startsWith("0")) {
            String rest = s.substring(1);
            return rest.matches("\\d{9}") ? "+254" + rest : null;
        }
        if (s.matches("\\d{9}")) return "+254" + s;
        return null;
    }

    public String dial(String number) {
        String normalized = normalizeKenyan(number);
        if (normalized == null) return "That doesn't look like a valid Kenyan phone number, Sir.";
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + normalized));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (intent.resolveActivity(context.getPackageManager()) == null) return "I couldn't open the phone dialer, Sir.";
            context.startActivity(intent);
            return String.format(Locale.US, "Opening the dialer for %s, Sir.", normalized);
        } catch (Exception e) {
            return "I couldn't open the phone dialer, Sir.";
        }
    }
}