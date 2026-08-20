package com.jay.ai;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
import android.widget.TextView;

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

    private static final int SPEECH_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        jayBrain = new JayBrain(this);
        database = new JayDatabase(this);

        createInterface();
        initializeVoice();
        initializeSpeechRecognition();
    }

    private void createInterface() {

        LinearLayout root = new LinearLayout(this);

        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 35, 30, 25);
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

        TextView title = new TextView(this);
        title.setText("🤖 JAY AI");
        title.setTextSize(32);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);

        root.addView(title);

        statusView = new TextView(this);
        statusView.setText("🧠 LOCAL MODE • ✈️ OFFLINE READY");
        statusView.setTextSize(14);
        statusView.setTextColor(Color.CYAN);
        statusView.setGravity(Gravity.CENTER);

        root.addView(statusView);

        jayAvatar = new TextView(this);
        jayAvatar.setText("🤖");
        jayAvatar.setTextSize(75);
        jayAvatar.setGravity(Gravity.CENTER);

        root.addView(
                jayAvatar,
                new LinearLayout.LayoutParams(-1, 150)
        );

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

        root.addView(responseView, responseParams);

        // INPUT

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
        input.setPadding(25, 0, 20, 0);

        inputRow.addView(
                input,
                new LinearLayout.LayoutParams(
                        0,
                        60,
                        1
                )
        );

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

        sendParams.setMargins(10, 0, 0, 0);

        inputRow.addView(send, sendParams);

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

        root.addView(
                talk,
                new LinearLayout.LayoutParams(
                        -1,
                        60
                )
        );

        // STOP BUTTON

        Button stop = new Button(this);

        stop.setText("⏸ STOP JAY");

        root.addView(stop);

        // SEND

        send.setOnClickListener(v -> sendMessage());

        // ENTER

        input.setOnEditorActionListener(
                (v, actionId, event) -> {

                    sendMessage();

                    return true;
                }
        );

        // TALK

        talk.setOnClickListener(v -> startListening());

        // STOP

        stop.setOnClickListener(v -> {

            stopJay();

            responseView.setText(
                    "⏸ Jay stopped."
            );
        });

        setContentView(root);
    }

    private void sendMessage() {

        String message =
                input.getText().toString().trim();

        if (message.isEmpty()) {
            return;
        }

        processMessage(message);

        input.setText("");
    }

    private void processMessage(String message) {

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

                                if (voice.getLocale()
                                        .getLanguage()
                                        .equals("en")
                                        && !voice.isNetworkConnectionRequired()) {

                                    tts.setVoice(voice);
                                    break;
                                }
                            }

                        } catch (Exception ignored) {
                        }
                    }
                }
        );
    }

    private void initializeSpeechRecognition() {

        if (SpeechRecognizer.isRecognitionAvailable(this)) {

            speechRecognizer =
                    SpeechRecognizer.createSpeechRecognizer(
                            this
                    );

            speechRecognizer.setRecognitionListener(
                    new android.speech.RecognitionListener() {

                        @Override
                        public void onReadyForSpeech(
                                Bundle params) {

                            statusView.setText(
                                    "🎤 LISTENING..."
                            );
                        }

                        @Override
                        public void onBeginningOfSpeech() {
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

                            statusView.setText(
                                    "🧠 JAY IS THINKING..."
                            );
                        }

                        @Override
                        public void onError(int error) {

                            statusView.setText(
                                    "🎤 Couldn't hear you. Try again."
                            );
                        }

                        @Override
                        public void onResults(
                                Bundle results) {

                            ArrayList<String> matches =
                                    results.getStringArrayList(
                                            SpeechRecognizer.RESULTS_RECOGNITION
                                    );

                            if (matches != null
                                    && !matches.isEmpty()) {

                                String spokenText =
                                        matches.get(0);

                                input.setText(
                                        spokenText
                                );

                                processMessage(
                                        spokenText
                                );

                                input.setText("");
                            }
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
    }

    private void startListening() {

        if (speechRecognizer == null) {

            statusView.setText(
                    "Speech recognition isn't available."
            );

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

        // English output/recognition preference.
        // Android may still recognize other languages
        // depending on the installed speech service.

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

    private void handleCommand(
            String userMessage,
            String answer) {

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

            startActivity(intent);

            return;
        }

        // ONLY ONE RESPONSE IS DISPLAYED HERE.

        responseView.setText(answer);

        database.saveConversation(
                userMessage,
                answer
        );

        speak(answer);
    }

    private void speak(String text) {

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

        jayAvatar.startAnimation(pulse);
    }

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
                    "🧠 LOCAL MODE • ✈️ OFFLINE READY"
            );
        }
    }

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
    } speaking = false;

        if (tts != null) {
            tts.stop();
        }

        if (jayAvatar != null) {
            jayAvatar.clearAnimation();
            jayAvatar.setAlpha(1.0f);
        }

        if (statusView != null) {
            statusView.setText(
                    "🧠 LOCAL MODE • ✈️ OFFLINE READY"
            );
        }
    }

    @Override
    protected void onDestroy() {

        stopJay();

        if (tts != null) {
            tts.shutdown();
        }

        if (database != null) {
            database.close();
        }

        super.onDestroy();
    }
            }
