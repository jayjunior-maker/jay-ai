package com.jay.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity
        implements TextToSpeech.OnInitListener {

    private TextToSpeech jayVoice;
    private SpeechRecognizer speechRecognizer;

    private TextView jayStatus;
    private TextView conversation;
    private EditText inputBox;

    private boolean voiceReady = false;

    // JAY VOICE PROFILE
    // Deep • Calm • Confident • Warm • Futuristic
    private final float jayPitch = 0.75f;
    private final float jaySpeed = 0.90f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        jayVoice = new TextToSpeech(this, this);

        setupSpeechRecognizer();
        buildJayInterface();
    }

    // =========================================================
    // JAY INTERFACE
    // =========================================================

    private void buildJayInterface() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(25, 25, 25, 25);

        GradientDrawable background = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        Color.rgb(8, 8, 25),
                        Color.rgb(20, 10, 45),
                        Color.rgb(5, 20, 35)
                }
        );

        root.setBackground(background);

        // TOP BAR
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText("JAY");
        title.setTextColor(Color.WHITE);
        title.setTextSize(28);
        title.setTypeface(null, 1);

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        topBar.addView(title, titleParams);

        Button settingsButton = new Button(this);
        settingsButton.setText("⚙");
        settingsButton.setTextSize(18);

        settingsButton.setOnClickListener(v -> showSettings());

        topBar.addView(settingsButton);
        root.addView(topBar);

        // STATUS
        jayStatus = new TextView(this);
        jayStatus.setText("● Jay online");
        jayStatus.setTextColor(Color.WHITE);
        jayStatus.setTextSize(15);
        jayStatus.setGravity(Gravity.CENTER);
        jayStatus.setPadding(10, 25, 10, 25);

        root.addView(jayStatus);

        // JAY CORE
        TextView jayCore = new TextView(this);
        jayCore.setText("J");
        jayCore.setTextColor(Color.WHITE);
        jayCore.setTextSize(80);
        jayCore.setGravity(Gravity.CENTER);

        GradientDrawable coreBackground =
                new GradientDrawable();

        coreBackground.setShape(
                GradientDrawable.OVAL
        );

        coreBackground.setStroke(
                4,
                Color.rgb(100, 180, 255)
        );

        coreBackground.setColor(
                Color.rgb(15, 15, 45)
        );

        jayCore.setBackground(coreBackground);

        LinearLayout.LayoutParams coreParams =
                new LinearLayout.LayoutParams(
                        220,
                        220
                );

        coreParams.gravity = Gravity.CENTER;

        root.addView(jayCore, coreParams);

        // CONVERSATION
        ScrollView scrollView = new ScrollView(this);

        conversation = new TextView(this);

        conversation.setText(
                "Jay: Good morning, Sir. I am ready.\n\n"
        );

        conversation.setTextColor(Color.WHITE);
        conversation.setTextSize(16);
        conversation.setPadding(20, 20, 20, 20);

        scrollView.addView(conversation);

        LinearLayout.LayoutParams scrollParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                );

        root.addView(scrollView, scrollParams);

        // INPUT
        inputBox = new EditText(this);

        inputBox.setHint("Talk to Jay...");
        inputBox.setHintTextColor(Color.LTGRAY);
        inputBox.setTextColor(Color.WHITE);
        inputBox.setSingleLine(true);

        root.addView(inputBox);

        // BUTTONS
        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(Gravity.CENTER);

        Button talkButton = new Button(this);
        talkButton.setText("🎙 TALK");

        talkButton.setOnClickListener(v -> startListening());

        Button sendButton = new Button(this);
        sendButton.setText("SEND");

        sendButton.setOnClickListener(v -> {

            String message =
                    inputBox.getText()
                            .toString()
                            .trim();

            if (!message.isEmpty()) {

                processMessage(message);

                inputBox.setText("");
            }
        });

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        buttons.addView(
                talkButton,
                buttonParams
        );

        buttons.addView(
                sendButton,
                buttonParams
        );

        root.addView(buttons);

        setContentView(root);
    }

    // =========================================================
    // SPEECH RECOGNITION
    // =========================================================

    private void setupSpeechRecognizer() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            Toast.makeText(
                    this,
                    "Speech recognition is not available",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        speechRecognizer =
                SpeechRecognizer.createSpeechRecognizer(this);

        speechRecognizer.setRecognitionListener(
                new RecognitionListener() {

                    @Override
                    public void onReadyForSpeech(Bundle params) {
                        updateStatus("Listening...");
                    }

                    @Override
                    public void onBeginningOfSpeech() {
                        updateStatus("Jay is listening...");
                    }

                    @Override
                    public void onRmsChanged(float rmsdB) {
                    }

                    @Override
                    public void onBufferReceived(byte[] buffer) {
                    }

                    @Override
                    public void onEndOfSpeech() {
                        updateStatus("Processing...");
                    }

                    @Override
                    public void onError(int error) {

                        updateStatus("● Jay online");

                        Toast.makeText(
                                MainActivity.this,
                                "Jay couldn't hear you. Try again.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onResults(Bundle results) {

                        ArrayList<String> matches =
                                results.getStringArrayList(
                                        SpeechRecognizer.RESULTS_RECOGNITION
                                );

                        if (matches != null &&
                                !matches.isEmpty()) {

                            String spokenText =
                                    matches.get(0);

                            processMessage(spokenText);

                            inputBox.setText("");
                        }

                        updateStatus("● Jay online");
                    }

                    @Override
                    public void onPartialResults(Bundle partialResults) {
                    }

                    @Override
                    public void onEvent(
                            int eventType,
                            Bundle params) {
                    }
                }
        );
    }

    private void startListening() {

        if (speechRecognizer == null) {

            Toast.makeText(
                    this,
                    "Speech recognition unavailable",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Intent intent =
                new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Talk to Jay"
        );

        speechRecognizer.startListening(intent);
    }

    // =========================================================
    // MESSAGE PROCESSING
    // =========================================================

    private void processMessage(String message) {

        if (message == null ||
                message.trim().isEmpty()) {
            return;
        }

        message = message.trim();

        addConversation("You: " + message);

        String lower =
                message.toLowerCase(Locale.ROOT);

        // WHATSAPP
        if (lower.contains("open whatsapp")) {

            openWhatsApp();
            return;
        }

        // CAMERA
        if (lower.contains("open camera") ||
                lower.equals("camera")) {

            openCamera();
            return;
        }

        // PHONE
        if (lower.contains("open phone") ||
                lower.contains("open dialer")) {

            openPhone();
            return;
        }

        // MESSAGES
        if (lower.contains("open messages") ||
                lower.contains("open sms")) {

            openMessages();
            return;
        }

        // SETTINGS
        if (lower.contains("open settings")) {

            openSettings();
            return;
        }

        // GREETING
        if (lower.contains("hello") ||
                lower.equals("hi") ||
                lower.contains("hey")) {

            String response =
                    "Hello, Sir. Jay is online and ready.";

            reply(response);
            return;
        }

        // IDENTITY
        if (lower.contains("who are you")) {

            String response =
                    "I am Jay, your personal AI assistant, Sir.";

            reply(response);
            return;
        }

        // GOOD MORNING
        if (lower.contains("good morning")) {

            String response =
                    "Good morning, Sir. I hope you slept well.";

            reply(response);
            return;
        }

        // GOOD NIGHT
        if (lower.contains("good night")) {

            String response =
                    "Good night, Sir. Sleep well.";

            reply(response);
            return;
        }

        // HOW ARE YOU
        if (lower.contains("how are you")) {

            String response =
                    "I'm operating normally, Sir. Ready when you are.";

            reply(response);
            return;
        }

        // PLACEHOLDER FOR ONLINE AI
        String response =
                "I'm processing that, Sir. My online AI brain will be connected next.";

        reply(response);
    }

    private void reply(String response) {

        addConversation("Jay: " + response);

        speak(response);
                }
                    // =========================================================
    // APP COMMANDS
    // =========================================================

    private void openWhatsApp() {

        try {

            Intent whatsappIntent =
                    getPackageManager()
                            .getLaunchIntentForPackage(
                                    "com.whatsapp"
                            );

            if (whatsappIntent != null) {

                String response =
                        "Opening WhatsApp, Sir.";

                addConversation(
                        "Jay: " + response
                );

                speak(response);

                startActivity(whatsappIntent);

            } else {

                String response =
                        "WhatsApp is not installed on this phone, Sir.";

                addConversation(
                        "Jay: " + response
                );

                speak(response);
            }

        } catch (Exception e) {

            String response =
                    "I couldn't open WhatsApp, Sir.";

            addConversation(
                    "Jay: " + response
            );

            speak(response);
        }
    }

    private void openCamera() {

        try {

            Intent cameraIntent =
                    new Intent(
                            android.provider.MediaStore.ACTION_IMAGE_CAPTURE
                    );

            if (cameraIntent.resolveActivity(
                    getPackageManager()) != null) {

                String response =
                        "Opening the camera, Sir.";

                addConversation(
                        "Jay: " + response
                );

                speak(response);

                startActivity(cameraIntent);

            } else {

                String response =
                        "I couldn't find a camera application, Sir.";

                addConversation(
                        "Jay: " + response
                );

                speak(response);
            }

        } catch (Exception e) {

            String response =
                    "I couldn't open the camera, Sir.";

            addConversation(
                    "Jay: " + response
            );

            speak(response);
        }
    }

    private void openPhone() {

        try {

            Intent phoneIntent =
                    new Intent(
                            Intent.ACTION_DIAL
                    );

            String response =
                    "Opening the phone, Sir.";

            addConversation(
                    "Jay: " + response
            );

            speak(response);

            startActivity(phoneIntent);

        } catch (Exception e) {

            String response =
                    "I couldn't open the phone application, Sir.";

            addConversation(
                    "Jay: " + response
            );

            speak(response);
        }
    }

    private void openMessages() {

        try {

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
                        "I couldn't find a messaging application, Sir.";

                addConversation(
                        "Jay: " + response
                );

                speak(response);
            }

        } catch (Exception e) {

            String response =
                    "I couldn't open messages, Sir.";

            addConversation(
                    "Jay: " + response
            );

            speak(response);
        }
    }

    private void openSettings() {

        try {

            Intent settingsIntent =
                    new Intent(
                            Settings.ACTION_SETTINGS
                    );

            String response =
                    "Opening settings, Sir.";

            addConversation(
                    "Jay: " + response
            );

            speak(response);

            startActivity(settingsIntent);

        } catch (Exception e) {

            String response =
                    "I couldn't open settings, Sir.";

            addConversation(
                    "Jay: " + response
            );

            speak(response);
        }
    }

    // =========================================================
    // JAY VOICE
    // =========================================================

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            voiceReady = true;

            /*
             * JAY VOICE PROFILE
             *
             * Male
             * Deep
             * Calm
             * Confident
             * Warm
             * Slightly futuristic
             *
             * Android TTS cannot guarantee an exact
             * 100–120 Hz voice. Pitch and speed depend
             * on the installed TTS engine and voice.
             */

            jayVoice.setPitch(jayPitch);

            jayVoice.setSpeechRate(jaySpeed);

            int result =
                    jayVoice.setLanguage(
                            Locale.US
                    );

            if (result ==
                    TextToSpeech.LANG_MISSING_DATA ||
                    result ==
                    TextToSpeech.LANG_NOT_SUPPORTED) {

                jayVoice.setLanguage(
                        Locale.getDefault()
                );
            }

        } else {

            voiceReady = false;

            Toast.makeText(
                    this,
                    "Jay voice could not start.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void speak(String text) {

        if (jayVoice == null ||
                !voiceReady ||
                text == null ||
                text.trim().isEmpty()) {

            return;
        }

        jayVoice.setPitch(
                jayPitch
        );

        jayVoice.setSpeechRate(
                jaySpeed
        );

        jayVoice.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "JAY_RESPONSE"
        );
    }

    // =========================================================
    // SETTINGS
    // =========================================================

    private void showSettings() {

        final String[] options = {
                "Voice settings",
                "Jay information",
                "Close"
        };

        new AlertDialog.Builder(this)
                .setTitle("Jay Settings")
                .setItems(
                        options,
                        (dialog, which) -> {

                            if (which == 0) {

                                showVoiceSettings();

                            } else if (which == 1) {

                                showJayInformation();
                            }
                        }
                )
                .show();
    }

    private void showVoiceSettings() {

        new AlertDialog.Builder(this)
                .setTitle("Jay Voice Profile")
                .setMessage(
                        "Voice: Male\n\n" +
                        "Character: Deep, calm, confident\n\n" +
                        "Pitch: Low\n\n" +
                        "Delivery: Smooth and controlled\n\n" +
                        "Speed: Moderate\n\n" +
                        "Tone: Intelligent, warm, slightly futuristic\n\n" +
                        "Emotion: Natural\n\n" +
                        "Pauses: Short and deliberate\n\n" +
                        "Pronunciation: Clear\n\n" +
                        "Personality: Confident but friendly\n\n" +
                        "Languages: English + Kiswahili + Sheng"
                )
                .setPositiveButton(
                        "OK",
                        null
                )
                .show();
    }

    private void showJayInformation() {

        new AlertDialog.Builder(this)
                .setTitle("About Jay")
                .setMessage(
                        "Jay is your personal AI assistant, Sir.\n\n" +
                        "Voice commands, application control, " +
                        "conversation and online AI capabilities " +
                        "are being developed."
                )
                .setPositiveButton(
                        "OK",
                        null
                )
                .show();
    }

    // =========================================================
    // UI HELPERS
    // =========================================================

    private void addConversation(String text) {

        if (conversation == null) {
            return;
        }

        conversation.append(
                text + "\n\n"
        );
    }

    private void updateStatus(String status) {

        if (jayStatus != null) {

            jayStatus.setText(
                    status
            );
        }
    }

    // =========================================================
    // ACTIVITY LIFECYCLE
    // =========================================================

        @Override
    protected void onDestroy() {

        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }

        if (jayVoice != null) {
            jayVoice.stop();
            jayVoice.shutdown();
            jayVoice = null;
        }

        super.onDestroy();
    }
        }
        }
