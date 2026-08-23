package com.jay.ai;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.util.Locale;

public class JayDeviceInfo {

    private final Context context;

    public JayDeviceInfo(Context context) {
        this.context = context.getApplicationContext();
    }

    public String getDeviceInfo() {

        StringBuilder info =
                new StringBuilder();

        info.append("Device Information\n\n");

        // Manufacturer
        info.append("Manufacturer: ")
                .append(Build.MANUFACTURER)
                .append("\n");

        // Model
        info.append("Model: ")
                .append(Build.MODEL)
                .append("\n");

        // Android version
        info.append("Android: ")
                .append(Build.VERSION.RELEASE)
                .append("\n");

        // SDK
        info.append("Android SDK: ")
                .append(Build.VERSION.SDK_INT)
                .append("\n");

        // Battery
        info.append("Battery: ")
                .append(getBatteryLevel())
                .append("%\n");

        info.append("Charging: ")
                .append(getChargingState())
                .append("\n");

        // Storage
        info.append("Storage available: ")
                .append(formatBytes(
                        getAvailableStorage()
                ))
                .append("\n");

        info.append("Storage total: ")
                .append(formatBytes(
                        getTotalStorage()
                ))
                .append("\n");

        // RAM
        info.append("RAM available: ")
                .append(formatBytes(
                        getAvailableRam()
                ))
                .append("\n");

        info.append("RAM total: ")
                .append(formatBytes(
                        getTotalRam()
                ))
                .append("\n");

        return info.toString();
    }

    // =========================================================
    // BATTERY
    // =========================================================

    public int getBatteryLevel() {

        IntentFilter filter =
                new IntentFilter(
                        Intent.ACTION_BATTERY_CHANGED
                );

        Intent battery =
                context.registerReceiver(
                        null,
                        filter
                );

        if (battery == null) {
            return -1;
        }

        int level =
                battery.getIntExtra(
                        BatteryManager.EXTRA_LEVEL,
                        -1
                );

        int scale =
                battery.getIntExtra(
                        BatteryManager.EXTRA_SCALE,
                        -1
                );

        if (level < 0 || scale <= 0) {
            return -1;
        }

        return Math.round(
                level * 100f / scale
        );
    }

    // =========================================================
    // CHARGING
    // =========================================================

    public String getChargingState() {

        IntentFilter filter =
                new IntentFilter(
                        Intent.ACTION_BATTERY_CHANGED
                );

        Intent battery =
                context.registerReceiver(
                        null,
                        filter
                );

        if (battery == null) {
            return "Unknown";
        }

        int status =
                battery.getIntExtra(
                        BatteryManager.EXTRA_STATUS,
                        -1
                );

        if (status ==
                BatteryManager.BATTERY_STATUS_CHARGING) {

            return "Charging";

        } else if (status ==
                BatteryManager.BATTERY_STATUS_FULL) {

            return "Full";

        } else {

            return "Not charging";
        }
    }

    // =========================================================
    // AVAILABLE STORAGE
    // =========================================================

    public long getAvailableStorage() {

        StatFs stat =
                new StatFs(
                        Environment
                                .getDataDirectory()
                                .getPath()
                );

        long blockSize =
                stat.getBlockSizeLong();

        long availableBlocks =
                stat.getAvailableBlocksLong();

        return blockSize *
                availableBlocks;
    }

    // =========================================================
    // TOTAL STORAGE
    // =========================================================

    public long getTotalStorage() {

        StatFs stat =
                new StatFs(
                        Environment
                                .getDataDirectory()
                                .getPath()
                );

        long blockSize =
                stat.getBlockSizeLong();

        long totalBlocks =
                stat.getBlockCountLong();

        return blockSize *
                totalBlocks;
    }

    // =========================================================
    // AVAILABLE RAM
    // =========================================================

    public long getAvailableRam() {

        ActivityManager manager =
                (ActivityManager)
                        context.getSystemService(
                                Context.ACTIVITY_SERVICE
                        );

        if (manager == null) {
            return -1;
        }

        ActivityManager.MemoryInfo memoryInfo =
                new ActivityManager.MemoryInfo();

        manager.getMemoryInfo(
                memoryInfo
        );

        return memoryInfo.availMem;
    }

    // =========================================================
    // TOTAL RAM
    // =========================================================

    public long getTotalRam() {

        ActivityManager manager =
                (ActivityManager)
                        context.getSystemService(
                                Context.ACTIVITY_SERVICE
                        );

        if (manager == null) {
            return -1;
        }

        ActivityManager.MemoryInfo memoryInfo =
                new ActivityManager.MemoryInfo();

        manager.getMemoryInfo(
                memoryInfo
        );

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.JELLY_BEAN) {

            return memoryInfo.totalMem;
        }

        return -1;
    }

    // =========================================================
    // FORMAT STORAGE / RAM
    // =========================================================

    private String formatBytes(long bytes) {

        if (bytes < 0) {
            return "Unknown";
        }

        double gb =
                bytes /
                (1024.0 * 1024.0 * 1024.0);

        if (gb >= 1.0) {

            return String.format(
                    Locale.US,
                    "%.2f GB",
                    gb
            );
        }

        double mb =
                bytes /
                (1024.0 * 1024.0);

        return String.format(
                Locale.US,
                "%.0f MB",
                mb
        );
    }
      }
