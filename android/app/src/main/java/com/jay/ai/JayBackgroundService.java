package com.jay.ai;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

/** Keeps Jay's wake-word listener available when the app task is closed. */
public class JayBackgroundService extends Service {
    private static final String CHANNEL_ID = "jay_background";
    private static final int NOTIFICATION_ID = 1001;
    private JayWakeWordManager wakeWordManager;
    private boolean deviceLocked = false;

    private final BroadcastReceiver lockStateReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) deviceLocked = true;
            else if (Intent.ACTION_SCREEN_ON.equals(action)) deviceLocked = false;
        }
    };

    @Override public void onCreate() {
        super.onCreate();
        deviceLocked = !((android.os.PowerManager) getSystemService(POWER_SERVICE)).isInteractive();
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE)
                : PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        startForeground(NOTIFICATION_ID, new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Jay is active")
                .setContentText("Say 'Jay' to wake your assistant. Protected actions require the device/admin authentication.")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent).setOngoing(true).build());

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) registerReceiver(lockStateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        else registerReceiver(lockStateReceiver, filter);

        wakeWordManager = new JayWakeWordManager(this, new JayWakeWordManager.Listener() {
            @Override public void onWakeWordDetected() {
                Intent wake = new Intent(JayWakeWordManager.ACTION_WAKE_WORD);
                wake.setPackage(getPackageName());
                wake.putExtra("device_locked", deviceLocked);
                wake.putExtra("protected_actions_require_auth", true);
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

    @Override public void onTaskRemoved(Intent rootIntent) {
        Intent restart = new Intent(this, JayBackgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(restart);
        else startService(restart);
        super.onTaskRemoved(rootIntent);
    }

    @Override public void onDestroy() {
        try { unregisterReceiver(lockStateReceiver); } catch (Exception ignored) { }
        if (wakeWordManager != null) wakeWordManager.stop();
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
