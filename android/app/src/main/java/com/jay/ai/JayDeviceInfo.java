package com.jay.ai;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.view.Display;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;
import java.util.TimeZone;

public class JayDeviceInfo {

    private final Context context;

    public JayDeviceInfo(Context context) {
        this.context = context.getApplicationContext();
    }

    // =========================================================
    // MAIN DEVICE REPORT
    // =========================================================

    public String getDetailedDeviceInfo() {

        StringBuilder report =
                new StringBuilder();

        report.append(
                "========== JAY DEVICE REPORT ==========\n\n"
        );

        appendDeviceIdentity(report);

        appendAndroidInfo(report);

        appendHardwareInfo(report);

        appendMemoryInfo(report);

        appendStorageInfo(report);

        appendBatteryInfo(report);

        appendDisplayInfo(report);

        appendNetworkInfo(report);

        appendSystemInfo(report);

        appendJayInfo(report);

        report.append(
                "========================================\n"
        );

        return report.toString();
    }

    // =========================================================
    // DEVICE IDENTITY
    // =========================================================

    private void appendDeviceIdentity(
            StringBuilder report) {

        report.append("📱 DEVICE\n");

        report.append("Manufacturer: ")
                .append(safe(Build.MANUFACTURER))
                .append("\n");

        report.append("Model: ")
                .append(safe(Build.MODEL))
                .append("\n");

        report.append("Device: ")
                .append(safe(Build.DEVICE))
                .append("\n");

        report.append("Product: ")
                .append(safe(Build.PRODUCT))
                .append("\n");

        report.append("Brand: ")
                .append(safe(Build.BRAND))
                .append("\n");

        report.append("Hardware: ")
                .append(safe(Build.HARDWARE))
                .append("\n");

        report.append("Board: ")
                .append(safe(Build.BOARD))
                .append("\n");

        report.append("Bootloader: ")
                .append(safe(Build.BOOTLOADER))
                .append("\n");

        report.append("Fingerprint: ")
                .append(safe(Build.FINGERPRINT))
                .append("\n\n");
    }

    // =========================================================
    // ANDROID INFORMATION
    // =========================================================

    private void appendAndroidInfo(
            StringBuilder report) {

        report.append("🤖 ANDROID\n");

        report.append("Android version: ")
                .append(
                        safe(
                                Build.VERSION.RELEASE
                        )
                )
                .append("\n");

        report.append("SDK level: ")
                .append(
                        Build.VERSION.SDK_INT
                )
                .append("\n");

        report.append("Codename: ")
                .append(
                        safe(
                                Build.VERSION.CODENAME
                        )
                )
                .append("\n");

        if (Build.VERSION.SDK_INT >= 23) {

            report.append("Security patch: ")
                    .append(
                            safe(
                                    Build.VERSION
                                            .SECURITY_PATCH
                            )
                    )
                    .append("\n");

            report.append("Base OS: ")
                    .append(
                            safe(
                                    Build.VERSION.BASE_OS
                            )
                    )
                    .append("\n");
        }

        report.append("Build ID: ")
                .append(
                        safe(Build.ID)
                )
                .append("\n");

        report.append("Build display: ")
                .append(
                        safe(Build.DISPLAY)
                )
                .append("\n\n");
    }

    // =========================================================
    // HARDWARE / CPU
    // =========================================================

    private void appendHardwareInfo(
            StringBuilder report) {

        report.append("⚡ HARDWARE\n");

        report.append("CPU ABI: ");

        if (Build.SUPPORTED_ABIS != null &&
                Build.SUPPORTED_ABIS.length > 0) {

            for (int i = 0;
                 i < Build.SUPPORTED_ABIS.length;
                 i++) {

                if (i > 0) {
                    report.append(", ");
                }

                report.append(
                        Build.SUPPORTED_ABIS[i]
                );
            }

        } else {

            report.append("Unknown");
        }

        report.append("\n");

        report.append("CPU cores: ")
                .append(
                        Runtime
                                .getRuntime()
                                .availableProcessors()
                )
                .append("\n");

        report.append("Supported 32-bit ABI: ");

        if (Build.SUPPORTED_32_BIT_ABIS != null &&
                Build.SUPPORTED_32_BIT_ABIS.length > 0) {

            report.append(
                    join(
                            Build.SUPPORTED_32_BIT_ABIS
                    )
            );

        } else {

            report.append("None reported");
        }

        report.append("\n");

        report.append("Supported 64-bit ABI: ");

        if (Build.SUPPORTED_64_BIT_ABIS != null &&
                Build.SUPPORTED_64_BIT_ABIS.length > 0) {

            report.append(
                    join(
                            Build.SUPPORTED_64_BIT_ABIS
                    )
            );

        } else {

            report.append("None reported");
        }

        report.append("\n\n");
    }

    // =========================================================
    // MEMORY
    // =========================================================

    private void appendMemoryInfo(
            StringBuilder report) {

        report.append("🧠 MEMORY\n");

        ActivityManager manager =
                (ActivityManager)
                        context.getSystemService(
                                Context.ACTIVITY_SERVICE
                        );

        if (manager != null) {

            ActivityManager.MemoryInfo memory =
                    new ActivityManager.MemoryInfo();

            manager.getMemoryInfo(memory);

            report.append("Total RAM: ")
                    .append(
                            formatBytes(
                                    memory.totalMem
                            )
                    )
                    .append("\n");

            report.append("Available RAM: ")
                    .append(
                            formatBytes(
                                    memory.availMem
                            )
                    )
                    .append("\n");

            report.append("Low memory: ")
                    .append(
                            memory.lowMemory
                                    ? "YES"
                                    : "NO"
                    )
                    .append("\n");

            report.append("Memory threshold: ")
                    .append(
                            formatBytes(
                                    memory.threshold
                            )
                    )
                    .append("\n");

        } else {

            report.append(
                    "Memory information unavailable.\n"
            );
        }

        report.append("\n");
                        }
        // =========================================================
    // STORAGE
    // =========================================================

    private void appendStorageInfo(
            StringBuilder report) {

        report.append("💾 STORAGE\n");

        try {

            StatFs statFs =
                    new StatFs(
                            Environment
                                    .getDataDirectory()
                                    .getPath()
                    );

            long totalBytes =
                    statFs.getTotalBytes();

            long freeBytes =
                    statFs.getAvailableBytes();

            long usedBytes =
                    totalBytes - freeBytes;

            report.append("Internal total: ")
                    .append(
                            formatBytes(totalBytes)
                    )
                    .append("\n");

            report.append("Internal used: ")
                    .append(
                            formatBytes(usedBytes)
                    )
                    .append("\n");

            report.append("Internal free: ")
                    .append(
                            formatBytes(freeBytes)
                    )
                    .append("\n");

            if (totalBytes > 0) {

                double percentage =
                        ((double) usedBytes /
                                (double) totalBytes)
                                * 100.0;

                report.append(
                        String.format(
                                Locale.US,
                                "Internal usage: %.1f%%\n",
                                percentage
                        )
                );
            }

        } catch (Exception e) {

            report.append(
                    "Storage information unavailable.\n"
            );
        }

        report.append("\n");
    }

    // =========================================================
    // BATTERY
    // =========================================================

    private void appendBatteryInfo(
            StringBuilder report) {

        report.append("🔋 BATTERY\n");

        try {

            IntentFilter filter =
                    new IntentFilter(
                            Intent.ACTION_BATTERY_CHANGED
                    );

            Intent battery =
                    context.registerReceiver(
                            null,
                            filter
                    );

            if (battery != null) {

                int level =
                        battery.getIntExtra(
                                BatteryManager
                                        .EXTRA_LEVEL,
                                -1
                        );

                int scale =
                        battery.getIntExtra(
                                BatteryManager
                                        .EXTRA_SCALE,
                                -1
                        );

                if (level >= 0 &&
                        scale > 0) {

                    int percentage =
                            (level * 100) / scale;

                    report.append(
                            "Battery level: "
                    )
                            .append(
                                    percentage
                            )
                            .append("%\n");
                }

                int status =
                        battery.getIntExtra(
                                BatteryManager
                                        .EXTRA_STATUS,
                                -1
                        );

                report.append(
                        "Charging status: "
                )
                        .append(
                                getChargingStatus(
                                        status
                                )
                        )
                        .append("\n");

                int plugged =
                        battery.getIntExtra(
                                BatteryManager
                                        .EXTRA_PLUGGED,
                                0
                        );

                report.append(
                        "Power source: "
                )
                        .append(
                                getPowerSource(
                                        plugged
                                )
                        )
                        .append("\n");

                if (Build.VERSION.SDK_INT >= 21) {

                    int temperature =
                            battery.getIntExtra(
                                    BatteryManager
                                            .EXTRA_TEMPERATURE,
                                    -1
                            );

                    if (temperature >= 0) {

                        report.append(
                                "Battery temperature: "
                        )
                                .append(
                                        temperature / 10.0
                                )
                                .append(" °C\n");
                    }
                }

                if (Build.VERSION.SDK_INT >= 21) {

                    int health =
                            battery.getIntExtra(
                                    BatteryManager
                                            .EXTRA_HEALTH,
                                    -1
                            );

                    report.append(
                            "Battery health: "
                    )
                            .append(
                                    getBatteryHealth(
                                            health
                                    )
                            )
                            .append("\n");
                }

            } else {

                report.append(
                        "Battery information unavailable.\n"
                );
            }

        } catch (Exception e) {

            report.append(
                    "Battery information unavailable.\n"
            );
        }

        report.append("\n");
    }

    // =========================================================
    // DISPLAY
    // =========================================================

    private void appendDisplayInfo(
            StringBuilder report) {

        report.append("🖥 DISPLAY\n");

        try {

            WindowManager windowManager =
                    (WindowManager)
                            context.getSystemService(
                                    Context.WINDOW_SERVICE
                            );

            if (windowManager != null) {

                Display display =
                        windowManager
                                .getDefaultDisplay();

                Point size =
                        new Point();

                display.getRealSize(size);

                report.append("Resolution: ")
                        .append(size.x)
                        .append(" x ")
                        .append(size.y)
                        .append("\n");

                report.append("Refresh rate: ")
                        .append(
                                String.format(
                                        Locale.US,
                                        "%.1f Hz",
                                        display.getRefreshRate()
                                )
                        )
                        .append("\n");

                report.append("Density: ")
                        .append(
                                context
                                        .getResources()
                                        .getDisplayMetrics()
                                        .density
                        )
                        .append("\n");

                report.append("Density DPI: ")
                        .append(
                                context
                                        .getResources()
                                        .getDisplayMetrics()
                                        .densityDpi
                        )
                        .append("\n");

                report.append("Scaled density: ")
                        .append(
                                context
                                        .getResources()
                                        .getDisplayMetrics()
                                        .scaledDensity
                        )
                        .append("\n");

            } else {

                report.append(
                        "Display information unavailable.\n"
                );
            }

        } catch (Exception e) {

            report.append(
                    "Display information unavailable.\n"
            );
        }

        report.append("\n");
    }

    // =========================================================
    // NETWORK
    // =========================================================

    private void appendNetworkInfo(
            StringBuilder report) {

        report.append("🌐 NETWORK\n");

        try {

            ConnectivityManager manager =
                    (ConnectivityManager)
                            context.getSystemService(
                                    Context.CONNECTIVITY_SERVICE
                            );

            if (manager != null) {

                NetworkInfo network =
                        manager.getActiveNetworkInfo();

                if (network != null &&
                        network.isConnected()) {

                    report.append(
                            "Internet/network: CONNECTED\n"
                    );

                    report.append(
                            "Network type: "
                    )
                            .append(
                                    network.getTypeName()
                            )
                            .append("\n");

                    report.append(
                            "Connection state: "
                    )
                            .append(
                                    network.getState()
                            )
                            .append("\n");

                } else {

                    report.append(
                            "Internet/network: NOT CONNECTED\n"
                    );
                }

            } else {

                report.append(
                        "Network information unavailable.\n"
                );
            }

        } catch (Exception e) {

            report.append(
                    "Network information unavailable.\n"
            );
        }

        report.append("\n");
    }

    // =========================================================
    // SYSTEM INFORMATION
    // =========================================================

    private void appendSystemInfo(
            StringBuilder report) {

        report.append("⚙ SYSTEM\n");

        report.append("Locale: ")
                .append(
                        Locale.getDefault()
                                .toString()
                )
                .append("\n");

        report.append("Language: ")
                .append(
                        Locale.getDefault()
                                .getDisplayLanguage()
                )
                .append("\n");

        report.append("Country: ")
                .append(
                        Locale.getDefault()
                                .getDisplayCountry()
                )
                .append("\n");

        report.append("Timezone: ")
                .append(
                        TimeZone
                                .getDefault()
                                .getID()
                )
                .append("\n");

        long uptime =
                SystemClock.elapsedRealtime();

        report.append("Device uptime: ")
                .append(
                        formatDuration(uptime)
                )
                .append("\n");

        report.append("Java version: ")
                .append(
                        System.getProperty(
                                "java.version"
                        )
                )
                .append("\n");

        report.append("\n");
        }
        // =========================================================
    // JAY APP INFORMATION
    // =========================================================

    private void appendJayInfo(
            StringBuilder report) {

        report.append("🤖 JAY\n");

        try {

            PackageManager packageManager =
                    context.getPackageManager();

            String packageName =
                    context.getPackageName();

            report.append("Package: ")
                    .append(packageName)
                    .append("\n");

            PackageInfo packageInfo =
                    packageManager.getPackageInfo(
                            packageName,
                            0
                    );

            report.append("Jay version: ")
                    .append(
                            packageInfo.versionName
                    )
                    .append("\n");

            report.append("Version code: ")
                    .append(
                            packageInfo.versionCode
                    )
                    .append("\n");

        } catch (Exception e) {

            report.append(
                    "Jay app information unavailable.\n"
            );
        }

        report.append("\n");
    }

    // =========================================================
    // CHARGING STATUS
    // =========================================================

    private String getChargingStatus(
            int status) {

        switch (status) {

            case BatteryManager
                    .BATTERY_STATUS_CHARGING:

                return "Charging";

            case BatteryManager
                    .BATTERY_STATUS_FULL:

                return "Full";

            case BatteryManager
                    .BATTERY_STATUS_DISCHARGING:

                return "Discharging";

            case BatteryManager
                    .BATTERY_STATUS_NOT_CHARGING:

                return "Not charging";

            default:

                return "Unknown";
        }
    }

    // =========================================================
    // POWER SOURCE
    // =========================================================

    private String getPowerSource(
            int plugged) {

        switch (plugged) {

            case BatteryManager
                    .BATTERY_PLUGGED_AC:

                return "AC charger";

            case BatteryManager
                    .BATTERY_PLUGGED_USB:

                return "USB";

            case BatteryManager
                    .BATTERY_PLUGGED_WIRELESS:

                return "Wireless charging";

            default:

                return "Battery / unknown";
        }
    }

    // =========================================================
    // BATTERY HEALTH
    // =========================================================

    private String getBatteryHealth(
            int health) {

        switch (health) {

            case BatteryManager
                    .BATTERY_HEALTH_GOOD:

                return "Good";

            case BatteryManager
                    .BATTERY_HEALTH_OVERHEAT:

                return "Overheating";

            case BatteryManager
                    .BATTERY_HEALTH_DEAD:

                return "Dead";

            case BatteryManager
                    .BATTERY_HEALTH_OVER_VOLTAGE:

                return "Over voltage";

            case BatteryManager
                    .BATTERY_HEALTH_UNSPECIFIED_FAILURE:

                return "Unspecified failure";

            case BatteryManager
                    .BATTERY_HEALTH_COLD:

                return "Cold";

            default:

                return "Unknown";
        }
    }

    // =========================================================
    // ABI LIST
    // =========================================================

    private String join(
            String[] values) {

        if (values == null ||
                values.length == 0) {

            return "None";
        }

        StringBuilder result =
                new StringBuilder();

        for (int i = 0;
             i < values.length;
             i++) {

            if (i > 0) {

                result.append(", ");
            }

            result.append(
                    safe(values[i])
            );
        }

        return result.toString();
    }

    // =========================================================
    // FORMAT BYTES
    // =========================================================

    private String formatBytes(
            long bytes) {

        if (bytes < 0) {

            return "Unknown";
        }

        if (bytes < 1024) {

            return bytes + " B";
        }

        double value =
                bytes / 1024.0;

        if (value < 1024) {

            return String.format(
                    Locale.US,
                    "%.2f KB",
                    value
            );
        }

        value =
                value / 1024.0;

        if (value < 1024) {

            return String.format(
                    Locale.US,
                    "%.2f MB",
                    value
            );
        }

        value =
                value / 1024.0;

        if (value < 1024) {

            return String.format(
                    Locale.US,
                    "%.2f GB",
                    value
            );
        }

        value =
                value / 1024.0;

        return String.format(
                Locale.US,
                "%.2f TB",
                value
        );
    }

    // =========================================================
    // FORMAT DURATION
    // =========================================================

    private String formatDuration(
            long milliseconds) {

        long totalSeconds =
                milliseconds / 1000;

        long days =
                totalSeconds / 86400;

        totalSeconds %= 86400;

        long hours =
                totalSeconds / 3600;

        totalSeconds %= 3600;

        long minutes =
                totalSeconds / 60;

        long seconds =
                totalSeconds % 60;

        if (days > 0) {

            return days + "d "
                    + hours + "h "
                    + minutes + "m "
                    + seconds + "s";
        }

        if (hours > 0) {

            return hours + "h "
                    + minutes + "m "
                    + seconds + "s";
        }

        if (minutes > 0) {

            return minutes + "m "
                    + seconds + "s";
        }

        return seconds + "s";
    }

    // =========================================================
    // SAFE STRING
    // =========================================================

    private String safe(
            String value) {

        if (value == null ||
                value.trim().isEmpty()) {

            return "Unknown";
        }

        return value;
    }
                }
