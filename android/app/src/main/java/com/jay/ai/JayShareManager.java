package com.jay.ai;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;

public class JayShareManager {

    private final Context context;

    public JayShareManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Checks whether a file can be shared.
     */
    public boolean canShareFile(String filePath) {

        if (filePath == null ||
                filePath.trim().isEmpty()) {
            return false;
        }

        File file = new File(filePath.trim());

        return file.exists()
                && file.isFile()
                && file.canRead();
    }

    /**
     * Creates a secure Android share intent.
     *
     * The FileProvider must be configured in the
     * AndroidManifest.xml and provider paths XML.
     */
    public Intent createShareIntent(
            String filePath,
            String contactName) {

        if (!canShareFile(filePath)) {
            return null;
        }

        try {

            File file =
                    new File(filePath.trim());

            Uri fileUri =
                    FileProvider.getUriForFile(
                            context,
                            context.getPackageName()
                                    + ".fileprovider",
                            file
                    );

            Intent intent =
                    new Intent(Intent.ACTION_SEND);

            intent.setType(
                    getMimeType(file)
            );

            intent.putExtra(
                    Intent.EXTRA_STREAM,
                    fileUri
            );

            if (contactName != null &&
                    !contactName.trim().isEmpty()) {

                intent.putExtra(
                        Intent.EXTRA_TITLE,
                        "Send to " + contactName.trim()
                );
            }

            intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            return intent;

        } catch (Exception e) {

            return null;
        }
    }

    /**
     * Opens Android's share chooser.
     *
     * This does not silently send the file.
     */
    public String openShareChooser(
            String filePath,
            String contactName) {

        Intent shareIntent =
                createShareIntent(
                        filePath,
                        contactName
                );

        if (shareIntent == null) {
            return "SHARE_PREPARATION_FAILED";
        }

        try {

            String title =
                    contactName != null &&
                    !contactName.trim().isEmpty()
                            ? "Share with "
                                + contactName.trim()
                            : "Share file";

            Intent chooser =
                    Intent.createChooser(
                            shareIntent,
                            title
                    );

            chooser.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            context.startActivity(chooser);

            return "SHARE_CHOOSER_OPENED";

        } catch (Exception e) {

            return "SHARE_CHOOSER_FAILED";
        }
    }

    /**
     * Returns a MIME type based on the file extension.
     */
    private String getMimeType(File file) {

        String name =
                file.getName()
                        .toLowerCase(
                                java.util.Locale.ROOT
                        );

        if (name.endsWith(".pdf")) {
            return "application/pdf";
        }

        if (name.endsWith(".txt")) {
            return "text/plain";
        }

        if (name.endsWith(".jpg") ||
                name.endsWith(".jpeg")) {
            return "image/jpeg";
        }

        if (name.endsWith(".png")) {
            return "image/png";
        }

        if (name.endsWith(".gif")) {
            return "image/gif";
        }

        if (name.endsWith(".webp")) {
            return "image/webp";
        }

        if (name.endsWith(".mp4")) {
            return "video/mp4";
        }

        if (name.endsWith(".mp3")) {
            return "audio/mpeg";
        }

        if (name.endsWith(".wav")) {
            return "audio/wav";
        }

        if (name.endsWith(".zip")) {
            return "application/zip";
        }

        if (name.endsWith(".json")) {
            return "application/json";
        }

        if (name.endsWith(".doc")) {
            return "application/msword";
        }

        if (name.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }

        if (name.endsWith(".xls")) {
            return "application/vnd.ms-excel";
        }

        if (name.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }

        if (name.endsWith(".ppt")) {
            return "application/vnd.ms-powerpoint";
        }

        if (name.endsWith(".pptx")) {
            return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        }

        return "*/*";
    }
}
