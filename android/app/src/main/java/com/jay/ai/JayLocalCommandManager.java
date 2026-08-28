package com.jay.ai;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;

public final class JayLocalCommandManager {
 private final Context context; private final JayAppManager appManager; private final JayContactManager contactManager; private final JayCallManager callManager; private final JayMpesaManager mpesaManager;
 public JayLocalCommandManager(Context c){context=c.getApplicationContext();appManager=new JayAppManager(context);contactManager=new JayContactManager(context);callManager=new JayCallManager(context);mpesaManager=new JayMpesaManager(context);}
 public String handle(String input){
  if(input==null||input.trim().isEmpty())return null;
  String original=input.trim(),text=original.toLowerCase(Locale.ROOT);
  if(isTime(text))return "The time is "+DateFormat.getTimeInstance(DateFormat.SHORT,Locale.getDefault()).format(new Date())+".";
  if(isDate(text))return "Today is "+new SimpleDateFormat("EEEE, d MMMM yyyy",Locale.getDefault()).format(new Date())+".";
  if(contains(text,"battery","bata","beteri"))return JayDeviceTools.getBattery(context)+".";
  if(contains(text,"network speed","internet speed","speed ya network"))return JayDeviceTools.getNetwork(context)+"\n"+JayDeviceTools.getNetworkSpeed(context);
  if(contains(text,"network information","network info","net info"))return JayDeviceTools.getNetwork(context);
  if(text.equals("internet status")||text.equals("internet")||text.equals("network status")||text.equals("network")||text.equals("net"))return networkStatus();
  if(contains(text,"m-pesa balance","mpesa balance","m pesa balance","my m-pesa balance","my mpesa balance"))return mpesaManager.getLatestBalance();
  if(contains(text,"phone information","phone info","device information","device info","about my phone"))return JayDeviceTools.getDevice(context)+"\n"+JayDeviceTools.getBattery(context)+"\n"+JayDeviceTools.getStorage(context)+"\n"+JayDeviceTools.getNetwork(context);
  if(contains(text,"my location","current location","where am i","location yangu","niko wapi","location"))return location();

  Matcher bright=Pattern.compile("(?:brightness|backlight|screen brightness)\\s*(?:to|at)?\\s*(\\d{1,3})%?").matcher(text);
  if(bright.find())return brightness(Integer.parseInt(bright.group(1)));
  Matcher vol=Pattern.compile("(?:volume|sound|media volume)\\s*(?:to|at)?\\s*(\\d{1,3})%?").matcher(text);
  if(vol.find())return volume(Integer.parseInt(vol.group(1)));
  if(contains(text,"torch on","flashlight on","turn torch on","washa torch"))return torch(true);
  if(contains(text,"torch off","flashlight off","turn torch off","zima torch"))return torch(false);

  String bg=background(text);if(bg!=null)return bg;
  String setting=openPhoneSetting(text);if(setting!=null)return setting;
  String clock=clockCommand(text);if(clock!=null)return clock;

  if(contains(text,"open camera","open the camera","launch camera","fungua camera","fungua kamera"))return openCamera();
  if(contains(text,"take a picture","take picture","capture photo","take photo","piga picha"))return openCamera();
  if(contains(text,"open gallery","open photos","open photos app","fungua gallery","fungua picha"))return openGallery();
  if(contains(text,"open contacts","open contact list","fungua contacts"))return openContacts();
  if(contains(text,"open messages","open messaging","open sms","fungua messages"))return openMessages();
  if(contains(text,"open calendar","fungua calendar","fungua kalenda","kalenda"))return openCalendar();
  if(contains(text,"open clock","open alarm","open alarms","open timer"))return openClock();
  if(contains(text,"open browser","open internet","open chrome","fungua browser"))return openBrowser();
  if(contains(text,"open maps","open google maps","open map","fungua maps"))return openMaps();
  if(contains(text,"open files","open file manager","open downloads","fungua files"))return openFiles();
  if(contains(text,"open calculator","fungua calculator","fungua calculator"))return openCalculator();
  if(contains(text,"open phone","open dialer","fungua simu","fungua dialer"))return openDialer();

  if(text.contains("whatsapp")&&(text.contains("text ")||text.contains("message ")||text.contains("send ")))return whatsappMessage(original);
  if(text.startsWith("text ")||text.startsWith("sms ")||text.startsWith("send sms "))return smsMessage(original);
  if(text.startsWith("call "))return callContact(original.substring(5).trim());
  if(text.startsWith("open ")){String app=original.substring(5).trim();String r=appManager.openApp(app);if(r.startsWith("OPENED_APP:"))return "Opening "+r.substring(11)+".";return r;}
  return null;
 }
 private boolean isTime(String t){return t.equals("time")||t.equals("what time")||t.equals("time now")||t.equals("what is the time")||t.equals("what's the time")||t.equals("what time is it")||t.contains("current time")||t.contains("saa ngapi")||t.contains("saa ni ngapi");}
 private boolean isDate(String t){return t.equals("today")||t.equals("what is today")||t.equals("when is today")||t.equals("date")||t.equals("what date is it")||t.equals("leo ni lini")||t.equals("leo ni tarehe gani")||t.contains("today's date")||t.contains("what day is today");}
 private String networkStatus(){try{ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);if(cm==null)return "Network information is unavailable.";NetworkCapabilities n=cm.getNetworkCapabilities(cm.getActiveNetwork());if(n==null)return "The phone is not currently connected to a network.";String type=n.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)?"Wi-Fi":n.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)?"mobile data":n.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)?"Ethernet":"network";String internet=n.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)?" with internet access":"";return "Internet is connected through "+type+internet+".";}catch(Exception e){return "I couldn't read the network status.";}}
 private String location(){if(context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED&&context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)return "LOCATION_PERMISSION_REQUIRED";try{LocationManager m=(LocationManager)context.getSystemService(Context.LOCATION_SERVICE);Location best=null;for(String p:new String[]{LocationManager.GPS_PROVIDER,LocationManager.NETWORK_PROVIDER,LocationManager.PASSIVE_PROVIDER})try{Location l=m.getLastKnownLocation(p);if(l!=null&&(best==null||l.getTime()>best.getTime()))best=l;}catch(Exception ignored){}if(best==null)return "I don't have a recent location fix yet.";String place=resolvePlace(best.getLatitude(),best.getLongitude());if(place==null)return "I found your location, but couldn't resolve it to a place name right now.";return "You're in "+place+".";}catch(Exception e){return "I couldn't read your location.";}}
 private String resolvePlace(double lat,double lon){try{if(!Geocoder.isPresent())return null;Geocoder g=new Geocoder(context,Locale.getDefault());List<Address>a=g.getFromLocation(lat,lon,1);if(a==null||a.isEmpty())return null;Address x=a.get(0);LinkedHashSet<String>parts=new LinkedHashSet<>();if(x.getFeatureName()!=null&&!x.getFeatureName().equals(x.getLocality()))parts.add(x.getFeatureName());if(x.getSubLocality()!=null)parts.add(x.getSubLocality());if(x.getLocality()!=null)parts.add(x.getLocality());if(x.getSubAdminArea()!=null)parts.add(x.getSubAdminArea());if(x.getAdminArea()!=null)parts.add(x.getAdminArea());if(x.getCountryName()!=null)parts.add(x.getCountryName());StringBuilder s=new StringBuilder();for(String p:parts)if(p!=null&&!p.trim().isEmpty()){if(s.length()>0)s.append(", ");s.append(p);}return s.length()==0?null:s.toString();}catch(IOException|RuntimeException e){return null;}}
 private String brightness(int p){p=Math.max(0,Math.min(100,p));try{if(!Settings.System.canWrite(context)){Intent i=new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);i.setData(Uri.parse("package:"+context.getPackageName()));i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(i);return "Jay needs permission to change system brightness. I opened the required Android setting.";}Settings.System.putInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,Math.round(255*p/100f));return "Screen brightness set to "+p+"%.";}catch(Exception e){return "I couldn't change screen brightness on this phone.";}}
 private String volume(int p){p=Math.max(0,Math.min(100,p));AudioManager a=(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);if(a==null)return "I couldn't access volume.";int max=a.getStreamMaxVolume(AudioManager.STREAM_MUSIC);a.setStreamVolume(AudioManager.STREAM_MUSIC,Math.round(max*p/100f),0);return "Media volume set to "+p+"%.";}
 private String torch(boolean on){try{CameraManager m=(CameraManager)context.getSystemService(Context.CAMERA_SERVICE);for(String id:m.getCameraIdList()){CameraCharacteristics c=m.getCameraCharacteristics(id);if(Boolean.TRUE.equals(c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE))){m.setTorchMode(id,on);return on?"Torch turned on.":"Torch turned off.";}}}catch(Exception ignored){}return "I couldn't control the torch.";}
 private String background(String t){if(contains(t,"change wallpaper","set wallpaper","wallpaper","badilisha wallpaper")){try{context.startActivity(new Intent(Intent.ACTION_SET_WALLPAPER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));return "Opening the wallpaper selector.";}catch(Exception e){return "I couldn't open the wallpaper selector.";}}if(contains(t,"restore default background","reset background","default background")){JayAppearanceManager.reset(context);return "Jay's default background has been restored.";}if(contains(t,"change background","background color","background colour","make the background")||t.startsWith("now to ")){String c=null;Matcher h=Pattern.compile("#[0-9a-f]{6}|#[0-9a-f]{8}",Pattern.CASE_INSENSITIVE).matcher(t);if(h.find())c=h.group();for(String x:new String[]{"black","blue","purple","red","green","cyan","dark","default"})if(t.contains(x))c=x;if(c==null&&t.startsWith("now to "))c=t.substring(7).trim();if(c==null)return "Tell me the background color you want.";return JayAppearanceManager.setBackgroundColor(context,c)?"Jay's background color is now "+c+".":"I don't recognize that background color.";}return null;}
 private String openPhoneSetting(String t){
  String a=null,label=null;
  if(contains(t,"airplane mode","flight mode")){a=Settings.ACTION_AIRPLANE_MODE_SETTINGS;label="Airplane mode";}
  else if(contains(t,"wifi settings","wi-fi settings","open wifi","wifi")){a=Settings.ACTION_WIFI_SETTINGS;label="Wi-Fi settings";}
  else if(contains(t,"bluetooth settings","open bluetooth","bluetooth")){a=Settings.ACTION_BLUETOOTH_SETTINGS;label="Bluetooth settings";}
  else if(contains(t,"mobile network","mobile networks","sim settings","apn settings","mobile data settings")){a=Settings.ACTION_WIRELESS_SETTINGS;label="mobile network settings";}
  else if(contains(t,"location settings","location services","turn on location","gps settings")){a=Settings.ACTION_LOCATION_SOURCE_SETTINGS;label="Location settings";}
  else if(contains(t,"display settings","screen settings","screen timeout","auto brightness")){a=Settings.ACTION_DISPLAY_SETTINGS;label="Display settings";}
  else if(contains(t,"sound settings","audio settings","ringer settings")){a=Settings.ACTION_SOUND_SETTINGS;label="Sound settings";}
  else if(contains(t,"security settings","open security","privacy settings")){a=Settings.ACTION_SECURITY_SETTINGS;label="Security settings";}
  else if(contains(t,"battery saver","power saving","battery settings")){a=Settings.ACTION_BATTERY_SAVER_SETTINGS;label="Battery saver settings";}
  else if(contains(t,"app settings","installed apps","manage apps","applications settings","apps settings")){a=Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS;label="Applications settings";}
  else if(contains(t,"notification settings","notifications settings")){a=Settings.ACTION_NOTIFICATION_SETTINGS;label="Notification settings";}
  else if(contains(t,"default apps","default application")){a=Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS;label="Default apps settings";}
  else if(contains(t,"usage access","app usage")){a=Settings.ACTION_USAGE_ACCESS_SETTINGS;label="Usage access settings";}
  else if(contains(t,"vpn settings","vpn")){a=Settings.ACTION_VPN_SETTINGS;label="VPN settings";}
  else if(contains(t,"accessibility settings","accessibility")){a=Settings.ACTION_ACCESSIBILITY_SETTINGS;label="Accessibility settings";}
  else if(contains(t,"keyboard settings","input method settings")){a=Settings.ACTION_INPUT_METHOD_SETTINGS;label="Keyboard settings";}
  else if(contains(t,"language settings","language and input","locale settings")){a=Settings.ACTION_LOCALE_SETTINGS;label="Language settings";}
  else if(contains(t,"date and time settings","time settings")){a=Settings.ACTION_DATE_SETTINGS;label="Date and time settings";}
  else if(contains(t,"storage settings","internal storage","storage")){a=Settings.ACTION_INTERNAL_STORAGE_SETTINGS;label="Storage settings";}
  else if(contains(t,"developer options","developer settings")){a=Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS;label="Developer options";}
  else if(contains(t,"open settings","phone settings","system settings","settings")){a=Settings.ACTION_SETTINGS;label="Settings";}
  if(a==null)return null;try{Intent i=new Intent(a).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);if(i.resolveActivity(context.getPackageManager())==null)return "That Android settings page isn't available on this phone.";context.startActivity(i);return "Opening "+label+".";}catch(Exception e){return "I couldn't open "+label+".";}
 }
 private String clockCommand(String t){try{if(contains(t,"show alarms","open alarms","my alarms")){context.startActivity(new Intent(AlarmClock.ACTION_SHOW_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));return "Opening alarms.";}if(contains(t,"show timers","open timers","my timers")){context.startActivity(new Intent(AlarmClock.ACTION_SHOW_TIMERS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));return "Opening timers.";}Matcher timer=Pattern.compile("(?:set|start) (?:a )?timer (?:for )?(\\d+)\\s*(seconds?|secs?|minutes?|mins?|hours?|hrs?)").matcher(t);if(timer.find()){long n=Long.parseLong(timer.group(1));String u=timer.group(2);long sec=u.startsWith("hour")||u.startsWith("hr")?n*3600:u.startsWith("min")?n*60:n;sec=Math.max(1,Math.min(86400,sec));Intent i=new Intent(AlarmClock.ACTION_SET_TIMER).putExtra(AlarmClock.EXTRA_LENGTH,(int)sec).putExtra(AlarmClock.EXTRA_MESSAGE,"Jay timer").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(i);return "Starting a "+timer.group(1)+" "+u+" timer.";}Matcher alarm=Pattern.compile("(?:set|create) (?:an )?alarm (?:for )?(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?").matcher(t);if(alarm.find()){int h=Integer.parseInt(alarm.group(1));int m=alarm.group(2)==null?0:Integer.parseInt(alarm.group(2));String ap=alarm.group(3);if(ap!=null){if(ap.equals("pm")&&h<12)h+=12;if(ap.equals("am")&&h==12)h=0;}Intent i=new Intent(AlarmClock.ACTION_SET_ALARM).putExtra(AlarmClock.EXTRA_HOUR,h).putExtra(AlarmClock.EXTRA_MINUTES,m).putExtra(AlarmClock.EXTRA_MESSAGE,"Jay alarm").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(i);return "Opening the alarm setup for "+String.format(Locale.US,"%02d:%02d",h,m)+".";}}catch(Exception e){return "I couldn't open the clock controls.";}return null;}
 private String openCamera(){try{Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);if(i.resolveActivity(context.getPackageManager())==null)return "I couldn't find a camera application.";context.startActivity(i);return "Opening the camera now.";}catch(Exception e){return "I couldn't open the camera.";}}
 private String openGallery(){try{Intent i=new Intent(Intent.ACTION_VIEW,MediaStore.Images.Media.EXTERNAL_CONTENT_URI).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(i);return "Opening your photos.";}catch(Exception e){return "I couldn't open your photos.";}}
 private String openContacts(){try{context.startActivity(new Intent(Intent.ACTION_VIEW,ContactsContract.Contacts.CONTENT_URI).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));return "Opening Contacts.";}catch(Exception e){return "I couldn't open Contacts.";}}
 private String openMessages(){try{Intent i=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);if(i.resolveActivity(context.getPackageManager())==null)return "I couldn't find a messaging app.";context.startActivity(i);return "Opening Messages.";}catch(Exception e){return "I couldn't open Messages.";}}
 private String openCalendar(){try{Intent i=new Intent(Intent.ACTION_VIEW,CalendarContract.CONTENT_URI).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(i);return "Opening Calendar.";}catch(Exception e){return "I couldn't open Calendar.";}}
 private String openClock(){try{Intent i=new Intent(AlarmClock.ACTION_SHOW_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(i);return "Opening Clock alarms.";}catch(Exception e){return "I couldn't open Clock.";}}
 private String openBrowser(){try{Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(i);return "Opening the browser.";}catch(Exception e){return "I couldn't open a browser.";}}
 private String openMaps(){try{Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse("geo:0,0?q=current+location")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(i);return "Opening Maps.";}catch(Exception e){return "I couldn't open a maps application.";}}
 private String openFiles(){try{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(i);return "Opening the file picker.";}catch(Exception e){return "I couldn't open the file picker.";}}
 private String openCalculator(){try{Intent i=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALCULATOR).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);if(i.resolveActivity(context.getPackageManager())==null)return "I couldn't find a calculator app.";context.startActivity(i);return "Opening Calculator.";}catch(Exception e){return "I couldn't open Calculator.";}}
 private String openDialer(){try{context.startActivity(new Intent(Intent.ACTION_DIAL).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));return "Opening the phone dialer.";}catch(Exception e){return "I couldn't open the phone dialer.";}}
 private String smsMessage(String original){String body=original.replaceFirst("(?i)^(text|sms|send sms)\\s*","").trim();String[]a=body.split("\\s+",2);if(a.length<2)return "Tell me the phone number or contact and the message.";String target=a[0];String number=target;String found=contactManager.findPhoneNumber(target);if("PERMISSION_REQUIRED".equals(found))return "Contacts permission is required to find that contact.";if(found!=null&&found.startsWith("CONTACT_FOUND|")){String[]d=found.split("\\|",3);number=d[2];target=d[1];}try{Intent i=new Intent(Intent.ACTION_SENDTO,Uri.parse("smsto:"+Uri.encode(number)));i.putExtra("sms_body",a[1]);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(i);return "Opening Messages with a message to "+target+". Please confirm and send it.";}catch(Exception e){return "I couldn't open a messaging app for that number.";}}
 private String whatsappMessage(String original){String l=original.toLowerCase(Locale.ROOT);int p=l.indexOf("text "),n=5;if(p<0){p=l.indexOf("message ");n=8;}if(p<0){p=l.indexOf("send ");n=5;}if(p<0)return null;String[]a=original.substring(p+n).trim().split("\\s+",2);if(a.length<2)return "What message should I prepare?";String found=contactManager.findPhoneNumber(a[0]);if("PERMISSION_REQUIRED".equals(found))return "Contacts permission is required.";if(!found.startsWith("CONTACT_FOUND|"))return "I couldn't find a contact named "+a[0]+".";String[]d=found.split("\\|",3);try{String num=d[2].replaceAll("[^0-9+]","");if(num.startsWith("+"))num=num.substring(1);Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse("https://wa.me/"+num+"?text="+Uri.encode(a[1])));i.setPackage("com.whatsapp");i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);context.startActivity(i);return "Opening WhatsApp with a message to "+d[1]+". Please confirm/send it in WhatsApp.";}catch(Exception e){return "I couldn't open that WhatsApp conversation.";}}
 private String callContact(String n){String f=contactManager.findPhoneNumber(n);if("PERMISSION_REQUIRED".equals(f))return "Contacts permission is required.";if(!f.startsWith("CONTACT_FOUND|"))return "I couldn't find a contact named "+n+".";String[]p=f.split("\\|",3);String r=callManager.callNumber(p[2]);return r.startsWith("CALL_STARTED|")?"Calling "+p[1]+".":"I couldn't start the call.";}
 private boolean contains(String t,String...w){for(String x:w)if(t.contains(x))return true;return false;}
}
