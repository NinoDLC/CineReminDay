package fr.delcey.cinereminday;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import fr.delcey.cinereminday.local_code_manager.CRDSharedPreferences;
import fr.delcey.cinereminday.local_code_manager.CRDSmsReceiver;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * Created by Nino on 09/03/2017.
 */

public class CRDUtils {

    private static final DecimalFormat sDecimalFormat = new DecimalFormat("#,###");

    public static final int MINUTE = 60;
    public static final int HOUR = 60 * MINUTE;
    public static final int DAY = 24 * HOUR;

    public static final String ORANGE_CINEDAY_NUMBER = "20000";
    public static final String ORANGE_CINEDAY_KEYWORD = "ciné";

    public static void sendSmsToOrange(Context context) {
        if (ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.SEND_SMS)
            == PackageManager.PERMISSION_GRANTED) {
            Log.v(CRDUtils.class.getName(), "sendSmsToOrange() => Sending SMS !");

            toggleSmsReceiver(context.getApplicationContext(), true);

            CRDSharedPreferences.getInstance(context).setSendingSmsEpoch();

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(CRDUtils.ORANGE_CINEDAY_NUMBER,
                                       null,
                                       CRDUtils.ORANGE_CINEDAY_KEYWORD,
                                       null,
                                       null);
        } else {
            Log.e(CRDUtils.class.getName(), "sendSmsToOrange() => FAILED TO SEND SMS ! (Missing permission)");

            // TODO VOLKO Display notification to tell him he should give us the right to send SMS !
        }
    }

    public static void toggleSmsReceiver(@NonNull Context context, boolean enable) {
        Log.v(CRDUtils.class.getName(), "toggleSmsReceiver() => enable = [" + enable + "]");
        PackageManager packageManager = context.getPackageManager();
        ComponentName componentName = new ComponentName(context.getApplicationContext(), CRDSmsReceiver.class);
        if (enable) {
            packageManager.setComponentEnabledSetting(componentName,
                                                      PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                                      PackageManager.DONT_KILL_APP);
        } else {
            packageManager.setComponentEnabledSetting(componentName,
                                                      PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                                      PackageManager.DONT_KILL_APP);
        }
    }

    public static boolean isSmsPermissionOK(@NonNull Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
            == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS)
            == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    public static void redirectToStore(@NonNull Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String playStoreLink = String.format("market://details?id=%s",
                                             context.getApplicationContext().getPackageName());
        intent.setData(Uri.parse(playStoreLink));
        context.startActivity(intent);
    }

    public static String secondsToHumanReadableCountDown(Context context, @Nullable Integer seconds) {
        if (seconds == null) {
            return context.getString(R.string.unknown_amount_of_time);
        }

        if (seconds <= 0) {
            return context.getString(R.string.less_than_a_minute);
        }

        int days = seconds / DAY;
        int hours = (seconds % DAY) / HOUR;
        int minutes = ((seconds % DAY) % HOUR) / MINUTE;

        if (seconds < MINUTE) {
            return context.getString(R.string.less_than_a_minute);
        }

        StringBuilder result = new StringBuilder();

        if (days >= 1) {
            result.append(days).append(" ");

            if (days == 1) {
                result.append(context.getString(R.string.day));
            } else {
                result.append(context.getString(R.string.days));
            }

            if (hours >= 1) {
                if (minutes >= 1) {
                    result.append(", ");
                } else {
                    result.append(" ").append(context.getString(R.string.and)).append(" ");
                }
            } else if (minutes >= 1) {
                result.append(" ").append(context.getString(R.string.and)).append(" ");
            }
        }

        if (hours >= 1) {
            result.append(hours).append(" ");

            if (hours == 1) {
                result.append(context.getString(R.string.hour));
            } else {
                result.append(context.getString(R.string.hours));
            }

            if (minutes >= 1) {
                result.append(" ").append(context.getString(R.string.and)).append(" ");
            }
        }

        if (minutes >= 1) {
            result.append(minutes).append(" ");

            if (minutes == 1) {
                result.append(context.getString(R.string.minute));
            } else {
                result.append(context.getString(R.string.minutes));
            }
        }

        return result.toString();
    }

    public static String epochToHumanReadableDate(long epoch) {
        return DateFormat.getDateTimeInstance().format(new Date(epoch));
    }

    public static String makeBigNumberReadable(long number) {
        return sDecimalFormat.format(number);
    }
}
