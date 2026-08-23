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

public class JayApiClient {

    private static final String SERVER_URL =
            "https://symmetrical-guide-jr546v5qqgv9f5qpg-5000.app.github.dev";

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

        new Thread(new Runnable() {

            @Override
            public void run() {

                HttpURLConnection connection = null;

                try {

                    URL url = new URL(
                            SERVER_URL + "/api/chat"
                    );

                    connection =
                            (HttpURLConnection)
                                    url.openConnection();

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

                    body.put(
                            "history",
                            new JSONArray()
                    );

                    String json =
                            body.toString();

                    OutputStream output =
                            connection.getOutputStream();

                    output.write(
                            json.getBytes("UTF-8")
                    );

                    output.flush();
                    output.close();

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

                    String response =
                            readStream(inputStream);

                    if (responseCode < 200 ||
                            responseCode >= 300) {

                        throw new Exception(
                                "Server error HTTP " +
                                responseCode
                        );
                    }

                    JSONObject jsonResponse =
                            new JSONObject(response);

                    String reply =
                            jsonResponse.optString(
                                    "reply",
                                    ""
                            );

                    if (reply.trim().isEmpty()) {

                        throw new Exception(
                                "Jay server returned no reply."
                        );
                    }

                    mainHandler.post(
                            new Runnable() {

                                @Override
                                public void run() {

                                    callback.onSuccess(
                                            reply
                                    );
                                }
                            }
                    );

                } catch (Exception e) {

                    String error =
                            e.getMessage();

                    if (error == null ||
                            error.trim().isEmpty()) {

                        error =
                                "Could not connect to Jay's server.";
                    }

                    final String finalError =
                            error;

                    mainHandler.post(
                            new Runnable() {

                                @Override
                                public void run() {

                                    callback.onError(
                                            finalError
                                    );
                                }
                            }
                    );

                } finally {

                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }

        }).start();
    }

    private String readStream(
            InputStream inputStream)
            throws Exception {

        if (inputStream == null) {
            return "";
        }

        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                inputStream,
                                "UTF-8"
                        )
                );

        StringBuilder result =
                new StringBuilder();

        String line;

        while ((line = reader.readLine())
                != null) {

            result.append(line);
        }

        reader.close();

        return result.toString();
    }
                        }
