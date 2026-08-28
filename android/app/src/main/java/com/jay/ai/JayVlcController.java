package com.jay.ai;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

/** VLC integration helper. Opens VLC directly, or asks Android to play a local audio URI with VLC. */
public final class JayVlcController {
    private static final String VLC_PACKAGE = "org.videolan.vlc";
    private final Context context;

    public JayVlcController(Context context) {
        this.context = context.getApplicationContext();
    }

    public boolean isInstalled() {
        try {
            context.getPackageManager().getPackageInfo(VLC_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public String openVlc() {
        if (!isInstalled()) return "VLC is not installed on this phone, Sir.";
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(VLC_PACKAGE);
        if (intent == null) return "I couldn't open VLC, Sir.";
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        return "Opening VLC, Sir.";
    }

    public String playUri(Uri audioUri) {
        if (audioUri == null) return openVlc();
        if (!isInstalled()) return "VLC is not installed on this phone, Sir.";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(audioUri, "audio/*");
        intent.setPackage(VLC_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            context.startActivity(intent);
            return "Playing the song in VLC, Sir.";
        } catch (Exception e) {
            return "I couldn't start that song in VLC, Sir.";
        }
    }
}
