package com.jay.ai;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.view.KeyEvent;

/** VLC/media helper. Uses VLC when available and Android media controls for playback commands. */
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
        try {
            context.startActivity(intent);
            return "Opening VLC, Sir.";
        } catch (Exception e) {
            return "I couldn't open VLC, Sir.";
        }
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

    public String mediaCommand(JayMusicCommandMatcher.Action action) {
        if (action == null || action == JayMusicCommandMatcher.Action.NONE) return null;
        int key;
        switch (action) {
            case PLAY: key = KeyEvent.KEYCODE_MEDIA_PLAY; break;
            case PAUSE: key = KeyEvent.KEYCODE_MEDIA_PAUSE; break;
            case RESUME: key = KeyEvent.KEYCODE_MEDIA_PLAY; break;
            case STOP: key = KeyEvent.KEYCODE_MEDIA_STOP; break;
            case NEXT: key = KeyEvent.KEYCODE_MEDIA_NEXT; break;
            case PREVIOUS: key = KeyEvent.KEYCODE_MEDIA_PREVIOUS; break;
            default: return null;
        }
        try {
            AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audio == null) return "I couldn't access the phone's media controls, Sir.";
            long now = android.os.SystemClock.uptimeMillis();
            audio.dispatchMediaKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, key, 0));
            audio.dispatchMediaKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, key, 0));
            switch (action) {
                case STOP: return "Music stopped, Sir.";
                case PAUSE: return "Music paused, Sir.";
                case RESUME: return "Music resumed, Sir.";
                case NEXT: return "Playing the next song, Sir.";
                case PREVIOUS: return "Playing the previous song, Sir.";
                default: return "Music playback started, Sir.";
            }
        } catch (Exception e) {
            return "I couldn't control the current music player, Sir.";
        }
    }
}
