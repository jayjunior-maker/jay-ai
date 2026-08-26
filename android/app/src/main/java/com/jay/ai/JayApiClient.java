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

    public interface Callback {
        void onSuccess(String response);
        void onError(String error);
    }

    public void chat(String message, Callback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                if (message == null || message.trim().isEmpty()) {
                    callback.onError("Message is empty.");
                    return;
                }

                URL url = new URL(JayApiConfig.SERVER_URL + JayApiConfig.CHAT_PATH);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(30000);
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");

                JSONObject request = new JSONObject();
                request.put("message", message);
                byte[] data = request.toString().getBytes(StandardCharsets.UTF_8);
                connection.setFixedLengthStreamingMode(data.length);

                try (OutputStream output = connection.getOutputStream()) {
                    output.write(data);
                    output.flush();
                }

                int responseCode = connection.getResponseCode();
                InputStream stream = responseCode >= 200 && responseCode < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();
                String responseText = readStream(stream);

                if (responseCode >= 200 && responseCode < 300) {
                    JSONObject response = new JSONObject(responseText);
                    String reply = response.optString("reply", "");
                    if (reply.trim().isEmpty()) {
                        callback.onError("Jay's server returned an empty response.");
                        return;
                    }
                    callback.onSuccess(reply);
                } else {
                    callback.onError("Server error " + responseCode + ": " + responseText);
                }
            } catch (Exception e) {
                callback.onError("Unable to reach Jay's online brain: " + e.getMessage());
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    public void health(Callback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(JayApiConfig.SERVER_URL + JayApiConfig.HEALTH_PATH);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestProperty("Accept", "application/json");

                int responseCode = connection.getResponseCode();
                InputStream stream = responseCode >= 200 && responseCode < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();
                String responseText = readStream(stream);

                if (responseCode >= 200 && responseCode < 300) {
                    callback.onSuccess(responseText);
                } else {
                    callback.onError("Health check error " + responseCode + ": " + responseText);
                }
            } catch (Exception e) {
                callback.onError("Jay API unavailable: " + e.getMessage());
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private String readStream(InputStream stream) throws Exception {
        if (stream == null) return "";
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) result.append(line);
        }
        return result.toString();
    }
}
