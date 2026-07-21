package com.fakecall.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

/**
 * Foreground service started when the user taps "Fake Call Now" from the
 * lock-screen flow. Waits a short delay then fires IncomingCallActivity.
 *
 * Kept alive in the foreground so the system cannot kill it before the
 * call appears.
 */
public class OverlayService extends Service {

    public static final String CHANNEL_ID    = "fakecall_channel";
    public static final String EXTRA_DELAY_MS = "delay_ms";
    private static final int NOTIF_ID        = 1001;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIF_ID, buildNotification());

        long delayMs = 3000L; // default 3 s
        if (intent != null && intent.hasExtra(EXTRA_DELAY_MS)) {
            delayMs = intent.getLongExtra(EXTRA_DELAY_MS, 3000L);
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent callIntent = new Intent(this, IncomingCallActivity.class);
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(callIntent);
            stopSelf();
        }, delayMs);

        return START_NOT_STICKY;
    }

    @Nullable @Override
    public IBinder onBind(Intent intent) { return null; }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Fake Call",
                NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Used to schedule fake incoming calls");
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    private Notification buildNotification() {
        Intent openIntent = new Intent(this, MainActivity.class);
        PendingIntent pi   = PendingIntent.getActivity(this, 0, openIntent,
                PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_call)
                .setContentTitle("Fake Call")
                .setContentText("Preparing incoming call…")
                .setOngoing(true)
                .setContentIntent(pi)
                .build();
    }
}
