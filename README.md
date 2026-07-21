# Fake Call App — Android (Java + Gradle)

A high-fidelity fake incoming call simulator that looks and behaves exactly like the stock Android phone app. Operates fully offline with no external server or database.

---

## Features

| Feature | Detail |
|---|---|
| **Realistic Incoming Call UI** | Full-screen dark themed incoming call screen with avatar, caller name, answer (green) and decline (red) buttons — matching stock Android |
| **Active Call UI** | In-call controls: mute, speaker, keypad, add call, hold, more; end-call button |
| **Audio Script Playback** | Plays a user-selected MP3/WAV/M4A file or a recorded voice note through the earpiece when the call is answered |
| **In-app Voice Recorder** | Record a custom "conversation" script directly inside the app |
| **Lock-screen / Fake Call Now button** | Dims/shows overlay then triggers the incoming call screen over the lock screen after a 3-second delay |
| **AlarmManager scheduling** | Schedule a fake call N seconds in the future; survives Doze mode |
| **5-contact hard cap** | SharedPreferences-backed contact list limited to exactly 5 entries; the Add button disables when the limit is reached |
| **15-second call timer** | Active call auto-hangs-up at exactly 15 seconds with a Toast notification |
| **System overlay permission** | Requests `SYSTEM_ALERT_WINDOW` at runtime before showing over the lock screen |
| **Vibration ring pattern** | Device vibrates in a realistic ring pattern during the incoming call screen |

---

## Project structure

```
app/src/main/
├── AndroidManifest.xml
├── java/com/fakecall/app/
│   ├── MainActivity.java          — Dashboard: caller selector, audio picker/recorder, trigger buttons
│   ├── IncomingCallActivity.java  — Full-screen incoming call over lock screen (showWhenLocked/turnScreenOn)
│   ├── ActiveCallActivity.java    — Active call with 15 s hard timer + audio playback
│   ├── ContactsActivity.java      — Add/delete contacts (max 5)
│   ├── ScheduleCallActivity.java  — Schedule via AlarmManager
│   ├── CallReceiver.java          — BroadcastReceiver for AlarmManager & BOOT_COMPLETED
│   ├── OverlayService.java        — ForegroundService that waits then fires IncomingCallActivity
│   ├── Contact.java               — Simple model (Gson-serialised)
│   ├── ContactsAdapter.java       — RecyclerView adapter for contact list
│   └── PrefsManager.java          — SharedPreferences helper (contacts, audio path, active index)
└── res/
    ├── layout/                    — activity_main, activity_incoming_call, activity_active_call,
    │                                activity_contacts, activity_schedule_call, item_contact
    ├── drawable/                  — Vector icons + shape backgrounds for call UI
    ├── mipmap-anydpi-v26/         — Adaptive launcher icon
    └── values/                    — colors, strings, themes (dark Material theme)
```

---

## How to import into Android Studio

1. Open **Android Studio** → **File → Open** → select the `FakeCallApp/` folder.
2. Let Gradle sync. On first run it will download dependencies from Maven Central.
3. Connect an Android device running **API 26+** (Android 8.0 Oreo or newer).
4. Run the app via **Run → Run 'app'**.

---

## First-run setup checklist

| Step | What to do |
|---|---|
| **Overlay permission** | Tap **"Fake Call Now"** → the app prompts you to grant *Display over other apps* in Settings |
| **Exact alarm permission** (Android 12+) | Go to Settings → Apps → Special app access → Alarms & reminders → allow Fake Call |
| **Microphone** | Grant microphone permission when prompted to use the in-app voice recorder |
| **Audio file storage** | Recorded scripts are saved to `Android/data/com.fakecall.app/files/recorded_script.m4a` |

---

## Hard limits (persisted via SharedPreferences)

- **Contact cap:** exactly **5** contacts maximum. The Add button is disabled and a red warning message is shown when the cap is reached.
- **Call duration:** active calls automatically end at **15 seconds** (`00:15`). A Toast appears: *"Call ended (15 s limit reached)"*.

---

## Extending the app

- **Ringtone audio:** in `IncomingCallActivity.onCreate()` add a `MediaPlayer` playing a ringtone URI (e.g. `RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)`) before the user answers.
- **Caller photo:** replace `@drawable/ic_person_placeholder` with a photo stored in `Contact.photoPath` and loaded with Glide/Picasso.
- **Lock-screen delay setting:** surface `OverlayService.EXTRA_DELAY_MS` as a user preference on the main dashboard.
- **Repeat scheduling:** change `AlarmManager.setExactAndAllowWhileIdle` to `setRepeating` in `ScheduleCallActivity`.

---

## Permissions declared

```
SYSTEM_ALERT_WINDOW       — show over other apps and lock screen
RECORD_AUDIO              — in-app voice script recording
READ_EXTERNAL_STORAGE     — pick audio files from device storage
VIBRATE                   — ring vibration pattern
WAKE_LOCK                 — keep CPU awake while waiting for alarm
RECEIVE_BOOT_COMPLETED    — reschedule alarms after reboot
SCHEDULE_EXACT_ALARM      — AlarmManager.setExactAndAllowWhileIdle
USE_EXACT_ALARM           — Android 13+ exact alarm
FOREGROUND_SERVICE        — OverlayService runs as a foreground service
DISABLE_KEYGUARD          — dismiss the keyguard when call arrives
```
