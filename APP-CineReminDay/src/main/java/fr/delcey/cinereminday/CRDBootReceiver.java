package fr.delcey.cinereminday;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Nino on 09/03/2017.
 */

public class CRDBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Device just booted, set alarm !
            PendingIntent alarmIntent = CRDUtils.getAlarmIntent(context);

            CRDUtils.scheduleWeeklyAlarm(context, alarmIntent);
        }
    }
}
