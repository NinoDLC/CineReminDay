package fr.delcey.cinereminday.local_manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import java.util.regex.Matcher;

import fr.delcey.cinereminday.CRDUtils;

/**
 * Created by Nino on 10/03/2017.
 */

public class CRDSmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
            if (smsMessage != null) {
                String sender = smsMessage.getDisplayOriginatingAddress();

                if (CRDUtils.ORANGE_CINEDAY_NUMBER.equalsIgnoreCase(sender)) {
                    String message = smsMessage.getDisplayMessageBody();

                    Matcher matcher = CRDUtils.CINEDAY_CODE_PATTERN.matcher(message);
                    if (matcher.matches()) {
                        // TODO VOLKO SAVE THIS TO FIREBASE !
                        String cinedayCode = matcher.group();
                    }
                }
            }
        }
    }
}
