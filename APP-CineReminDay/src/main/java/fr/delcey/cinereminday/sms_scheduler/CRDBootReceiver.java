package fr.delcey.cinereminday.sms_scheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import fr.delcey.cinereminday.local_code_manager.CRDTimeManager;

/**
 * Created by Nino on 09/03/2017.
 */

public class CRDBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Alarms won't resist a reboot
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            CRDTimeManager.scheduleWeeklyAlarm(context);
        }
    }
}