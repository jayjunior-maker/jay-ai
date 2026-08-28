package com.jay.ai;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.core.content.ContextCompat;

public class JayCallManager {

    private final Context context;

    public JayCallManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Checks whether Android has granted CALL_PHONE permission.
     */
    public boolean hasCallPermission() {
        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Places a phone call to a number.
     *
     * This method requires CALL_PHONE permission.
     */
    public String callNumber(String phoneNumber) {

        if (phoneNumber == null ||
                phoneNumber.trim().isEmpty()) {

            return "Please provide a phone number, Sir.";
        }

        String number = phoneNumber.trim();

        if (!hasCallPermission()) {
            return "PERMISSION_REQUIRED";
        }

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_CALL,
                            Uri.parse("tel:" + Uri.encode(number))
                    );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            context.startActivity(intent);

            return "CALL_STARTED|" + number;

        } catch (SecurityException e) {

            return "PERMISSION_REQUIRED";

        } catch (Exception e) {

            return "CALL_FAILED";
        }
    }

    /**
     * Opens the phone dialer without immediately placing a call.
     *
     * This does not require CALL_PHONE permission.
     */
    public String openDialer(String phoneNumber) {

        if (phoneNumber == null ||
                phoneNumber.trim().isEmpty()) {

            return "Please provide a phone number, Sir.";
        }

        String number = phoneNumber.trim();

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_DIAL,
                            Uri.parse("tel:" + Uri.encode(number))
                    );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            context.startActivity(intent);

            return "DIALER_OPENED|" + number;

        } catch (Exception e) {

            return "DIALER_FAILED";
        }
    }

    /**
     * Validates a basic phone number.
     */
    public boolean isValidPhoneNumber(String phoneNumber) {

        if (phoneNumber == null) {
            return false;
        }

        String number =
                phoneNumber
                        .trim()
                        .replace(" ", "")
                        .replace("-", "")
                        .replace("(", "")
                        .replace(")", "");

        if (number.isEmpty()) {
            return false;
        }

        if (number.startsWith("+")) {
            number = number.substring(1);
        }

        if (number.startsWith("00")) {
            number = number.substring(2);
        }

        return number.matches("\\d{7,15}");
    }
}
