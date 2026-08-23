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

public class JayDeviceInfo {

    private final Context context;

    public JayDeviceInfo(Context context) {

        this.context =
                context.getApplicationContext();
    }

    // =========================================================
    // FULL DEVICE REPORT
    // =========================================================

    public String getDetailedDeviceInfo() {

        StringBuilder info =
                new StringBuilder();

        info.append(
                "========== JAY DEVICE REPORT ==========\n\n"
        );

        info.append(
                getDeviceSection()
        );

        info.append(
                getAndroidSection()
        );

        info.append(
                getCpuSection()
        );

        info.append(
                getMemorySection()
        );

        info.append(
                getStorageSection()
        );

        info.append(
                getBatterySection()
        );

        info.append(
                getNetworkSection()
        );

        info.append(
                getWifiSection()
        );

        info.append(
                getDisplaySection()
        );

        info.append(
                getSensorSection()
        );

        info.append(
                getSystemSection()
        );

        info.append(
                getJaySection()
        );

        info.append(
                "\n========== END REPORT ==========\n"
        );

        return info.toString();
    }

    // =========================================================
    // DEVICE
    // =========================================================

    private String getDeviceSection() {

        StringBuilder info =
                new StringBuilder();

        info.append(
                "📱 DEVICE\n"
        );

        info.append(
                "Manufacturer: "
        ).append(
                safe(Build.MANUFACTURER)
        ).append("\n");

        info.append(
                "Model: "
        ).append(
                safe(Build.MODEL)
        ).append("\n");

        info.append(
                "Device: "
        ).append(
                safe(Build.DEVICE)
        ).append("\n");

        info.append(
                "Product: "
        ).append(
                safe(Build.PRODUCT)
        ).append("\n");

        info.append(
                "Hardware: "
        ).append(
                safe(Build.HARDWARE)
        ).append("\n");

        info.append(
                "Board: "
        ).append(
                safe(Build.BOARD)
        ).append("\n");

        info.append(
                "Brand: "
        ).append(
                safe(Build.BRAND)
        ).append("\n");

        info.append(
                "Fingerprint: "
        ).append(
                safe(Build.FINGERPRINT)
        ).append("\n\n");

        return info.toString();
    }

    // =========================================================
    // ANDROID
    // =========================================================

    private String getAndroidSection() {

        StringBuilder info =
                new StringBuilder();

        info.append(
                "🤖 ANDROID\n"
        );

        info.append(
                "Version: "
        ).append(
                safe(Build.VERSION.RELEASE)
        ).append("\n");

        info.append(
                "SDK: "
        ).append(
                Build.VERSION.SDK_INT
        ).append("\n");

        info.append(
                "Build ID: "
        ).append(
                safe(Build.ID)
        ).append("\n");

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.M) {

            info.append(
                    "Security Patch: "
            ).append(
                    safe(
                            Build.VERSION
                                    .SECURITY_PATCH
                    )
            ).append("\n");
        }

        info.append("\n");

        return info.toString();
    }

    // =========================================================
    // CPU
    // =========================================================

    private String getCpuSection() {

        StringBuilder info =
                new StringBuilder();

        info.append(
                "⚙️ CPU\n"
        );

        info.append(
                "Available processors: "
        ).append(
                Runtime
                        .getRuntime()
                        .availableProcessors()
        ).append("\n");

        info.append(
                "Primary ABI: "
        ).append(
                Build.CPU_ABI
        ).append("\n");

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.LOLLIPOP) {

            info.append(
                    "Supported ABIs: "
            );

            for (String abi :
                    Build.SUPPORTED_ABIS) {

                info.append(
                        abi
                ).append(" ");
            }

            info.append("\n");
        }

        info.append("\n");

        return info.toString();
    }

    // =========================================================
    // MEMORY
    // =========================================================

    private String getMemorySection() {

        StringBuilder info =
                new StringBuilder();

        ActivityManager manager =
                (ActivityManager)
                        context.getSystemService(
                                Context.ACTIVITY_SERVICE
                        );

        info.append(
                "🧠 MEMORY\n"
        );

        if (manager != null) {

            ActivityManager.MemoryInfo memory =
                    new ActivityManager.MemoryInfo();

            manager.getMemoryInfo(
                    memory
            );

            info.append(
                    "Total RAM: "
            ).append(
                    formatBytes(
                            memory.totalMem
                    )
            ).append("\n");

            info.append(
                    "Available RAM: "
            ).append(
                    formatBytes(
                            memory.availMem
                    )
            ).append("\n");

            info.append(
                    "Low memory state: "
            ).append(
                    memory.lowMemory
                            ? "YES"
                            : "NO"
            ).append("\n");

            info.append(
                    "Memory threshold: "
            ).append(
                    formatBytes(
                            memory.threshold
                    )
            ).append("\n");
        }

        info.append("\n");

        return info.toString();
    }

    // =========================================================
    // STORAGE
    // =========================================================

    private String getStorageSection() {

        StringBuilder info =
                new StringBuilder();

        info.append(
                "💾 STORAGE\n"
        );

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

            info.append(
                    "Internal total: "
            ).append(
                    formatBytes(total)
            ).append("\n");

            info.append(
                    "Internal used: "
            ).append(
                    formatBytes(used)
            ).append("\n");

            info.append(
                    "Internal available: "
            ).append(
                    formatBytes(available)
            ).append("\n");

        } catch (Exception e) {

            info.append(
                    "Storage information unavailable.\n"
            );
        }

        info.append("\n");

        return info.toString();
    }

    private String formatBytes(
            long bytes) {

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

    private String safe(
            String value) {

        if (value == null ||
                value.trim().isEmpty()) {

            return "Unknown";
        }

        return value;
                // =========================================================
    // BATTERY
    // =========================================================

    private String getBatterySection() {

        StringBuilder info =
                new StringBuilder();

        info.append(
                "🔋 BATTERY\n"
        );

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

                info.append(
                        "Battery information unavailable.\n\n"
                );

                return info.toString();
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

            if (level >= 0 &&
                    scale > 0) {

                int percentage =
                        Math.round(
                                level * 100f / scale
                        );

                info.append(
                        "Level: "
                ).append(
                        percentage
                ).append("%\n");
            }

            int status =
                    battery.getIntExtra(
                            BatteryManager.EXTRA_STATUS,
                            -1
                    );

            String statusText;

            if (status ==
                    BatteryManager
                            .BATTERY_STATUS_CHARGING) {

                statusText =
                        "Charging";

            } else if (status ==
                    BatteryManager
                            .BATTERY_STATUS_FULL) {

                statusText =
                        "Full";

            } else if (status ==
                    BatteryManager
                            .BATTERY_STATUS_DISCHARGING) {

                statusText =
                        "Discharging";

            } else {

                statusText =
                        "Not charging";
            }

            info.append(
                    "Status: "
            ).append(
                    statusText
            ).append("\n");

            int plugged =
                    battery.getIntExtra(
                            BatteryManager.EXTRA_PLUGGED,
                            0
                    );

            String powerSource;

            if ((plugged &
                    BatteryManager
                            .BATTERY_PLUGGED_USB) != 0) {

                powerSource = "USB";

            } else if ((plugged &
                    BatteryManager
                            .BATTERY_PLUGGED_AC) != 0) {

                powerSource = "AC";

            } else if (Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.JELLY_BEAN_MR1 &&
                    (plugged &
                            BatteryManager
                                    .BATTERY_PLUGGED_WIRELESS) != 0) {

                powerSource = "Wireless";

            } else {

                powerSource = "Battery";
            }

            info.append(
                    "Power source: "
            ).append(
                    powerSource
            ).append("\n");

            int temperature =
                    battery.getIntExtra(
                            BatteryManager.EXTRA_TEMPERATURE,
                            -1
                    );

            if (temperature >= 0) {

                float celsius =
                        temperature / 10f;

                info.append(
                        "Temperature: "
                ).append(
                        String.format(
                                Locale.US,
                                "%.1f °C",
                                celsius
                        )
                ).append("\n");
            }

            int voltage =
                    battery.getIntExtra(
                            BatteryManager.EXTRA_VOLTAGE,
                            -1
                    );

            if (voltage >= 0) {

                info.append(
                        "Voltage: "
                ).append(
                        voltage
                ).append(" mV\n");
            }

            String technology =
                    battery.getStringExtra(
                            BatteryManager.EXTRA_TECHNOLOGY
                    );

            info.append(
                    "Technology: "
            ).append(
                    safe(technology)
            ).append("\n");

        } catch (Exception e) {

            info.append(
                    "Battery information unavailable.\n"
            );
        }

        info.append("\n");

        return info.toString();
    }

    // =========================================================
    // NETWORK
    // =========================================================

    private String getNetworkSection() {

        StringBuilder info =
                new StringBuilder();

        info.append(
                "📡 NETWORK\n"
        );

        try {

            ConnectivityManager manager =
                    (ConnectivityManager)
                            context.getSystemService(
                                    Context.CONNECTIVITY_SERVICE
                            );

            if (manager == null) {

                info.append(
                        "Network information unavailable.\n\n"
                );

                return info.toString();
            }

            Network network =
                    manager.getActiveNetwork();

            if (network == null) {

                info.append(
                        "Connected: NO\n"
                );

                info.append("\n");

                return info.toString();
            }

            NetworkCapabilities capabilities =
                    manager.getNetworkCapabilities(
                            network
                    );

            if (capabilities == null) {

                info.append(
                        "Connected: UNKNOWN\n\n"
                );

                return info.toString();
            }

            info.append(
                    "Connected: YES\n"
            );

            if (capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
            )) {

                info.append(
                        "Connection: Wi-Fi\n"
                );

            } else if (capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_CELLULAR
            )) {

                info.append(
                        "Connection: Mobile data\n"
                );

            } else if (capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_ETHERNET
            )) {

                info.append(
                        "Connection: Ethernet\n"
                );

            } else {

                info.append(
                        "Connection: Other\n"
                );
            }

            info.append(
                    "Internet validated: "
            ).append(
                    capabilities.hasCapability(
                            NetworkCapabilities
                                    .NET_CAPABILITY_VALIDATED
                    )
                            ? "YES"
                            : "NO"
            ).append("\n");

            info.append(
                    "Internet capability: "
            ).append(
                    capabilities.hasCapability(
                            NetworkCapabilities
                                    .NET_CAPABILITY_INTERNET
                    )
                            ? "YES"
                            : "NO"
            ).append("\n");

        } catch (Exception e) {

            info.append(
                    "Network information unavailable.\n"
            );
        }

        info.append("\n");

        return info.toString();
    }

    // =========================================================
    // WI-FI
    // =========================================================

    private String getWifiSection() {

        StringBuilder info =
                new StringBuilder();

        info.append(
                "📶 WI-FI\n"
        );

        try {

            WifiManager wifiManager =
                    (WifiManager)
                            context.getApplicationContext()
                                    .getSystemService(
                                            Context.WIFI_SERVICE
                                    );

            if (wifiManager == null) {

                info.append(
                        "Wi-Fi information unavailable.\n\n"
                );

                return info.toString();
            }

            info.append(
                    "Wi-Fi enabled: "
            ).append(
                    wifiManager.isWifiEnabled()
                            ? "YES"
                            : "NO"
            ).append("\n");

            WifiInfo wifiInfo =
                    wifiManager.getConnectionInfo();

            if (wifiInfo != null) {

                String ssid =
                        wifiInfo.getSSID();

                if (ssid != null &&
                        !ssid.equals("<unknown ssid>")) {

                    info.append(
                            "SSID: "
                    ).append(
                            ssid
                    ).append("\n");
                }

                int linkSpeed =
                        wifiInfo.getLinkSpeed();

                if (linkSpeed >= 0) {

                    info.append(
                            "Link speed: "
                    ).append(
                            linkSpeed
                    ).append(" Mbps\n");
                }
            }

        } catch (Exception e) {

            info.append(
                    "Wi-Fi details require Android permission or are unavailable.\n"
            );
        }

        info.append("\n");

        return info.toString();
    }

    // =========================================================
    // DISPLAY
    // =========================================================

    private String getDisplaySection() {

        StringBuilder info =
                new StringBuilder();

        info.append(
                "🖥️ DISPLAY\n"
        );

        try {

            WindowManager windowManager =
                    (WindowManager)
                            context.getSystemService(
                                    Context.WINDOW_SERVICE
                            );

            if (windowManager == null) {

                info.append(
                        "Display information unavailable.\n\n"
                );

                return info.toString();
            }

            Display display =
                    windowManager.getDefaultDisplay();

            DisplayMetrics metrics =
                    new DisplayMetrics();

            display.getMetrics(
                    metrics
            );

            info.append(
                    "Resolution: "
            ).append(
                    metrics.widthPixels
            ).append(
                    " × "
            ).append(
                    metrics.heightPixels
            ).append("\n");

            info.append(
                    "Density: "
            ).append(
                    metrics.density
            ).append("\n");

            info.append(
                    "DPI: "
            ).append(
                    metrics.densityDpi
            ).append("\n");

            info.append(
                    "Scaled density: "
            ).append(
                    metrics.scaledDensity
            ).append("\n");

            if (Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.M) {

                info.append(
                        "Refresh rate: "
                ).append(
                        display.getRefreshRate()
                ).append(" Hz\n");
            }

        } catch (Exception e) {

            info.append(
                    "Display information unavailable.\n"
            );
        }

        info.append("\n");

        return info.toString();
    }

    // =========================================================
    // SENSORS
    // =========================================================

    private String getSensorSection() {

        StringBuilder info =
                new StringBuilder();

        info.append(
                "🧭 SENSORS\n"
        );

        try {

            SensorManager sensorManager =
                    (SensorManager)
                            context.getSystemService(
                                    Context.SENSOR_SERVICE
                            );

            if (sensorManager == null) {

                info.append(
                        "Sensor information unavailable.\n\n"
                );

                return info.toString();
            }

            List<Sensor> sensors =
                    sensorManager.getSensorList(
                            Sensor.TYPE_ALL
                    );

            info.append(
                    "Sensor count: "
            ).append(
                    sensors.size()
            ).append("\n\n");

            for (Sensor sensor : sensors) {

                info.append(
                        "• "
                ).append(
                        sensor.getName()
                ).append("\n");

                info.append(
                        "  Type: "
                ).append(
                        sensor.getType()
                ).append("\n");

                if (Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.GINGERBREAD) {

                    info.append(
                            "  Vendor: "
                    ).append(
                            safe(
                                    sensor.getVendor()
                            )
                    ).append("\n");
                }

                info.append("\n");
            }

        } catch (Exception e) {

            info.append(
                    "Sensor information unavailable.\n"
            );
        }

        return info.toString();
        }
            // =========================================================
    // SYSTEM
    // =========================================================

    private String getSystemSection() {

        StringBuilder info =
                new StringBuilder();

        info.append(
                "⚙️ SYSTEM\n"
        );

        try {

            long uptime =
                    SystemClock.elapsedRealtime();

            long seconds =
                    uptime / 1000;

            long minutes =
                    seconds / 60;

            long hours =
                    minutes / 60;

            long days =
                    hours / 24;

            hours %= 24;
            minutes %= 60;
            seconds %= 60;

            info.append(
                    "Device uptime: "
            ).append(
                    days
            ).append("d ")
            .append(
                    hours
            ).append("h ")
            .append(
                    minutes
            ).append("m ")
            .append(
                    seconds
            ).append("s\n");

            info.append(
                    "Locale: "
            ).append(
                    Locale.getDefault()
                            .toString()
            ).append("\n");

            info.append(
                    "Time zone: "
            ).append(
                    java.util.TimeZone
                            .getDefault()
                            .getID()
            ).append("\n");

            info.append(
                    "Java runtime: "
            ).append(
                    System.getProperty(
                            "java.version"
                    )
            ).append("\n");

        } catch (Exception e) {

            info.append(
                    "System information unavailable.\n"
            );
        }

        info.append("\n");

        return info.toString();
    }

    // =========================================================
    // JAY APPLICATION
    // =========================================================

    private String getJaySection() {

        StringBuilder info =
                new StringBuilder();

        info.append(
                "🤖 JAY APPLICATION\n"
        );

        try {

            PackageManager packageManager =
                    context.getPackageManager();

            PackageInfo packageInfo =
                    packageManager.getPackageInfo(
                            context.getPackageName(),
                            0
                    );

            info.append(
                    "Package: "
            ).append(
                    context.getPackageName()
            ).append("\n");

            info.append(
                    "Version: "
            ).append(
                    packageInfo.versionName
            ).append("\n");

            if (Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.P) {

                info.append(
                        "Version code: "
                ).append(
                        packageInfo
                                .getLongVersionCode()
                ).append("\n");

            } else {

                info.append(
                        "Version code: "
                ).append(
                        packageInfo.versionCode
                ).append("\n");
            }

        } catch (Exception e) {

            info.append(
                    "Jay application information unavailable.\n"
            );
        }

        info.append("\n");

        return info.toString();
    }

    // =========================================================
    // QUICK BATTERY VALUE
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
                Build.MANUFACTURER +
                " " +
                Build.MODEL +
                ", Android " +
                Build.VERSION.RELEASE +
                ", battery " +
                getBatteryLevel() +
                "%";
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

    // =========================================================
    // BYTE FORMATTER
    // =========================================================

    private String formatBytes(
            long bytes) {

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
