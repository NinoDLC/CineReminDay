package fr.delcey.cinereminday.local_manager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.delcey.cinereminday.CRDUtils;

/**
 * Created by Nino on 10/03/2017.
 */

public class CRDSmsReceiver extends BroadcastReceiver {
    public static final Pattern CINEDAY_CODE_PATTERN = Pattern.compile("\\d{4} \\d{4}");

    @Override
    public void onReceive(Context context, Intent intent) {
        for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
            if (smsMessage != null) {
                String sender = smsMessage.getDisplayOriginatingAddress();

                if (CRDUtils.ORANGE_CINEDAY_NUMBER.equalsIgnoreCase(sender)) {
                    String message = smsMessage.getDisplayMessageBody();

                    Matcher matcher = CINEDAY_CODE_PATTERN.matcher(message);
                    if (matcher.matches()) {
                        String cinedayCode = matcher.group();

                        // We save it in local
                        CRDSharedPreferences.getInstance(context).setCinedayCode(cinedayCode, Calendar.getInstance().getTime().getTime());

                        // Don't spy on our beloved users !
                        CRDUtils.toggleSmsReceiver(context, false);
                    }

                    // TODO VOLKO IF MESSAGE ERROR, WHAT DO ?
                } else {
                    break;
                }
            }
        }
    }
}
