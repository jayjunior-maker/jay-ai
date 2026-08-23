package com.jay.ai;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class JayApiClient {

    /*
     * IMPORTANT:
     *
     * For development in GitHub Codespaces, use your
     * forwarded port 5000 URL here.
     *
     * Example:
     *
     * https://YOUR-CODESPACE-5000.app.github.dev
     *
     * Do NOT include /api/chat here.
     */
    private static final String SERVER_URL =
            "https://YOUR-CODESPACE-5000.app.github.dev";

    private static final int TIMEOUT = 15000;

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public interface Callback {

        void onSuccess(String response);

        void onError(String error);
    }

    public void chat(
            String message,
            Callback callback) {

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {

                URL url = new URL(
                        SERVER_URL + "/api/chat"
                );

                connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");

                connection.setConnectTimeout(
                        TIMEOUT
                );

                connection.setReadTimeout(
                        TIMEOUT
                );

                connection.setDoInput(true);
                connection.setDoOutput(true);

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );

                connection.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                JSONObject body =
                        new JSONObject();

                body.put(
                        "message",
                        message
                );

                JSONArray history =
                        new JSONArray();

                body.put(
                        "history",
                        history
                );

                byte[] data =
                        body.toString()
                                .getBytes(
                                        StandardCharsets.UTF_8
                                );

                OutputStream output =
                        connection.getOutputStream();

                output.write(data);
                output.flush();
                output.close();

                int responseCode =
                        connection.getResponseCode();

                InputStream stream;

                if (responseCode >= 200 &&
                        responseCode < 300) {

                    stream =
                            connection.getInputStream();

                } else {

                    stream =
                            connection.getErrorStream();
                }

                String responseBody =
                        readStream(stream);

                if (responseCode < 200 ||
                        responseCode >= 300) {

                    throw new Exception(
                            "Server returned HTTP " +
                            responseCode +
                            ": " +
                            responseBody
                    );
                }

                JSONObject json =
                        new JSONObject(
                                responseBody
                        );

                String reply =
                        json.optString(
                                "reply",
                                ""
                        );

                if (reply.trim().isEmpty()) {

                    throw new Exception(
                            "Server returned an empty reply."
                    );
                }

                mainHandler.post(() ->
                        callback.onSuccess(reply)
                );

            } catch (Exception e) {

                String error =
                        e.getMessage();

                if (error == null ||
                        error.trim().isEmpty()) {

                    error =
                            "Unable to contact Jay's server.";
                }

                String finalError =
                        error;

                mainHandler.post(() ->
                        callback.onError(finalError)
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }

    private String readStream(
            InputStream stream)
            throws Exception {

        if (stream == null) {
            return "";
        }

        StringBuilder result =
                new StringBuilder();

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                stream,
                                StandardCharsets.UTF_8
                        )
                );

        String line;

        while ((line = reader.readLine())
                != null) {

            result.append(line);
        }

        reader.close();

        return result.toString();
    }
                                            }
