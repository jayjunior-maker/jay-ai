package com.jay.ai;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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

    private static final int MICROPHONE_PERMISSION_REQUEST = 100;

    private TextToSpeech jayVoice;
    private SpeechRecognizer speechRecognizer;
    private JayBrain jayBrain;
    private JayDatabase jayDatabase;

    private TextView jayStatus;
    private TextView conversation;
    private EditText inputBox;

    private boolean voiceReady = false;

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

        jayDatabase = new JayDatabase(this);
        jayBrain = new JayBrain(this);

        jayVoice = new TextToSpeech(this, this);

        buildJayInterface();

        setupSpeechRecognizer();
    }

    private void buildJayInterface() {

        LinearLayout root = new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                25,
                25,
                25,
                25
        );

        GradientDrawable background =
                new GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        new int[]{
                                Color.rgb(8, 8, 25),
                                Color.rgb(20, 10, 45),
                                Color.rgb(5, 20, 35)
                        }
                );

        root.setBackground(background);

        // TOP BAR

        LinearLayout topBar =
                new LinearLayout(this);

        topBar.setOrientation(
                LinearLayout.HORIZONTAL
        );

        topBar.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView title =
                new TextView(this);

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

        topBar.addView(
                title,
                titleParams
        );

        Button settingsButton =
                new Button(this);

        settingsButton.setText("⚙");
        settingsButton.setTextSize(18);

        settingsButton.setOnClickListener(
                v -> showSettings()
        );

        topBar.addView(
                settingsButton
        );

        root.addView(topBar);

        // STATUS

        jayStatus =
                new TextView(this);

        jayStatus.setText(
                "● Jay online"
        );

        jayStatus.setTextColor(
                Color.WHITE
        );

        jayStatus.setTextSize(15);

        jayStatus.setGravity(
                Gravity.CENTER
        );

        jayStatus.setPadding(
                10,
                25,
                10,
                25
        );

        root.addView(jayStatus);

        // JAY CORE

        TextView jayCore =
                new TextView(this);

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

        LinearLayout.LayoutParams coreParams =
                new LinearLayout.LayoutParams(
                        220,
                        220
                );

        coreParams.gravity =
                Gravity.CENTER;

        jayCore.setBackground(
                coreBackground
        );

        root.addView(
                jayCore,
                coreParams
        );

        // CONVERSATION

        ScrollView scrollView =
                new ScrollView(this);

        conversation =
                new TextView(this);

        conversation.setText(
                "Jay: Good morning, Sir. I am ready.\n\n"
        );

        conversation.setTextColor(
                Color.WHITE
        );

        conversation.setTextSize(16);

        conversation.setPadding(
                20,
                20,
                20,
                20
        );

        scrollView.addView(
                conversation
        );

        LinearLayout.LayoutParams scrollParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                );

        root.addView(
                scrollView,
                scrollParams
        );

        // INPUT

        inputBox =
                new EditText(this);

        inputBox.setHint(
                "Talk to Jay..."
        );

        inputBox.setHintTextColor(
                Color.LTGRAY
        );

        inputBox.setTextColor(
                Color.WHITE
        );

        inputBox.setSingleLine(true);

        root.addView(inputBox);

        // BUTTONS

        LinearLayout buttons =
                new LinearLayout(this);

        buttons.setOrientation(
                LinearLayout.HORIZONTAL
        );

        buttons.setGravity(
                Gravity.CENTER
        );

        Button talkButton =
                new Button(this);

        talkButton.setText(
                "🎙 TALK"
        );

        talkButton.setOnClickListener(
                v -> startListening()
        );

        Button sendButton =
                new Button(this);

        sendButton.setText(
                "SEND"
        );

        sendButton.setOnClickListener(v -> {

            String message =
                    inputBox
                            .getText()
                            .toString()
                            .trim();

            if (!message.isEmpty()) {

                processMessage(
                        message
                );

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

    private void setupSpeechRecognizer() {

        if (!SpeechRecognizer
                .isRecognitionAvailable(this)) {

            Toast.makeText(
                    this,
                    "Speech recognition is not available.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        speechRecognizer =
                SpeechRecognizer
                        .createSpeechRecognizer(this);

        speechRecognizer.setRecognitionListener(
                new RecognitionListener() {

                    @Override
                    public void onReadyForSpeech(
                            Bundle params) {

                        updateStatus(
                                "● Listening..."
                        );
                    }

                    @Override
                    public void onBeginningOfSpeech() {

                        updateStatus(
                                "● Jay is listening..."
                        );
                    }

                    @Override
                    public void onRmsChanged(
                            float rmsdB) {
                    }

                    @Override
                    public void onBufferReceived(
                            byte[] buffer) {
                    }

                    @Override
                    public void onEndOfSpeech() {

                        updateStatus(
                                "● Processing..."
                        );
                    }

                    @Override
                    public void onError(
                            int error) {

                        updateStatus(
                                "● Jay online"
                        );

                        String message;

                        if (error ==
                                SpeechRecognizer
                                        .ERROR_INSUFFICIENT_PERMISSIONS) {

                            message =
                                    "Microphone permission is required, Sir.";

                        } else if (error ==
                                SpeechRecognizer
                                        .ERROR_NO_MATCH) {

                            message =
                                    "I didn't catch that, Sir. Please try again.";

                        } else {

                            message =
                                    "Jay couldn't hear you. Try again, Sir.";
                        }

                        Toast.makeText(
                                MainActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onResults(
                            Bundle results) {

                        ArrayList<String> matches =
                                results.getStringArrayList(
                                        SpeechRecognizer
                                                .RESULTS_RECOGNITION
                                );

                        if (matches != null &&
                                !matches.isEmpty()) {

                            String spokenText =
                                    matches.get(0);

                            inputBox.setText(
                                    spokenText
                            );

                            processMessage(
                                    spokenText
                            );

                            inputBox.setText("");
                        }

                        updateStatus(
                                "● Jay online"
                        );
                    }

                    @Override
                    public void onPartialResults(
                            Bundle partialResults) {
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

        if (checkSelfPermission(
                Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    MICROPHONE_PERMISSION_REQUEST
            );

            return;
        }

        if (speechRecognizer == null) {

            setupSpeechRecognizer();

            if (speechRecognizer == null) {

                Toast.makeText(
                        this,
                        "Speech recognition unavailable.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }
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

        try {

            speechRecognizer.startListening(intent);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Jay could not start listening.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode ==
                MICROPHONE_PERMISSION_REQUEST) {

            if (grantResults.length > 0 &&
                    grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(
                        this,
                        "Microphone enabled, Sir.",
                        Toast.LENGTH_SHORT
                ).show();

                startListening();

            } else {

                Toast.makeText(
                        this,
                        "Microphone permission is needed for Talk.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
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

        addConversation(
                "You: " + message
        );

        String response;

        try {

            response =
                    jayBrain.think(message);

        } catch (Exception e) {

            response =
                    "I encountered a local processing error, Sir.";
        }

        if (response == null ||
                response.trim().isEmpty()) {

            response =
                    "I couldn't process that request, Sir.";
        }

        // =====================================================
        // ONLINE AI FALLBACK
        // =====================================================

        if (response.equals(
                "ONLINE_REQUIRED")) {

            updateStatus(
                    "● Connecting to online brain..."
            );

            final String userMessage =
                    message;

            jayBrain.askOnline(
                    userMessage,
                    new JayApiClient.Callback() {

                        @Override
                        public void onSuccess(
                                String onlineResponse) {

                            runOnUiThread(() -> {

                                updateStatus(
                                        "● Jay online"
                                );

                                reply(
                                        onlineResponse
                                );

                                jayDatabase
                                        .saveConversation(
                                                userMessage,
                                                onlineResponse
                                        );
                            });
                        }

                        @Override
                        public void onError(
                                String error) {

                            runOnUiThread(() -> {

                                updateStatus(
                                        "● Jay offline"
                                );

                                reply(
                                        "I can't reach my online brain right now, Sir. I'm still in offline mode."
                                );
                            });
                        }
                    }
            );

            return;
        }

        handleBrainResponse(
                message,
                response
        );
    }

    // =========================================================
    // BRAIN RESPONSE HANDLER
    // =========================================================

    private void handleBrainResponse(
            String userMessage,
            String response) {

        if (response.equals(
                "OPEN_SETTINGS")) {

            openSettings();
            return;
        }

        if (response.equals(
                "OPEN_PHONE")) {

            openPhone();
            return;
        }

        if (response.equals(
                "OPEN_CALENDAR")) {

            openCalendar();
            return;
        }

        if (response.equals(
                "OPEN_INVENTORY")) {

            reply(
                    "The inventory module is being prepared, Sir."
            );

            return;
        }

        if (response.equals(
                "OPEN_REPAIRS")) {

            reply(
                    "The repairs module is being prepared, Sir."
            );

            return;
        }

        reply(response);

        jayDatabase.saveConversation(
                userMessage,
                response
        );
    }

    // =========================================================
    // JAY RESPONSE
    // =========================================================

    private void reply(String response) {

        addConversation(
                "Jay: " + response
        );

        speak(response);
    }

    // =========================================================
    // OPEN ANDROID SETTINGS
    // =========================================================

    private void openSettings() {

        try {

            Intent intent =
                    new Intent(
                            Settings.ACTION_SETTINGS
                    );

            reply(
                    "Opening settings, Sir."
            );

            startActivity(intent);

        } catch (Exception e) {

            reply(
                    "I couldn't open settings, Sir."
            );
        }
    }

    // =========================================================
    // OPEN PHONE
    // =========================================================

    private void openPhone() {

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_DIAL
                    );

            reply(
                    "Opening the phone, Sir."
            );

            startActivity(intent);

        } catch (Exception e) {

            reply(
                    "I couldn't open the phone application, Sir."
            );
        }
    }

    // =========================================================
    // OPEN CALENDAR
    // =========================================================

    private void openCalendar() {

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_MAIN
                    );

            intent.addCategory(
                    Intent.CATEGORY_APP_CALENDAR
            );

            if (intent.resolveActivity(
                    getPackageManager()
            ) != null) {

                reply(
                        "Opening the calendar, Sir."
                );

                startActivity(intent);

            } else {

                reply(
                        "I couldn't find a calendar application, Sir."
                );
            }

        } catch (Exception e) {

            reply(
                    "I couldn't open the calendar, Sir."
            );
        }
                    }
                    // =========================================================
    // TEXT TO SPEECH
    // =========================================================

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            voiceReady = true;

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

    // =========================================================
    // VOICE SETTINGS
    // =========================================================

    private void showVoiceSettings() {

        new AlertDialog.Builder(this)
                .setTitle(
                        "Jay Voice Profile"
                )
                .setMessage(
                        "Voice: Male\n\n" +
                        "Character: Deep, calm, confident\n\n" +
                        "Pitch: Low\n\n" +
                        "Delivery: Smooth and controlled\n\n" +
                        "Speed: Moderate\n\n" +
                        "Tone: Intelligent and futuristic\n\n" +
                        "Languages: English + Kiswahili + Sheng"
                )
                .setPositiveButton(
                        "OK",
                        null
                )
                .show();
    }

    // =========================================================
    // JAY INFORMATION
    // =========================================================

    private void showJayInformation() {

        new AlertDialog.Builder(this)
                .setTitle(
                        "About Jay"
                )
                .setMessage(
                        "Jay is your personal AI assistant, Sir.\n\n" +
                        "Jay currently has local conversation, " +
                        "memory, voice recognition, text-to-speech, " +
                        "Android command capabilities and an " +
                        "online AI connection.\n\n" +
                        "Jay can use the online brain when an " +
                        "internet connection is available."
                )
                .setPositiveButton(
                        "OK",
                        null
                )
                .show();
    }

    // =========================================================
    // CONVERSATION
    // =========================================================

    private void addConversation(
            String text) {

        if (conversation == null) {
            return;
        }

        conversation.append(
                text + "\n\n"
        );
    }

    // =========================================================
    // STATUS
    // =========================================================

    private void updateStatus(
            String status) {

        if (jayStatus != null) {

            jayStatus.setText(
                    status
            );
        }
    }

    // =========================================================
    // CLEANUP
    // =========================================================

    @Override
    protected void onDestroy() {

        if (speechRecognizer != null) {

            speechRecognizer.cancel();

            speechRecognizer.destroy();

            speechRecognizer = null;
        }

        if (jayVoice != null) {

            jayVoice.stop();

            jayVoice.shutdown();

            jayVoice = null;
        }

        if (jayDatabase != null) {

            jayDatabase.close();
        }

        super.onDestroy();
    }
                }
