package com.jay.ai;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Handles commands that must work locally on the Android device.
 * These commands never depend on Jay's online backend.
 */
public final class JayLocalCommandManager {

    private final Context context;
    private final JayAppManager appManager;
    private final JayContactManager contactManager;
    private final JayCallManager callManager;

    public JayLocalCommandManager(Context context) {
        this.context = context.getApplicationContext();
        this.appManager = new JayAppManager(this.context);
        this.contactManager = new JayContactManager(this.context);
        this.callManager = new JayCallManager(this.context);
    }

    /**
     * Returns null when the message is not a local device command.
     */
    public String handle(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String text = input.trim().toLowerCase(Locale.ROOT);

        // TIME must always be available locally.
        if (containsAny(text, "what's the time", "what is the time", "what time is it",
                "time now", "current time", "saa ngapi", "saa ni ngapi")) {
            String time = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())
                    .format(new Date());
            return "The time is " + time + ", Sir.";
        }

        // BATTERY / HEALTH must always be available locally.
        if (containsAny(text, "battery", "bata", "battery status", "battery health",
                "beteri", "beteri status", "beteri health")) {
            return JayDeviceTools.getBattery(context) + ".";
        }

        // General phone/device information is local.
        if (containsAny(text, "phone information", "phone info", "device information",
                "device info", "information about my phone", "about my phone")) {
            return JayDeviceTools.getDevice(context) + "\n" +
                    JayDeviceTools.getBattery(context) + "\n" +
                    JayDeviceTools.getStorage(context) + "\n" +
                    JayDeviceTools.getNetwork(context);
        }

        // Open an installed app locally. No online brain is required.
        String appName = extractAfter(text, "open ");
        if (appName != null && !appName.isEmpty()) {
            if (appName.equals("whatsapp")) {
                return openApp("WhatsApp");
            }
            if (!appName.contains("settings") && !appName.contains("calendar")
                    && !appName.contains("phone") && !appName.contains("camera")) {
                return openApp(appName);
            }
        }

        // Camera is a local Android action.
        if (containsAny(text, "open camera", "camera", "fungua camera", "fungua kamera",
                "take a photo", "take photo")) {
            return openCamera();
        }

        // Contact calling is local. Never send this command to the online server.
        if (text.startsWith("call ") || text.startsWith("piga simu ") ||
                text.startsWith("pigia ")) {
            String contactName;
            if (text.startsWith("call ")) {
                contactName = input.trim().substring(5).trim();
            } else if (text.startsWith("piga simu ")) {
                contactName = input.trim().substring(10).trim();
            } else {
                contactName = input.trim().substring(6).trim();
            }
            return callContact(contactName);
        }

        // Destructive actions are deliberately not executed from a vague sentence.
        if (containsAny(text, "delete the file", "delete file", "delete this file",
                "futa file", "futa faili")) {
            return "Sir, I can delete a specific file, but I need the file selected or its exact path first. Deletion will require the 5-second safety confirmation.";
        }

        return null;
    }

    private String openApp(String name) {
        String result = appManager.openApp(name);
        if (result.startsWith("OPENED_APP:")) {
            return "Opening " + result.substring("OPENED_APP:".length()) + ", Sir.";
        }
        return result;
    }

    private String callContact(String contactName) {
        if (contactName.isEmpty()) {
            return "Which contact should I call, Sir?";
        }

        String found = contactManager.findPhoneNumber(contactName);
        if (found.equals("PERMISSION_REQUIRED")) {
            return "Sir, Jay needs Contacts permission before I can find that contact.";
        }
        if (found.equals("CONTACT_NOT_FOUND")) {
            return "I couldn't find a contact named " + contactName + ", Sir.";
        }
        if (found.equals("ERROR")) {
            return "I couldn't read your contacts, Sir.";
        }
        if (!found.startsWith("CONTACT_FOUND|")) {
            return "I couldn't find that contact, Sir.";
        }

        String[] parts = found.split("\\|", 3);
        if (parts.length < 3) {
            return "I found the contact, but I couldn't read the phone number, Sir.";
        }

        String name = parts[1];
        String number = parts[2];
        String result = callManager.callNumber(number);

        if (result.startsWith("CALL_STARTED|")) {
            return "Calling " + name + ", Sir.";
        }
        if (result.equals("PERMISSION_REQUIRED")) {
            return "Sir, Jay found " + name + ", but Phone permission is required to place the call.";
        }
        return "I couldn't start the call to " + name + ", Sir.";
    }

    private String openCamera() {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            PackageManager pm = context.getPackageManager();
            if (intent.resolveActivity(pm) == null) {
                return "I couldn't find a camera application on this phone, Sir.";
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return "Opening the camera now, Sir. I can open the camera locally; live image understanding will be connected to Jay's vision system next.";
        } catch (Exception e) {
            return "I couldn't open the camera, Sir.";
        }
    }

    private String extractAfter(String text, String prefix) {
        if (!text.startsWith(prefix)) {
            return null;
        }
        return text.substring(prefix.length()).trim();
    }

    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
