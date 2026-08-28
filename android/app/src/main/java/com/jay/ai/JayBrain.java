package com.jay.ai;

import android.content.Context;
import java.util.Locale;

public class JayBrain {
    private final JayDatabase database;
    private final JayApiClient apiClient;
    private final JayLocalCommandManager localCommands;
    private final JayLearningManager learningManager;
    private final JayVlcController musicController;

    public JayBrain(Context context) {
        this.database = new JayDatabase(context);
        this.apiClient = new JayApiClient();
        this.localCommands = new JayLocalCommandManager(context);
        this.learningManager = new JayLearningManager(context);
        this.musicController = new JayVlcController(context);
    }

    public String think(String input) {
        if (input == null || input.trim().isEmpty()) return "I'm here. Tell me what you would like me to do.";
        if (learningManager.learnFromCommand(input)) return learningManager.learningResponse();
        String math = JayMathEngine.trySolve(input);
        if (math != null) return math;

        String text = input.trim().toLowerCase(Locale.ROOT);
        // Handle media commands locally before any online fallback. This keeps
        // playback controls working when the phone has no internet connection.
        if (text.contains("vlc") && (text.contains("play") || text.contains("open"))) {
            String vlc = musicController.openVlc();
            if (!vlc.startsWith("VLC is not installed")) return vlc;
        }
        JayMusicCommandMatcher.Action musicAction = JayMusicCommandMatcher.match(text);
        if (musicAction != JayMusicCommandMatcher.Action.NONE) {
            String media = musicController.mediaCommand(musicAction);
            if (media != null && !media.trim().isEmpty()) {
                learningManager.observeCommand(input);
                return media;
            }
        }

        // Understand natural language first, then hand the safe canonical action to the
        // existing local executor. Understanding itself never performs a device action.
        JayCommandUnderstanding.Result understood = JayCommandUnderstanding.understand(input);
        String canonical = canonicalCommand(understood);
        if (canonical != null) {
            String local = localCommands.handle(canonical);
            if (local != null && !local.trim().isEmpty()) {
                learningManager.observeCommand(input);
                return local;
            }
        }

        String local = localCommands.handle(input);
        if (local != null && !local.trim().isEmpty()) { learningManager.observeCommand(input); return local; }
        if (containsAny(text,"repeat","again","rudia","tena")) return "REPEAT_LAST";
        if (containsAny(text,"hello","hi","hey","habari","mambo","niaje","sasa")) return "Hello. Jay is ready.";
        if (containsAny(text,"who are you","what are you","wewe ni nani","jay ni nani")) return "I'm Jay, your personal AI assistant.";
        if (containsAny(text,"what can you do","what do you do","explain what you can do","unaweza kufanya nini")) return "I can work locally with supported phone actions, local memory and conversation features. For broader questions I can use Jay's online brain when available.";
        if (containsAny(text,"weather","what's the weather","what is the weather","hali ya hewa")) return "WEATHER_REQUIRED";
        if (containsAny(text,"change voice","change your voice","different voice","voice change","badilisha sauti","siri voice","siri-like voice")) return "My voice profile is set to a calm, deep male style with controlled speed and a warm futuristic tone. I can use the male voices installed in your Android text-to-speech engine; I cannot reproduce a proprietary voice exactly.";
        if (containsAny(text,"what can you see","what do you see","tell me what you see","unaona nini")) return "Live camera vision analysis is not connected yet.";
        if (containsAny(text,"are you online","are you still online","online status","upo online")) return "ONLINE_STATUS";
        if (containsAny(text,"what did i tell you","what did i ask","unakumbuka nini","who am i","who i am")) { String memory=database.getMemory("last_request"); return memory==null?"I don't have your name saved yet. Tell me: remember my name is ...":"The saved memory says: "+memory; }
        if (containsAny(text,"what have you learned","what did you learn","what do you know about me","umejifunza nini kuhusu mimi")) return "I learn from information and patterns you choose to let Jay remember, and keep those memories locally.";
        if (containsAny(text,"open settings","fungua settings","fungua mipangilio")) return "OPEN_SETTINGS";
        if (containsAny(text,"open phone","open dialer","fungua simu","fungua dialer")) return "OPEN_PHONE";
        if (containsAny(text,"open calendar","fungua calendar","fungua kalenda","kalenda")) return "OPEN_CALENDAR";
        if (containsAny(text,"inventory","stock","parts","bidhaa","spares")) return "OPEN_INVENTORY";
        if (containsAny(text,"repair","repairs","repair job","matengenezo","phone repair","karabati")) return "OPEN_REPAIRS";
        if (containsAny(text,"kiswahili","swahili","sheng")) return "I understand English, Kiswahili and Sheng.";
        return "ONLINE_REQUIRED";
    }

    private String canonicalCommand(JayCommandUnderstanding.Result r) {
        if (r == null) return null;
        switch (r.intent) {
            case OPEN_SETTINGS:
                if (r.steps.isEmpty()) return "open settings";
                return "open settings";
            case OPEN_APP:
                return "open " + r.target;
            case PLAY_MUSIC:
                return r.target == null || r.target.isEmpty() ? "play music" : "play " + r.target;
            case STOP_MUSIC:
                return "stop music";
            case PAUSE_MUSIC:
                return "pause music";
            case RESUME_MUSIC:
                return "resume music";
            case NEXT_MUSIC:
                return "next music";
            case PREVIOUS_MUSIC:
                return "previous music";
            case CALL_CONTACT:
                return "call " + r.target;
            case SEND_MESSAGE:
                return "whatsapp text " + r.target + " saying " + r.message;
            case SET_ALARM:
                return "set alarm for " + r.target;
            case CANCEL_ALARM:
                return r.target.isEmpty() ? "cancel alarm" : "cancel alarm for " + r.target;
            case NETWORK_STATUS:
                return "network status";
            case DEVICE_NAVIGATION:
                return "open " + r.target;
            case CLEAR_CHAT:
            case CHECK_UPDATE:
            case MPESA_BALANCE:
            case UNKNOWN:
            default:
                return null;
        }
    }

    public void askOnline(String message, JayApiClient.Callback callback) { if(message==null||message.trim().isEmpty()){callback.onError("Empty message.");return;}apiClient.chat(message.trim(),callback); }
    private boolean containsAny(String text,String...words){for(String word:words)if(text.contains(word))return true;return false;}
}