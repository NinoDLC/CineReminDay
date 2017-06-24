package fr.delcey.cinereminday.local_manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.delcey.cinereminday.CRDTimeManager;
import fr.delcey.cinereminday.CRDUtils;

/**
 * Created by Nino on 10/03/2017.
 */

public class CRDSmsReceiver extends BroadcastReceiver {
    public static final Pattern CINEDAY_CODE_PATTERN = Pattern.compile("\\d{4} \\d{4}");

    @Override
    public void onReceive(Context context, Intent intent) {
        StringBuilder smsBuilder = new StringBuilder(140);
        boolean fromOrange = false;

        for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
            if (smsMessage != null) {
                String sender = smsMessage.getDisplayOriginatingAddress();

                if (CRDUtils.ORANGE_CINEDAY_NUMBER.equalsIgnoreCase(sender)) {
                    fromOrange = true;
                    smsBuilder.append(smsMessage.getDisplayMessageBody());
                } else {
                    break;
                }
            }
        }

        if (fromOrange) {
            CRDSharedPreferences.getInstance(context).clear();

            Matcher matcher = CINEDAY_CODE_PATTERN.matcher(smsBuilder.toString());

            if (matcher.matches()) {
                String cinedayCode = matcher.group();

                // We save it in local
                CRDSharedPreferences.getInstance(context).setCinedayCode(cinedayCode);

                // Don't spy on our beloved users !
                CRDUtils.toggleSmsReceiver(context, false);
            } else {
                CRDSharedPreferences.getInstance(context).setError(smsBuilder.toString());
            }
        }
    }
}
