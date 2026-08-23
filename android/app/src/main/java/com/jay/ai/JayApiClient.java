package com.jay.ai;

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
     * =========================================================
     * JAY ONLINE BACKEND
     * =========================================================
     *
     * IMPORTANT:
     * Paste your CURRENT Codespace port-5000 URL here.
     *
     * Example:
     *
     * https://your-codespace-5000.app.github.dev
     *
     * Do NOT add:
     * /health
     * /api/chat
     *
     * The app adds /api/chat automatically.
     */
    private static final String SERVER_URL =
            "PASTE_YOUR_CURRENT_CODESPACE_URL_HERE";

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

                if (message == null ||
                        message.trim().isEmpty()) {

                    callback.onError(
                            "Message is empty."
                    );

                    return;
                }

                String endpoint =
                        SERVER_URL +
                        "/api/chat";

                URL url =
                        new URL(endpoint);

                connection =
                        (HttpURLConnection)
                                url.openConnection();

                connection.setRequestMethod("POST");

                connection.setConnectTimeout(15000);

                connection.setReadTimeout(30000);

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

                JSONObject request =
                        new JSONObject();

                request.put(
                        "message",
                        message
                );

                byte[] data =
                        request.toString()
                                .getBytes(
                                        StandardCharsets.UTF_8
                                );

                connection.setFixedLengthStreamingMode(
                        data.length
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

                String responseText =
                        readStream(stream);

                if (responseCode >= 200 &&
                        responseCode < 300) {

                    JSONObject response =
                            new JSONObject(
                                    responseText
                            );

                    String reply =
                            response.optString(
                                    "reply",
                                    ""
                            );

                    if (reply.trim().isEmpty()) {

                        callback.onError(
                                "Jay's server returned an empty response."
                        );

                        return;
                    }

                    callback.onSuccess(reply);

                } else {

                    callback.onError(
                            "Server error " +
                            responseCode +
                            ": " +
                            responseText
                    );
                }

            } catch (Exception e) {

                callback.onError(
                        "Unable to reach Jay's online brain: " +
                        e.getMessage()
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }

    private String readStream(
            InputStream stream) throws Exception {

        if (stream == null) {
            return "";
        }

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                stream,
                                StandardCharsets.UTF_8
                        )
                );

        StringBuilder result =
                new StringBuilder();

        String line;

        while ((line = reader.readLine()) != null) {

            result.append(line);
        }

        reader.close();

        return result.toString();
    }
                    }
