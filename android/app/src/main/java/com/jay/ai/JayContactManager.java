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

    /**
     * Finds a contact by name and returns the first matching
     * phone number.
     */
    public String findPhoneNumber(String contactName) {

        if (!hasContactsPermission()) {
            return "PERMISSION_REQUIRED";
        }

        if (contactName == null ||
                contactName.trim().isEmpty()) {
            return "Please tell me the contact name, Sir.";
        }

        String target =
                contactName.trim();

        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                    },
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                            + " LIKE ?",
                    new String[]{"%" + target + "%"},
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                            + " ASC"
            );

            if (cursor != null &&
                    cursor.moveToFirst()) {

                int nameIndex =
                        cursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                        );

                int numberIndex =
                        cursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                        );

                if (numberIndex >= 0) {

                    String name =
                            nameIndex >= 0
                                    ? cursor.getString(nameIndex)
                                    : target;

                    String number =
                            cursor.getString(numberIndex);

                    return "CONTACT_FOUND|"
                            + name
                            + "|"
                            + number;
                }
            }

        } catch (Exception e) {

            return "ERROR";
        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return "CONTACT_NOT_FOUND";
    }

    /**
     * Returns all contacts that have phone numbers.
     */
    public List<String> getContactsWithPhoneNumbers() {

        List<String> contacts =
                new ArrayList<>();

        if (!hasContactsPermission()) {
            return contacts;
        }

        Cursor cursor = null;

        try {

            cursor = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                    },
                    null,
                    null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                            + " ASC"
            );

            if (cursor == null) {
                return contacts;
            }

            int nameIndex =
                    cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                    );

            int numberIndex =
                    cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                    );

            while (cursor.moveToNext()) {

                String name =
                        nameIndex >= 0
                                ? cursor.getString(nameIndex)
                                : "Unknown";

                String number =
                        numberIndex >= 0
                                ? cursor.getString(numberIndex)
                                : "";

                if (number != null &&
                        !number.trim().isEmpty()) {

                    contacts.add(
                            name + " — " + number
                    );
                }
            }

        } catch (Exception ignored) {

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return contacts;
    }

    /**
     * Checks whether READ_CONTACTS has been granted.
     */
    public boolean hasContactsPermission() {

        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Simple contact-name validation.
     */
    public boolean isValidContactName(String name) {

        return name != null &&
                !name.trim().isEmpty() &&
                name.trim().length() <= 100;
    }

    /**
     * Normalizes a contact search string.
     */
    public String normalizeContactName(String name) {

        if (name == null) {
            return "";
        }

        return name
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }
}

