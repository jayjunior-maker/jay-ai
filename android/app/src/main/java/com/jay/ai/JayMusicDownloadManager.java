package com.jay.ai;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

/** Downloads only from a caller-supplied legitimate URL; no DRM/copyright bypass. */
public final class JayMusicDownloadManager {
    private final Context context;

    public JayMusicDownloadManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public long download(String url, String fileName) {
        if (url == null || !(url.startsWith("https://") || url.startsWith("http://"))) {
            throw new IllegalArgumentException("A valid download URL is required.");
        }
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager == null) throw new IllegalStateException("Download service unavailable.");
        String safeName = (fileName == null || fileName.trim().isEmpty()) ? "jay-music-download" : fileName.trim();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setTitle(safeName)
                .setDescription("Jay music download")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, safeName);
        return manager.enqueue(request);
    }
}
