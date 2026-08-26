package com.jay.ai;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Wake-word foundation for Jay. Uses Android speech recognition and deliberately
 * requires RECORD_AUDIO permission. It does not bypass Android microphone rules.
 */
public final class JayWakeWordManager {
    public interface Listener {
        void onWakeWordDetected();
        void onListeningChanged(boolean listening);
    }

    private final Context context;
    private final Listener listener;
    private SpeechRecognizer recognizer;
    private boolean running;

    public JayWakeWordManager(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
    }

    public boolean isAvailable() {
        return SpeechRecognizer.isRecognitionAvailable(context);
    }

    public void start() {
        if (running || !isAvailable()) return;
        running = true;
        recognizer = SpeechRecognizer.createSpeechRecognizer(context);
        recognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(android.os.Bundle params) { notifyListening(true); }
            @Override public void onBeginningOfSpeech() { notifyListening(true); }
            @Override public void onRmsChanged(float rmsdB) { }
            @Override public void onBufferReceived(byte[] buffer) { }
            @Override public void onEndOfSpeech() { notifyListening(false); }
            @Override public void onError(int error) { notifyListening(false); restartIfRunning(); }
            @Override public void onResults(android.os.Bundle results) {
                notifyListening(false);
                ArrayList<String> matches = results == null ? null :
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {
                    for (String text : matches) {
                        if (containsWakeWord(text)) {
                            listener.onWakeWordDetected();
                            break;
                        }
                    }
                }
                restartIfRunning();
            }
            @Override public void onPartialResults(android.os.Bundle partialResults) { }
            @Override public void onEvent(int eventType, android.os.Bundle params) { }
        });
        listenOnce();
    }

    private void listenOnce() {
        if (!running || recognizer == null) return;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        try { recognizer.startListening(intent); }
        catch (Exception ignored) { restartIfRunning(); }
    }

    private boolean containsWakeWord(String value) {
        if (value == null) return false;
        String normalized = value.toLowerCase(Locale.ROOT).trim();
        return normalized.equals("jay") || normalized.startsWith("jay ") ||
                normalized.contains(" jay ") || normalized.endsWith(" jay");
    }

    private void restartIfRunning() {
        if (!running) return;
        new android.os.Handler(context.getMainLooper()).postDelayed(this::listenOnce, 350);
    }

    private void notifyListening(boolean value) {
        if (listener != null) listener.onListeningChanged(value);
    }

    public void stop() {
        running = false;
        notifyListening(false);
        if (recognizer != null) {
            try { recognizer.cancel(); } catch (Exception ignored) { }
            try { recognizer.destroy(); } catch (Exception ignored) { }
            recognizer = null;
        }
    }
}
