package com.fakecall.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;

/**
 * Lets the user schedule a fake incoming call N seconds in the future
 * using AlarmManager so it survives Doze mode.
 */
public class ScheduleCallActivity extends AppCompatActivity {

    private PrefsManager prefs;
    private TextInputEditText etDelay;
    private Spinner spinnerCaller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_call);

        prefs = new PrefsManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etDelay       = findViewById(R.id.etDelay);
        spinnerCaller = findViewById(R.id.spinnerScheduleCaller);

        // Populate spinner
        List<Contact> contacts = prefs.getContacts();
        if (contacts.isEmpty()) contacts.add(new Contact("Unknown"));
        ArrayAdapter<Contact> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, contacts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCaller.setAdapter(adapter);
        spinnerCaller.setSelection(prefs.getActiveContactIndex());

        // Schedule button
        findViewById(R.id.btnSchedule).setOnClickListener(v -> scheduleCall());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void scheduleCall() {
        String raw = etDelay.getText() != null ? etDelay.getText().toString().trim() : "";
        if (TextUtils.isEmpty(raw)) {
            Toast.makeText(this, "Enter a delay in seconds.", Toast.LENGTH_SHORT).show();
            return;
        }
        int delaySec;
        try {
            delaySec = Integer.parseInt(raw);
            if (delaySec < 1) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter a valid positive number.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save selected caller
        int selectedIdx = spinnerCaller.getSelectedItemPosition();
        prefs.setActiveContactIndex(selectedIdx);

        // Schedule via AlarmManager (exact, can wake from Doze)
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent   = new Intent(this, CallReceiver.class);
        intent.setAction(CallReceiver.ACTION_TRIGGER_CALL);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long triggerAt = System.currentTimeMillis() + (delaySec * 1000L);

        if (am != null) {
            try {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
                Toast.makeText(this, getString(R.string.scheduled_ok) +
                        " (" + delaySec + "s)", Toast.LENGTH_SHORT).show();
                finish();
            } catch (SecurityException e) {
                Toast.makeText(this,
                        "Please grant exact alarm permission in system settings.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
