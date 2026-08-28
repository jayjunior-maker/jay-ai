package com.jay.ai;

import android.content.Context;
import java.util.Locale;

public class JayBrain {
    private final JayDatabase database;
    private final JayApiClient apiClient;
    private final JayLocalCommandManager localCommands;
    private final JayLearningManager learningManager;

    public JayBrain(Context context) {
        this.database = new JayDatabase(context);
        this.apiClient = new JayApiClient();
        this.localCommands = new JayLocalCommandManager(context);
        this.learningManager = new JayLearningManager(context);
    }

    public String think(String input) {
        if (input == null || input.trim().isEmpty()) return "I'm here. Tell me what you would like me to do, Sir.";
        if (learningManager.learnFromCommand(input)) return learningManager.learningResponse();
        String math = JayMathEngine.trySolve(input);
        if (math != null) return math;
        String local = localCommands.handle(input);
        if (local != null && !local.trim().isEmpty()) { learningManager.observeCommand(input); return local; }
        String text = input.trim().toLowerCase(Locale.ROOT);
        if (containsAny(text,"hello","hi","hey","habari","mambo","niaje","sasa")) return "Hello, Sir. Jay is ready.";
        if (containsAny(text,"who are you","what are you","wewe ni nani","jay ni nani")) return "I'm Jay, your personal AI assistant, Sir.";
        if (containsAny(text,"what can you do","what do you do","explain what you can do","unaweza kufanya nini")) return "Sir, I can work locally with supported phone actions, local memory and conversation features. For broader questions I can use Jay's online brain when available.";
        if (containsAny(text,"weather","what's the weather","what is the weather","hali ya hewa")) return "WEATHER_REQUIRED";
        if (containsAny(text,"change voice","change your voice","different voice","voice change","badilisha sauti")) return "Sir, Jay's voice is currently male, low and calm. A voice selector is not connected yet.";
        if (containsAny(text,"what can you see","what do you see","tell me what you see","unaona nini")) return "Sir, live camera vision analysis is not connected yet.";
        if (containsAny(text,"are you online","are you still online","online status","upo online")) return "ONLINE_STATUS";
        if (containsAny(text,"what did i tell you","what did i ask","unakumbuka nini")) { String memory=database.getMemory("last_request"); return memory==null?"I don't have anything saved in that memory yet, Sir.":"I remember you said: "+memory; }
        if (containsAny(text,"what have you learned","what did you learn","what do you know about me","umejifunza nini kuhusu mimi")) return "Sir, I learn from information and patterns you choose to let Jay remember, and keep those memories locally.";
        if (containsAny(text,"open settings","fungua settings","fungua mipangilio")) return "OPEN_SETTINGS";
        if (containsAny(text,"open phone","open dialer","fungua simu","fungua dialer")) return "OPEN_PHONE";
        if (containsAny(text,"open calendar","fungua calendar","fungua kalenda","kalenda")) return "OPEN_CALENDAR";
        if (containsAny(text,"inventory","stock","parts","bidhaa","spares")) return "OPEN_INVENTORY";
        if (containsAny(text,"repair","repairs","repair job","matengenezo","phone repair","karabati")) return "OPEN_REPAIRS";
        if (containsAny(text,"kiswahili","swahili","sheng")) return "I understand English, Kiswahili and Sheng, Sir.";
        return "ONLINE_REQUIRED";
    }

    public void askOnline(String message, JayApiClient.Callback callback) {
        if (message == null || message.trim().isEmpty()) { callback.onError("Empty message."); return; }
        apiClient.chat(message.trim(), callback);
    }
    private boolean containsAny(String text,String...words){for(String word:words)if(text.contains(word))return true;return false;}
}
