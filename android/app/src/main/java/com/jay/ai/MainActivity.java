package com.jay.ai;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.view.Gravity;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private JayBrain jayBrain;
    private JayDatabase database;

    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;

    private TextView responseView;
    private TextView jayAvatar;
    private TextView statusView;
    private EditText input;

    private boolean speaking = false;

    private static final int MICROPHONE_PERMISSION = 2001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        jayBrain = new JayBrain(this);
        database = new JayDatabase(this);

        createInterface();
        initializeVoice();
        initializeSpeechRecognition();
    }

    // =========================================================
    // JAY HOME SCREEN
    // =========================================================

    private void createInterface() {

        LinearLayout root = new LinearLayout(this);

        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(25, 25, 25, 25);
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        GradientDrawable background =
                new GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        new int[]{
                                Color.rgb(35, 10, 65),
                                Color.rgb(10, 30, 80),
                                Color.rgb(5, 65, 90),
                                Color.rgb(70, 15, 90)
                        }
                );

        root.setBackground(background);

        // TOP BAR

        LinearLayout topBar = new LinearLayout(this);

        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);

        title.setText("🤖 JAY AI");
        title.setTextSize(30);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER_VERTICAL);

        topBar.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        65,
                        1
                )
        );

        // SETTINGS BUTTON

        Button settingsButton = new Button(this);

        settingsButton.setText("⚙️");
        settingsButton.setTextSize(22);
        settingsButton.setTextColor(Color.WHITE);

        GradientDrawable settingsBackground =
                new GradientDrawable();

        settingsBackground.setColor(
                Color.argb(100, 255, 255, 255)
        );

        settingsBackground.setCornerRadius(40);

        settingsButton.setBackground(settingsBackground);

        topBar.addView(
                settingsButton,
                new LinearLayout.LayoutParams(
                        65,
                        65
                )
        );

        root.addView(topBar);

        settingsButton.setOnClickListener(
                v -> showJaySettings()
        );

        // STATUS

        statusView = new TextView(this);

        statusView.setText(
                "🧠 LOCAL MODE • 🌐 ONLINE READY"
        );

        statusView.setTextSize(14);
        statusView.setTextColor(Color.CYAN);
        statusView.setGravity(Gravity.CENTER);

        root.addView(
                statusView,
                new LinearLayout.LayoutParams(
                        -1,
                        45
                )
        );

        // JAY AVATAR

        jayAvatar = new TextView(this);

        jayAvatar.setText("🤖");
        jayAvatar.setTextSize(75);
        jayAvatar.setGravity(Gravity.CENTER);

        root.addView(
                jayAvatar,
                new LinearLayout.LayoutParams(
                        -1,
                        150
                )
        );

        // RESPONSE

        responseView = new TextView(this);

        responseView.setText(
                "Hello! 👋\n\n" +
                "I'm Jay, your AI assistant.\n" +
                "How can I help you?"
        );

        responseView.setTextSize(19);
        responseView.setTextColor(Color.WHITE);
        responseView.setGravity(Gravity.CENTER);
        responseView.setPadding(20, 20, 20, 20);

        GradientDrawable responseBackground =
                new GradientDrawable();

        responseBackground.setColor(
                Color.argb(80, 255, 255, 255)
        );

        responseBackground.setCornerRadius(35);

        responseView.setBackground(responseBackground);

        LinearLayout.LayoutParams responseParams =
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                );

        responseParams.setMargins(0, 10, 0, 20);

        root.addView(
                responseView,
                responseParams
        );

        // INPUT ROW

        LinearLayout inputRow = new LinearLayout(this);

        inputRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        inputRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        input = new EditText(this);

        input.setHint("Ask Jay...");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.LTGRAY);
        input.setSingleLine(true);
        input.setTextSize(16);

        GradientDrawable inputBackground =
                new GradientDrawable();

        inputBackground.setColor(
                Color.argb(90, 255, 255, 255)
        );

        inputBackground.setCornerRadius(40);

        input.setBackground(inputBackground);

        input.setPadding(
                25,
                0,
                20,
                0
        );

        inputRow.addView(
                input,
                new LinearLayout.LayoutParams(
                        0,
                        60,
                        1
                )
        );

        // SEND BUTTON

        Button send = new Button(this);

        send.setText("SEND ➤");
        send.setTextColor(Color.WHITE);

        GradientDrawable sendBackground =
                new GradientDrawable();

        sendBackground.setColor(
                Color.rgb(0, 150, 220)
        );

        sendBackground.setCornerRadius(40);

        send.setBackground(sendBackground);

        LinearLayout.LayoutParams sendParams =
                new LinearLayout.LayoutParams(
                        110,
                        60
                );

        sendParams.setMargins(
                10,
                0,
                0,
                0
        );

        inputRow.addView(
                send,
                sendParams
        );

        root.addView(inputRow);

        // TALK BUTTON

        Button talk = new Button(this);

        talk.setText("🎤 TALK");
        talk.setTextColor(Color.WHITE);
        talk.setTextSize(16);

        GradientDrawable talkBackground =
                new GradientDrawable();

        talkBackground.setColor(
                Color.rgb(180, 50, 180)
        );

        talkBackground.setCornerRadius(40);

        talk.setBackground(talkBackground);

        LinearLayout.LayoutParams talkParams =
                new LinearLayout.LayoutParams(
                        -1,
                        60
                );

        talkParams.setMargins(
                0,
                10,
                0,
                5
        );

        root.addView(
                talk,
                talkParams
        );

        // STOP BUTTON

        Button stop = new Button(this);

        stop.setText("⏸ STOP JAY");
        stop.setTextColor(Color.WHITE);

        root.addView(stop);

        // ACTIONS

        send.setOnClickListener(
                v -> sendMessage()
        );

        input.setOnEditorActionListener(
                (v, actionId, event) -> {

                    sendMessage();

                    return true;
                }
        );

        talk.setOnClickListener(
                v -> startListening()
        );

        stop.setOnClickListener(
                v -> {

                    stopJay();

                    responseView.setText(
                            "⏸ Jay stopped."
                    );
                }
        );

        setContentView(root);
    }

    // =========================================================
    // SETTINGS SCREEN
    // =========================================================

    private void showJaySettings() {

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
                                Color.rgb(20, 10, 50),
                                Color.rgb(5, 40, 80),
                                Color.rgb(40, 10, 70)
                        }
                );

        root.setBackground(background);

        LinearLayout topBar = new LinearLayout(this);

        topBar.setOrientation(
                LinearLayout.HORIZONTAL
        );

        topBar.setGravity(
                Gravity.CENTER_VERTICAL
        );

        Button back = new Button(this);

        back.setText("←");
        back.setTextSize(22);
        back.setTextColor(Color.WHITE);

        topBar.addView(
                back,
                new LinearLayout.LayoutParams(
                        60,
                        60
                )
        );

        TextView title = new TextView(this);

        title.setText("⚙️ JAY SETTINGS");
        title.setTextSize(25);
        title.setTextColor(Color.WHITE);
        title.setGravity(
                Gravity.CENTER_VERTICAL
        );

        topBar.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        60,
                        1
                )
        );

        root.addView(topBar);

        back.setOnClickListener(
                v -> createInterface()
        );

        ScrollView scrollView =
                new ScrollView(this);

        LinearLayout content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        content.setPadding(
                0,
                20,
                0,
                20
        );

        scrollView.addView(content);

        TextView permissionsTitle =
                new TextView(this);

        permissionsTitle.setText(
                "🔐 PERMISSIONS"
        );

        permissionsTitle.setTextSize(20);
        permissionsTitle.setTextColor(Color.CYAN);

        content.addView(
                permissionsTitle
        );

        Button microphone =
                createSettingsButton(
                        "🎤 Microphone Permission"
                );

        content.addView(microphone);

        microphone.setOnClickListener(
                v -> requestMicrophone()
        );

        Button appSettings =
                createSettingsButton(
                        "📱 Android App Settings"
                );

        content.addView(appSettings);

        appSettings.setOnClickListener(
                v -> openAppSettings()
        );

        TextView featuresTitle =
                new TextView(this);

        featuresTitle.setText(
                "🤖 JAY FEATURES"
        );

        featuresTitle.setTextSize(20);
        featuresTitle.setTextColor(Color.CYAN);

        LinearLayout.LayoutParams featureTitleParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        featureTitleParams.setMargins(
                0,
                25,
                0,
                5
        );

        content.addView(
                featuresTitle,
                featureTitleParams
        );

        Button online =
                createSettingsButton(
                        "🌐 Online Mode"
                );

        content.addView(online);

        online.setOnClickListener(
                v -> Toast.makeText(
                        MainActivity.this,
                        "🌐 Jay is ready for online mode.",
                        Toast.LENGTH_SHORT
                ).show()
        );

        Button voice =
                createSettingsButton(
                        "🔊 Voice & Speech"
                );

        content.addView(voice);

        voice.setOnClickListener(
                v -> initializeVoice()
        );

        Button speech =
                createSettingsButton(
                        "🎤 Speech Recognition"
                );

        content.addView(speech);

        speech.setOnClickListener(
                v -> initializeSpeechRecognition()
        );

        TextView systemTitle =
                new TextView(this);

        systemTitle.setText(
                "📲 SYSTEM"
        );

        systemTitle.setTextSize(20);
        systemTitle.setTextColor(Color.CYAN);

        LinearLayout.LayoutParams systemTitleParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        systemTitleParams.setMargins(
                0,
                25,
                0,
                5
        );

        content.addView(
                systemTitle,
                systemTitleParams
        );

        Button notifications =
                createSettingsButton(
                        "🔔 Notification Settings"
                );

        content.addView(notifications);

        notifications.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    Settings.ACTION_APP_NOTIFICATION_SETTINGS
                            );

                    intent.putExtra(
                            Settings.EXTRA_APP_PACKAGE,
                            getPackageName()
                    );

                    startActivity(intent);
                }
        );

        Button battery =
                createSettingsButton(
                        "🔋 Battery / Background Settings"
                );

        content.addView(battery);

        battery.setOnClickListener(
                v -> openAppSettings()
        );

        TextView about =
                new TextView(this);

        about.setText(
                "\n🤖 JAY AI\n\n" +
                "Your personal Android AI assistant.\n\n" +
                "🧠 Local intelligence\n" +
                "🌐 Online capabilities\n" +
                "🎤 Voice recognition\n" +
                "🔊 Text-to-speech\n" +
                "📱 Android controls"
        );

        about.setTextSize(16);
        about.setTextColor(Color.WHITE);
        about.setGravity(Gravity.CENTER);
        about.setPadding(
                20,
                25,
                20,
                25
        );

        content.addView(about);

        root.addView(
                scrollView,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        setContentView(root);
    }

    // CONTINUE WITH PART 2 BELOW
    // =========================================================
    // SETTINGS BUTTON CREATOR
    // =========================================================

    private Button createSettingsButton(
            String text
    ) {

        Button button =
                new Button(this);

        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(16);
        button.setGravity(Gravity.CENTER);

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                Color.argb(100, 255, 255, 255)
        );

        background.setCornerRadius(30);

        button.setBackground(background);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        60
                );

        params.setMargins(
                0,
                6,
                0,
                6
        );

        button.setLayoutParams(params);

        return button;
    }

    // =========================================================
    // MICROPHONE PERMISSION
    // =========================================================

    private void requestMicrophone() {

        if (android.os.Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(
                    Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.RECORD_AUDIO
                        },
                        MICROPHONE_PERMISSION
                );

            } else {

                Toast.makeText(
                        this,
                        "🎤 Microphone permission is already enabled.",
                        Toast.LENGTH_SHORT
                ).show();
            }

        } else {

            Toast.makeText(
                    this,
                    "🎤 Microphone permission is available.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // =========================================================
    // OPEN ANDROID APP SETTINGS
    // =========================================================

    private void openAppSettings() {

        Intent intent =
                new Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                );

        intent.setData(
                Uri.parse(
                        "package:" + getPackageName()
                )
        );

        startActivity(intent);
    }

    // =========================================================
    // SEND MESSAGE
    // =========================================================

    private void sendMessage() {

        String message =
                input.getText()
                        .toString()
                        .trim();

        if (message.isEmpty()) {
            return;
        }

        processMessage(message);

        input.setText("");
    }

    // =========================================================
    // PROCESS MESSAGE
    // =========================================================

    private void processMessage(
            String message
    ) {

        statusView.setText(
                "🧠 JAY IS THINKING..."
        );

        String answer =
                jayBrain.think(message);

        handleCommand(
                message,
                answer
        );
    }

    // =========================================================
    // TEXT TO SPEECH
    // =========================================================

    private void initializeVoice() {

        tts = new TextToSpeech(
                this,
                status -> {

                    if (status ==
                            TextToSpeech.SUCCESS) {

                        tts.setLanguage(
                                Locale.US
                        );

                        try {

                            for (Voice voice :
                                    tts.getVoices()) {

                                if (
                                        voice.getLocale()
                                                .getLanguage()
                                                .equals("en")
                                                &&
                                        !voice
                                                .isNetworkConnectionRequired()
                                ) {

                                    tts.setVoice(
                                            voice
                                    );

                                    break;
                                }
                            }

                        } catch (Exception ignored) {
                        }
                    }
                }
        );
    }

    // =========================================================
    // SPEECH RECOGNITION
    // =========================================================

    private void initializeSpeechRecognition() {

        if (!SpeechRecognizer
                .isRecognitionAvailable(this)) {

            return;
        }

        speechRecognizer =
                SpeechRecognizer
                        .createSpeechRecognizer(this);

        speechRecognizer.setRecognitionListener(
                new android.speech.RecognitionListener() {

                    @Override
                    public void onReadyForSpeech(
                            Bundle params
                    ) {

                        if (statusView != null) {

                            statusView.setText(
                                    "🎤 LISTENING..."
                            );
                        }
                    }

                    @Override
                    public void onBeginningOfSpeech() {
                    }

                    @Override
                    public void onRmsChanged(
                            float rmsdB
                    ) {
                    }

                    @Override
                    public void onBufferReceived(
                            byte[] buffer
                    ) {
                    }

                    @Override
                    public void onEndOfSpeech() {

                        if (statusView != null) {

                            statusView.setText(
                                    "🧠 JAY IS THINKING..."
                            );
                        }
                    }

                    @Override
                    public void onError(
                            int error
                    ) {

                        if (statusView != null) {

                            statusView.setText(
                                    "🎤 Couldn't hear you. Try again."
                            );
                        }
                    }

                    @Override
                    public void onResults(
                            Bundle results
                    ) {

                        ArrayList<String> matches =
                                results.getStringArrayList(
                                        SpeechRecognizer
                                                .RESULTS_RECOGNITION
                                );

                        if (
                                matches != null
                                        &&
                                !matches.isEmpty()
                        ) {

                            String spokenText =
                                    matches.get(0);

                            if (input != null) {

                                input.setText(
                                        spokenText
                                );
                            }

                            processMessage(
                                    spokenText
                            );

                            if (input != null) {

                                input.setText("");
                            }
                        }
                    }

                    @Override
                    public void onPartialResults(
                            Bundle partialResults
                    ) {
                    }

                    @Override
                    public void onEvent(
                            int eventType,
                            Bundle params
                    ) {
                    }
                }
        );
    }

    // =========================================================
    // START LISTENING
    // =========================================================

    private void startListening() {

        if (speechRecognizer == null) {

            statusView.setText(
                    "Speech recognition isn't available."
            );

            return;
        }

        if (
                android.os.Build.VERSION.SDK_INT >= 23
                        &&
                checkSelfPermission(
                        Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
        ) {

            requestMicrophone();

            return;
        }

        Intent intent =
                new Intent(
                        RecognizerIntent
                                .ACTION_RECOGNIZE_SPEECH
                );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent
                        .LANGUAGE_MODEL_FREE_FORM
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Talk to Jay"
        );

        speechRecognizer.startListening(
                intent
        );
    }

    // =========================================================
    // COMMAND HANDLER
    // =========================================================

    private void handleCommand(
            String userMessage,
            String answer
    ) {

        if ("OPEN_SETTINGS".equals(answer)) {

            database.saveConversation(
                    userMessage,
                    "Opening Settings."
            );

            speak(
                    "Sure. Opening Settings."
            );

            startActivity(
                    new Intent(
                            Settings.ACTION_SETTINGS
                    )
            );

            return;
        }

        if ("OPEN_PHONE".equals(answer)) {

            database.saveConversation(
                    userMessage,
                    "Opening the phone."
            );

            speak(
                    "Sure. Opening the phone."
            );

            startActivity(
                    new Intent(
                            Intent.ACTION_DIAL
                    )
            );

            return;
        }

        if ("OPEN_CALENDAR".equals(answer)) {

            database.saveConversation(
                    userMessage,
                    "Opening your calendar."
            );

            speak(
                    "Sure. Opening your calendar."
            );

            Intent intent =
                    new Intent(
                            Intent.ACTION_MAIN
                    );

            intent.addCategory(
                    Intent.CATEGORY_APP_CALENDAR
            );

            try {

                startActivity(intent);

            } catch (Exception e) {

                Toast.makeText(
                        this,
                        "Calendar app not found.",
                        Toast.LENGTH_SHORT
                ).show();
            }

            return;
        }

        responseView.setText(answer);

        database.saveConversation(
                userMessage,
                answer
        );

        speak(answer);
    }

    // =========================================================
    // SPEAK
    // =========================================================

    private void speak(
            String text
    ) {

        if (tts == null) {
            return;
        }

        speaking = true;

        statusView.setText(
                "🔊 JAY IS SPEAKING..."
        );

        animateJay();

        tts.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "JAY_RESPONSE"
        );
    }

    // =========================================================
    // JAY ANIMATION
    // =========================================================

    private void animateJay() {

        if (!speaking) {
            return;
        }

        AlphaAnimation pulse =
                new AlphaAnimation(
                        0.35f,
                        1.0f
                );

        pulse.setDuration(500);

        pulse.setRepeatMode(
                AlphaAnimation.REVERSE
        );

        pulse.setRepeatCount(
                AlphaAnimation.INFINITE
        );

        jayAvatar.startAnimation(
                pulse
        );
    }

    // =========================================================
    // STOP JAY
    // =========================================================

    private void stopJay() {

        speaking = false;

        if (tts != null) {
            tts.stop();
        }

        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }

        if (jayAvatar != null) {

            jayAvatar.clearAnimation();
            jayAvatar.setAlpha(1.0f);
        }

        if (statusView != null) {

            statusView.setText(
                    "🧠 LOCAL MODE • 🌐 ONLINE READY"
            );
        }
    }

    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        stopJay();

        if (speechRecognizer != null) {

            speechRecognizer.destroy();
        }

        if (tts != null) {

            tts.shutdown();
        }

        if (database != null) {

            database.close();
        }

        super.onDestroy();
    }
        }
