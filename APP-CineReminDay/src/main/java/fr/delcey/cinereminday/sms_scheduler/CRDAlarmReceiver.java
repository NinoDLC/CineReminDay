package fr.delcey.cinereminday.sms_scheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;

import fr.delcey.cinereminday.CRDUtils;
import fr.delcey.cinereminday.local_manager.CRDSharedPreferences;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Created by Nino on 10/03/2017.
 */

public class CRDAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        sendSms(context);

        // Schedule the next week's alarm !
        // We can't use setRepeating because it won't wake up doze on API 19+
        CRDUtils.scheduleWeeklyAlarm(context);
    }

    public static void sendSms(final Context context) {
        Log.v(CRDAlarmReceiver.class.getName(), "sendSms() => Sending SMS...");

        CRDSharedPreferences.getInstance(context).setSmsSendingTimestamp();

        final TelephonyManager telephonyManager = (TelephonyManager) context.getApplicationContext().getSystemService(TELEPHONY_SERVICE);
        final PhoneStateListener phoneStateListener = new PhoneStateListener() {
            // Fired when the service state changes or immediately after registration via .listen()
            @Override
            public void onServiceStateChanged(ServiceState serviceState) {
                super.onServiceStateChanged(serviceState);

                String serviceStateDebug;

                switch (serviceState.getState()) {
                    case ServiceState.STATE_IN_SERVICE:
                        serviceStateDebug = "STATE_IN_SERVICE";
                        break;
                    case ServiceState.STATE_OUT_OF_SERVICE:
                        serviceStateDebug = "OUT_OF_SERVICE";
                        break;
                    case ServiceState.STATE_EMERGENCY_ONLY :
                        serviceStateDebug = "EMERGENCY_ONLY";
                        break;
                    case ServiceState.STATE_POWER_OFF:
                        serviceStateDebug = "POWER_OFF";
                        break;
                    default:
                        serviceStateDebug = "OTHER";
                        break;
                }

                Log.v(CRDAlarmReceiver.class.getName(), "onServiceStateChanged() => " + "serviceState = [" + serviceStateDebug +"]");

                if (serviceState.getState() == ServiceState.STATE_IN_SERVICE) {
                    CRDUtils.sendSmsToOrange(context);

                    telephonyManager.listen(this, PhoneStateListener.LISTEN_NONE);
                }
            }
        };

        // We can't simply have the current state of the network. We have to register to its changes, then it will fire
        // immediately after the registration an event with the initial state. Super retarded imo.
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);
    }
}
