package com.fakecall.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * BroadcastReceiver that fires when the AlarmManager triggers a scheduled call,
 * or on device boot (to reschedule pending alarms if needed).
 *
 * Registered in AndroidManifest with:
 *   action: com.fakecall.app.TRIGGER_CALL
 *   action: android.intent.action.BOOT_COMPLETED
 */
public class CallReceiver extends BroadcastReceiver {

    public static final String ACTION_TRIGGER_CALL = "com.fakecall.app.TRIGGER_CALL";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_TRIGGER_CALL.equals(intent.getAction())) {
            launchIncomingCall(context);
        }
        // BOOT_COMPLETED: nothing to reschedule unless app had a pending alarm;
        // left as extension point.
    }

    private void launchIncomingCall(Context context) {
        Intent callIntent = new Intent(context, IncomingCallActivity.class);
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(callIntent);
    }
}
