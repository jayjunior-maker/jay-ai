package com.jay.ai;

import android.app.Activity;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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

    /*
     * JAY VOICE PROFILE
     *
     * Character : Deep, calm, confident
     * Delivery  : Smooth and controlled
     * Speed     : Moderate
     * Tone      : Intelligent, warm, futuristic
     * Emotion   : Natural
     * Pauses    : Short and deliberate
     *
     * These values control Android TTS for now.
     * A dedicated neural voice engine can replace this later.
     */
    private float jayPitch = 0.75f;
    private float jaySpeed = 0.90f;

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

        // -----------------------------------------------------
        // TOP BAR
        // -----------------------------------------------------

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

        settingsButton.setOnClickListener(v ->
                showSettings()
        );

        topBar.addView(settingsButton);

        root.addView(topBar);

        // -----------------------------------------------------
        // STATUS
        // -----------------------------------------------------

        jayStatus = new TextView(this);
        jayStatus.setText("● Jay online");
        jayStatus.setTextColor(Color.WHITE);
        jayStatus.setTextSize(15);
        jayStatus.setGravity(Gravity.CENTER);
        jayStatus.setPadding(10, 25, 10, 25);

        root.addView(jayStatus);

        // -----------------------------------------------------
        // JAY CORE
        // -----------------------------------------------------

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

        // -----------------------------------------------------
        // CONVERSATION
        // -----------------------------------------------------

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

        // -----------------------------------------------------
        // TEXT INPUT
        // -----------------------------------------------------

        inputBox = new EditText(this);

        inputBox.setHint("Talk to Jay...");
        inputBox.setHintTextColor(Color.LTGRAY);
        inputBox.setTextColor(Color.WHITE);
        inputBox.setSingleLine(true);

        root.addView(inputBox);

        // -----------------------------------------------------
        // BUTTONS
        // -----------------------------------------------------

        LinearLayout buttons =
                new LinearLayout(this);

        buttons.setOrientation(
                LinearLayout.HORIZONTAL
        );

        buttons.setGravity(Gravity.CENTER);

        Button talkButton =
                new Button(this);

        talkButton.setText("🎙 TALK");

        talkButton.setOnClickListener(v ->
                startListening()
        );

        Button sendButton =
                new Button(this);

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
                new android.speech.RecognitionListener() {

                    @Override
                    public void onReadyForSpeech(
                            android.os.Bundle params) {

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
                    public void onBufferReceived(
                            byte[] buffer) {
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
                    public void onResults(
                            android.os.Bundle results) {

                        ArrayList<String> matches =
                                results.getStringArrayList(
                                        SpeechRecognizer.RESULTS_RECOGNITION
                                );

                        if (matches != null &&
                                !matches.isEmpty()) {

                            String spokenText =
                                    matches.get(0);

                            /*
                             * We process the spoken command directly.
                             *
                             * Jay does NOT say:
                             * "I heard you say..."
                             */

                            processMessage(spokenText);

                            inputBox.setText("");
                        }

                        updateStatus("● Jay online");
                    }

                    @Override
                    public void onPartialResults(
                            android.os.Bundle partialResults) {
                    }

                    @Override
                    public void onEvent(
                            int eventType,
                            android.os.Bundle params) {
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
    // JAY MESSAGE PROCESSING
    // =========================================================

    private void processMessage(String message) {

        if (message == null ||
                message.trim().isEmpty()) {
            return;
        }

        message = message.trim();

        addConversation(
                "You: " + message
        );

        String lower =
                message.toLowerCase(Locale.ROOT);

        String response;

        if (lower.contains("hello") ||
                lower.equals("hi") ||
                lower.contains("hey")) {

            response =
                    "Hello, Sir. Jay is online and ready.";

        } else if (lower.contains("who are you")) {

            response =
                    "I am Jay, your personal AI assistant, Sir.";

        } else if (lower.contains("good morning")) {

            response =
                    "Good morning, Sir. I hope you slept well.";

        } else if (lower.contains("good night")) {

            response =
                    "Good night, Sir. Sleep well.";

        } else if (lower.contains("how are you")) {

            response =
                    "I'm operating normally, Sir. Ready when you are.";

        } else {

            /*
             * IMPORTANT:
             *
             * Jay no longer repeats the user's words.
             *
             * This is currently a placeholder until the
             * online AI provider is connected.
             */

            response =
                    "I'm processing that, Sir. My online AI brain will be connected next.";
        }

        addConversation(
                "Jay: " + response
        );

        speak(response);
    }

    // =========================================================
    // JAY VOICE
    // =========================================================

    @Override
    public void onInit(int status) {

        if (status ==
                TextToSpeech.SUCCESS) {

            voiceReady = true;

            /*
             * Jay's current Android TTS profile.
             *
             * Lower pitch = deeper voice.
             * Slightly slower speed = calm delivery.
             */

            jayVoice.setPitch(jayPitch);
            jayVoice.setSpeechRate(jaySpeed);

            int result =
                    jayVoice.setLanguage(
                            Locale.getDefault()
                    );

            if (result ==
                    TextToSpeech.LANG_MISSING_DATA ||
                    result ==
                    TextToSpeech.LANG_NOT_SUPPORTED) {

                /*
                 * Fall back to English.
                 */

                jayVoice.setLanguage(
                        Locale.US
                );
            }
        }
    }

    private void speak(String text) {

        if (jayVoice == null ||
                !voiceReady) {
            return;
        }

        jayVoice.setPitch(jayPitch);
        jayVoice.setSpeechRate(jaySpeed);

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

        new android.app.AlertDialog.Builder(this)
                .setTitle("Jay Settings")
                .setItems(
                        options,
                        (dialog, which) -> {

                            if (which == 0) {

                                showVoiceSettings();

                            } else if (which == 1) {

                                new android.app.AlertDialog.Builder(
                                        this
                                )
                                        .setTitle("About Jay")
                                        .setMessage(
                                                "Jay is your personal AI assistant, Sir."
                                        )
                                        .setPositiveButton(
                                                "OK",
                                                null
                                        )
                                        .show();
                            }
                        }
                )
                .show();
    }

    private void showVoiceSettings() {

        new android.app.AlertDialog.Builder(this)
                .setTitle("Jay Voice Profile")
                .setMessage(
                        "Male\n" +
                        "Deep • Calm • Confident\n" +
                        "Low pitch • Moderate speed\n" +
                        "Smooth and controlled delivery\n" +
                        "Intelligent • Warm • Slightly futuristic\n" +
                        "Natural emotion • Clear pronunciation\n" +
                        "English + Kiswahili + Sheng"
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
            jayStatus.setText(status);
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
