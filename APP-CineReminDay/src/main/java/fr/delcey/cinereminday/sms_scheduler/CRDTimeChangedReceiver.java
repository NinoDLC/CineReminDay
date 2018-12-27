package fr.delcey.cinereminday.sms_scheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import fr.delcey.cinereminday.local_code_manager.CRDTimeManager;

/**
 * Created by Nino on 04/07/2017.
 */

public class CRDTimeChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        CRDTimeManager.scheduleWeeklyAlarm(context);
    }
}
