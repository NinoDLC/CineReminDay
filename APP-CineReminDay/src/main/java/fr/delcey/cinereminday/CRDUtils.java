package fr.delcey.cinereminday;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

import fr.delcey.cinereminday.local_manager.CRDSharedPreferences;
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

    // Wakes up the app at 9:00 AM on Tuesdays
    public static void scheduleWeeklyAlarm(@NonNull Context context) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, CRDAlarmReceiver.class), 0);

        long millisecondsUntilNextTuesdayMorning = getMillisUntilNextTuesdayMorning();

        Log.v(CRDUtils.class.getName(), "scheduleWeeklyAlarm() => Alarm scheduled in : " + "millisecondsUntilNextTuesdayMorning = [" + millisecondsUntilNextTuesdayMorning + "], which is around = [" + CRDUtils.secondsToHumanReadableCountDown(context, (int) millisecondsUntilNextTuesdayMorning / 1_000) + "]");

        if (CRDUtils.isTodayTuesdayPast8AM()
                && CRDUtils.isSmsPermissionOK(context)
                && !CRDSharedPreferences.getInstance(context).isSmsSentToday()
                && !CRDSharedPreferences.getInstance(context).isCinedayCodeValid()
                && !CRDSharedPreferences.getInstance(context).shouldCancelNextSmsSending()) {
            CRDAlarmReceiver.sendSms(context);
        }

        long currentTime = System.currentTimeMillis();
        long scheduledTime = System.currentTimeMillis() + millisecondsUntilNextTuesdayMorning;

        Log.v(CRDUtils.class.getName(), "scheduleWeeklyAlarm() => Alarm scheduled to happen at : " + "scheduledTime = [" + new DecimalFormat("#,###").format(scheduledTime) + "], currentTime is = [" + new DecimalFormat("#,###").format(currentTime) + "]");

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduledTime, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, scheduledTime, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, scheduledTime, pendingIntent);
        }
    }

    public static long getMillisUntilNextTuesdayMorning() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(CRDTimeManager.getEpoch());

        setCalendarTimeToTuesdayMorning(calendar);

        if (calendar.getTimeInMillis() < CRDTimeManager.getEpoch()) {
            calendar.add(Calendar.DAY_OF_YEAR, 7);
        }

        return setCalendarTimeToTuesdayMorning(calendar).getTimeInMillis() - CRDTimeManager.getEpoch();
    }

    /**
     * DEBUG ONLY !
     */
    public static Calendar getTrueTimeTuesdayCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        return setCalendarTimeToTuesdayMorning(calendar);
    }

    private static Calendar setCalendarTimeToTuesdayMorning(Calendar calendar) {
        calendar.setFirstDayOfWeek(Calendar.WEDNESDAY); // Trick not to get a "tuesday" timestamp in the past !
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar;
    }

    public static boolean isEpochBetween(long epochBetween, long epochBefore, long epochAfter) {
        if (epochBetween >= 0 && epochBefore >= 0 && epochAfter >= 0) {
            Date dateBetween = new Date(epochBetween);

            return dateBetween.after(new Date(epochBefore)) && new Date(epochAfter).after(dateBetween);
        }

        return false;
    }

    public static String secondsToHumanReadableCountDown(Context context, int seconds) {
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

    public static boolean isTodayMonday() {
        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(CRDTimeManager.getEpoch());

        return today.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY;
    }

    public static boolean isTodayTuesday() {
        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(CRDTimeManager.getEpoch());

        return today.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY;
    }

    public static boolean isTodayTuesdayPast8AM() {
        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(CRDTimeManager.getEpoch());

        return today.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY
                && today.get(Calendar.HOUR_OF_DAY) >= 8;
    }

    public static void sendSmsToOrange(Context context) {
        if (ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            Log.v(CRDUtils.class.getName(), "sendSmsToOrange() => Sending SMS !");
            toggleSmsReceiver(context.getApplicationContext(), true);

            CRDSharedPreferences.getInstance(context).setSmsSendingTimestamp();

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(CRDUtils.ORANGE_CINEDAY_NUMBER, null, CRDUtils.ORANGE_CINEDAY_KEYWORD, null, null);
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
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        } else {
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    public static boolean isSmsPermissionOK(@NonNull Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isEpochBetweenTuesdayMorningAndTuesdayEvening(long codeEpoch) {
        // Code is valid only between 0800 and 2359, on tuesday
        Calendar tuesdayMorning = Calendar.getInstance();
        tuesdayMorning.setTimeInMillis(CRDTimeManager.getEpoch());
        tuesdayMorning.setFirstDayOfWeek(Calendar.WEDNESDAY);
        tuesdayMorning.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        tuesdayMorning.set(Calendar.HOUR_OF_DAY, 8);
        tuesdayMorning.set(Calendar.MINUTE, 0);
        tuesdayMorning.set(Calendar.SECOND, 0);
        tuesdayMorning.set(Calendar.MILLISECOND, 0);

        long tuesdayMorningEpoch = tuesdayMorning.getTimeInMillis();

        Calendar tuesdayEvening = Calendar.getInstance();
        tuesdayEvening.setTimeInMillis(CRDTimeManager.getEpoch());
        tuesdayEvening.setFirstDayOfWeek(Calendar.WEDNESDAY);
        tuesdayEvening.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        tuesdayEvening.set(Calendar.HOUR_OF_DAY, 23);
        tuesdayEvening.set(Calendar.MINUTE, 59);
        tuesdayEvening.set(Calendar.SECOND, 59);
        tuesdayEvening.set(Calendar.MILLISECOND, 999);

        long tuesdayEveningEpoch = tuesdayEvening.getTimeInMillis();

        return CRDUtils.isEpochBetween(codeEpoch, tuesdayMorningEpoch, tuesdayEveningEpoch);
    }

    public static boolean isEpochBetweenTuesdayMorningAndNextTuesdayMorning(long codeEpoch) {
        Calendar tuesdayMorning = Calendar.getInstance();
        tuesdayMorning.setTimeInMillis(CRDTimeManager.getEpoch());
        tuesdayMorning.setFirstDayOfWeek(Calendar.WEDNESDAY);
        tuesdayMorning.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        tuesdayMorning.set(Calendar.HOUR_OF_DAY, 8);
        tuesdayMorning.set(Calendar.MINUTE, 0);
        tuesdayMorning.set(Calendar.SECOND, 0);
        tuesdayMorning.set(Calendar.MILLISECOND, 0);

        long tuesdayMorningEpoch = tuesdayMorning.getTimeInMillis();

        Calendar nextTuesdayMorning = Calendar.getInstance();
        nextTuesdayMorning.setTimeInMillis(tuesdayMorningEpoch);
        nextTuesdayMorning.add(Calendar.DAY_OF_YEAR, 7);

        long nextTuesdayMorningEpoch = nextTuesdayMorning.getTimeInMillis();

        return CRDUtils.isEpochBetween(codeEpoch, tuesdayMorningEpoch, nextTuesdayMorningEpoch);
    }
}
