package com.jay.ai;
import android.content.Context;
import java.util.Locale;

public class JayBrain {
    private final JayDatabase database;
    private final JayApiClient apiClient;
    private final JayLocalCommandManager localCommands;

    public JayBrain(Context context) {
        this.database = new JayDatabase(context);
        this.apiClient = new JayApiClient();
        this.localCommands = new JayLocalCommandManager(context);
    }

    public String think(String input) {
        if (input == null || input.trim().isEmpty()) return "I'm here. Tell me what you would like me to do, Sir.";

        String math = JayMathEngine.trySolve(input);
        if (math != null) return math;

        String local = localCommands.handle(input);
        if (local != null && !local.trim().isEmpty()) return local;

        String text = input.trim().toLowerCase(Locale.ROOT);

        if (containsAny(text, "hello", "hi", "hey", "habari", "mambo", "niaje", "sasa"))
            return "Hello, Sir. Jay is ready.";
        if (containsAny(text, "who are you", "what are you", "wewe ni nani", "jay ni nani"))
            return "I'm Jay, your personal AI assistant, Sir.";
        if (containsAny(text, "what can you do", "what can u do", "what do you do", "explain what you can do", "what are your capabilities", "unaweza kufanya nini"))
            return "Sir, I can work locally with supported phone actions, local memory and conversation features. I can open installed apps, calculate mathematics, check time, battery and device information, open the camera, and call contacts when Android permissions are granted. For broader questions I can use Jay's online brain when it is available.";
        if (containsAny(text, "weather", "what's the weather", "what is the weather", "hali ya hewa")) return "WEATHER_REQUIRED";
        if (containsAny(text, "change voice", "change your voice", "different voice", "voice change", "badilisha sauti")) return "Sir, Jay's voice is currently male, low and calm. A voice selector is not connected yet, so I won't pretend that I changed it.";
        if (containsAny(text, "what can you see", "what do you see", "tell me what you see", "what are you seeing", "look at this", "unaona nini", "niambie unaona nini")) return "Sir, I can open the camera locally, but live camera vision analysis is not connected yet. I won't pretend that I can see an image when I cannot.";
        if (containsAny(text, "change background", "change the background", "background", "badilisha background")) return "Sir, the background editor is not connected yet. I won't pretend that I changed it.";
        if (containsAny(text, "are you online", "are you still online", "online status", "upo online")) return "ONLINE_STATUS";
        if (containsAny(text, "remember", "kumbuka", "save this", "hifadhi hii")) { database.saveMemory("last_request", input); return "Sure, Sir. I've saved that in my local memory."; }
        if (containsAny(text, "what did i tell you", "what did i ask", "unakumbuka nini")) { String memory = database.getMemory("last_request"); return memory == null ? "I don't have anything saved in that memory yet, Sir." : "I remember you said: " + memory; }
        if (containsAny(text, "open settings", "fungua settings", "fungua mipangilio")) return "OPEN_SETTINGS";
        if (containsAny(text, "open phone", "open dialer", "fungua simu", "fungua dialer")) return "OPEN_PHONE";
        if (containsAny(text, "open calendar", "fungua calendar", "fungua kalenda", "kalenda")) return "OPEN_CALENDAR";
        if (containsAny(text, "inventory", "stock", "parts", "bidhaa", "spares")) return "OPEN_INVENTORY";
        if (containsAny(text, "repair", "repairs", "repair job", "matengenezo", "phone repair", "karabati")) return "OPEN_REPAIRS";
        if (containsAny(text, "kiswahili", "swahili", "sheng")) return "I understand English, Kiswahili and Sheng, Sir.";
        return "ONLINE_REQUIRED";
    }

    public void askOnline(String message, JayApiClient.Callback callback) {
        if (message == null || message.trim().isEmpty()) { callback.onError("Empty message."); return; }
        apiClient.chat(message.trim(), callback);
    }
    private boolean containsAny(String text, String... words) { for (String word : words) if (text.contains(word)) return true; return false; }
}
