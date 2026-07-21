package com.fakecall.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_AUDIO_PERM = 100;

    private PrefsManager prefs;
    private Spinner spinnerCaller;
    private TextView tvAudioPath;
    private List<Contact> contactList;

    private MediaRecorder recorder;
    private boolean isRecording = false;
    private String recordedFilePath;

    // ── Audio file picker ─────────────────────────────────────────────────────
    private final ActivityResultLauncher<Intent> audioPickerLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        String path = uri.toString();
                        prefs.setAudioPath(path);
                        updateAudioLabel(path);
                        Toast.makeText(this, getString(R.string.audio_saved), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = new PrefsManager(this);

        spinnerCaller = findViewById(R.id.spinnerCaller);
        tvAudioPath   = findViewById(R.id.tvAudioPath);

        // ── Buttons ───────────────────────────────────────────────────────────
        findViewById(R.id.btnFakeCallNow).setOnClickListener(v -> triggerFakeCallNow());
        findViewById(R.id.btnScheduleCall).setOnClickListener(v ->
                startActivity(new Intent(this, ScheduleCallActivity.class)));
        findViewById(R.id.btnManageContacts).setOnClickListener(v ->
                startActivity(new Intent(this, ContactsActivity.class)));
        findViewById(R.id.btnPickAudio).setOnClickListener(v -> pickAudioFile());
        findViewById(R.id.btnRecordAudio).setOnClickListener(v -> toggleRecording());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCallerSpinner();
        String audioPath = prefs.getAudioPath();
        updateAudioLabel(audioPath);
    }

    // ── Caller spinner ────────────────────────────────────────────────────────

    private void refreshCallerSpinner() {
        contactList = prefs.getContacts();
        if (contactList.isEmpty()) {
            contactList = new ArrayList<>();
            contactList.add(new Contact(getString(R.string.no_contacts)));
        }
        ArrayAdapter<Contact> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, contactList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCaller.setAdapter(adapter);

        int savedIdx = prefs.getActiveContactIndex();
        if (savedIdx < contactList.size()) spinnerCaller.setSelection(savedIdx);

        spinnerCaller.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, android.view.View v, int pos, long id) {
                prefs.setActiveContactIndex(pos);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
    }

    // ── Trigger immediate fake call ────────────────────────────────────────────

    private void triggerFakeCallNow() {
        if (!Settings.canDrawOverlays(this)) {
            showOverlayPermissionDialog();
            return;
        }
        // Start foreground service with a 3-second delay (simulates ringing after dimming)
        Intent svcIntent = new Intent(this, OverlayService.class);
        svcIntent.putExtra(OverlayService.EXTRA_DELAY_MS, 3000L);
        startForegroundService(svcIntent);
        Toast.makeText(this, "Fake call incoming in 3 seconds…", Toast.LENGTH_SHORT).show();
    }

    // ── Audio picker ──────────────────────────────────────────────────────────

    private void pickAudioFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        audioPickerLauncher.launch(Intent.createChooser(intent, "Select audio"));
    }

    // ── Audio recorder ────────────────────────────────────────────────────────

    private void toggleRecording() {
        if (isRecording) {
            stopRecording();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, REQ_AUDIO_PERM);
            } else {
                startRecording();
            }
        }
    }

    private void startRecording() {
        recordedFilePath = getExternalFilesDir(null).getAbsolutePath() + "/recorded_script.m4a";
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOutputFile(recordedFilePath);
        try {
            recorder.prepare();
            recorder.start();
            isRecording = true;
            findViewById(R.id.btnRecordAudio).setEnabled(true);
            Toast.makeText(this, getString(R.string.audio_recording), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Recording failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void stopRecording() {
        if (recorder != null) {
            try { recorder.stop(); } catch (RuntimeException ignored) {}
            recorder.release();
            recorder = null;
        }
        isRecording = false;
        prefs.setAudioPath("file://" + recordedFilePath);
        updateAudioLabel("file://" + recordedFilePath);
        Toast.makeText(this, getString(R.string.audio_saved), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_AUDIO_PERM && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecording();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void updateAudioLabel(String path) {
        if (path == null || path.isEmpty()) {
            tvAudioPath.setText(getString(R.string.no_audio));
        } else {
            String name = path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;
            tvAudioPath.setText(name);
        }
    }

    private void showOverlayPermissionDialog() {
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.perm_overlay_title))
            .setMessage(getString(R.string.perm_overlay_msg))
            .setPositiveButton(getString(R.string.perm_open_settings), (d, w) -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            })
            .setNegativeButton(getString(R.string.perm_cancel), null)
            .show();
    }
}
