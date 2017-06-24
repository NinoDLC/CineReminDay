package fr.delcey.cinereminday.local_manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArraySet;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import fr.delcey.cinereminday.CRDUtils;

/**
 * Created by Nino on 16/03/2017.
 */

public class CRDSharedPreferences implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String SHARED_PREF_IDENTIFIER = "prefs";
    private static final String SHARED_PREF_KEY_CINEDAY_EPOCH = "CINEDAY_EPOCH";

    public static final String SHARED_PREF_KEY_CINEDAY = "CINEDAY";
    public static final String SHARED_PREF_KEY_SMS_SEND_EPOCH = "SMS_SEND_EPOCH";

    // region Singleton
    private static volatile CRDSharedPreferences sCRDSharedPreferences;

    private CRDSharedPreferences(@NonNull Context context) {
        mSharedPreferences = context.getSharedPreferences(SHARED_PREF_IDENTIFIER, Context.MODE_PRIVATE);
    }

    public static CRDSharedPreferences getInstance(@NonNull Context context) {
        if (CRDSharedPreferences.sCRDSharedPreferences == null) {
            synchronized (CRDSharedPreferences.class) {
                if (CRDSharedPreferences.sCRDSharedPreferences == null) {
                    CRDSharedPreferences.sCRDSharedPreferences = new CRDSharedPreferences(context);
                }
            }
        }

        return CRDSharedPreferences.sCRDSharedPreferences;
    }
    // endregion

    private final ConcurrentHashMap<OnSharedPreferenceListener, ArraySet<String>> mSharedPreferenceListeners = new ConcurrentHashMap<>();

    private SharedPreferences mSharedPreferences;

    public void setCinedayCode(String cinedayCode, long epoch) {
        mSharedPreferences.edit()
                .putString(SHARED_PREF_KEY_CINEDAY, cinedayCode)
                .putLong(SHARED_PREF_KEY_CINEDAY_EPOCH, epoch)
                .apply();
    }

    @Nullable
    public String getCinedayCode() {
        return mSharedPreferences.getString(SHARED_PREF_KEY_CINEDAY, null);
    }

    public boolean isCinedayCodeValid() {
        long codeEpoch = mSharedPreferences.getLong(SHARED_PREF_KEY_CINEDAY_EPOCH, -1);

        // Code is valid only between 0800 and 2359, on tuesday
        Calendar tuesdayMorning = Calendar.getInstance();
        tuesdayMorning.setTimeInMillis(System.currentTimeMillis());
        tuesdayMorning.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        tuesdayMorning.set(Calendar.HOUR_OF_DAY, 8);

        long tuesdayMorningEpoch = tuesdayMorning.getTimeInMillis();

        Calendar tuesdayEvening = Calendar.getInstance();
        tuesdayEvening.setTimeInMillis(System.currentTimeMillis());
        tuesdayEvening.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        tuesdayEvening.set(Calendar.HOUR_OF_DAY, 23);
        tuesdayEvening.set(Calendar.MINUTE, 59);

        long tuesdayEveningEpoch = tuesdayEvening.getTimeInMillis();

        return CRDUtils.isEpochBetween(codeEpoch, tuesdayMorningEpoch, tuesdayEveningEpoch);
    }


    public void setSmsSendingTimestamp(long epoch) {
        mSharedPreferences.edit()
                .putLong(SHARED_PREF_KEY_SMS_SEND_EPOCH, epoch)
                .apply();
    }

    public boolean isSmsSent1HourAgo() {
        long smsSendingEpoch = mSharedPreferences.getLong(SHARED_PREF_KEY_SMS_SEND_EPOCH, -1);

        // SMS answer from Cineday should be immediate but it could take almost an hour
        Calendar nowEpoch = Calendar.getInstance();
        nowEpoch.setTimeInMillis(System.currentTimeMillis());
        nowEpoch.add(Calendar.HOUR, -1);

        return new Date(smsSendingEpoch).after(nowEpoch.getTime());
    }

    public void addOnSharedPreferenceListener(@NonNull OnSharedPreferenceListener listener, @NonNull String... preferenceKeysToListen) {
        ArraySet<String> preferenceKeysListened = mSharedPreferenceListeners.get(listener);

        if (preferenceKeysListened == null) {
            preferenceKeysListened = new ArraySet<>();
        }

        for (String key : preferenceKeysToListen) {
            if (key != null) {
                preferenceKeysListened.add(key);
            }
        }

        mSharedPreferenceListeners.put(listener, preferenceKeysListened);

        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Stop listening to all preference keys for this listener
     *
     * @param listener the listener to remove
     * @see #removeOnSharedPreferenceListener(OnSharedPreferenceListener, String...)
     */
    public void removeOnSharedPreferenceListener(@NonNull OnSharedPreferenceListener listener) {
        mSharedPreferenceListeners.remove(listener);

        if (mSharedPreferenceListeners.isEmpty()) {
            mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    /**
     * Stop listening for said preference keys for this listener
     *
     * @param listener                      the listener that stops listening these preference keys
     * @param preferenceKeysToStopListening the keys to unregister from the listener
     */
    public void removeOnSharedPreferenceListener(@NonNull OnSharedPreferenceListener listener, @NonNull String... preferenceKeysToStopListening) {
        ArraySet<String> preferenceKeysListened = mSharedPreferenceListeners.get(listener);

        if (preferenceKeysListened != null) {
            for (String key : preferenceKeysToStopListening) {
                if (key != null) {
                    preferenceKeysListened.remove(key);
                }
            }

            if (preferenceKeysListened.isEmpty()) {
                mSharedPreferenceListeners.remove(listener);
            }
        }

        if (mSharedPreferenceListeners.isEmpty()) {
            mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        for (OnSharedPreferenceListener onSharedPreferenceListener : mSharedPreferenceListeners.keySet()) {
            ArraySet<String> preferenceKeysListened = mSharedPreferenceListeners.get(onSharedPreferenceListener);

            if (preferenceKeysListened != null) {
                for (String preferenceKey : preferenceKeysListened) {
                    if (preferenceKey.equalsIgnoreCase(key)) {
                        onSharedPreferenceListener.onSharedPreferenceChanged(key, sharedPreferences.getString(key, null));

                        break;
                    }
                }
            }
        }
    }

    public interface OnSharedPreferenceListener {
        void onSharedPreferenceChanged(@NonNull String key, @Nullable String value);
    }
}
