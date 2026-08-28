package com.jay.ai;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/** Handles commands that can be completed locally on the Android device. */
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

    public String handle(String input) {
        if (input == null || input.trim().isEmpty()) return null;
        String text = input.trim().toLowerCase(Locale.ROOT);

        if (containsAny(text, "what's the time", "what is the time", "what time is it", "time now", "current time", "saa ngapi", "saa ni ngapi")) {
            String time = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()).format(new Date());
            return "The time is " + time + ", Sir.";
        }

        if (containsAny(text, "battery", "bata", "beteri", "beteri status", "battery status", "battery health")) {
            return JayDeviceTools.getBattery(context) + ".";
        }

        if (containsAny(text, "network speed", "internet speed", "speed ya network", "network information", "network info")) {
            return JayDeviceTools.getNetwork(context) + "\n" + JayDeviceTools.getNetworkSpeed(context);
        }

        if (containsAny(text, "phone information", "phone info", "device information", "device info", "information about my phone", "about my phone")) {
            return JayDeviceTools.getDevice(context) + "\n" + JayDeviceTools.getBattery(context) + "\n" + JayDeviceTools.getStorage(context) + "\n" + JayDeviceTools.getNetwork(context);
        }

        if (containsAny(text, "my location", "my current location", "where am i", "location yangu", "niko wapi")) {
            return "LOCATION_REQUEST";
        }

        if (containsAny(text, "turn torch on", "turn flashlight on", "torch on", "flashlight on", "wash torch", "washa torch")) {
            return "TORCH_ON";
        }
        if (containsAny(text, "turn torch off", "turn flashlight off", "torch off", "flashlight off", "zima torch")) {
            return "TORCH_OFF";
        }

        if (text.contains("volume") || text.contains("sound level") || text.contains("sauti")) {
            Integer percent = extractPercent(text);
            if (percent != null) {
                return setVolume(percent);
            }
        }

        if (containsAny(text, "open settings", "open phone settings", "fungua settings", "fungua mipangilio")) {
            if (text.contains("developer options") || text.contains("developer mode") || text.contains("developer settings")) {
                return openSettings(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
            }
            return openSettings(Settings.ACTION_SETTINGS);
        }

        if (containsAny(text, "open camera", "open the camera", "camera", "fungua camera", "fungua kamera")) {
            return openCamera();
        }

        if (text.startsWith("call ") || text.startsWith("piga simu ") || text.startsWith("pigia ")) {
            String name = text.startsWith("call ") ? input.trim().substring(5).trim()
                    : text.startsWith("piga simu ") ? input.trim().substring(10).trim() : input.trim().substring(6).trim();
            return callContact(name);
        }

        if (text.startsWith("open ")) {
            String appName = input.trim().substring(5).trim();
            if (!appName.isEmpty() && !appName.equalsIgnoreCase("settings") && !appName.equalsIgnoreCase("camera") && !appName.equalsIgnoreCase("phone") && !appName.equalsIgnoreCase("calendar")) {
                String result = appManager.openApp(appName);
                if (result.startsWith("OPENED_APP:")) return "Opening " + result.substring("OPENED_APP:".length()) + ", Sir.";
                return result;
            }
        }

        if (containsAny(text, "delete the file", "delete file", "delete this file", "futa file", "futa faili")) {
            return "Sir, I need the specific file selected or identified before I can delete it. A 5-second safety confirmation will be required before deletion.";
        }
        return null;
    }

    private String setVolume(int percent) {
        int safe = Math.max(0, Math.min(100, percent));
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audio == null) return "I couldn't access the phone volume, Sir.";
        int max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int target = Math.round(max * safe / 100f);
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0);
        return "Media volume set to " + safe + "%, Sir.";
    }

    private Integer extractPercent(String text) {
        int p = text.indexOf('%');
        if (p <= 0) return null;
        int end = p;
        int start = end - 1;
        while (start >= 0 && Character.isDigit(text.charAt(start))) start--;
        if (start == end - 1) return null;
        try { return Integer.parseInt(text.substring(start + 1, end)); }
        catch (NumberFormatException e) { return null; }
    }

    private String openSettings(String action) {
        try {
            Intent intent = new Intent(action);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (intent.resolveActivity(context.getPackageManager()) == null) intent = new Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return "Opening Settings, Sir.";
        } catch (Exception e) { return "I couldn't open Android Settings, Sir."; }
    }

    private String openCamera() {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(context.getPackageManager()) == null) return "I couldn't find a camera application on this phone, Sir.";
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return "Opening the camera now, Sir.";
        } catch (Exception e) { return "I couldn't open the camera, Sir."; }
    }

    private String callContact(String contactName) {
        if (contactName.isEmpty()) return "Which contact should I call, Sir?";
        String found = contactManager.findPhoneNumber(contactName);
        if ("PERMISSION_REQUIRED".equals(found)) return "Sir, Jay needs Contacts permission before I can find that contact.";
        if ("CONTACT_NOT_FOUND".equals(found)) return "I couldn't find a contact named " + contactName + ", Sir.";
        if ("ERROR".equals(found)) return "I couldn't read your contacts, Sir.";
        if (!found.startsWith("CONTACT_FOUND|")) return "I couldn't find that contact, Sir.";
        String[] parts = found.split("\\|", 3);
        if (parts.length < 3) return "I found the contact, but couldn't read the phone number, Sir.";
        String result = callManager.callNumber(parts[2]);
        if (result.startsWith("CALL_STARTED|")) return "Calling " + parts[1] + ", Sir.";
        if ("PERMISSION_REQUIRED".equals(result)) return "Sir, phone-call permission is required to call " + parts[1] + ".";
        return "I couldn't start the call to " + parts[1] + ", Sir.";
    }

    private boolean containsAny(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }
}
