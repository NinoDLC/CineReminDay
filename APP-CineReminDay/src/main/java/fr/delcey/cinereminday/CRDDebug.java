package fr.delcey.cinereminday;

import com.hulab.debugkit.DebugFunction;
import com.hulab.debugkit.DevTool;

import java.util.Calendar;
import java.util.Random;

import fr.delcey.cinereminday.local_manager.CRDSharedPreferences;
import fr.delcey.cinereminday.main.CRDMainActivity;

/**
 * Created by Nino on 24/06/2017.
 */

public class CRDDebug {
    private static boolean sSmsReceiverEnabled = true;

    public static void addDebugMenu(final CRDMainActivity activity) {
        // Add debug menu
        final DevTool.Builder builder = new DevTool.Builder(activity);

        builder.addFunction(new DebugFunction("Time travel") {
            @Override
            public String call() throws Exception {
                timeTravelToTuesdayMorning();

                activity.manageCardviews();

                CRDUtils.scheduleWeeklyAlarm(activity);

                return "Today is now the next tuesday, 8:59:40 AM.";
            }
        }).addFunction(new DebugFunction("Time travel²") {
            @Override
            public String call() throws Exception {
                Calendar calendar = CRDUtils.getTrueTimeTuesdayCalendar();
                calendar.add(Calendar.DAY_OF_YEAR, 1); // Wednesday, 09:00:00

                CRDTimeManager.setEpoch(calendar.getTimeInMillis());

                activity.manageCardviews();

                CRDUtils.scheduleWeeklyAlarm(activity);

                return "Today is now the next wednesday, 9:00:00 AM.";
            }
        }).addFunction(new DebugFunction("Today is today") {
            @Override
            public String call() throws Exception {
                CRDTimeManager.reset();

                activity.manageCardviews();

                CRDUtils.scheduleWeeklyAlarm(activity);

                return "Application time changed to what time it is now actually. No more time traveling for you !";
            }
        }).addFunction(new DebugFunction("SMS sent ! (fake)") {
            @Override
            public String call() throws Exception {
                timeTravelToTuesdayMorning();

                CRDSharedPreferences.getInstance(activity).setSmsSendingTimestamp();

                return "SMS didn't really send, but app believes so";
            }
        }).addFunction(new DebugFunction("Cineday code received") {
            @Override
            public String call() throws Exception {
                timeTravelToTuesdayMorning();

                StringBuilder cinedayCode = new StringBuilder(8);

                for (int i = 0; i < 8; i++) {
                    cinedayCode.append(new Random().nextInt(10));

                    if (i == 3) {
                        cinedayCode.append(' ');
                    }
                }

                CRDSharedPreferences.getInstance(activity).setCinedayCode(cinedayCode.toString());

                return "Looks like we just got a Cineday code !";
            }
        }).addFunction(new DebugFunction("Send SMS (true)") {
            @Override
            public String call() throws Exception {
                timeTravelToTuesdayMorning();

                CRDUtils.sendSmsToOrange(activity);

                return "Truely sent a SMS to Orange to get a Cineday code";
            }
        }).addFunction(new DebugFunction("Toggle SMS receiver") {
            @Override
            public String call() throws Exception {
                sSmsReceiverEnabled = !sSmsReceiverEnabled;

                CRDUtils.toggleSmsReceiver(activity, sSmsReceiverEnabled);

                return "SMS Receiver is now : " + (sSmsReceiverEnabled ? "ON" : "OFF");
            }
        });

        builder.build();
    }

    private static void timeTravelToTuesdayMorning() {
        Calendar calendar = CRDUtils.getTrueTimeTuesdayCalendar();
        calendar.add(Calendar.SECOND, -20); // 8:59:40

        CRDTimeManager.setEpoch(calendar.getTimeInMillis());
    }
}
