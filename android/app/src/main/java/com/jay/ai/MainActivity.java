package com.jay.ai;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    private static final int MICROPHONE_PERMISSION_REQUEST = 100;
    private TextToSpeech jayVoice;
    private SpeechRecognizer speechRecognizer;
    private JayBrain jayBrain;
    private JayDatabase jayDatabase;
    private JayConversationManager jayConversationManager;
    private TextView jayStatus;
    private TextView conversation;
    private EditText inputBox;
    private LinearLayout rootView;
    private boolean voiceReady = false;
    private final float jayPitch = 0.65f;
    private final float jaySpeed = 0.90f;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        jayDatabase = new JayDatabase(this);
        jayBrain = new JayBrain(this);
        jayConversationManager = new JayConversationManager();
        jayVoice = new TextToSpeech(this, this);
        buildJayInterface();
        setupSpeechRecognizer();
        announceStartupStatus();
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this::startFirstLaunchPermissionSetup, 900);
    }

    private void startFirstLaunchPermissionSetup() {
        android.content.SharedPreferences prefs = getSharedPreferences("jay_setup", MODE_PRIVATE);
        if (prefs.getBoolean("completed", false)) return;
        showPermissionWizard();
    }

    private void showPermissionWizard() {
        final String[] names = {"Microphone", "Camera", "Contacts", "Phone calls", "Location", "M-Pesa/SMS (optional)"};
        final String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_SMS};
        final int[] codes = {JayPermissionManager.REQUEST_MICROPHONE, JayPermissionManager.REQUEST_CAMERA, JayPermissionManager.REQUEST_CONTACTS, JayPermissionManager.REQUEST_PHONE, JayPermissionManager.REQUEST_LOCATION, JayPermissionManager.REQUEST_SMS};
        new android.app.AlertDialog.Builder(this).setTitle("Welcome to Jay, Sir")
                .setMessage("Before Jay becomes fully active, let's set up the permissions needed for your device features. SMS access is optional and is used for M-Pesa messages.")
                .setPositiveButton("Start setup", (d, w) -> requestNextPermission(names, permissions, codes, 0))
                .setNegativeButton("Skip for now", (d, w) -> getSharedPreferences("jay_setup", MODE_PRIVATE).edit().putBoolean("completed", true).apply())
                .show();
    }

    private void requestNextPermission(String[] names, String[] permissions, int[] codes, int index) {
        if (index >= permissions.length) {
            getSharedPreferences("jay_setup", MODE_PRIVATE).edit().putBoolean("completed", true).apply();
            Toast.makeText(this, "Jay setup complete, Sir.", Toast.LENGTH_SHORT).show();
            return;
        }
        String permission = permissions[index];
        if (JayPermissionManager.has(this, permission)) { requestNextPermission(names, permissions, codes, index + 1); return; }
        new android.app.AlertDialog.Builder(this).setTitle(names[index])
                .setMessage(index == 5 ? "Allow Jay to read SMS messages so it can find M-Pesa balance notifications. This is optional." : "Jay needs this permission for the corresponding device feature.")
                .setPositiveButton("Allow", (d, w) -> requestPermissions(new String[]{permission}, codes[index]))
                .setNegativeButton("Skip", (d, w) -> requestNextPermission(names, permissions, codes, index + 1)).show();
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MICROPHONE_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { startListening(); return; }
        int next = -1;
        if (requestCode == JayPermissionManager.REQUEST_MICROPHONE) next = 1;
        else if (requestCode == JayPermissionManager.REQUEST_CAMERA) next = 2;
        else if (requestCode == JayPermissionManager.REQUEST_CONTACTS) next = 3;
        else if (requestCode == JayPermissionManager.REQUEST_PHONE) next = 4;
        else if (requestCode == JayPermissionManager.REQUEST_LOCATION) next = 5;
        else if (requestCode == JayPermissionManager.REQUEST_SMS) { getSharedPreferences("jay_setup", MODE_PRIVATE).edit().putBoolean("completed", true).apply(); return; }
        if (next >= 0) continueSetupAfterResult(next);
    }

    private void continueSetupAfterResult(int index) {
        String[] names = {"Microphone", "Camera", "Contacts", "Phone calls", "Location", "M-Pesa/SMS (optional)"};
        String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_SMS};
        int[] codes = {JayPermissionManager.REQUEST_MICROPHONE, JayPermissionManager.REQUEST_CAMERA, JayPermissionManager.REQUEST_CONTACTS, JayPermissionManager.REQUEST_PHONE, JayPermissionManager.REQUEST_LOCATION, JayPermissionManager.REQUEST_SMS};
        requestNextPermission(names, permissions, codes, index);
    }

    private boolean isNetworkAvailable() { try { android.net.ConnectivityManager cm=(android.net.ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE); if(cm==null)return false; android.net.Network n=cm.getActiveNetwork(); if(n==null)return false; android.net.NetworkCapabilities c=cm.getNetworkCapabilities(n); return c!=null&&c.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET); } catch(Exception e){return false;} }
    private void updateConnectivityStatus(boolean announce) { boolean online=isNetworkAvailable(); if(jayStatus!=null)jayStatus.setText(online?"● JAY ONLINE":"● JAY OFFLINE"); if(announce&&jayVoice!=null&&voiceReady)speak(jayConversationManager.getStartupGreeting(online)); }
    private void announceStartupStatus(){updateConnectivityStatus(false); new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(()->{boolean online=isNetworkAvailable();updateConnectivityStatus(false);if(voiceReady)speak(jayConversationManager.getStartupGreeting(online));},700);}

    private void buildJayInterface(){
        rootView=new LinearLayout(this);rootView.setOrientation(LinearLayout.VERTICAL);rootView.setPadding(25,25,25,25);applySavedAppearance();
        LinearLayout topBar=new LinearLayout(this);topBar.setOrientation(LinearLayout.HORIZONTAL);topBar.setGravity(Gravity.CENTER_VERTICAL);TextView title=new TextView(this);title.setText("JAY");title.setTextColor(Color.WHITE);title.setTextSize(28);title.setTypeface(null,1);topBar.addView(title,new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1));Button settingsButton=new Button(this);settingsButton.setText("⚙");settingsButton.setTextSize(18);settingsButton.setOnClickListener(v->showSettings());topBar.addView(settingsButton);rootView.addView(topBar);
        jayStatus=new TextView(this);updateConnectivityStatus(false);jayStatus.setTextColor(Color.WHITE);jayStatus.setTextSize(15);jayStatus.setGravity(Gravity.CENTER);jayStatus.setPadding(10,25,10,25);rootView.addView(jayStatus);
        TextView jayCore=new TextView(this);jayCore.setText("J");jayCore.setTextColor(Color.WHITE);jayCore.setTextSize(80);jayCore.setGravity(Gravity.CENTER);GradientDrawable coreBackground=new GradientDrawable();coreBackground.setShape(GradientDrawable.OVAL);coreBackground.setStroke(4,Color.rgb(100,180,255));coreBackground.setColor(Color.rgb(15,15,45));jayCore.setBackground(coreBackground);LinearLayout.LayoutParams coreParams=new LinearLayout.LayoutParams(220,220);coreParams.gravity=Gravity.CENTER;rootView.addView(jayCore,coreParams);
        ScrollView scrollView=new ScrollView(this);conversation=new TextView(this);conversation.setText("Jay: "+jayConversationManager.getStartupGreeting(isNetworkAvailable())+"\n\n");conversation.setTextColor(Color.WHITE);conversation.setTextSize(16);conversation.setPadding(20,20,20,20);scrollView.addView(conversation);rootView.addView(scrollView,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1));
        inputBox=new EditText(this);inputBox.setHint("Talk to Jay...");inputBox.setHintTextColor(Color.LTGRAY);inputBox.setTextColor(Color.WHITE);inputBox.setSingleLine(true);rootView.addView(inputBox);
        LinearLayout buttons=new LinearLayout(this);buttons.setOrientation(LinearLayout.HORIZONTAL);buttons.setGravity(Gravity.CENTER);Button talkButton=new Button(this);talkButton.setText("🎙 TALK");talkButton.setOnClickListener(v->startListening());Button sendButton=new Button(this);sendButton.setText("SEND");sendButton.setOnClickListener(v->{String message=inputBox.getText().toString().trim();if(!message.isEmpty()){processMessage(message);inputBox.setText("");}});LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);buttons.addView(talkButton,bp);buttons.addView(sendButton,bp);rootView.addView(buttons);setContentView(rootView);
    }

    private void applySavedAppearance(){
        if(rootView==null)return;
        int color=JayAppearanceManager.getBackgroundColor(this);
        rootView.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{color,darken(color),darken(darken(color))}));
    }

    private int darken(int color){return Color.rgb((int)(Color.red(color)*0.65f),(int)(Color.green(color)*0.65f),(int)(Color.blue(color)*0.65f));}

    private boolean handleAppearanceCommand(String message){
        String lower=message.toLowerCase(Locale.ROOT).trim();
        if(lower.contains("change wallpaper")||lower.contains("change the wallpaper")||lower.equals("wallpaper")){
            try{Intent intent=new Intent(Intent.ACTION_SET_WALLPAPER);startActivity(Intent.createChooser(intent,"Choose wallpaper"));reply("Opening the wallpaper selector, Sir.");}catch(Exception e){reply("Sir, I couldn't open the wallpaper selector.");}return true;
        }
        String[] colors={"black","blue","purple","red","green","cyan","dark","default"};
        for(String color:colors){
            if(lower.contains("background")&&lower.contains(color)){
                if(JayAppearanceManager.setBackgroundColor(this,color)){applySavedAppearance();reply("Background changed to "+color+", Sir.");}else reply("Sir, I couldn't change the background to "+color+".");
                return true;
            }
        }
        if(lower.contains("background")&&lower.contains("#")){
            int hash=lower.indexOf('#');String value=lower.substring(hash).split("\\s")[0];
            if(JayAppearanceManager.setBackgroundColor(this,value)){applySavedAppearance();reply("Background color changed, Sir.");}else reply("Sir, that background color isn't valid.");
            return true;
        }
        if(lower.contains("restore")&&lower.contains("background")){JayAppearanceManager.reset(this);applySavedAppearance();reply("Default Jay background restored, Sir.");return true;}
        return false;
    }

    private void setupSpeechRecognizer(){if(!SpeechRecognizer.isRecognitionAvailable(this)){Toast.makeText(this,"Speech recognition is not available.",Toast.LENGTH_LONG).show();return;}speechRecognizer=SpeechRecognizer.createSpeechRecognizer(this);speechRecognizer.setRecognitionListener(new RecognitionListener(){public void onReadyForSpeech(Bundle b){updateStatus("● Listening...");}public void onBeginningOfSpeech(){updateStatus("● Jay is listening...");}public void onRmsChanged(float r){}public void onBufferReceived(byte[] b){}public void onEndOfSpeech(){updateStatus("● Processing...");}public void onError(int e){updateConnectivityStatus(false);}public void onResults(Bundle r){ArrayList<String>m=r.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);if(m!=null&&!m.isEmpty()){String t=m.get(0);inputBox.setText(t);processMessage(t);inputBox.setText("");}updateConnectivityStatus(false);}public void onPartialResults(Bundle b){}public void onEvent(int e,Bundle b){}});}
    private void startListening(){if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},MICROPHONE_PERMISSION_REQUEST);return;}if(speechRecognizer==null){setupSpeechRecognizer();if(speechRecognizer==null)return;}Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault());i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Talk to Jay");try{speechRecognizer.startListening(i);}catch(Exception e){Toast.makeText(this,"Jay could not start listening.",Toast.LENGTH_SHORT).show();}}
    private void processMessage(String message){if(message==null||message.trim().isEmpty())return;message=message.trim();addConversation("You: "+message);final String userMessage=message;if(handleAppearanceCommand(message))return;String response;try{response=jayBrain.think(message);}catch(Exception e){response="I encountered a local processing error, Sir.";}if(response==null||response.trim().isEmpty())response=jayConversationManager.friendlyUnknown();if(response.equals("ONLINE_STATUS")){reply(isNetworkAvailable()?"Yes, Sir. This phone has an internet connection. My online brain may still be unavailable if the server is not reachable.":"No, Sir. This phone is currently offline. My local systems are still available.");return;}if(response.equals("WEATHER_REQUIRED")){updateStatus("● Checking weather...");jayBrain.askOnline(userMessage,new JayApiClient.Callback(){public void onSuccess(String a){runOnUiThread(()->{updateConnectivityStatus(false);reply(a);});}public void onError(String e){runOnUiThread(()->reply("Sir, I can't retrieve live weather right now because Jay's online service is unavailable. Your local systems are still working."));}});return;}if(response.equals("ONLINE_REQUIRED")){updateStatus("● Connecting to online brain...");jayBrain.askOnline(userMessage,new JayApiClient.Callback(){public void onSuccess(String a){runOnUiThread(()->{updateConnectivityStatus(false);reply(a);jayDatabase.saveConversation(userMessage,a);});}public void onError(String e){runOnUiThread(()->{updateStatus("● Online brain unavailable");reply("Sir, my online brain is unavailable right now. Your local Jay systems are still working.");});}});return;}handleBrainResponse(userMessage,response);}
    private void handleBrainResponse(String userMessage,String response){if(response.equals("OPEN_SETTINGS")){openSettings();return;}if(response.equals("OPEN_PHONE")){openPhone();return;}if(response.equals("OPEN_CALENDAR")){openCalendar();return;}if(response.equals("OPEN_INVENTORY")){reply("The inventory module is being prepared, Sir.");return;}if(response.equals("OPEN_REPAIRS")){reply("The repairs module is being prepared, Sir.");return;}reply(response);jayDatabase.saveConversation(userMessage,response);}
    private void reply(String response){if(response!=null&&!response.trim().isEmpty()){addConversation("Jay: "+response);speak(response);}}
    private void openSettings(){try{startActivity(new Intent(Settings.ACTION_SETTINGS));}catch(Exception e){Toast.makeText(this,"Unable to open Settings.",Toast.LENGTH_SHORT).show();}}
    private void openPhone(){try{startActivity(new Intent(Intent.ACTION_DIAL));}catch(Exception e){Toast.makeText(this,"Unable to open Phone.",Toast.LENGTH_SHORT).show();}}
    private void openCalendar(){try{Intent i=new Intent(Intent.ACTION_VIEW);i.setData(CalendarContract.CONTENT_URI);startActivity(i);}catch(Exception e){Toast.makeText(this,"Unable to open Calendar.",Toast.LENGTH_SHORT).show();}}
    private void showDeviceInfo(){try{JayDeviceInfo d=new JayDeviceInfo(this);new android.app.AlertDialog.Builder(this).setTitle("Jay Device Information").setMessage(d.getDetailedDeviceInfo()).setPositiveButton("OK",null).show();}catch(Exception e){Toast.makeText(this,"Unable to read device information.",Toast.LENGTH_LONG).show();}}
    private void showSettings(){String[]o={"Jay Device Information","Android Settings","Cancel"};new android.app.AlertDialog.Builder(this).setTitle("Jay Settings").setItems(o,(d,w)->{if(w==0)showDeviceInfo();else if(w==1)openSettings();}).show();}
    private void addConversation(String t){if(conversation!=null)conversation.append(t+"\n\n");}private void updateStatus(String s){runOnUiThread(()->{if(jayStatus!=null)jayStatus.setText(s);});}
    @Override public void onInit(int status){if(status==TextToSpeech.SUCCESS){voiceReady=true;jayVoice.setLanguage(Locale.US);selectPreferredJayVoice();jayVoice.setPitch(jayPitch);jayVoice.setSpeechRate(jaySpeed);}else voiceReady=false;}
    private void selectPreferredJayVoice(){try{List<Voice>voices=new ArrayList<>(jayVoice.getVoices());Voice fallback=null;for(Voice v:voices){Locale l=v.getLocale();if(l==null||!"en".equalsIgnoreCase(l.getLanguage()))continue;if(fallback==null)fallback=v;String f=v.getFeatures()==null?"":v.getFeatures().toString().toLowerCase(Locale.ROOT);String n=v.getName()==null?"":v.getName().toLowerCase(Locale.ROOT);if(f.contains("gender=male")||f.contains("gender:male")||n.contains("male")){jayVoice.setVoice(v);return;}}if(fallback!=null)jayVoice.setVoice(fallback);}catch(Exception ignored){}}
    private void speak(String t){if(voiceReady&&jayVoice!=null&&t!=null&&!t.trim().isEmpty())jayVoice.speak(t,TextToSpeech.QUEUE_FLUSH,null,"JAY_RESPONSE");}
    @Override protected void onDestroy(){if(speechRecognizer!=null){try{speechRecognizer.cancel();}catch(Exception ignored){}speechRecognizer.destroy();speechRecognizer=null;}if(jayVoice!=null){try{jayVoice.stop();jayVoice.shutdown();}catch(Exception ignored){}jayVoice=null;}super.onDestroy();}
}
