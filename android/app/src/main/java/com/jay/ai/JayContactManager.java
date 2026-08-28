package com.jay.ai;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JayContactManager {
    private final Context context;

    public JayContactManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /** Finds a contact by name and returns a normalized Kenyan/international number. */
    public String findPhoneNumber(String contactName) {
        if (!hasContactsPermission()) return "PERMISSION_REQUIRED";
        if (!isValidContactName(contactName)) return "Please tell me the contact name, Sir.";
        String target = contactName.trim();
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER},
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?",
                    new String[]{"%" + target + "%"},
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                if (numberIndex >= 0) {
                    String name = nameIndex >= 0 ? cursor.getString(nameIndex) : target;
                    String number = cursor.getString(numberIndex);
                    String normalized = JayKenyaPhoneNormalizer.normalize(number);
                    return normalized == null ? "CONTACT_INVALID_NUMBER|" + name + "|" + number
                            : "CONTACT_FOUND|" + name + "|" + normalized;
                }
            }
        } catch (Exception e) {
            return "ERROR";
        } finally {
            if (cursor != null) cursor.close();
        }
        return "CONTACT_NOT_FOUND";
    }

    /** Returns likely name matches so Jay can ask instead of guessing. */
    public List<String> findLikelyContacts(String name) {
        List<String> matches = new ArrayList<>();
        if (!hasContactsPermission() || !isValidContactName(name)) return matches;
        String target = normalizeContactName(name);
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER},
                    null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            if (cursor == null) return matches;
            int ni = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int pi = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            while (cursor.moveToNext() && matches.size() < 5) {
                String n = ni >= 0 ? cursor.getString(ni) : "";
                String p = pi >= 0 ? cursor.getString(pi) : "";
                String normalized = normalizeContactName(n);
                if (!n.isEmpty() && (normalized.contains(target) || target.contains(normalized))) {
                    String phone = JayKenyaPhoneNormalizer.normalize(p);
                    if (phone != null) matches.add(n + "|" + phone);
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        return matches;
    }

    public List<String> getContactsWithPhoneNumbers() {
        List<String> contacts = new ArrayList<>();
        if (!hasContactsPermission()) return contacts;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER},
                    null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            if (cursor == null) return contacts;
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            while (cursor.moveToNext()) {
                String name = nameIndex >= 0 ? cursor.getString(nameIndex) : "Unknown";
                String number = numberIndex >= 0 ? cursor.getString(numberIndex) : "";
                if (number != null && !number.trim().isEmpty()) {
                    String normalized = JayKenyaPhoneNormalizer.normalize(number);
                    contacts.add(name + " — " + (normalized == null ? number : normalized));
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        return contacts;
    }

    public boolean hasContactsPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isValidContactName(String name) {
        return name != null && !name.trim().isEmpty() && name.trim().length() <= 100;
    }

    public String normalizeContactName(String name) {
        if (name == null) return "";
        return name.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
