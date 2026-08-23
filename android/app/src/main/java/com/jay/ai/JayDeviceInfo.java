package com.jay.ai;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class JayDeviceInfo {

    private final Context context;

    public JayDeviceInfo(Context context) {
        this.context = context.getApplicationContext();
    }

    public String getDetailedDeviceInfo() {

        StringBuilder report = new StringBuilder();

        report.append("========== JAY DEVICE REPORT ==========\n\n");

        appendDeviceInfo(report);
        appendAndroidInfo(report);
        appendCpuInfo(report);
        appendMemoryInfo(report);
        appendStorageInfo(report);
        appendBatteryInfo(report);
        appendNetworkInfo(report);
        appendWifiInfo(report);
        appendDisplayInfo(report);
        appendSensorInfo(report);
        appendSystemInfo(report);
        appendJayInfo(report);

        report.append("========== END REPORT ==========\n");

        return report.toString();
    }

    private void appendDeviceInfo(StringBuilder report) {

        report.append("📱 DEVICE\n");

        report.append("Manufacturer: ")
                .append(safe(Build.MANUFACTURER))
                .append("\n");

        report.append("Brand: ")
                .append(safe(Build.BRAND))
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

        report.append("Hardware: ")
                .append(safe(Build.HARDWARE))
                .append("\n");

        report.append("Board: ")
                .append(safe(Build.BOARD))
                .append("\n");

        report.append("Fingerprint: ")
                .append(safe(Build.FINGERPRINT))
                .append("\n\n");
    }

    private void appendAndroidInfo(StringBuilder report) {

        report.append("🤖 ANDROID\n");

        report.append("Version: ")
                .append(safe(Build.VERSION.RELEASE))
                .append("\n");

        report.append("SDK: ")
                .append(Build.VERSION.SDK_INT)
                .append("\n");

        report.append("Build ID: ")
                .append(safe(Build.ID))
                .append("\n");

        if (Build.VERSION.SDK_INT >= 23) {

            report.append("Security patch: ")
                    .append(
                            safe(
                                    Build.VERSION.SECURITY_PATCH
                            )
                    )
                    .append("\n");
        }

        report.append("\n");
    }

    private void appendCpuInfo(StringBuilder report) {

        report.append("⚙️ CPU\n");

        report.append("Available processors: ")
                .append(
                        Runtime.getRuntime()
                                .availableProcessors()
                )
                .append("\n");

        if (Build.VERSION.SDK_INT >= 21) {

            report.append("Supported ABIs: ");

            String[] abis = Build.SUPPORTED_ABIS;

            for (int i = 0; i < abis.length; i++) {

                if (i > 0) {
                    report.append(", ");
                }

                report.append(abis[i]);
            }

            report.append("\n");

        } else {

            report.append("CPU ABI: ")
                    .append(safe(Build.CPU_ABI))
                    .append("\n");

            report.append("CPU ABI2: ")
                    .append(safe(Build.CPU_ABI2))
                    .append("\n");
        }

        report.append("\n");
    }

    private void appendMemoryInfo(StringBuilder report) {

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
                    .append(formatBytes(memory.totalMem))
                    .append("\n");

            report.append("Available RAM: ")
                    .append(formatBytes(memory.availMem))
                    .append("\n");

            report.append("Low memory: ")
                    .append(
                            memory.lowMemory
                                    ? "YES"
                                    : "NO"
                    )
                    .append("\n");

            report.append("Memory threshold: ")
                    .append(formatBytes(memory.threshold))
                    .append("\n");

        } else {

            report.append(
                    "Memory information unavailable.\n"
            );
        }

        report.append("\n");
    }

    private void appendStorageInfo(StringBuilder report) {

        report.append("💾 STORAGE\n");

        try {

            StatFs stat =
                    new StatFs(
                            Environment
                                    .getDataDirectory()
                                    .getPath()
                    );

            long blockSize =
                    stat.getBlockSizeLong();

            long total =
                    stat.getBlockCountLong()
                            * blockSize;

            long available =
                    stat.getAvailableBlocksLong()
                            * blockSize;

            long used =
                    total - available;

            report.append("Internal total: ")
                    .append(formatBytes(total))
                    .append("\n");

            report.append("Internal used: ")
                    .append(formatBytes(used))
                    .append("\n");

            report.append("Internal available: ")
                    .append(formatBytes(available))
                    .append("\n");

        } catch (Exception e) {

            report.append(
                    "Storage information unavailable.\n"
            );
        }

        report.append("\n");
    }

    private void appendBatteryInfo(StringBuilder report) {

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

            if (battery == null) {

                report.append(
                        "Battery information unavailable.\n\n"
                );

                return;
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

            if (level >= 0 && scale > 0) {

                int percentage =
                        Math.round(
                                level * 100f / scale
                        );

                report.append("Level: ")
                        .append(percentage)
                        .append("%\n");
            }

            int status =
                    battery.getIntExtra(
                            BatteryManager.EXTRA_STATUS,
                            -1
                    );

            report.append("Status: ")
                    .append(getBatteryStatus(status))
                    .append("\n");

            int plugged =
                    battery.getIntExtra(
                            BatteryManager.EXTRA_PLUGGED,
                            0
                    );

            report.append("Power source: ")
                    .append(getPowerSource(plugged))
                    .append("\n");

            int temperature =
                    battery.getIntExtra(
                            BatteryManager.EXTRA_TEMPERATURE,
                            -1
                    );

            if (temperature >= 0) {

                report.append("Temperature: ")
                        .append(
                                String.format(
                                        Locale.US,
                                        "%.1f °C",
                                        temperature / 10f
                                )
                        )
                        .append("\n");
            }

            int voltage =
                    battery.getIntExtra(
                            BatteryManager.EXTRA_VOLTAGE,
                            -1
                    );

            if (voltage >= 0) {

                report.append("Voltage: ")
                        .append(voltage)
                        .append(" mV\n");
            }

            String technology =
                    battery.getStringExtra(
                            BatteryManager.EXTRA_TECHNOLOGY
                    );

            report.append("Technology: ")
                    .append(safe(technology))
                    .append("\n");

        } catch (Exception e) {

            report.append(
                    "Battery information unavailable.\n"
            );
        }

        report.append("\n");
    }

    private String getBatteryStatus(int status) {

        switch (status) {

            case BatteryManager.BATTERY_STATUS_CHARGING:
                return "Charging";

            case BatteryManager.BATTERY_STATUS_FULL:
                return "Full";

            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                return "Discharging";

            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                return "Not charging";

            default:
                return "Unknown";
        }
    }

    private String getPowerSource(int plugged) {

        if ((plugged &
                BatteryManager.BATTERY_PLUGGED_USB) != 0) {

            return "USB";
        }

        if ((plugged &
                BatteryManager.BATTERY_PLUGGED_AC) != 0) {

            return "AC";
        }

        if (Build.VERSION.SDK_INT >= 17) {

            if ((plugged &
                    BatteryManager.BATTERY_PLUGGED_WIRELESS) != 0) {

                return "Wireless";
            }
        }
            // =========================================================
    // NETWORK
    // =========================================================

    private void appendNetworkInfo(StringBuilder report) {

        report.append("📡 NETWORK\n");

        if (Build.VERSION.SDK_INT < 23) {

            report.append(
                    "Detailed network information requires Android 6.0+.\n\n"
            );

            return;
        }

        try {

            ConnectivityManager manager =
                    (ConnectivityManager)
                            context.getSystemService(
                                    Context.CONNECTIVITY_SERVICE
                            );

            if (manager == null) {

                report.append(
                        "Network information unavailable.\n\n"
                );

                return;
            }

            Network network =
                    manager.getActiveNetwork();

            if (network == null) {

                report.append(
                        "Connected: NO\n\n"
                );

                return;
            }

            NetworkCapabilities capabilities =
                    manager.getNetworkCapabilities(
                            network
                    );

            if (capabilities == null) {

                report.append(
                        "Network capabilities unavailable.\n\n"
                );

                return;
            }

            report.append("Connected: YES\n");

            if (capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
            )) {

                report.append(
                        "Connection: Wi-Fi\n"
                );

            } else if (capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_CELLULAR
            )) {

                report.append(
                        "Connection: Mobile data\n"
                );

            } else if (capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_ETHERNET
            )) {

                report.append(
                        "Connection: Ethernet\n"
                );

            } else {

                report.append(
                        "Connection: Other\n"
                );
            }

            report.append("Internet capability: ")
                    .append(
                            capabilities.hasCapability(
                                    NetworkCapabilities
                                            .NET_CAPABILITY_INTERNET
                            )
                                    ? "YES"
                                    : "NO"
                    )
                    .append("\n");

            report.append("Validated internet: ")
                    .append(
                            capabilities.hasCapability(
                                    NetworkCapabilities
                                            .NET_CAPABILITY_VALIDATED
                            )
                                    ? "YES"
                                    : "NO"
                    )
                    .append("\n");

        } catch (Exception e) {

            report.append(
                    "Network information unavailable.\n"
            );
        }

        report.append("\n");
    }

    // =========================================================
    // WI-FI
    // =========================================================

    private void appendWifiInfo(StringBuilder report) {

        report.append("📶 WI-FI\n");

        try {

            WifiManager wifiManager =
                    (WifiManager)
                            context.getApplicationContext()
                                    .getSystemService(
                                            Context.WIFI_SERVICE
                                    );

            if (wifiManager == null) {

                report.append(
                        "Wi-Fi information unavailable.\n\n"
                );

                return;
            }

            report.append("Wi-Fi enabled: ")
                    .append(
                            wifiManager.isWifiEnabled()
                                    ? "YES"
                                    : "NO"
                    )
                    .append("\n");

            WifiInfo wifiInfo =
                    wifiManager.getConnectionInfo();

            if (wifiInfo != null) {

                String ssid =
                        wifiInfo.getSSID();

                if (ssid != null &&
                        !ssid.equals("<unknown ssid>")) {

                    report.append("SSID: ")
                            .append(ssid)
                            .append("\n");
                }

                int linkSpeed =
                        wifiInfo.getLinkSpeed();

                if (linkSpeed >= 0) {

                    report.append("Link speed: ")
                            .append(linkSpeed)
                            .append(" Mbps\n");
                }
            }

        } catch (Exception e) {

            report.append(
                    "Wi-Fi details unavailable.\n"
            );
        }

        report.append("\n");
    }

    // =========================================================
    // DISPLAY
    // =========================================================

    private void appendDisplayInfo(StringBuilder report) {

        report.append("🖥️ DISPLAY\n");

        try {

            WindowManager manager =
                    (WindowManager)
                            context.getSystemService(
                                    Context.WINDOW_SERVICE
                            );

            if (manager == null) {

                report.append(
                        "Display information unavailable.\n\n"
                );

                return;
            }

            Display display =
                    manager.getDefaultDisplay();

            DisplayMetrics metrics =
                    new DisplayMetrics();

            display.getMetrics(metrics);

            report.append("Resolution: ")
                    .append(metrics.widthPixels)
                    .append(" × ")
                    .append(metrics.heightPixels)
                    .append("\n");

            report.append("Density: ")
                    .append(metrics.density)
                    .append("\n");

            report.append("DPI: ")
                    .append(metrics.densityDpi)
                    .append("\n");

            report.append("Scaled density: ")
                    .append(metrics.scaledDensity)
                    .append("\n");

            if (Build.VERSION.SDK_INT >= 23) {

                report.append("Refresh rate: ")
                        .append(display.getRefreshRate())
                        .append(" Hz\n");
            }

        } catch (Exception e) {

            report.append(
                    "Display information unavailable.\n"
            );
        }

        report.append("\n");
    }

    // =========================================================
    // SENSORS
    // =========================================================

    private void appendSensorInfo(StringBuilder report) {

        report.append("🧭 SENSORS\n");

        try {

            SensorManager manager =
                    (SensorManager)
                            context.getSystemService(
                                    Context.SENSOR_SERVICE
                            );

            if (manager == null) {

                report.append(
                        "Sensor information unavailable.\n\n"
                );

                return;
            }

            List<Sensor> sensors =
                    manager.getSensorList(
                            Sensor.TYPE_ALL
                    );

            report.append("Sensor count: ")
                    .append(sensors.size())
                    .append("\n");

            for (Sensor sensor : sensors) {

                report.append("• ")
                        .append(
                                safe(
                                        sensor.getName()
                                )
                        )
                        .append("\n");

                report.append("  Type: ")
                        .append(sensor.getType())
                        .append("\n");

                report.append("  Vendor: ")
                        .append(
                                safe(
                                        sensor.getVendor()
                                )
                        )
                        .append("\n");

                report.append("  Power: ")
                        .append(sensor.getPower())
                        .append(" mA\n");

                report.append("\n");
            }

        } catch (Exception e) {

            report.append(
                    "Sensor information unavailable.\n"
            );
        }
                }
        // =========================================================
    // SYSTEM INFORMATION
    // =========================================================

    private void appendSystemInfo(StringBuilder report) {

        report.append("\n⚙️ SYSTEM\n");

        long uptime =
                SystemClock.elapsedRealtime();

        long totalSeconds =
                uptime / 1000;

        long days =
                totalSeconds / 86400;

        long hours =
                (totalSeconds % 86400) / 3600;

        long minutes =
                (totalSeconds % 3600) / 60;

        long seconds =
                totalSeconds % 60;

        report.append("Uptime: ")
                .append(days)
                .append("d ")
                .append(hours)
                .append("h ")
                .append(minutes)
                .append("m ")
                .append(seconds)
                .append("s\n");

        report.append("Locale: ")
                .append(
                        Locale.getDefault()
                                .toString()
                )
                .append("\n");

        report.append("Timezone: ")
                .append(
                        TimeZone.getDefault()
                                .getID()
                )
                .append("\n");

        report.append("Java version: ")
                .append(
                        safe(
                                System.getProperty(
                                        "java.version"
                                )
                        )
                )
                .append("\n\n");
    }

    // =========================================================
    // JAY APPLICATION INFORMATION
    // =========================================================

    private void appendJayInfo(StringBuilder report) {

        report.append("🤖 JAY APPLICATION\n");

        try {

            PackageManager manager =
                    context.getPackageManager();

            PackageInfo packageInfo =
                    manager.getPackageInfo(
                            context.getPackageName(),
                            0
                    );

            report.append("Package: ")
                    .append(
                            context.getPackageName()
                    )
                    .append("\n");

            report.append("Version: ")
                    .append(
                            safe(
                                    packageInfo.versionName
                            )
                    )
                    .append("\n");

            if (Build.VERSION.SDK_INT >= 28) {

                report.append("Version code: ")
                        .append(
                                packageInfo
                                        .getLongVersionCode()
                        )
                        .append("\n");

            } else {

                report.append("Version code: ")
                        .append(
                                packageInfo.versionCode
                        )
                        .append("\n");
            }

        } catch (Exception e) {

            report.append(
                    "Jay application information unavailable.\n"
            );
        }

        report.append("\n");
    }

    // =========================================================
    // QUICK BATTERY LEVEL
    // =========================================================

    public int getBatteryLevel() {

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

            if (level < 0 ||
                    scale <= 0) {

                return -1;
            }

            return Math.round(
                    level * 100f / scale
            );

        } catch (Exception e) {

            return -1;
        }
    }

    // =========================================================
    // QUICK DEVICE SUMMARY
    // =========================================================

    public String getDeviceSummary() {

        return
                safe(Build.MANUFACTURER) +
                " " +
                safe(Build.MODEL) +
                ", Android " +
                safe(Build.VERSION.RELEASE) +
                ", battery " +
                getBatteryLevel() +
                "%";
    }

    // =========================================================
    // SAFE STRING
    // =========================================================

    private String safe(String value) {

        if (value == null ||
                value.trim().isEmpty()) {

            return "Unknown";
        }

        return value;
    }

    // =========================================================
    // BYTE FORMATTER
    // =========================================================

    private String formatBytes(long bytes) {

        if (bytes < 0) {

            return "Unknown";
        }

        double gb =
                bytes /
                        (1024.0 *
                                1024.0 *
                                1024.0);

        if (gb >= 1.0) {

            return String.format(
                    Locale.US,
                    "%.2f GB",
                    gb
            );
        }

        double mb =
                bytes /
                        (1024.0 *
                                1024.0);

        return String.format(
                Locale.US,
                "%.0f MB",
                mb
        );
    }
            }
        return "Battery";
            }
