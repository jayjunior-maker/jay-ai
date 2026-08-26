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

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(
                    this, 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(
                    this, 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        startForeground(NOTIFICATION_ID, new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Jay is running")
                .setContentText("Jay is available in the background.")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build());

        wakeWordManager = new JayWakeWordManager(this, new JayWakeWordManager.Listener() {
            @Override public void onWakeWordDetected() {
                // Wake event is intentionally kept local until the activity/event bus is connected.
            }
            @Override public void onListeningChanged(boolean listening) { }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (wakeWordManager != null && wakeWordManager.isAvailable()) {
            wakeWordManager.start();
        }
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Jay Background Service", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Keeps Jay available in the background.");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        if (wakeWordManager != null) wakeWordManager.stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
