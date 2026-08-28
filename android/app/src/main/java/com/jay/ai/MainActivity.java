package com.jay.ai;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.view.View;
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
    private TextView jayStatus, conversation;
    private EditText inputBox;
    private LinearLayout rootView;
    private boolean voiceReady = false;
    private String lastJayResponse = "";
    private final float jayPitch = 0.65f;
    private final float jaySpeed = 0.90f;
    private final int BG = Color.rgb(2, 7, 20);
    private final int PANEL = Color.rgb(7, 17, 37);
    private final int BLUE = Color.rgb(30, 145, 255);
    private final int CYAN = Color.rgb(0, 220, 255);

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

    private TextView makeLabel(String text,float size,int color){TextView v=new TextView(this);v.setText(text);v.setTextColor(color);v.setTextSize(size);return v;}
    private Button makeAction(String text){Button b=new Button(this);b.setText(text);b.setTextColor(Color.WHITE);return b;}
    private GradientDrawable panel(int stroke,int fill,int radius){GradientDrawable g=new GradientDrawable();g.setColor(fill);g.setCornerRadius(radius);if(stroke!=0)g.setStroke(2,stroke);return g;}
    private void add(LinearLayout p,View v,int l,int t,int r,int b){p.addView(v);LinearLayout.LayoutParams x=(LinearLayout.LayoutParams)v.getLayoutParams();x.setMargins(l,t,r,b);v.setLayoutParams(x);}

    private void buildJayInterface(){
        rootView=new LinearLayout(this);rootView.setOrientation(LinearLayout.VERTICAL);rootView.setPadding(16,10,16,8);rootView.setBackgroundColor(BG);
        ScrollView page=new ScrollView(this);page.setFillViewport(true);LinearLayout content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);

        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);
        Button menu=makeAction("☰");menu.setTextSize(24);menu.setBackground(panel(Color.rgb(20,45,80),Color.rgb(7,16,32),22));menu.setOnClickListener(v->showMainMenu());header.addView(menu,new LinearLayout.LayoutParams(58,58));
        LinearLayout brand=new LinearLayout(this);brand.setOrientation(LinearLayout.VERTICAL);TextView title=makeLabel("JAY",40,Color.WHITE);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);TextView sub=makeLabel("AI ASSISTANT",16,BLUE);brand.addView(title);brand.addView(sub);header.addView(brand,new LinearLayout.LayoutParams(0,68,1));
        Button settings=makeAction("⚙");settings.setTextSize(28);settings.setBackground(panel(Color.rgb(30,85,145),Color.rgb(5,14,30),28));settings.setOnClickListener(v->showSettings());header.addView(settings,new LinearLayout.LayoutParams(70,70));add(content,header,0,4,0,12);

        LinearLayout greeting=new LinearLayout(this);greeting.setOrientation(LinearLayout.VERTICAL);TextView g1=makeLabel("Good morning,",27,Color.LTGRAY);TextView g2=makeLabel("I'm Jay",42,BLUE);g2.setTypeface(Typeface.DEFAULT,Typeface.BOLD);greeting.addView(g1);greeting.addView(g2);add(content,greeting,14,4,0,8);
        LinearLayout secure=new LinearLayout(this);secure.setGravity(Gravity.CENTER_VERTICAL);TextView sh=makeLabel("🛡",28,Color.rgb(0,255,170));secure.addView(sh,new LinearLayout.LayoutParams(48,52));TextView st=makeLabel("PERSONAL MODE\n●  Local-first systems active",14,Color.WHITE);secure.addView(st);add(content,secure,14,0,0,8);

        LinearLayout statusCard=new LinearLayout(this);statusCard.setOrientation(LinearLayout.VERTICAL);statusCard.setPadding(16,10,16,10);statusCard.setBackground(panel(Color.rgb(25,55,100),PANEL,26));jayStatus=makeLabel("● JAY ONLINE\n   Connected",16,Color.rgb(0,255,150));statusCard.addView(jayStatus,new LinearLayout.LayoutParams(-1,58));TextView version=makeLabel("🧠  JAY AI   v2.0.0",15,BLUE);statusCard.addView(version);add(content,statusCard,0,0,0,12);

        LinearLayout coreRow=new LinearLayout(this);coreRow.setGravity(Gravity.CENTER);TextView core=makeLabel("J",78,Color.WHITE);core.setGravity(Gravity.CENTER);core.setTypeface(Typeface.DEFAULT,Typeface.BOLD);GradientDrawable ring=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Color.rgb(4,50,110),Color.rgb(5,14,36)});ring.setShape(GradientDrawable.OVAL);ring.setStroke(5,CYAN);core.setBackground(ring);coreRow.addView(core,new LinearLayout.LayoutParams(225,225));add(content,coreRow,0,2,0,12);

        LinearLayout inputRow=new LinearLayout(this);inputRow.setGravity(Gravity.CENTER_VERTICAL);inputRow.setPadding(14,4,7,4);inputRow.setBackground(panel(BLUE,Color.rgb(5,17,37),45));
        inputBox=new EditText(this);inputBox.setHint("Ask Jay anything...");inputBox.setHintTextColor(Color.rgb(135,155,185));inputBox.setTextColor(Color.WHITE);inputBox.setTextSize(18);inputBox.setSingleLine(true);inputBox.setOnEditorActionListener((v,a,e)->{sendCurrentMessage();return true;});inputRow.addView(inputBox,new LinearLayout.LayoutParams(0,66,1));
        Button mic=makeAction("🎙");mic.setTextSize(23);mic.setBackground(panel(0,Color.rgb(8,25,49),20));mic.setOnClickListener(v->startListening());inputRow.addView(mic,new LinearLayout.LayoutParams(58,60));
        Button send=makeAction("SEND");send.setTextSize(12);send.setTypeface(Typeface.DEFAULT,Typeface.BOLD);send.setBackground(panel(BLUE,Color.rgb(8,25,49),20));send.setOnClickListener(v->sendCurrentMessage());inputRow.addView(send,new LinearLayout.LayoutParams(76,60));
        add(content,inputRow,8,0,8,14);

        Button talk=makeAction("🎙   TALK TO JAY\n        Tap to speak");talk.setTextSize(19);talk.setTypeface(Typeface.DEFAULT,Typeface.BOLD);talk.setGravity(Gravity.CENTER);talk.setBackground(panel(BLUE,Color.rgb(5,20,43),50));talk.setOnClickListener(v->startListening());add(content,talk,8,0,8,14);talk.setMinimumHeight(105);

        LinearLayout cards=new LinearLayout(this);cards.setGravity(Gravity.CENTER);
        cards.addView(feature("💬","CHAT","With Jay",Color.rgb(190,110,255),this::openChatTab),new LinearLayout.LayoutParams(0,145,1));
        cards.addView(feature("🧠","MEMORY","Jay's Memory",Color.rgb(70,255,110),this::openMemoryTab),new LinearLayout.LayoutParams(0,145,1));
        cards.addView(feature("◇","TASKS","Automations",Color.rgb(255,175,30),this::openTasksTab),new LinearLayout.LayoutParams(0,145,1));
        cards.addView(feature("☎","PHONE","Assistant",Color.rgb(255,95,95),this::openPhoneTab),new LinearLayout.LayoutParams(0,145,1));
        cards.addView(feature("▦","APPS","Applications",Color.CYAN,this::openAppsTab),new LinearLayout.LayoutParams(0,145,1));
        add(content,cards,0,0,0,12);

        LinearLayout service=new LinearLayout(this);service.setGravity(Gravity.CENTER_VERTICAL);service.setPadding(14,8,14,8);service.setBackground(panel(Color.rgb(20,50,90),Color.rgb(6,16,33),26));service.setOnClickListener(v->showBackgroundServices());service.addView(makeLabel("🛡",27,Color.rgb(0,255,170)),new LinearLayout.LayoutParams(50,56));LinearLayout ss=new LinearLayout(this);ss.setOrientation(LinearLayout.VERTICAL);ss.addView(makeLabel("Background services are active",15,Color.WHITE));ss.addView(makeLabel("Tap to manage Jay's background service",12,Color.LTGRAY));service.addView(ss,new LinearLayout.LayoutParams(0,60,1));service.addView(makeLabel("MANAGE  ›",14,BLUE),new LinearLayout.LayoutParams(100,60));add(content,service,0,0,0,12);

        ScrollView chat=new ScrollView(this);conversation=makeLabel("Jay: "+jayConversationManager.getStartupGreeting(isNetworkAvailable())+"\n\n",15,Color.LTGRAY);conversation.setPadding(8,5,8,5);chat.addView(conversation);add(content,chat,0,0,0,8);

        LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER);nav.setPadding(4,4,4,4);nav.setBackground(panel(Color.rgb(20,55,100),Color.rgb(6,16,34),30));
        Button home=navButton("⌂\nHome",true);home.setOnClickListener(v->scrollToTop(page));nav.addView(home,new LinearLayout.LayoutParams(0,62,1));
        Button talkNav=navButton("◌\nTalk",false);talkNav.setOnClickListener(v->startListening());nav.addView(talkNav,new LinearLayout.LayoutParams(0,62,1));
        Button memoryNav=navButton("▣\nMemory",false);memoryNav.setOnClickListener(v->openMemoryTab());nav.addView(memoryNav,new LinearLayout.LayoutParams(0,62,1));
        Button settingsNav=navButton("⚙\nSettings",false);settingsNav.setOnClickListener(v->showSettings());nav.addView(settingsNav,new LinearLayout.LayoutParams(0,62,1));add(content,nav,0,0,0,2);
        page.addView(content);rootView.addView(page,new LinearLayout.LayoutParams(-1,0,1));setContentView(rootView);applySavedAppearance();
    }

    private void sendCurrentMessage(){String text=inputBox==null?"":inputBox.getText().toString().trim();if(text.isEmpty()){if(inputBox!=null)inputBox.requestFocus();return;}if(inputBox!=null)inputBox.setText("");processMessage(text);}
    private LinearLayout feature(String icon,String title,String sub,int accent,final Runnable action){LinearLayout b=new LinearLayout(this);b.setOrientation(LinearLayout.VERTICAL);b.setGravity(Gravity.CENTER);b.setPadding(4,5,4,4);b.setBackground(panel(Color.rgb(25,55,100),Color.rgb(7,15,32),24));b.setClickable(true);b.setFocusable(true);b.setOnClickListener(v->action.run());TextView i=makeLabel(icon,24,accent);i.setGravity(Gravity.CENTER);b.addView(i,new LinearLayout.LayoutParams(-1,36));TextView t=makeLabel(title,14,Color.WHITE);t.setGravity(Gravity.CENTER);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.addView(t,new LinearLayout.LayoutParams(-1,27));TextView s=makeLabel(sub,10,Color.LTGRAY);s.setGravity(Gravity.CENTER);b.addView(s,new LinearLayout.LayoutParams(-1,24));return b;}
    private Button navButton(String text,boolean selected){Button b=makeAction(text);b.setTextSize(11);b.setTextColor(selected?BLUE:Color.LTGRAY);b.setBackground(selected?panel(BLUE,Color.rgb(8,28,55),25):panel(0,Color.TRANSPARENT,25));return b;}
    private void scrollToTop(ScrollView page){page.post(()->page.fullScroll(ScrollView.FOCUS_UP));}

    private void openChatTab(){inputBox.requestFocus();((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(inputBox,android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);}
    private void openMemoryTab(){new android.app.AlertDialog.Builder(this).setTitle("Jay Memory").setMessage("Jay's local memory is active.\n\nConversations and memories are stored locally for this Jay installation.\n\nAsk Jay: \"what have you learned about me?\" or \"remember ...\" to use memory.").setPositiveButton("Talk to Jay",(d,w)->openChatTab()).setNegativeButton("Close",null).show();}
    private void openTasksTab(){new android.app.AlertDialog.Builder(this).setTitle("Jay Tasks").setItems(new String[]{"Open Calendar","Create a task with Jay","View task area"},(d,w)->{if(w==0)openCalendar();else if(w==1){inputBox.setText("create a task");openChatTab();}else reply("The Jay task area is ready for task and automation commands.");}).show();}
    private void openPhoneTab(){new android.app.AlertDialog.Builder(this).setTitle("Jay Phone").setItems(new String[]{"Open Phone","Call a contact","Open Contacts"},(d,w)->{if(w==0)openPhone();else if(w==1){inputBox.setText("call ");openChatTab();}else{try{startActivity(new Intent(Intent.ACTION_VIEW,android.provider.ContactsContract.Contacts.CONTENT_URI));}catch(Exception e){reply("I couldn't open Contacts.");}}}).show();}
    private void openAppsTab(){try{Intent i=new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);startActivity(i);}catch(Exception e){try{startActivity(new Intent(Settings.ACTION_SETTINGS));}catch(Exception ignored){reply("I couldn't open the applications screen.");}}}
    private void showBackgroundServices(){new android.app.AlertDialog.Builder(this).setTitle("Jay Background Services").setMessage("Jay's background-service status can be managed from Android settings.\n\nFor continuous listening, Android battery optimization and microphone restrictions may still apply.").setPositiveButton("Open App Settings",(d,w)->{try{Intent i=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);i.setData(android.net.Uri.parse("package:"+getPackageName()));startActivity(i);}catch(Exception e){}}).setNegativeButton("Close",null).show();}
    private void showMainMenu(){new android.app.AlertDialog.Builder(this).setTitle("JAY").setItems(new String[]{"Chat","Memory","Tasks","Phone","Apps","Settings"},(d,w)->{if(w==0)openChatTab();else if(w==1)openMemoryTab();else if(w==2)openTasksTab();else if(w==3)openPhoneTab();else if(w==4)openAppsTab();else showSettings();}).show();}

    private void startFirstLaunchPermissionSetup(){if(getSharedPreferences("jay_setup",MODE_PRIVATE).getBoolean("completed",false))return;showPermissionWizard();}
    private void showPermissionWizard(){final String[]names={"Microphone","Camera","Contacts","Phone calls","Location","M-Pesa/SMS (optional)"};final String[]permissions={Manifest.permission.RECORD_AUDIO,Manifest.permission.CAMERA,Manifest.permission.READ_CONTACTS,Manifest.permission.CALL_PHONE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_SMS};final int[]codes={JayPermissionManager.REQUEST_MICROPHONE,JayPermissionManager.REQUEST_CAMERA,JayPermissionManager.REQUEST_CONTACTS,JayPermissionManager.REQUEST_PHONE,JayPermissionManager.REQUEST_LOCATION,JayPermissionManager.REQUEST_SMS};new android.app.AlertDialog.Builder(this).setTitle("Welcome to Jay").setMessage("Let's set up permissions needed for Jay's device features. SMS access is optional and is used for M-Pesa messages.").setPositiveButton("Start setup",(d,w)->requestNextPermission(names,permissions,codes,0)).setNegativeButton("Skip for now",(d,w)->getSharedPreferences("jay_setup",MODE_PRIVATE).edit().putBoolean("completed",true).apply()).show();}
    private void requestNextPermission(String[]names,String[]permissions,int[]codes,int index){if(index>=permissions.length){getSharedPreferences("jay_setup",MODE_PRIVATE).edit().putBoolean("completed",true).apply();Toast.makeText(this,"Jay setup complete.",Toast.LENGTH_SHORT).show();return;}String permission=permissions[index];if(JayPermissionManager.has(this,permission)){requestNextPermission(names,permissions,codes,index+1);return;}new android.app.AlertDialog.Builder(this).setTitle(names[index]).setMessage(index==5?"Allow Jay to read SMS messages so it can find M-Pesa balance notifications. This is optional.":"Jay needs this permission for the corresponding device feature.").setPositiveButton("Allow",(d,w)->requestPermissions(new String[]{permission},codes[index])).setNegativeButton("Skip",(d,w)->requestNextPermission(names,permissions,codes,index+1)).show();}
    @Override public void onRequestPermissionsResult(int requestCode,String[]permissions,int[]grantResults){super.onRequestPermissionsResult(requestCode,permissions,grantResults);int next=-1;if(requestCode==JayPermissionManager.REQUEST_MICROPHONE)next=1;else if(requestCode==JayPermissionManager.REQUEST_CAMERA)next=2;else if(requestCode==JayPermissionManager.REQUEST_CONTACTS)next=3;else if(requestCode==JayPermissionManager.REQUEST_PHONE)next=4;else if(requestCode==JayPermissionManager.REQUEST_LOCATION)next=5;else if(requestCode==JayPermissionManager.REQUEST_SMS){getSharedPreferences("jay_setup",MODE_PRIVATE).edit().putBoolean("completed",true).apply();return;}if(requestCode==MICROPHONE_PERMISSION_REQUEST&&grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){startListening();return;}if(next>=0)continueSetupAfterResult(next);}
    private void continueSetupAfterResult(int index){String[]n={"Microphone","Camera","Contacts","Phone calls","Location","M-Pesa/SMS (optional)"};String[]p={Manifest.permission.RECORD_AUDIO,Manifest.permission.CAMERA,Manifest.permission.READ_CONTACTS,Manifest.permission.CALL_PHONE,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_SMS};int[]c={JayPermissionManager.REQUEST_MICROPHONE,JayPermissionManager.REQUEST_CAMERA,JayPermissionManager.REQUEST_CONTACTS,JayPermissionManager.REQUEST_PHONE,JayPermissionManager.REQUEST_LOCATION,JayPermissionManager.REQUEST_SMS};requestNextPermission(n,p,c,index);}

    private boolean isNetworkAvailable(){try{android.net.ConnectivityManager cm=(android.net.ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);if(cm==null)return false;android.net.Network n=cm.getActiveNetwork();if(n==null)return false;android.net.NetworkCapabilities c=cm.getNetworkCapabilities(n);return c!=null&&c.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET);}catch(Exception e){return false;}}
    private void updateConnectivityStatus(boolean announce){boolean online=isNetworkAvailable();if(jayStatus!=null)jayStatus.setText(online?"● JAY ONLINE\n   Connected":"● JAY OFFLINE\n   Local systems active");if(announce&&jayVoice!=null&&voiceReady)speak(jayConversationManager.getStartupGreeting(online));}
    private void announceStartupStatus(){updateConnectivityStatus(false);new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(()->{boolean online=isNetworkAvailable();updateConnectivityStatus(false);if(voiceReady)speak(jayConversationManager.getStartupGreeting(online));},700);}

    private boolean handleAppearanceCommand(String message){String lower=message.toLowerCase(Locale.ROOT).trim();if(lower.contains("change wallpaper")||lower.contains("change the wallpaper")||lower.equals("wallpaper")){try{Intent i=new Intent(Intent.ACTION_SET_WALLPAPER);startActivity(Intent.createChooser(i,"Choose wallpaper"));reply("Opening the wallpaper selector.");}catch(Exception e){reply("I couldn't open the wallpaper selector.");}return true;}String[]colors={"black","blue","purple","red","green","cyan","dark","default"};for(String color:colors)if(lower.contains("background")&&lower.contains(color)){if(JayAppearanceManager.setBackgroundColor(this,color)){applySavedAppearance();reply("Background changed to "+color+".");}else reply("I couldn't change the background to "+color+".");return true;}if(lower.contains("background")&&lower.contains("#")){int h=lower.indexOf('#');String value=lower.substring(h).split("\\s")[0];if(JayAppearanceManager.setBackgroundColor(this,value)){applySavedAppearance();reply("Background color changed.");}else reply("That background color isn't valid.");return true;}if(lower.contains("restore")&&lower.contains("background")){JayAppearanceManager.reset(this);applySavedAppearance();reply("Default Jay background restored.");return true;}return false;}
    private void applySavedAppearance(){if(rootView==null)return;int color=JayAppearanceManager.getBackgroundColor(this);rootView.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{color,darken(color),darken(darken(color))}));}
    private int darken(int c){return Color.rgb((int)(Color.red(c)*.65f),(int)(Color.green(c)*.65f),(int)(Color.blue(c)*.65f));}

    private void setupSpeechRecognizer(){if(!SpeechRecognizer.isRecognitionAvailable(this)){Toast.makeText(this,"Speech recognition is not available.",Toast.LENGTH_LONG).show();return;}speechRecognizer=SpeechRecognizer.createSpeechRecognizer(this);speechRecognizer.setRecognitionListener(new RecognitionListener(){public void onReadyForSpeech(Bundle b){updateStatus("● Listening...");}public void onBeginningOfSpeech(){updateStatus("● Jay is listening...");}public void onRmsChanged(float r){}public void onBufferReceived(byte[]b){}public void onEndOfSpeech(){updateStatus("● Processing...");}public void onError(int e){updateConnectivityStatus(false);}public void onResults(Bundle r){ArrayList<String>m=r.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);if(m!=null&&!m.isEmpty()){String t=m.get(0);inputBox.setText(t);processMessage(t);inputBox.setText("");}updateConnectivityStatus(false);}public void onPartialResults(Bundle b){}public void onEvent(int e,Bundle b){}});}
    private void startListening(){if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},MICROPHONE_PERMISSION_REQUEST);return;}if(speechRecognizer==null){setupSpeechRecognizer();if(speechRecognizer==null)return;}Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault());i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Talk to Jay");try{speechRecognizer.startListening(i);}catch(Exception e){Toast.makeText(this,"Jay could not start listening.",Toast.LENGTH_SHORT).show();}}
    private void processMessage(String message){if(message==null||message.trim().isEmpty())return;message=message.trim();addConversation("You: "+message);final String userMessage=message;if(handleAppearanceCommand(message))return;String response;try{response=jayBrain.think(message);}catch(Exception e){response="I encountered a local processing error.";}if(response==null||response.trim().isEmpty())response=jayConversationManager.friendlyUnknown();if(response.equals("ONLINE_STATUS")){reply(isNetworkAvailable()?"Yes. This phone has an internet connection. My online brain may still be unavailable if the server is not reachable.":"No. This phone is currently offline. My local systems are still available.");return;}if(response.equals("WEATHER_REQUIRED")){updateStatus("● Checking weather...");jayBrain.askOnline(userMessage,new JayApiClient.Callback(){public void onSuccess(String a){runOnUiThread(()->{updateConnectivityStatus(false);reply(a);});}public void onError(String e){runOnUiThread(()->reply("I can't retrieve live weather right now because the online service is unavailable."));}});return;}if(response.equals("ONLINE_REQUIRED")){updateStatus("● Connecting to online brain...");jayBrain.askOnline(userMessage,new JayApiClient.Callback(){public void onSuccess(String a){runOnUiThread(()->{updateConnectivityStatus(false);reply(a);jayDatabase.saveConversation(userMessage,a);});}public void onError(String e){runOnUiThread(()->{updateStatus("● Online brain unavailable");reply("My online brain is unavailable right now. My local Jay systems are still working.");});}});return;}handleBrainResponse(userMessage,response);}
    private void handleBrainResponse(String userMessage,String response){if(response.equals("OPEN_SETTINGS")){openSettings();return;}if(response.equals("OPEN_PHONE")){openPhone();return;}if(response.equals("OPEN_CALENDAR")){openCalendar();return;}if(response.equals("OPEN_INVENTORY")){reply("The inventory module is being prepared.");return;}if(response.equals("OPEN_REPAIRS")){reply("The repairs module is being prepared.");return;}if(response.equals("LOCATION_REQUEST")){reply("Location handling is available through the device location service.");return;}reply(response);jayDatabase.saveConversation(userMessage,response);}
    private void reply(String r){if(r!=null&&!r.trim().isEmpty()){lastJayResponse=r;addConversation("Jay: "+r);speak(r);}}
    private void addConversation(String t){if(conversation!=null)conversation.append(t+"\n\n");}
    private void updateStatus(String s){runOnUiThread(()->{if(jayStatus!=null)jayStatus.setText(s);});}
    private void openSettings(){try{startActivity(new Intent(Settings.ACTION_SETTINGS));}catch(Exception e){Toast.makeText(this,"Unable to open Settings.",Toast.LENGTH_SHORT).show();}}
    private void openPhone(){try{startActivity(new Intent(Intent.ACTION_DIAL));}catch(Exception e){Toast.makeText(this,"Unable to open Phone.",Toast.LENGTH_SHORT).show();}}
    private void openCalendar(){try{Intent i=new Intent(Intent.ACTION_VIEW);i.setData(CalendarContract.CONTENT_URI);startActivity(i);}catch(Exception e){Toast.makeText(this,"Unable to open Calendar.",Toast.LENGTH_SHORT).show();}}
    private void showDeviceInfo(){try{JayDeviceInfo d=new JayDeviceInfo(this);new android.app.AlertDialog.Builder(this).setTitle("Jay Device Information").setMessage(d.getDetailedDeviceInfo()).setPositiveButton("OK",null).show();}catch(Exception e){Toast.makeText(this,"Unable to read device information.",Toast.LENGTH_LONG).show();}}
    private void showSettings(){String[]o={"Jay Device Information","Android Settings","Applications","Background Services","Cancel"};new android.app.AlertDialog.Builder(this).setTitle("Jay Settings").setItems(o,(d,w)->{if(w==0)showDeviceInfo();else if(w==1)openSettings();else if(w==2)openAppsTab();else if(w==3)showBackgroundServices();}).show();}
    @Override public void onInit(int status){if(status==TextToSpeech.SUCCESS){voiceReady=true;jayVoice.setLanguage(Locale.US);selectPreferredJayVoice();jayVoice.setPitch(jayPitch);jayVoice.setSpeechRate(jaySpeed);}else voiceReady=false;}
    private void selectPreferredJayVoice(){try{List<Voice>voices=new ArrayList<>(jayVoice.getVoices());Voice fallback=null;for(Voice v:voices){Locale l=v.getLocale();if(l==null||!"en".equalsIgnoreCase(l.getLanguage()))continue;if(fallback==null)fallback=v;String f=v.getFeatures()==null?"":v.getFeatures().toString().toLowerCase(Locale.ROOT);String n=v.getName()==null?"":v.getName().toLowerCase(Locale.ROOT);if(f.contains("gender=male")||f.contains("gender:male")||n.contains("male")){jayVoice.setVoice(v);return;}}if(fallback!=null)jayVoice.setVoice(fallback);}catch(Exception ignored){}}
    private void speak(String t){if(voiceReady&&jayVoice!=null&&t!=null&&!t.trim().isEmpty())jayVoice.speak(t,TextToSpeech.QUEUE_FLUSH,null,"JAY_RESPONSE");}
    @Override protected void onDestroy(){if(speechRecognizer!=null){try{speechRecognizer.cancel();}catch(Exception ignored){}speechRecognizer.destroy();speechRecognizer=null;}if(jayVoice!=null){try{jayVoice.stop();jayVoice.shutdown();}catch(Exception ignored){}jayVoice=null;}super.onDestroy();}
}
