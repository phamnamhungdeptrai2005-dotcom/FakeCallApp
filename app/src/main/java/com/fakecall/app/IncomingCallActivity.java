package com.fakecall.app;

import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Displays a full-screen incoming call UI that appears over the lock screen.
 * Vibrates the device to simulate a real ringtone (audio ringtone can be added).
 */
public class IncomingCallActivity extends AppCompatActivity {

    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show over lock screen and turn the screen on
        setShowWhenLocked(true);
        setTurnScreenOn(true);

        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (km != null) km.requestDismissKeyguard(this, null);

        // Keep screen on while the call screen is visible
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        setContentView(R.layout.activity_incoming_call);

        // Populate caller name
        PrefsManager prefs    = new PrefsManager(this);
        Contact      contact  = prefs.getActiveContact();
        TextView     tvName   = findViewById(R.id.tvCallerName);
        tvName.setText(contact.getName());

        // Vibrate pattern: ring-ring simulation
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        startVibration();

        // Answer button
        ImageButton btnAnswer = findViewById(R.id.btnAnswer);
        btnAnswer.setOnClickListener(v -> answerCall(prefs));

        // Decline button
        ImageButton btnDecline = findViewById(R.id.btnDecline);
        btnDecline.setOnClickListener(v -> declineCall());
    }

    private void answerCall(PrefsManager prefs) {
        stopVibration();
        Intent intent = new Intent(this, ActiveCallActivity.class);
        startActivity(intent);
        finish();
    }

    private void declineCall() {
        stopVibration();
        finish();
    }

    private void startVibration() {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        // Ring pattern: wait 0 ms, vibrate 800 ms, pause 1000 ms — repeat from index 0
        long[] pattern = {0, 800, 1000};
        VibrationEffect effect = VibrationEffect.createWaveform(pattern, 0);
        vibrator.vibrate(effect);
    }

    private void stopVibration() {
        if (vibrator != null) vibrator.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopVibration();
    }
}
