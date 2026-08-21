private void processMessage(String message) {

    if (message == null || message.trim().isEmpty()) {
        return;
    }

    message = message.trim();

    addConversation("You: " + message);

    String lower = message.toLowerCase(Locale.ROOT).trim();

    // =====================================================
    // BASIC CONVERSATION
    // =====================================================

    if (lower.contains("hello") ||
            lower.equals("hi") ||
            lower.contains("hey")) {

        String response =
                "Hello, Sir. Jay is online and ready.";

        addConversation("Jay: " + response);
        speak(response);
        return;
    }

    if (lower.contains("who are you")) {

        String response =
                "I am Jay, your personal AI assistant, Sir.";

        addConversation("Jay: " + response);
        speak(response);
        return;
    }

    if (lower.contains("good morning")) {

        String response =
                "Good morning, Sir. I hope you slept well.";

        addConversation("Jay: " + response);
        speak(response);
        return;
    }

    if (lower.contains("good night")) {

        String response =
                "Good night, Sir. Sleep well.";

        addConversation("Jay: " + response);
        speak(response);
        return;
    }

    if (lower.contains("how are you")) {

        String response =
                "I'm operating normally, Sir. Ready when you are.";

        addConversation("Jay: " + response);
        speak(response);
        return;
    }

    // =====================================================
    // APP COMMANDS
    // =====================================================

    if (lower.contains("open whatsapp")) {

        openApp(
                "com.whatsapp",
                "WhatsApp"
        );

        return;
    }

    if (lower.contains("open chrome") ||
            lower.contains("open browser")) {

        openApp(
                "com.android.chrome",
                "Chrome"
        );

        return;
    }

    if (lower.contains("open camera")) {

        Intent cameraIntent =
                new Intent(
                        android.provider.MediaStore.ACTION_IMAGE_CAPTURE
                );

        if (cameraIntent.resolveActivity(
                getPackageManager()) != null) {

            String response =
                    "Opening camera, Sir.";

            addConversation(
                    "Jay: " + response
            );

            speak(response);

            startActivity(cameraIntent);

        } else {

            String response =
                    "I couldn't find a camera app, Sir.";

            addConversation(
                    "Jay: " + response
            );

            speak(response);
        }

        return;
    }

    if (lower.contains("open settings")) {

        Intent settingsIntent =
                new Intent(
                        android.provider.Settings.ACTION_SETTINGS
                );

        String response =
                "Opening settings, Sir.";

        addConversation(
                "Jay: " + response
        );

        speak(response);

        startActivity(settingsIntent);

        return;
    }

    if (lower.contains("open phone") ||
            lower.contains("open dialer")) {

        Intent phoneIntent =
                new Intent(
                        Intent.ACTION_DIAL
                );

        String response =
                "Opening phone, Sir.";

        addConversation(
                "Jay: " + response
        );

        speak(response);

        startActivity(phoneIntent);

        return;
    }

    if (lower.contains("open messages") ||
            lower.contains("open sms")) {

        Intent smsIntent =
                new Intent(
                        Intent.ACTION_MAIN
                );

        smsIntent.addCategory(
                Intent.CATEGORY_APP_MESSAGING
        );

        if (smsIntent.resolveActivity(
                getPackageManager()) != null) {

            String response =
                    "Opening messages, Sir.";

            addConversation(
                    "Jay: " + response
            );

            speak(response);

            startActivity(smsIntent);

        } else {

            String response =
                    "I couldn't find your messaging app, Sir.";

            addConversation(
                    "Jay: " + response
            );

            speak(response);
        }

        return;
    }

    // =====================================================
    // UNKNOWN COMMAND
    // =====================================================

    String response =
            "I can help with that, Sir. My online AI brain will handle this request.";

    addConversation(
            "Jay: " + response
    );

    speak(response);
}
