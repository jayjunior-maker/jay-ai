package com.jay.ai;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

/** Safe, intent-based device actions. Android decides whether each action is permitted. */
public final class JayDeviceActions {
    private JayDeviceActions() { }

    public static boolean openNetworkSettings(Context context) {
        return openSettings(context, Settings.ACTION_WIRELESS_SETTINGS);
    }

    public static boolean openWifiSettings(Context context) {
        return openSettings(context, Settings.ACTION_WIFI_SETTINGS);
    }

    public static boolean openBluetoothSettings(Context context) {
        return openSettings(context, Settings.ACTION_BLUETOOTH_SETTINGS);
    }

    public static boolean openAppSettings(Context context) {
        return openSettings(context, Settings.ACTION_SETTINGS);
    }

    private static boolean openSettings(Context context, String action) {
        try {
            Intent intent = new Intent(action);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
