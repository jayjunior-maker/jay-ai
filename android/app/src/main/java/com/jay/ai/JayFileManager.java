package com.jay.ai;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class JayFileManager {

    private final Context context;

    public JayFileManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Checks whether a file exists and is readable.
     */
    public boolean isValidFile(String filePath) {

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
     * Returns basic information about a file.
     */
    public String getFileInfo(String filePath) {

        if (!isValidFile(filePath)) {
            return "FILE_NOT_FOUND";
        }

        try {

            File file =
                    new File(filePath.trim());

            String name =
                    file.getName();

            long size =
                    file.length();

            return "FILE_FOUND|"
                    + name
                    + "|"
                    + formatFileSize(size);

        } catch (Exception e) {

            return "FILE_ERROR";
        }
    }

    /**
     * Finds files inside a directory.
     */
    public List<String> listFiles(String directoryPath) {

        List<String> results =
                new ArrayList<>();

        if (directoryPath == null ||
                directoryPath.trim().isEmpty()) {
            return results;
        }

        try {

            File directory =
                    new File(directoryPath.trim());

            if (!directory.exists() ||
                    !directory.isDirectory()) {
                return results;
            }

            File[] files =
                    directory.listFiles();

            if (files == null) {
                return results;
            }

            Arrays.sort(
                    files,
                    Comparator.comparing(
                            File::getName,
                            String.CASE_INSENSITIVE_ORDER
                    )
            );

            for (File file : files) {

                if (file.isFile() &&
                        file.canRead()) {

                    results.add(
                            file.getAbsolutePath()
                    );
                }
            }

        } catch (Exception ignored) {
        }

        return results;
    }

    /**
     * Creates a secure Android share intent for a file.
     *
     * The application must have a FileProvider configured
     * in AndroidManifest.xml and a matching paths XML file.
     */
    public Intent createShareIntent(String filePath) {

        if (!isValidFile(filePath)) {
            return null;
        }

        try {

            File file =
                    new File(filePath.trim());

            Uri contentUri =
                    FileProvider.getUriForFile(
                            context,
                            context.getPackageName()
                                    + ".fileprovider",
                            file
                    );

            Intent shareIntent =
                    new Intent(
                            Intent.ACTION_SEND
                    );

            shareIntent.setType(
                    getMimeType(file)
            );

            shareIntent.putExtra(
                    Intent.EXTRA_STREAM,
                    contentUri
            );

            shareIntent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            return shareIntent;

        } catch (Exception e) {

            return null;
        }
    }

    /**
     * Opens the Android share chooser for a file.
     *
     * This prepares sharing but does not silently send the file.
     */
    public String shareFile(String filePath) {

        Intent shareIntent =
                createShareIntent(filePath);

        if (shareIntent == null) {
            return "FILE_SHARE_FAILED";
        }

        try {

            Intent chooser =
                    Intent.createChooser(
                            shareIntent,
                            "Share file with..."
                    );

            chooser.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            context.startActivity(chooser);

            return "FILE_SHARE_OPENED";

        } catch (Exception e) {

            return "FILE_SHARE_FAILED";
        }
    }

    /**
     * Returns a best-effort MIME type.
     */
    private String getMimeType(File file) {

        String name =
                file.getName()
                        .toLowerCase();

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

        if (name.endsWith(".pdf")) {
            return "application/pdf";
        }

        if (name.endsWith(".txt")) {
            return "text/plain";
        }

        if (name.endsWith(".zip")) {
            return "application/zip";
        }

        if (name.endsWith(".json")) {
            return "application/json";
        }

        return "*/*";
    }

    /**
     * Formats a file size for Jay's responses.
     */
    private String formatFileSize(long bytes) {

        if (bytes < 1024) {
            return bytes + " B";
        }

        double kb =
                bytes / 1024.0;

        if (kb < 1024) {
            return String.format(
                    java.util.Locale.US,
                    "%.1f KB",
                    kb
            );
        }

        double mb =
                kb / 1024.0;

        if (mb < 1024) {
            return String.format(
                    java.util.Locale.US,
                    "%.1f MB",
                    mb
            );
        }

        double gb =
                mb / 1024.0;

        return String.format(
                java.util.Locale.US,
                "%.2f GB",
                gb
        );
    }
}
