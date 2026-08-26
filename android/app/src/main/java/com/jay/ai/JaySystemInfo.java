package com.jay.ai;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import org.json.JSONObject;

/** Separate system-information tools; each method returns only its requested category. */
public final class JaySystemInfo {
    private JaySystemInfo() { }

    public static String memory(Context context) {
        JSONObject out = new JSONObject();
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            if (am != null) {
                am.getMemoryInfo(info);
                out.put("totalBytes", info.totalMem);
                out.put("availableBytes", info.availMem);
                out.put("usedBytes", Math.max(0L, info.totalMem - info.availMem));
                out.put("lowMemory", info.lowMemory);
            }
        } catch (Exception e) { out.put("error", e.getMessage()); }
        return out.toString();
    }

    public static String cpu() {
        JSONObject out = new JSONObject();
        try {
            out.put("cores", Runtime.getRuntime().availableProcessors());
            out.put("architecture", Build.SUPPORTED_ABIS.length == 0 ? "unknown" : Build.SUPPORTED_ABIS[0]);
        } catch (Exception e) { out.put("error", e.getMessage()); }
        return out.toString();
    }

    public static String display(Context context) {
        JSONObject out = new JSONObject();
        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm == null ? null : wm.getDefaultDisplay();
            android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
            if (display != null) {
                display.getRealMetrics(metrics);
                out.put("widthPixels", metrics.widthPixels);
                out.put("heightPixels", metrics.heightPixels);
                out.put("density", metrics.density);
                out.put("refreshRateHz", display.getRefreshRate());
            }
        } catch (Exception e) { out.put("error", e.getMessage()); }
        return out.toString();
    }
}
