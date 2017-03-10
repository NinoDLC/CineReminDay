package fr.delcey.cinereminday;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Nino on 10/03/2017.
 */

public class CRDAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        CRDUtils.scheduleSmsSending(context);
    }
}
