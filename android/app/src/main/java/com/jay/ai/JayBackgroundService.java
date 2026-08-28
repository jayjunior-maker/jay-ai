package com.jay.ai;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

public class JayBackgroundService extends Service {
    private static final String CHANNEL_ID = "jay_background";
    private static final int NOTIFICATION_ID = 1001;
    private JayWakeWordManager wakeWordManager;

    @Override public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE)
                : PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        startForeground(NOTIFICATION_ID, new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Jay is listening")
                .setContentText("Say 'Jay' to wake your assistant.")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent).setOngoing(true).build());

        wakeWordManager = new JayWakeWordManager(this, new JayWakeWordManager.Listener() {
            @Override public void onWakeWordDetected() {
                Intent wake = new Intent(JayWakeWordManager.ACTION_WAKE_WORD);
                wake.setPackage(getPackageName());
                sendBroadcast(wake);
            }
            @Override public void onListeningChanged(boolean listening) { }
        });
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (wakeWordManager != null && wakeWordManager.isAvailable()) wakeWordManager.start();
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Jay Background Service", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Keeps Jay available for the wake word.");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Override public void onDestroy() { if (wakeWordManager != null) wakeWordManager.stop(); super.onDestroy(); }
    @Override public IBinder onBind(Intent intent) { return null; }
}
