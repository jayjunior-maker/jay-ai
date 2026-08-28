package com.jay.ai;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Handles commands that can be completed locally on the Android device. */
public final class JayLocalCommandManager {
    private final Context context;
    private final JayAppManager appManager;
    private final JayContactManager contactManager;
    private final JayCallManager callManager;
    private final JayMpesaManager mpesaManager;

    public JayLocalCommandManager(Context context) {
        this.context = context.getApplicationContext();
        this.appManager = new JayAppManager(this.context);
        this.contactManager = new JayContactManager(this.context);
        this.callManager = new JayCallManager(this.context);
        this.mpesaManager = new JayMpesaManager(this.context);
    }

    public String handle(String input) {
        if (input == null || input.trim().isEmpty()) return null;
        String original = input.trim();
        String text = original.toLowerCase(Locale.ROOT);

        if (isTimeCommand(text)) return "The time is " + DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()).format(new Date()) + ", Sir.";
        if (isDateCommand(text)) return "Today is " + new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(new Date()) + ", Sir.";
        if (containsAny(text, "battery", "bata", "beteri", "battery status", "battery health")) return JayDeviceTools.getBattery(context) + ".";
        if (containsAny(text, "network speed", "internet speed", "speed ya network")) return JayDeviceTools.getNetwork(context) + "\n" + JayDeviceTools.getNetworkSpeed(context);
        if (containsAny(text, "network information", "network info", "network status", "net status", "net", "network")) return JayDeviceTools.getNetwork(context);
        if (containsAny(text, "m-pesa balance", "mpesa balance", "m pesa balance", "my m-pesa balance", "my mpesa balance", "m pesa")) return mpesaManager.getLatestBalance();
        if (containsAny(text, "phone information", "phone info", "device information", "device info", "information about my phone", "about my phone")) return JayDeviceTools.getDevice(context) + "\n" + JayDeviceTools.getBattery(context) + "\n" + JayDeviceTools.getStorage(context) + "\n" + JayDeviceTools.getNetwork(context);
        if (containsAny(text, "my location", "my current location", "where am i", "location yangu", "niko wapi", "location")) return getLastKnownLocation();
        if (containsAny(text, "turn torch on", "turn flashlight on", "torch on", "flashlight on", "wash torch", "washa torch")) return setTorch(true);
        if (containsAny(text, "turn torch off", "turn flashlight off", "torch off", "flashlight off", "zima torch")) return setTorch(false);
        if (text.contains("volume") || text.contains("sound level") || text.contains("sauti")) { Integer percent = extractPercent(text); if (percent != null) return setVolume(percent); }

        String background = handleBackgroundCommand(text);
        if (background != null) return background;

        if (text.contains("settings") && (text.contains("developer options") || text.contains("developer mode") || text.contains("developer settings"))) return openSettings(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
        if (containsAny(text, "open settings", "open phone settings", "fungua settings", "fungua mipangilio")) return openSettings(Settings.ACTION_SETTINGS);
        if (containsAny(text, "open camera", "open the camera", "camera", "fungua camera", "fungua kamera")) return openCamera();

        if (text.contains("whatsapp") && (text.contains("text ") || text.contains("message ") || text.contains("send "))) return whatsappMessage(original);
        if (text.startsWith("call ") || text.startsWith("piga simu ") || text.startsWith("pigia ")) {
            String name = text.startsWith("call ") ? original.substring(5).trim() : text.startsWith("piga simu ") ? original.substring(10).trim() : original.substring(6).trim();
            return callContact(name);
        }
        if (text.startsWith("open ")) {
            String appName = original.substring(5).trim();
            if (!appName.isEmpty()) {
                String result = appManager.openApp(appName);
                if (result.startsWith("OPENED_APP:")) return "Opening " + result.substring("OPENED_APP:".length()) + ", Sir.";
                return result;
            }
        }
        if (containsAny(text, "delete the file", "delete file", "delete this file", "futa file", "futa faili")) return "Sir, I need the specific file selected or identified before I can delete it. A 5-second safety confirmation will be required before deletion.";
        return null;
    }

    private boolean isTimeCommand(String text) { return text.equals("time") || text.equals("what time") || text.equals("time now") || text.equals("what is the time") || text.equals("what's the time") || text.equals("what time is it") || text.contains("current time") || text.contains("saa ngapi") || text.contains("saa ni ngapi"); }
    private boolean isDateCommand(String text) { return text.equals("today") || text.equals("what is today") || text.equals("when is today") || text.equals("date") || text.equals("what date is it") || text.equals("leo ni lini") || text.equals("leo ni tarehe gani") || text.contains("today's date") || text.contains("what day is today"); }

    private String handleBackgroundCommand(String text) {
        if (containsAny(text, "change wallpaper", "change the wallpaper", "set wallpaper", "wallpaper", "badilisha wallpaper")) {
            try { Intent i = new Intent(Intent.ACTION_SET_WALLPAPER); i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); context.startActivity(i); return "Opening the wallpaper selector, Sir."; } catch (Exception e) { return "I couldn't open the wallpaper selector, Sir."; }
        }
        if (containsAny(text, "restore default background", "reset background", "default background")) { JayAppearanceManager.reset(context); return "Jay's background has been restored to the default, Sir."; }
        if (containsAny(text, "change background", "change the background", "background color", "background colour", "make the background", "badilisha background") || text.startsWith("now to ")) {
            String color = extractBackgroundColor(text);
            if (color == null && text.startsWith("now to ")) color = text.substring(7).trim();
            if (color == null) return "Which background color would you like, Sir? You can say blue, purple, cyan, green, red, black, or a hex color such as #10152F.";
            if (JayAppearanceManager.setBackgroundColor(context, color)) return "Jay's background color is now " + color + ", Sir.";
            return "I don't recognize that background color, Sir.";
        }
        return null;
    }

    private String extractBackgroundColor(String text) { Matcher hex=Pattern.compile("#[0-9a-f]{6}|#[0-9a-f]{8}",Pattern.CASE_INSENSITIVE).matcher(text); if(hex.find())return hex.group(); String[]colors={"black","blue","purple","red","green","cyan","dark","default"}; for(String c:colors)if(text.contains(c))return c; return null; }

    private String getLastKnownLocation() {
        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) return "LOCATION_PERMISSION_REQUIRED";
        try { LocationManager manager=(LocationManager)context.getSystemService(Context.LOCATION_SERVICE); if(manager==null)return "Location services are unavailable, Sir."; Location best=null; String[]providers={LocationManager.GPS_PROVIDER,LocationManager.NETWORK_PROVIDER,LocationManager.PASSIVE_PROVIDER}; for(String provider:providers){try{Location l=manager.getLastKnownLocation(provider);if(l!=null&&(best==null||l.getTime()>best.getTime()))best=l;}catch(SecurityException ignored){}} if(best==null)return "I don't have a recent location fix yet, Sir."; return String.format(Locale.US,"Your last known location is latitude %.6f, longitude %.6f, Sir.",best.getLatitude(),best.getLongitude()); } catch(Exception e){return "I couldn't read your location, Sir.";}
    }

    private String whatsappMessage(String original) {
        String lower=original.toLowerCase(Locale.ROOT); int marker=lower.indexOf("text "); int length=5; if(marker<0){marker=lower.indexOf("message ");length=8;} if(marker<0){marker=lower.indexOf("send ");length=5;} if(marker<0)return "Who should I message on WhatsApp, Sir?"; String rest=original.substring(marker+length).trim(); String[]parts=rest.split("\\s+",2); if(parts.length<2)return "What message should I send, Sir?"; String contact=parts[0]; String message=parts[1]; String found=contactManager.findPhoneNumber(contact); if("PERMISSION_REQUIRED".equals(found))return "Sir, Jay needs Contacts permission before it can find that WhatsApp contact."; if(!found.startsWith("CONTACT_FOUND|"))return "I couldn't find a contact named "+contact+", Sir."; String[]data=found.split("\\|",3); if(data.length<3)return "I couldn't read that contact's phone number, Sir."; String number=data[2].replaceAll("[^0-9+]",""); try{String digits=number.startsWith("+")?number.substring(1):number;Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse("https://wa.me/"+digits+"?text="+Uri.encode(message)));intent.setPackage("com.whatsapp");intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(intent);return "Opening WhatsApp with a message to "+data[1]+", Sir. Please confirm/send it in WhatsApp.";}catch(Exception e){return "I couldn't open that WhatsApp conversation, Sir.";}
    }

    private String setVolume(int percent){int safe=Math.max(0,Math.min(100,percent));AudioManager audio=(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);if(audio==null)return "I couldn't access the phone volume, Sir.";int max=audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);audio.setStreamVolume(AudioManager.STREAM_MUSIC,Math.round(max*safe/100f),0);return "Media volume set to "+safe+"%, Sir.";}
    private String setTorch(boolean enabled){try{CameraManager manager=(CameraManager)context.getSystemService(Context.CAMERA_SERVICE);if(manager==null)return "This phone does not expose a flashlight control, Sir.";for(String id:manager.getCameraIdList()){CameraCharacteristics c=manager.getCameraCharacteristics(id);Boolean flash=c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);Integer facing=c.get(CameraCharacteristics.LENS_FACING);if(Boolean.TRUE.equals(flash)&&(facing==null||facing==CameraCharacteristics.LENS_FACING_BACK)){manager.setTorchMode(id,enabled);return enabled?"Torch turned on, Sir.":"Torch turned off, Sir.";}}return "I couldn't find a flashlight on this phone, Sir.";}catch(Exception e){return "I couldn't control the torch, Sir.";}}
    private Integer extractPercent(String text){int p=text.indexOf('%');if(p<=0)return null;int start=p-1;while(start>=0&&Character.isDigit(text.charAt(start)))start--;if(start==p-1)return null;try{return Integer.parseInt(text.substring(start+1,p));}catch(NumberFormatException e){return null;}}
    private String openSettings(String action){try{Intent intent=new Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);if(intent.resolveActivity(context.getPackageManager())==null)intent=new Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(intent);return "Opening Settings, Sir.";}catch(Exception e){return "I couldn't open Android Settings, Sir.";}}
    private String openCamera(){try{Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);if(intent.resolveActivity(context.getPackageManager())==null)return "I couldn't find a camera application on this phone, Sir.";intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(intent);return "Opening the camera now, Sir.";}catch(Exception e){return "I couldn't open the camera, Sir.";}}
    private String callContact(String contactName){if(contactName.isEmpty())return "Which contact should I call, Sir?";String found=contactManager.findPhoneNumber(contactName);if("PERMISSION_REQUIRED".equals(found))return "Sir, Jay needs Contacts permission before I can find that contact.";if("CONTACT_NOT_FOUND".equals(found))return "I couldn't find a contact named "+contactName+", Sir.";if("ERROR".equals(found))return "I couldn't read your contacts, Sir.";if(!found.startsWith("CONTACT_FOUND|"))return "I couldn't find that contact, Sir.";String[]parts=found.split("\\|",3);if(parts.length<3)return "I found the contact, but couldn't read the phone number, Sir.";String result=callManager.callNumber(parts[2]);if(result.startsWith("CALL_STARTED|"))return "Calling "+parts[1]+", Sir.";if("PERMISSION_REQUIRED".equals(result))return "Sir, phone-call permission is required to call "+parts[1]+".";return "I couldn't start the call to "+parts[1]+", Sir.";}
    private boolean containsAny(String text,String...words){for(String word:words)if(text.contains(word))return true;return false;}
}
