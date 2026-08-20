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
import android.view.View;
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

    private JaySecurityManager securityManager;

    private final int SPEECH_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        securityManager = new JaySecurityManager(this);

        jayVoice = new TextToSpeech(this, this);

        setupSpeechRecognizer();

        buildJayInterface();
    }

    // ---------------------------------------------------------
    // JAY INTERFACE
    // ---------------------------------------------------------

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

        Button securityButton = new Button(this);
        securityButton.setText("🔐");
        securityButton.setTextSize(18);

        securityButton.setOnClickListener(v ->
                showSecurityMenu()
        );

        topBar.addView(securityButton);

        root.addView(topBar);

        // -----------------------------------------------------
        // STATUS
        // -----------------------------------------------------

        jayStatus = new TextView(this);

        jayStatus.setText(
                "● Jay online\nSecurity: " +
                securityManager.getSecurityStatus()
        );

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
                "Jay: Welcome. I am ready.\n\n"
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
        // INPUT
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
                // ---------------------------------------------------------
    // SPEECH RECOGNITION
    // ---------------------------------------------------------

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

                        updateStatus(
                                "● Jay online\nSecurity: "
                                        + securityManager
                                        .getSecurityStatus()
                        );

                        Toast.makeText(
                                MainActivity.this,
                                "I couldn't hear you. Try again.",
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

                            inputBox.setText(spokenText);

                            processMessage(spokenText);
                        }

                        updateStatus(
                                "● Jay online\nSecurity: "
                                        + securityManager
                                        .getSecurityStatus()
                        );
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

    // ---------------------------------------------------------
    // JAY MESSAGE PROCESSING
    // ---------------------------------------------------------

    private void processMessage(String message) {

        addConversation(
                "You: " + message
        );

        String lower =
                message.toLowerCase(Locale.ROOT);

        String response;

        if (lower.contains("hello") ||
                lower.contains("hi") ||
                lower.contains("hey")) {

            response =
                    "Hello Admin. Jay is online and ready.";

        } else if (lower.contains("who are you")) {

            response =
                    "I am Jay, your personal AI assistant.";

        } else if (lower.contains("security")) {

            response =
                    "Jay security is currently "
                            + securityManager
                            .getSecurityStatus()
                            + ".";

        } else if (lower.contains("guest mode")) {

            securityManager.enterGuestMode();

            response =
                    "Guest mode has been activated.";

            updateStatus(
                    "● Guest Mode\nSecurity: GUEST"
            );

        } else if (lower.contains("admin mode")) {

            showAdminAuthorization();

            return;

        } else if (lower.contains("lock")) {

            enterRestrictedMode();

            response =
                    "Jay restricted mode activated.";

        } else {

            response =
                    "I heard you say: " + message
                            + ". My online AI brain will be connected next.";
        }

        addConversation(
                "Jay: " + response
        );

        speak(response);
    }

    // ---------------------------------------------------------
    // JAY VOICE
    // ---------------------------------------------------------

    @Override
    public void onInit(int status) {

        if (status ==
                TextToSpeech.SUCCESS) {

            int result =
                    jayVoice.setLanguage(
                            Locale.getDefault()
                    );

            if (result ==
                    TextToSpeech.LANG_MISSING_DATA ||
                    result ==
                    TextToSpeech.LANG_NOT_SUPPORTED) {

                Toast.makeText(
                        this,
                        "Jay voice language unavailable",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    private void speak(String text) {

        if (jayVoice == null) {
            return;
        }

        jayVoice.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "JAY_RESPONSE"
        );
    }

    // ---------------------------------------------------------
    // SECURITY MENU
    // ---------------------------------------------------------

    private void showSecurityMenu() {

        final String[] options = {
                "Admin authorization",
                "Enter Guest Mode",
                "Exit Guest Mode",
                "Restricted Mode"
        };

        new android.app.AlertDialog.Builder(this)
                .setTitle("Jay Security")
                .setItems(
                        options,
                        (dialog, which) -> {

                            if (which == 0) {

                                showAdminAuthorization();

                            } else if (which == 1) {

                                securityManager
                                        .enterGuestMode();

                                updateSecurityDisplay();

                            } else if (which == 2) {

                                showAdminAuthorization();

                            } else if (which == 3) {

                                enterRestrictedMode();
                            }
                        }
                )
                .show();
    }

    // ---------------------------------------------------------
    // ADMIN AUTHORIZATION
    // ---------------------------------------------------------

    private void showAdminAuthorization() {

        securityManager.requestAdminAuthorization(
                new JaySecurityManager.AuthorizationCallback() {

                    @Override
                    public void onAuthorized() {

                        securityManager
                                .setGuestMode(false);

                        securityManager
                                .setAdminAuthorized(true);

                        updateSecurityDisplay();

                        String message =
                                "Welcome back, Admin.";

                        addConversation(
                                "Jay: " + message
                        );

                        speak(message);
                    }

                    @Override
                    public void onDenied() {

                        securityManager
                                .setGuestMode(true);

                        updateSecurityDisplay();

                        String message =
                                "Authorization denied. Guest mode remains active.";

                        addConversation(
                                "Jay: " + message
                        );

                        speak(message);
                    }
                }
        );
    }

    // ---------------------------------------------------------
    // RESTRICTED MODE
    // ---------------------------------------------------------

    private void enterRestrictedMode() {

        securityManager
                .setAdminAuthorized(false);

        securityManager
                .setGuestMode(true);

        updateSecurityDisplay();

        Toast.makeText(
                this,
                "Jay Restricted Mode",
                Toast.LENGTH_SHORT
        ).show();
    }

    // ---------------------------------------------------------
    // UI HELPERS
    // ---------------------------------------------------------

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

    private void updateSecurityDisplay() {

        String status =
                securityManager
                        .getSecurityStatus();

        updateStatus(
                "● Jay online\nSecurity: "
                        + status
        );
    }

    // ---------------------------------------------------------
    // ACTIVITY LIFECYCLE
    // ---------------------------------------------------------

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
