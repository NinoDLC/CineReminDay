package fr.delcey.cinereminday;

import android.support.annotation.Nullable;

import com.hulab.debugkit.DebugFunction;
import com.hulab.debugkit.DevTool;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.Duration;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjusters;

import java.util.Random;

import fr.delcey.cinereminday.local_code_manager.CRDSharedPreferences;
import fr.delcey.cinereminday.local_code_manager.CRDTimeManager;
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
                ZonedDateTime tuesdayMorningBeforeSmsSending = timeTravelToTuesdayMorningBeforeSmsSending();

                changeSpaceTimeContinuum(tuesdayMorningBeforeSmsSending, activity);

                return "Today is now the next tuesday (or 08:09:45 if today is Tuesday anyway), " + CRDUtils.epochToHumanReadableDate(CRDTimeManager.getNowTimeMilli());
            }
        }).addFunction(new DebugFunction("Time travel² (W)") {
            @Override
            public String call() throws Exception {
                ZonedDateTime nextWednesday = ZonedDateTime.now(ZoneId.of("Europe/Paris"))
                        .with(TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY));

                changeSpaceTimeContinuum(nextWednesday, activity);

                return "Today is now the next wednesday, " + CRDUtils.epochToHumanReadableDate(CRDTimeManager.getNowTimeMilli());
            }
        }).addFunction(new DebugFunction("Time travel³ (+1D)") {
            @Override
            public String call() throws Exception {
                ZonedDateTime tomorrow = ZonedDateTime.now(ZoneId.of("Europe/Paris"))
                        .plus(Duration.ofDays(1));

                changeSpaceTimeContinuum(tomorrow, activity);

                return "Leaped forward in time of 24 hours. Today is " + CRDUtils.epochToHumanReadableDate(CRDTimeManager.getNowTimeMilli());
            }
        }).addFunction(new DebugFunction("Time travel^4 (+7D)") {
            @Override
            public String call() throws Exception {
                ZonedDateTime tomorrow = ZonedDateTime.now(ZoneId.of("Europe/Paris"))
                        .plus(Duration.ofDays(7));

                changeSpaceTimeContinuum(tomorrow, activity);

                return "Leaped forward in time of 7 days. Today is " + CRDUtils.epochToHumanReadableDate(CRDTimeManager.getNowTimeMilli());
            }
        }).addFunction(new DebugFunction("Today is today") {
            @Override
            public String call() throws Exception {
                changeSpaceTimeContinuum(null, activity);

                return "Application time changed to what time it is now actually. No more time traveling for you ! Today is " + CRDUtils.epochToHumanReadableDate(CRDTimeManager.getNowTimeMilli());
            }
        }).addFunction(new DebugFunction("SMS sent ! (fake)") {
            @Override
            public String call() throws Exception {
                ZonedDateTime tuesday = timeTravelToTuesdayMorning();

                CRDTimeManager.setEpoch(tuesday.toInstant().toEpochMilli());

                CRDSharedPreferences.getInstance(activity).setSendingSmsEpoch();

                adjustUi(activity);

                return "SMS didn't really send, but app believes so";
            }
        }).addFunction(new DebugFunction("Cineday code received") {
            @Override
            public String call() throws Exception {
                ZonedDateTime tuesday = timeTravelToTuesdayMorning();

                CRDTimeManager.setEpoch(tuesday.toInstant().toEpochMilli());

                CRDSharedPreferences.getInstance(activity).setSendingSmsEpoch();

                StringBuilder cinedayCode = new StringBuilder(8);

                for (int i = 0; i < 8; i++) {
                    cinedayCode.append(new Random().nextInt(10));

                    if (i == 3) {
                        cinedayCode.append(' ');
                    }
                }

                CRDSharedPreferences.getInstance(activity).setCinedayCode(cinedayCode.toString());

                adjustUi(activity);

                return "Looks like we just got a Cineday code !";
            }
        }).addFunction(new DebugFunction("Send SMS (true)") {
            @Override
            public String call() throws Exception {
                ZonedDateTime tuesday = timeTravelToTuesdayMorning();

                CRDTimeManager.setEpoch(tuesday.toInstant().toEpochMilli());

                CRDUtils.sendSmsToOrange(activity);

                adjustUi(activity);

                return "Truely sent a SMS to Orange to get a Cineday code";
            }
        }).addFunction(new DebugFunction("Reset") {
            @Override
            public String call() throws Exception {
                CRDSharedPreferences.getInstance(activity).clear();

                changeSpaceTimeContinuum(null, activity);

                return "Application RESET !";
            }
        });

        builder.build();
    }

    private static ZonedDateTime timeTravelToTuesdayMorningBeforeSmsSending() {
        return ZonedDateTime.now(ZoneId.of("Europe/Paris"))
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY))
                .with(LocalTime.of(8, 9, 45));
    }

    private static ZonedDateTime timeTravelToTuesdayMorning() {
        return ZonedDateTime.now(ZoneId.of("Europe/Paris"))
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY))
                .with(LocalTime.of(8, 10, 7)); // I like number 7, lucky lucky !
    }

    private static void changeSpaceTimeContinuum(@Nullable ZonedDateTime time, CRDMainActivity activity) {
        if (time != null) {
            CRDTimeManager.setEpoch(time.toInstant().toEpochMilli());
        } else {
            CRDTimeManager.reset();
        }

        adjustUi(activity);
    }

    private static void adjustUi(CRDMainActivity activity) {
        CRDTimeManager.scheduleWeeklyAlarm(activity);

        activity.manageCardviews();
    }
}
