package com.fakecall.app;

import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

/**
 * Active call screen — shows the in-call UI, plays the audio script through
 * the earpiece/speaker, and auto-hangs-up at exactly 15 seconds.
 */
public class ActiveCallActivity extends AppCompatActivity {

    // ── Hard limit: 15 seconds ────────────────────────────────────────────────
    private static final int MAX_CALL_SECONDS = 15;

    private final Handler handler  = new Handler(Looper.getMainLooper());
    private int   elapsedSeconds   = 0;
    private MediaPlayer mediaPlayer;

    private final Runnable timerRunnable = new Runnable() {
        @Override public void run() {
            elapsedSeconds++;
            updateTimerDisplay();

            if (elapsedSeconds >= MAX_CALL_SECONDS) {
                endCall(true);
            } else {
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setShowWhenLocked(true);
        setTurnScreenOn(true);
        setContentView(R.layout.activity_active_call);

        PrefsManager prefs   = new PrefsManager(this);
        Contact      contact = prefs.getActiveContact();

        // Populate caller name
        TextView tvName = findViewById(R.id.tvActiveCallerName);
        tvName.setText(contact.getName());

        // End call button
        ImageButton btnEnd = findViewById(R.id.btnEndCall);
        btnEnd.setOnClickListener(v -> endCall(false));

        // Mute / speaker toggles (visual only — extend as needed)
        findViewById(R.id.btnMute).setOnClickListener(v ->
                Toast.makeText(this, "Mute toggled", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnSpeaker).setOnClickListener(v -> toggleSpeaker());

        // Start timer
        handler.postDelayed(timerRunnable, 1000);

        // Play audio script
        playAudioScript(prefs);
    }

    // ── Timer ─────────────────────────────────────────────────────────────────

    private void updateTimerDisplay() {
        TextView tvTimer = findViewById(R.id.tvCallTimer);
        if (tvTimer == null) return;
        int minutes = elapsedSeconds / 60;
        int seconds = elapsedSeconds % 60;
        tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
    }

    // ── End call ──────────────────────────────────────────────────────────────

    private void endCall(boolean autoEnded) {
        handler.removeCallbacks(timerRunnable);
        releasePlayer();

        if (autoEnded) {
            Toast.makeText(this, getString(R.string.call_ended), Toast.LENGTH_LONG).show();
        }

        finish();
    }

    // ── Audio playback ────────────────────────────────────────────────────────

    private void playAudioScript(PrefsManager prefs) {
        String path = prefs.getAudioPath();
        if (path == null || path.isEmpty()) return; // silent call

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build());

            if (path.startsWith("file://")) {
                // Recorded file
                mediaPlayer.setDataSource(path.replace("file://", ""));
            } else {
                // Content URI from picker
                mediaPlayer.setDataSource(this, Uri.parse(path));
            }

            mediaPlayer.setOnPreparedListener(mp -> mp.start());
            mediaPlayer.setOnCompletionListener(mp -> releasePlayer());
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                releasePlayer();
                return true;
            });
            mediaPlayer.prepareAsync();

        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            releasePlayer();
        }
    }

    private void toggleSpeaker() {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (am == null) return;
        boolean isSpeaker = am.isSpeakerphoneOn();
        am.setSpeakerphoneOn(!isSpeaker);
        Toast.makeText(this, isSpeaker ? "Earpiece" : "Speaker", Toast.LENGTH_SHORT).show();
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            try { mediaPlayer.stop(); } catch (IllegalStateException ignored) {}
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timerRunnable);
        releasePlayer();
    }
}
