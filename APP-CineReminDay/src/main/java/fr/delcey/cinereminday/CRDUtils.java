package fr.delcey.cinereminday;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

import fr.delcey.cinereminday.local_manager.CRDSmsReceiver;
import fr.delcey.cinereminday.sms_scheduler.CRDAlarmReceiver;

/**
 * Created by Nino on 09/03/2017.
 */

public class CRDUtils {
    public static final int MINUTE = 60;
    public static final int HOUR = 60 * MINUTE;
    public static final int DAY = 24 * HOUR;

    public static final String ORANGE_CINEDAY_NUMBER = "20000";
    public static final String ORANGE_CINEDAY_KEYWORD = "ciné";

    // Wakes up the app at 8:10 AM on Tuesdays
    public static void scheduleWeeklyAlarm(@NonNull Context context) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, CRDAlarmReceiver.class), 0);

        Calendar tuesday = getTuesdayCalendar();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, tuesday.getTimeInMillis(), pendingIntent);
    }

    private static Calendar getTuesdayCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.setFirstDayOfWeek(Calendar.WEDNESDAY); // Trick not to get a "tuesday" timestamp in the past !
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 10);

        return calendar;
    }

    public static boolean isEpochBetween(long epochBetween, long epochBefore, long epochAfter) {
        if (epochBetween >= 0 && epochBefore >= 0 && epochAfter >= 0) {
            Date dateBetween = new Date(epochBetween);

            return dateBetween.after(new Date(epochBefore)) && new Date(epochAfter).after(dateBetween);
        }

        return false;
    }

    public static String howMuchTimeUntilCineday(Context context) {
        Calendar tuesday = getTuesdayCalendar();

        int secondsBeforeNextTuesday = (int) ((tuesday.getTimeInMillis() - System.currentTimeMillis()) / 1_000);

        if (secondsBeforeNextTuesday == 0) {
            return context.getString(R.string.now);
        }

        int days = secondsBeforeNextTuesday / DAY;
        int hours = (secondsBeforeNextTuesday % DAY) / HOUR;
        int minutes = ((secondsBeforeNextTuesday % DAY) % HOUR) / MINUTE;

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

            if (days == 1) {
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

    public static boolean isTodayTuesday() {
        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(System.currentTimeMillis());

        return today.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY;
    }

    public static void toggleSmsReceiver(@NonNull Context context, boolean enable) {
        PackageManager packageManager = context.getPackageManager();
        ComponentName componentName = new ComponentName(context.getApplicationContext(), CRDSmsReceiver.class);
        if (enable) {
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        } else {
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }
}
