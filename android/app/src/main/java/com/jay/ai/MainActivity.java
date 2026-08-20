package com.jay.ai;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity {

    private JayBrain jayBrain;
    private JayDatabase database;
    private TextToSpeech tts;

    private TextView responseView;
    private TextView jayAvatar;
    private TextView statusView;

    private Handler animationHandler = new Handler();

    private boolean speaking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        jayBrain = new JayBrain(this);
        database = new JayDatabase(this);

        createInterface();
        initializeVoice();
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

        // TITLE

        TextView title = new TextView(this);

        title.setText("🤖 JAY AI");
        title.setTextSize(32);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);

        root.addView(title);

        // STATUS

        statusView = new TextView(this);

        statusView.setText(
                "🧠 LOCAL MODE • ✈️ OFFLINE READY"
        );

        statusView.setTextSize(14);
        statusView.setTextColor(Color.CYAN);
        statusView.setGravity(Gravity.CENTER);

        root.addView(statusView);

        // JAY AVATAR

        jayAvatar = new TextView(this);

        jayAvatar.setText("🤖");
        jayAvatar.setTextSize(75);
        jayAvatar.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams avatarParams =
                new LinearLayout.LayoutParams(
                        -1,
                        150
                );

        avatarParams.setMargins(0, 20, 0, 10);

        root.addView(jayAvatar, avatarParams);

        // RESPONSE AREA

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

        // TEXT INPUT ROW

        LinearLayout inputRow =
                new LinearLayout(this);

        inputRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        inputRow.setGravity(Gravity.CENTER_VERTICAL);

        EditText input = new EditText(this);

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

        LinearLayout.LayoutParams inputParams =
                new LinearLayout.LayoutParams(
                        0,
                        60,
                        1
                );

        inputParams.setMargins(0, 0, 10, 0);

        inputRow.addView(input, inputParams);

        // SEND BUTTON

        Button send = new Button(this);

        send.setText("SEND ➤");
        send.setTextColor(Color.WHITE);
        send.setTextSize(14);

        GradientDrawable sendBackground =
                new GradientDrawable();

        sendBackground.setColor(
                Color.rgb(0, 150, 220)
        );

        sendBackground.setCornerRadius(40);

        send.setBackground(sendBackground);

        inputRow.addView(
                send,
                new LinearLayout.LayoutParams(
                        110,
                        60
                )
        );

        root.addView(inputRow);

        // STOP BUTTON

        Button mute = new Button(this);

        mute.setText("⏸ STOP JAY");
        mute.setTextColor(Color.WHITE);

        root.addView(mute);

        // SEND ACTION

        send.setOnClickListener(v -> {

            String message =
                    input.getText().toString().trim();

            if (message.isEmpty()) {
                return;
            }

            String answer =
                    jayBrain.think(message);

            handleCommand(
                    message,
                    answer
            );

            input.setText("");
        });

        // ENTER KEY

        input.setOnEditorActionListener(
                (v, actionId, event) -> {

                    send.performClick();

                    return true;
                }
        );

        // STOP

        mute.setOnClickListener(v -> {

            stopJay();

            responseView.setText(
                    "⏸ Jay stopped."
            );
        });

        setContentView(root);
    }

    private void initializeVoice() {

        tts = new TextToSpeech(
                this,
                status -> {

                    if (status == TextToSpeech.SUCCESS) {

                        int result =
                                tts.setLanguage(
                                        Locale.US
                                );

                        // Prefer a natural English voice
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

    private void handleCommand(
            String userMessage,
            String answer) {

        if ("OPEN_SETTINGS".equals(answer)) {

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

            speak(
                    "Sure. Opening the phone."
            );

            Intent intent =
                    new Intent(
                            Intent.ACTION_DIAL
                    );

            startActivity(intent);

            return;
        }

        if ("OPEN_CALENDAR".equals(answer)) {

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

        Animation pulse =
                new AlphaAnimation(
                        0.35f,
                        1.0f
                );

        pulse.setDuration(500);
        pulse.setRepeatMode(
                Animation.REVERSE
        );
        pulse.setRepeatCount(
                Animation.INFINITE
        );

        jayAvatar.startAnimation(pulse);
    }

    private void stopJay() {

        speaking = false;

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
