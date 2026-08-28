package com.jay.ai;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;
import java.util.Locale;

/** Continuously listens for the local wake word "Jay" and restarts after recognizer timeouts/errors. */
public final class JayWakeWordManager {
    public interface Listener {
        void onWakeWordDetected();
        void onListeningChanged(boolean listening);
    }

    public static final String ACTION_WAKE_WORD = "com.jay.ai.ACTION_WAKE_WORD";
    private final Context context;
    private final Listener listener;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SpeechRecognizer recognizer;
    private boolean running;
    private boolean restarting;

    public JayWakeWordManager(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
    }

    public boolean isAvailable() { return SpeechRecognizer.isRecognitionAvailable(context); }

    public void start() {
        if (running || !isAvailable()) return;
        running = true;
        createRecognizer();
        listenOnce();
    }

    private void createRecognizer() {
        destroyRecognizer();
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
                ArrayList<String> matches = results == null ? null : results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {
                    for (String text : matches) {
                        if (containsWakeWord(text)) {
                            listener.onWakeWordDetected();
                            broadcastWakeWord();
                            break;
                        }
                    }
                }
                restartIfRunning();
            }
            @Override public void onPartialResults(android.os.Bundle partialResults) {
                if (partialResults == null) return;
                ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) for (String text : matches) if (containsWakeWord(text)) { listener.onWakeWordDetected(); broadcastWakeWord(); break; }
            }
            @Override public void onEvent(int eventType, android.os.Bundle params) { }
        });
    }

    private void listenOnce() {
        if (!running || recognizer == null) return;
        restarting = false;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        try { recognizer.startListening(intent); }
        catch (Exception ignored) { restartIfRunning(); }
    }

    private void restartIfRunning() {
        if (!running || restarting) return;
        restarting = true;
        handler.postDelayed(() -> { if (running) { createRecognizer(); listenOnce(); } }, 350);
    }

    private boolean containsWakeWord(String value) {
        if (value == null) return false;
        String normalized = value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ").trim();
        String[] words = normalized.split("\\s+");
        for (String word : words) if (word.equals("jay")) return true;
        return false;
    }

    private void broadcastWakeWord() {
        Intent intent = new Intent(ACTION_WAKE_WORD);
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);
    }

    private void notifyListening(boolean value) { if (listener != null) listener.onListeningChanged(value); }

    public void stop() {
        running = false;
        notifyListening(false);
        handler.removeCallbacksAndMessages(null);
        destroyRecognizer();
    }

    private void destroyRecognizer() {
        if (recognizer != null) {
            try { recognizer.cancel(); } catch (Exception ignored) { }
            try { recognizer.destroy(); } catch (Exception ignored) { }
            recognizer = null;
        }
    }
}
