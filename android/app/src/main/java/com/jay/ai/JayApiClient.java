package com.jay.ai;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JayApiClient {

    public interface Callback {
        void onSuccess(String reply);
        void onError(String error);
    }

    /*
     * IMPORTANT:
     *
     * localhost on the Android phone means the PHONE itself.
     * It does NOT mean the GitHub Codespace.
     *
     * We will replace this with the Codespace forwarded URL
     * before testing the Android connection.
     */
    private static final String BASE_URL =
            "http://10.0.2.2:5000";

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public void chat(
            String message,
            Callback callback) {

        executor.execute(() -> {

            HttpURLConnection connection = null;

            try {

                URL url = new URL(
                        BASE_URL + "/api/chat"
                );

                connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");

                connection.setConnectTimeout(10000);
                connection.setReadTimeout(30000);

                connection.setDoInput(true);
                connection.setDoOutput(true);

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );

                JSONObject request =
                        new JSONObject();

                request.put(
                        "message",
                        message
                );

                request.put(
                        "history",
                        new org.json.JSONArray()
                );

                byte[] data =
                        request.toString()
                                .getBytes(
                                        StandardCharsets.UTF_8
                                );

                try (OutputStream output =
                             connection.getOutputStream()) {

                    output.write(data);
                    output.flush();
                }

                int responseCode =
                        connection.getResponseCode();

                InputStream inputStream;

                if (responseCode >= 200 &&
                        responseCode < 300) {

                    inputStream =
                            connection.getInputStream();

                } else {

                    inputStream =
                            connection.getErrorStream();
                }

                StringBuilder response =
                        new StringBuilder();

                if (inputStream != null) {

                    try (BufferedReader reader =
                                 new BufferedReader(
                                         new InputStreamReader(
                                                 inputStream,
                                                 StandardCharsets.UTF_8
                                         )
                                 )) {

                        String line;

                        while ((line =
                                reader.readLine()) != null) {

                            response.append(line);
                        }
                    }
                }

                if (responseCode >= 200 &&
                        responseCode < 300) {

                    JSONObject json =
                            new JSONObject(
                                    response.toString()
                            );

                    String reply =
                            json.optString(
                                    "reply",
                                    ""
                            );

                    if (reply.isEmpty()) {
                        throw new Exception(
                                "Backend returned an empty reply."
                        );
                    }

                    mainHandler.post(() ->
                            callback.onSuccess(reply)
                    );

                } else {

                    mainHandler.post(() ->
                            callback.onError(
                                    "Backend error: HTTP "
                                            + responseCode
                            )
                    );
                }

            } catch (Exception e) {

                mainHandler.post(() ->
                        callback.onError(
                                "Could not connect to Jay backend: "
                                        + e.getMessage()
                        )
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    public void shutdown() {
        executor.shutdownNow();
    }
  }
