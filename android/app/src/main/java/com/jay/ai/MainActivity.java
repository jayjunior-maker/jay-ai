package com.jay.ai;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        jayBrain = new JayBrain(this);
        database = new JayDatabase(this);

        createInterface();

        tts = new TextToSpeech(
                this,
                status -> {

                    if (status == TextToSpeech.SUCCESS) {
                        tts.setLanguage(Locale.US);
                    }
                }
        );
    }

    private void createInterface() {

        LinearLayout root = new LinearLayout(this);

        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 40, 30, 30);
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        root.setBackgroundColor(
                android.graphics.Color.rgb(18, 10, 35)
        );

        TextView title = new TextView(this);

        title.setText("🤖 JAY AI");
        title.setTextSize(32);
        title.setTextColor(
                android.graphics.Color.WHITE
        );
        title.setGravity(Gravity.CENTER);

        root.addView(title);

        TextView status = new TextView(this);

        status.setText(
                "🧠 LOCAL MODE • ✈️ OFFLINE READY"
        );

        status.setTextSize(14);
        status.setGravity(Gravity.CENTER);
        status.setTextColor(
                android.graphics.Color.CYAN
        );

        root.addView(status);

        responseView = new TextView(this);

        responseView.setText(
                "Habari! 😄\n\n" +
                "Mimi ni Jay.\n" +
                "Niko tayari."
        );

        responseView.setTextSize(20);
        responseView.setTextColor(
                android.graphics.Color.WHITE
        );

        responseView.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams responseParams =
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                );

        responseParams.setMargins(0, 40, 0, 30);

        root.addView(responseView, responseParams);

        EditText input = new EditText(this);

        input.setHint("Ask Jay...");
        input.setTextColor(
                android.graphics.Color.WHITE
        );
        input.setHintTextColor(
                android.graphics.Color.LTGRAY
        );

        root.addView(input);

        Button ask = new Button(this);

        ask.setText("🧠 ASK JAY");

        root.addView(ask);

        Button mute = new Button(this);

        mute.setText("⏸️ MUTE / STOP");

        root.addView(mute);

        ask.setOnClickListener(v -> {

            String message =
                    input.getText().toString();

            String answer =
                    jayBrain.think(message);

            handleCommand(
                    message,
                    answer
            );
        });

        mute.setOnClickListener(v -> {

            if (tts != null) {
                tts.stop();
            }

            responseView.setText(
                    "⏸️ Jay stopped."
            );
        });

        setContentView(root);
    }

    private void handleCommand(
            String userMessage,
            String answer) {

        if ("OPEN_SETTINGS".equals(answer)) {

            startActivity(
                    new Intent(
                            Settings.ACTION_SETTINGS
                    )
            );

            return;
        }

        if ("OPEN_PHONE".equals(answer)) {

            Intent intent =
                    new Intent(
                            Intent.ACTION_DIAL
                    );

            startActivity(intent);

            return;
        }

        if ("OPEN_CALENDAR".equals(answer)) {

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

        tts.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "JAY_RESPONSE"
        );
    }

    @Override
    protected void onDestroy() {

        if (tts != null) {

            tts.stop();
            tts.shutdown();
        }

        if (database != null) {
            database.close();
        }

        super.onDestroy();
    }
                        }
