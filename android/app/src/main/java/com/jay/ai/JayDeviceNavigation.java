package com.jay.ai;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

/** Safe, centralized navigation to Android system screens. */
public final class JayDeviceNavigation {
    private final Context context;

    public JayDeviceNavigation(Context context) {
        this.context = context.getApplicationContext();
    }

    public String openSettings() { return launch(Settings.ACTION_SETTINGS, "Opening Settings, Sir."); }
    public String openAccessibility() { return launch(Settings.ACTION_ACCESSIBILITY_SETTINGS, "Opening Accessibility, Sir."); }
    public String openWifi() { return launch(Settings.ACTION_WIFI_SETTINGS, "Opening Wi-Fi settings, Sir."); }
    public String openBluetooth() { return launch(Settings.ACTION_BLUETOOTH_SETTINGS, "Opening Bluetooth settings, Sir."); }
    public String openDisplay() { return launch(Settings.ACTION_DISPLAY_SETTINGS, "Opening Display settings, Sir."); }
    public String openSound() { return launch(Settings.ACTION_SOUND_SETTINGS, "Opening Sound settings, Sir."); }
    public String openNotifications() { return launch("android.settings.NOTIFICATION_SETTINGS", "Opening notification settings, Sir."); }

    public String openAppSettings(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return "I need an app package name, Sir.";
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + packageName.trim()));
        return launch(intent, "Opening app settings, Sir.");
    }

    private String launch(String action, String success) { return launch(new Intent(action), success); }

    private String launch(Intent intent, String success) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            if (intent.resolveActivity(context.getPackageManager()) == null)
                return "That settings screen isn't available on this phone, Sir.";
            context.startActivity(intent);
            return success;
        } catch (Exception e) {
            return "I couldn't open that settings screen, Sir.";
        }
    }
}
