package fr.delcey.cinereminday.local_manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArraySet;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import fr.delcey.cinereminday.CRDTimeManager;
import fr.delcey.cinereminday.CRDUtils;

/**
 * Created by Nino on 16/03/2017.
 */

public class CRDSharedPreferences implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String SHARED_PREF_IDENTIFIER = "prefs";
    private static final String SHARED_PREF_KEY_CINEDAY_EPOCH = "CINEDAY_EPOCH";
    private static final String SHARED_PREF_KEY_ERROR_EPOCH = "ERROR_EPOCH";

    public static final String SHARED_PREF_KEY_CINEDAY = "CINEDAY";
    public static final String SHARED_PREF_KEY_ERROR = "ERROR";
    public static final String SHARED_PREF_KEY_SMS_SEND_EPOCH = "SMS_SEND_EPOCH";
    public static final String SHARED_PREF_KEY_CANCEL_SMS_SENDING_EPOCH = "CANCEL_SMS_SENDING_EPOCH";

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

    public void setCinedayCode(String cinedayCode) {
        mSharedPreferences.edit()
                .putString(SHARED_PREF_KEY_CINEDAY, cinedayCode)
                .putLong(SHARED_PREF_KEY_CINEDAY_EPOCH, CRDTimeManager.getEpoch())
                .apply();
    }

    @Nullable
    public String getCinedayCode() {
        return mSharedPreferences.getString(SHARED_PREF_KEY_CINEDAY, null);
    }

    public boolean isCinedayCodeValid() {
        long codeEpoch = mSharedPreferences.getLong(SHARED_PREF_KEY_CINEDAY_EPOCH, -1);

        return CRDUtils.isEpochBetweenTuesdayMorningAndTuesdayEvening(codeEpoch);
    }

    public void setSmsSendingTimestamp() {
        mSharedPreferences.edit()
                .putLong(SHARED_PREF_KEY_SMS_SEND_EPOCH, CRDTimeManager.getEpoch())
                .apply();
    }

    public boolean isSmsSentLessThan1HourAgo() {
        long smsSendingEpoch = mSharedPreferences.getLong(SHARED_PREF_KEY_SMS_SEND_EPOCH, -1);

        if (smsSendingEpoch == -1) {
            return false;
        }

        // SMS answer from Cineday should be immediate but it could take almost an hour
        Calendar oneHourAgoEpoch = Calendar.getInstance();
        oneHourAgoEpoch.setTimeInMillis(CRDTimeManager.getEpoch());
        oneHourAgoEpoch.add(Calendar.HOUR, -1);

        return new Date(smsSendingEpoch).after(oneHourAgoEpoch.getTime());
    }

    public boolean isSmsSentToday() {
        long smsSendingEpoch = mSharedPreferences.getLong(SHARED_PREF_KEY_SMS_SEND_EPOCH, -1);

        if (smsSendingEpoch == -1) {
            return false;
        }

        return CRDUtils.isEpochBetweenTuesdayMorningAndTuesdayEvening(smsSendingEpoch);

    }

    public void setError(String message) {
        mSharedPreferences.edit()
                .putString(SHARED_PREF_KEY_ERROR, message)
                .putLong(SHARED_PREF_KEY_ERROR_EPOCH, CRDTimeManager.getEpoch())
                .apply();
    }

    @Nullable
    public String getTodayError() {
        long errorEpoch = mSharedPreferences.getLong(SHARED_PREF_KEY_ERROR_EPOCH, -1);

        if (CRDUtils.isEpochBetweenTuesdayMorningAndTuesdayEvening(errorEpoch)) {
            return mSharedPreferences.getString(SHARED_PREF_KEY_ERROR, null);
        }

        return null;
    }

    public void setCancelNextTuesdaySmsSending() { // TODO VOLKO ADD BUTTON TO STOP SMS SENDING FOR NEXT WEEK ON STATUS DASHBOARD
        mSharedPreferences.edit()
                .putLong(SHARED_PREF_KEY_CANCEL_SMS_SENDING_EPOCH, CRDTimeManager.getEpoch())
                .apply();
    }

    public boolean shouldCancelNextSmsSending() {
        long cancelEpoch = mSharedPreferences.getLong(SHARED_PREF_KEY_CANCEL_SMS_SENDING_EPOCH, -1);

        return CRDUtils.isEpochBetweenTuesdayMorningAndNextTuesdayMorning(cancelEpoch);
    }

    public void clear() {
        mSharedPreferences.edit().clear().apply();
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
                        onSharedPreferenceListener.onSharedPreferenceChanged(key, sharedPreferences.getAll().get(key));

                        break;
                    }
                }
            }
        }
    }

    public interface OnSharedPreferenceListener {
        void onSharedPreferenceChanged(@NonNull String key, @Nullable Object value);
    }
}
