package com.jay.ai;

/** Central configuration for Jay's online backend. */
public final class JayApiConfig {

    private JayApiConfig() {
    }

    /**
     * Development backend. Keep this separate from the client so the
     * endpoint can later be replaced by Jay's permanent HTTPS API.
     */
    public static final String SERVER_URL =
            "https://musical-adventure-g5rrjww47r2ppx9-5000.app.github.dev";

    public static final String HEALTH_ENDPOINT = SERVER_URL + "/health";
    public static final String CHAT_ENDPOINT = SERVER_URL + "/api/chat";
}
