package com.jay.ai;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class JayAppManager {

    private final Context context;
    private final PackageManager packageManager;

    public JayAppManager(Context context) {
        this.context = context.getApplicationContext();
        this.packageManager = this.context.getPackageManager();
    }

    /**
     * Opens an installed application by name.
     */
    public String openApp(String appName) {

        if (appName == null ||
                appName.trim().isEmpty()) {

            return "Please tell me which app you want me to open, Sir.";
        }

        String requestedName =
                appName.trim();

        ApplicationInfo app =
                findApplication(requestedName);

        if (app == null) {

            return "I couldn't find an installed app called "
                    + requestedName
                    + ", Sir.";
        }

        try {

            Intent launchIntent =
                    packageManager.getLaunchIntentForPackage(
                            app.packageName
                    );

            if (launchIntent == null) {

                return "I found "
                        + getApplicationLabel(app)
                        + ", but Android does not provide a launch action for it, Sir.";
            }

            launchIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            context.startActivity(launchIntent);

            return "OPENED_APP:"
                    + getApplicationLabel(app);

        } catch (Exception e) {

            return "I couldn't open "
                    + getApplicationLabel(app)
                    + ", Sir.";
        }
    }

    /**
     * Returns a list of launchable applications.
     */
    public List<String> getInstalledApps() {

        List<String> apps =
                new ArrayList<>();

        try {

            Intent launcherIntent =
                    new Intent(
                            Intent.ACTION_MAIN,
                            null
                    );

            launcherIntent.addCategory(
                    Intent.CATEGORY_LAUNCHER
            );

            List<ApplicationInfo> installedApps =
                    packageManager.queryIntentActivities(
                            launcherIntent,
                            0
                    )
                    .stream()
                    .map(resolveInfo ->
                            resolveInfo.activityInfo.applicationInfo)
                    .collect(
                            java.util.stream.Collectors.toList()
                    );

            for (ApplicationInfo app :
                    installedApps) {

                String label =
                        getApplicationLabel(app);

                if (!label.isEmpty() &&
                        !apps.contains(label)) {

                    apps.add(label);
                }
            }

            Collections.sort(
                    apps,
                    String.CASE_INSENSITIVE_ORDER
            );

        } catch (Exception ignored) {
        }

        return apps;
    }

    /**
     * Finds an application using its visible name,
     * package name, or a partial name.
     */
    private ApplicationInfo findApplication(
            String requestedName) {

        String target =
                requestedName
                        .toLowerCase(Locale.ROOT);

        try {

            List<ApplicationInfo> applications =
                    packageManager.getInstalledApplications(
                            PackageManager.GET_META_DATA
                    );

            // Exact application-name match.
            for (ApplicationInfo app :
                    applications) {

                String label =
                        getApplicationLabel(app);

                if (label.equalsIgnoreCase(
                        requestedName
                )) {

                    return app;
                }
            }

            // Exact package-name match.
            for (ApplicationInfo app :
                    applications) {

                if (app.packageName
                        .equalsIgnoreCase(
                                requestedName
                        )) {

                    return app;
                }
            }

            // Partial application-name/package match.
            for (ApplicationInfo app :
                    applications) {

                String label =
                        getApplicationLabel(app)
                                .toLowerCase(
                                        Locale.ROOT
                                );

                if (label.contains(target) ||
                        app.packageName
                                .toLowerCase(
                                        Locale.ROOT
                                )
                                .contains(target)) {

                    return app;
                }
            }

        } catch (Exception ignored) {
        }

        return null;
    }

    private String getApplicationLabel(
            ApplicationInfo app) {

        try {

            CharSequence label =
                    packageManager
                            .getApplicationLabel(app);

            if (label != null) {
                return label.toString();
            }

        } catch (Exception ignored) {
        }

        return app.packageName;
    }
}
