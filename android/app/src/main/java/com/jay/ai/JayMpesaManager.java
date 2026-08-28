package com.jay.ai;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reads the latest M-PESA balance from an on-device SMS after explicit SMS permission. */
public final class JayMpesaManager {
    private static final Pattern BALANCE = Pattern.compile("(?i)(?:new\\s+)?m[- ]?pesa\\s+balance\\s+is\\s*(?:ksh|kes)?\\s*([0-9][0-9,]*(?:\\.[0-9]{1,2})?)");
    private static final Pattern CURRENT_BALANCE = Pattern.compile("(?i)your\\s+m[- ]?pesa\\s+balance\\s+was\\s*(?:ksh|kes)?\\s*([0-9][0-9,]*(?:\\.[0-9]{1,2})?)");
    private final Context context;

    public JayMpesaManager(Context context) { this.context = context.getApplicationContext(); }

    public String getLatestBalance() {
        if (context.checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED)
            return "MPESA_PERMISSION_REQUIRED";
        Cursor cursor = null;
        try {
            Uri uri = Uri.parse("content://sms/inbox");
            cursor = context.getContentResolver().query(uri, new String[]{"address", "body", "date"}, null, null, "date DESC");
            if (cursor == null) return "MPESA_UNAVAILABLE";
            int checked = 0;
            while (cursor.moveToNext() && checked++ < 100) {
                String address = cursor.getString(0);
                String body = cursor.getString(1);
                if (body == null) continue;
                String source = address == null ? "" : address.toLowerCase();
                String lower = body.toLowerCase();
                if (!(source.contains("mpesa") || source.contains("safaricom") || lower.contains("m-pesa") || lower.contains("m-pesa balance"))) continue;
                Matcher m = BALANCE.matcher(body);
                if (m.find()) return "Your latest M-PESA balance is Ksh " + m.group(1).replace(",", "") + ", Sir. This came from the latest matching M-PESA SMS on your phone.";
                Matcher current = CURRENT_BALANCE.matcher(body);
                if (current.find()) return "Your latest M-PESA balance is Ksh " + current.group(1).replace(",", "") + ", Sir. This came from the latest matching M-PESA SMS on your phone.";
            }
            return "I couldn't find a recent M-PESA balance in your SMS messages, Sir.";
        } catch (SecurityException e) { return "MPESA_PERMISSION_REQUIRED"; }
        catch (Exception e) { return "MPESA_UNAVAILABLE"; }
        finally { if (cursor != null) cursor.close(); }
    }
}
