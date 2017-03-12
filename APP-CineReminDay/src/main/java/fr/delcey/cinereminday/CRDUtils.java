package fr.delcey.cinereminday;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.regex.Pattern;

import fr.delcey.cinereminday.sms_scheduler.CRDAlarmReceiver;
import fr.delcey.cinereminday.sms_scheduler.CRDSmsJobSchedulerService;

/**
 * Created by Nino on 09/03/2017.
 */

public class CRDUtils {
    public static final String ORANGE_CINEDAY_NUMBER = "20000";
    public static final String ORANGE_CINEDAY_KEYWORD = "ciné";

    public static final Pattern CINEDAY_CODE_PATTERN = Pattern.compile("\\d{4} \\d{4}");

    private static final int TWO_HOURS = 2 * 60 * 60 * 1_000; // 2 hours : SMS should be sent before 10:30 AM, whatever conditions are

    public static void scheduleWeeklyAlarm(Context context, PendingIntent intent) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.setFirstDayOfWeek(Calendar.WEDNESDAY); // Trick not to get a "tuesday" timestamp in the past !
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 10);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intent);
    }

    // Adds some "jitter" to SMS sending and spare some battery drain !
    public static void scheduleSmsSending(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        // ID = 0 because our job is unique !
        JobInfo.Builder builder = new JobInfo.Builder(0, new ComponentName(context.getPackageName(), CRDSmsJobSchedulerService.class.getName()));

        JobInfo job = builder.setPersisted(true) // Resists reboots
                .setOverrideDeadline(TWO_HOURS)
                .setRequiresCharging(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build();

        jobScheduler.schedule(job);
    }

    public static PendingIntent getAlarmIntent(Context context) {
        Intent intent = new Intent(context, CRDAlarmReceiver.class);

        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}
