package fr.delcey.cinereminday.sms_scheduler;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import fr.delcey.cinereminday.CRDUtils;
import fr.delcey.cinereminday.local_manager.CRDSharedPreferences;

/**
 * Created by Nino on 10/03/2017.
 */

public class CRDAlarmReceiver extends BroadcastReceiver {
    private static final int TWO_HOURS = 2 * 60 * 60 * 1_000; // 2 hours in ms

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(CRDAlarmReceiver.class.getName(), "onReceive() => Schedule job to send SMS");

        scheduleSmsSending(context);
    }

    public static void scheduleSmsSending(Context context) {
        Log.v(CRDAlarmReceiver.class.getName(), "scheduleSmsSending() => Using JobScheduler to send SMS in up to 2 hours...");

        CRDSharedPreferences.getInstance(context).setJobScheduled();

        // This broadcast receiver will be awaken on tuesdays, 8:10 AM precisely. Adds some "jitter" to SMS sending and
        // spare some battery drain !
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        // ID = 0 because our job is unique !
        JobInfo.Builder builder = new JobInfo.Builder(0, new ComponentName(context.getPackageName(), CRDSmsJobSchedulerService.class.getName()))
                .setPersisted(true) // Resists reboots
                .setOverrideDeadline(TWO_HOURS) // Fires anyway 2 hours later :  SMS should be sent before 10:30 AM, whatever conditions are
                .setRequiresCharging(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NOT_ROAMING); // SMS sending in roaming may cost some money
        } else {
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        }

        jobScheduler.schedule(builder.build());

        // Schedule the next week's alarm !
        // We can't use setRepeating because it won't wake up doze on API 19+
        CRDUtils.scheduleWeeklyAlarm(context);
    }
}
