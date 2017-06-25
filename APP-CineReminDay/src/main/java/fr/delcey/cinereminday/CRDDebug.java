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
    public static void addDebugMenu(final CRDMainActivity activity) {
        // Add debug menu
        final DevTool.Builder builder = new DevTool.Builder(activity);

        builder.addFunction(new DebugFunction("Time travel") {
            @Override
            public String call() throws Exception {
                timeTravelToTuesdayMorning();

                activity.manageCardviews();

                CRDUtils.scheduleWeeklyAlarm(activity);

                return "Today is now the next tuesday, " + CRDUtils.epochToHumanReadableDate(CRDTimeManager.getEpoch());
            }
        }).addFunction(new DebugFunction("Time travel² (W)") {
            @Override
            public String call() throws Exception {
                Calendar calendar = CRDUtils.getTrueTimeTuesdayCalendar();
                calendar.add(Calendar.DAY_OF_YEAR, 1); // Wednesday, 08:10:00

                CRDTimeManager.setEpoch(calendar.getTimeInMillis());

                activity.manageCardviews();

                CRDUtils.scheduleWeeklyAlarm(activity);

                return "Today is now the next wednesday, " +CRDUtils.epochToHumanReadableDate(CRDTimeManager.getEpoch());
            }
        }).addFunction(new DebugFunction("Time travel³ (+1D)") {
            @Override
            public String call() throws Exception {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(CRDTimeManager.getEpoch());
                calendar.add(Calendar.DAY_OF_YEAR, 1);

                CRDTimeManager.setEpoch(calendar.getTimeInMillis());

                activity.manageCardviews();

                CRDUtils.scheduleWeeklyAlarm(activity);

                return "Leaped forward in time of 24 hours. Today is now " + CRDUtils.epochToHumanReadableDate(CRDTimeManager.getEpoch());
            }
        }).addFunction(new DebugFunction("Time travel^4 (+7D)") {
            @Override
            public String call() throws Exception {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(CRDTimeManager.getEpoch());
                calendar.add(Calendar.DAY_OF_YEAR, 7);

                CRDTimeManager.setEpoch(calendar.getTimeInMillis());

                activity.manageCardviews();

                CRDUtils.scheduleWeeklyAlarm(activity);

                return "Leaped forward in time of 7 days. Today is now " + CRDUtils.epochToHumanReadableDate(CRDTimeManager.getEpoch());
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
        }).addFunction(new DebugFunction("Reset") {
            @Override
            public String call() throws Exception {
                CRDTimeManager.reset();

                CRDSharedPreferences.getInstance(activity).clear();

                activity.manageCardviews();

                CRDUtils.scheduleWeeklyAlarm(activity);

                return "Application RESET !";
            }
        });

        builder.build();
    }

    private static void timeTravelToTuesdayMorning() {
        Calendar calendar = CRDUtils.getTrueTimeTuesdayCalendar();
        calendar.add(Calendar.SECOND, -20); // 8:09:40

        CRDTimeManager.setEpoch(calendar.getTimeInMillis());
    }
}
