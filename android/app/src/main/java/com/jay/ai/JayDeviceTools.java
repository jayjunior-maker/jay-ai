package com.jay.ai;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.BatteryManager;
import android.os.Build;
import android.os.StatFs;

/** Local device information helpers. */
public final class JayDeviceTools {
    private JayDeviceTools() { }

    public static String getNetwork(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network network = cm == null ? null : cm.getActiveNetwork();
            NetworkCapabilities caps = network == null ? null : cm.getNetworkCapabilities(network);
            if (caps == null) return "Network: not connected.";
            String type = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ? "Wi-Fi"
                    : caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ? "mobile data"
                    : caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ? "Ethernet" : "other";
            boolean internet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            return "Network: " + type + "\nInternet: " + (internet ? "connected" : "not connected");
        } catch (Exception e) { return "Network information unavailable, Sir."; }
    }

    public static String getNetworkSpeed(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network network = cm == null ? null : cm.getActiveNetwork();
            NetworkCapabilities caps = network == null ? null : cm.getNetworkCapabilities(network);
            if (caps == null) return "Network speed: unavailable (not connected).";
            int down = caps.getLinkDownstreamBandwidthKbps();
            int up = caps.getLinkUpstreamBandwidthKbps();
            return "Estimated link speed: " + formatKbps(down) + " down / " + formatKbps(up) + " up.";
        } catch (Exception e) { return "Network speed unavailable, Sir."; }
    }

    public static String getBattery(Context context) {
        try {
            BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            if (bm == null) return "Battery information unavailable, Sir.";
            int level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            String charging = Build.VERSION.SDK_INT >= 23 ? (bm.isCharging() ? "yes" : "no") : "unknown";
            return "Battery: " + level + "%\nCharging: " + charging;
        } catch (Exception e) { return "Battery information unavailable, Sir."; }
    }

    public static String getStorage(Context context) {
        try {
            StatFs fs = new StatFs(context.getFilesDir().getAbsolutePath());
            long total = fs.getTotalBytes();
            long free = fs.getAvailableBytes();
            return "Storage: " + format(total - free) + " used\nAvailable: " + format(free) + "\nTotal: " + format(total);
        } catch (Exception e) { return "Storage information unavailable, Sir."; }
    }

    public static String getDevice(Context context) {
        return "Device: " + Build.MANUFACTURER + " " + Build.MODEL + "\nAndroid: " + Build.VERSION.RELEASE + "\nAPI: " + Build.VERSION.SDK_INT;
    }

    private static String formatKbps(int kbps) {
        if (kbps <= 0) return "unknown";
        if (kbps >= 1000) return String.format(java.util.Locale.US, "%.1f Mbps", kbps / 1000.0);
        return kbps + " Kbps";
    }

    private static String format(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double value = bytes;
        String[] units = {"KB", "MB", "GB", "TB"};
        int index = -1;
        while (value >= 1024 && index < units.length - 1) { value /= 1024.0; index++; }
        return String.format(java.util.Locale.US, "%.1f %s", value, units[index]);
    }
}
