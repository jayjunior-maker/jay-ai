package com.jay.ai;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

public class JayBackgroundService extends Service {

    private static final String CHANNEL_ID = "JAY_BACKGROUND";
    private static final int NOTIFICATION_ID = 7001;

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId) {

        Notification notification =
                createNotification();

        startForeground(
                NOTIFICATION_ID,
                notification
        );

        return START_STICKY;
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Jay Background Service",
                            NotificationManager.IMPORTANCE_LOW
                    );

            channel.setDescription(
                    "Keeps Jay running in the background"
            );

            NotificationManager manager =
                    getSystemService(
                            NotificationManager.class
                    );

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            return new Notification.Builder(
                    this,
                    CHANNEL_ID
            )
                    .setContentTitle("Jay is running")
                    .setContentText(
                            "Jay is active in the background"
                    )
                    .setSmallIcon(
                            android.R.drawable.ic_btn_speak_now
                    )
                    .setOngoing(true)
                    .build();

        } else {

            return new Notification.Builder(this)
                    .setContentTitle("Jay is running")
                    .setContentText(
                            "Jay is active in the background"
                    )
                    .setSmallIcon(
                            android.R.drawable.ic_btn_speak_now
                    )
                    .setOngoing(true)
                    .build();
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}
