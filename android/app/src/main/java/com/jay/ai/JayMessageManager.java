package com.jay.ai;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.core.content.ContextCompat;

public class JayMessageManager {

    private final Context context;

    public JayMessageManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Checks whether Android has granted SEND_SMS permission.
     */
    public boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Sends an SMS directly.
     *
     * This requires SEND_SMS permission.
     */
    public String sendSms(String phoneNumber, String message) {

        if (phoneNumber == null ||
                phoneNumber.trim().isEmpty()) {

            return "Please provide a phone number, Sir.";
        }

        if (message == null ||
                message.trim().isEmpty()) {

            return "Please provide a message, Sir.";
        }

        if (!hasSmsPermission()) {
            return "PERMISSION_REQUIRED";
        }

        try {

            android.telephony.SmsManager smsManager =
                    android.telephony.SmsManager.getDefault();

            String number =
                    phoneNumber.trim();

            String text =
                    message.trim();

            if (text.length() <= 160) {

                smsManager.sendTextMessage(
                        number,
                        null,
                        text,
                        null,
                        null
                );

            } else {

                java.util.ArrayList<String> parts =
                        smsManager.divideMessage(text);

                smsManager.sendMultipartTextMessage(
                        number,
                        null,
                        parts,
                        null,
                        null
                );
            }

            return "SMS_SENT|" + number;

        } catch (SecurityException e) {

            return "PERMISSION_REQUIRED";

        } catch (Exception e) {

            return "SMS_FAILED";
        }
    }

    /**
     * Opens the Android SMS composer with the recipient
     * and message already filled in.
     *
     * This does not directly send the message.
     */
    public String openSmsComposer(
            String phoneNumber,
            String message) {

        if (phoneNumber == null ||
                phoneNumber.trim().isEmpty()) {

            return "Please provide a phone number, Sir.";
        }

        if (message == null ||
                message.trim().isEmpty()) {

            return "Please provide a message, Sir.";
        }

        try {

            Uri uri =
                    Uri.parse(
                            "smsto:"
                                    + Uri.encode(
                                    phoneNumber.trim()
                            )
                    );

            Intent intent =
                    new Intent(
                            Intent.ACTION_SENDTO,
                            uri
                    );

            intent.putExtra(
                    "sms_body",
                    message.trim()
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            context.startActivity(intent);

            return "SMS_COMPOSER_OPENED";

        } catch (Exception e) {

            return "SMS_COMPOSER_FAILED";
        }
    }

    /**
     * Basic phone-number validation.
     */
    public boolean isValidPhoneNumber(
            String phoneNumber) {

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
