package fr.delcey.cinereminday.local_code_manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import fr.delcey.cinereminday.BuildConfig;
import fr.delcey.cinereminday.CRDUtils;
import fr.delcey.cinereminday.sms_scheduler.CRDAlarmReceiver;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjusters;

/**
 * Created by Nino on 24/06/2017.
 */

public class CRDTimeManager {

    private static long sDeltaEpoch; // Delta of time between the desired epoch and when we set the desired epoch

    public static void setEpoch(long desiredEpoch) {
        Log.v(CRDTimeManager.class.getName(),
              "setEpoch() called with: " + "desiredEpoch = [" + CRDUtils.makeBigNumberReadable(desiredEpoch)
                  + "], human-readable date = [" + CRDUtils.epochToHumanReadableDate(desiredEpoch) + "]");

        long currentEpoch = ZonedDateTime.now(ZoneId.of("Europe/Paris")).toInstant().toEpochMilli();

        sDeltaEpoch = desiredEpoch - currentEpoch;
    }

    public static void reset() {
        sDeltaEpoch = 0;
    }

    @NonNull
    private static ZonedDateTime getNowTime() {
        return ZonedDateTime.now(ZoneId.of("Europe/Paris")).plusSeconds(sDeltaEpoch / 1_000);
    }

    public static long getNowTimeMilli() {
        return getNowTime().toInstant().toEpochMilli();
    }

    // Wakes up the app at 8:10 AM on Tuesdays
    public static void scheduleWeeklyAlarm(@NonNull Context context) {
        Long lastAlarmTriggerEpoch = CRDSharedPreferences.getInstance(context).getLastAlarmTriggeredEpoch();

        // Schedule alarm the same day only if it didn't already ring today tuesday
        scheduleAlarm(context,
                      getNextTuesdayMorningTimestamp(
                          lastAlarmTriggerEpoch == null
                              || !isEpochBetweenTuesdayMorningAndEvening(lastAlarmTriggerEpoch)));
    }

    public static void scheduleNextWeekAlarm(@NonNull Context context) {
        scheduleAlarm(context, getNextTuesdayMorningTimestamp(false));
    }

    public static long getNextTuesdayMorningTimestamp(boolean allowSameDay) {
        ZonedDateTime nextTuesday;

        if (allowSameDay) {
            // If today is Tuesday (before 08:10), we'll get a timestamp in the future. Great !
            // If today is Tuesday (after 08:10), we'll get a timestamp in the past, AlarmManager is garanteed to
            // trigger immediatly. Neat.
            // If today is not Tuesday, everything is fine, we get next tuesday epoch
            nextTuesday = getNowTime().with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY));
        } else {
            // We are tuesday, get next week timestamp with TemporalAdjusters.next instead of TemporalAdjusters
            // .nextOrSame
            // If we are not tuesday, it doesn't "loop over" and give a timestamp in > 7 days
            nextTuesday = getNowTime().with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
        }

        nextTuesday = nextTuesday.with(LocalTime.of(8, 10)); // TODO VOLKO USER CAN SET THIS IN OPTION

        return nextTuesday.toInstant().toEpochMilli();
    }

    private static void scheduleAlarm(Context context, long epoch) {
        long nowDebug = getNowTime().toInstant().toEpochMilli();

        Log.v(CRDTimeManager.class.getName(),
              "scheduleAlarm() => Alarm scheduled to happen at epoch = [" + CRDUtils.makeBigNumberReadable(epoch)
                  + "], current epoch is = [" + CRDUtils.makeBigNumberReadable(nowDebug) + "]");
        Log.v(CRDTimeManager.class.getName(),
              "scheduleAlarm() => Alarm scheduled in = [" +
                  CRDUtils.secondsToHumanReadableCountDown(context, (int) (epoch - nowDebug) / 1_000)
                  + "], at = [" + CRDUtils.epochToHumanReadableDate(epoch) + "}");

        CRDSharedPreferences.getInstance(context).setNextAlarmEpoch(epoch);

        // Double protection, sDeltaEpoch should be != 0 only on debug mode anyway...
        if (BuildConfig.DEBUG) {
            epoch = epoch - sDeltaEpoch;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                                                                 0,
                                                                 new Intent(context, CRDAlarmReceiver.class),
                                                                 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, epoch, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, epoch, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, epoch, pendingIntent);
        }
    }

    private static long getSameOrPreviousTuesdayEveningTimestamp() {
        ZonedDateTime sameOfPreviousTuesday = getNowTime()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.TUESDAY))
            .with(LocalTime.of(23, 59, 59));

        return sameOfPreviousTuesday.toInstant().toEpochMilli();
    }

    public static boolean isTodayTuesdayBetweeenMorningAndEvening() {
        return isEpochBetweenTuesdayMorningAndEvening(getNowTime().toInstant().toEpochMilli());
    }

    public static boolean isEpochBetweenTuesdayMorningAndEvening(long epoch) {
        Instant instant = Instant.ofEpochMilli(epoch);

        return instant.isAfter(Instant.ofEpochMilli(getNextTuesdayMorningTimestamp(true)))
            && instant.isBefore(Instant.ofEpochMilli(getSameOrPreviousTuesdayEveningTimestamp()));
    }

    public static boolean isTodayTuesday() {
        return getNowTime().getDayOfWeek().equals(DayOfWeek.TUESDAY);
    }

    @Nullable
    public static Integer getSecondsBeforeNextAlarm(Context context) {
        Long nextAlarmEpoch = CRDSharedPreferences.getInstance(context).getNextAlarmEpoch();

        if (nextAlarmEpoch == null) {
            return null;
        }

        return ((int) (nextAlarmEpoch - getNowTime().toInstant().toEpochMilli())) / 1_000;
    }

    public static boolean isCinedayCodeValid(Context context) {
        Long cinedayCodeEpoch = CRDSharedPreferences.getInstance(context).getCinedayCodeEpoch();

        return cinedayCodeEpoch != null && isEpochBetweenTuesdayMorningAndEvening(cinedayCodeEpoch);
    }

    public static boolean isCinedayCodeToShareEpochValid(Context context) {
        Long cinedayCodeToShareEpoch = CRDSharedPreferences.getInstance(context).getCinedayCodeToShareEpoch();

        return cinedayCodeToShareEpoch != null && isEpochBetweenTuesdayMorningAndEvening(cinedayCodeToShareEpoch);
    }

    public static boolean isSmsSentOneHourAgo(Context context) {
        Long smsSentEpoch = CRDSharedPreferences.getInstance(context).getSendingSmsEpoch();

        if (smsSentEpoch == null) {
            return false;
        }

        Instant oneHourAgo = getNowTime().toInstant().minus(Duration.ofHours(1));
        Instant smsSentInstant = Instant.ofEpochMilli(smsSentEpoch);

        return smsSentInstant.isAfter(oneHourAgo) && smsSentInstant.isBefore(getNowTime().toInstant());
    }

    public static boolean isSmsSentToday(Context context) {
        Long smsSentEpoch = CRDSharedPreferences.getInstance(context).getSendingSmsEpoch();

        return smsSentEpoch != null && isEpochBetweenTuesdayMorningAndEvening(smsSentEpoch);
    }

    public static boolean isSmsErrorOccuredToday(Context context) {
        Long smsErrorEpoch = CRDSharedPreferences.getInstance(context).getSmsErrorEpoch();

        return smsErrorEpoch != null && isEpochBetweenTuesdayMorningAndEvening(smsErrorEpoch);
    }

    public static boolean shouldCancelNextSmsSending(Context context) {
        Long cancelNextSmsSendingEpoch = CRDSharedPreferences.getInstance(context).getCancelNextSmsSendingEpoch();

        return cancelNextSmsSendingEpoch != null && isEpochBetweenTuesdayMorningAndEvening(cancelNextSmsSendingEpoch);
    }

    public static boolean isCinedayCodeGivenToday(Context context) {
        Long cinedayCodeGivenEpoch = CRDSharedPreferences.getInstance(context).getCinedayCodeGivenEpoch();

        return cinedayCodeGivenEpoch != null && isEpochBetweenTuesdayMorningAndEvening(cinedayCodeGivenEpoch);
    }
}
