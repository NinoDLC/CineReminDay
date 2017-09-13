package fr.delcey.cinereminday.local_code_manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArraySet;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Nino on 16/03/2017.
 */

public class CRDSharedPreferences implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String SHARED_PREF_IDENTIFIER = "prefs";
    private static final String SHARED_PREF_KEY_LAST_TRIGGERED_ALARM_EPOCH = "LAST_TRIGGERED_ALARM_EPOCH";
    private static final String SHARED_PREF_KEY_CINEDAY_TO_SHARE_EPOCH = "CINEDAY_TO_SHARE_EPOCH";
    private static final String SHARED_PREF_KEY_SMS_ERROR_EPOCH = "SMS_ERROR_EPOCH";

    // Public keys to listen to events on theses
    public static final String IS_USER_OK_WITH_SMS_SENDING = "SMS_OK";
    public static final String SHARED_PREF_KEY_CINEDAY = "CINEDAY";
    public static final String SHARED_PREF_KEY_CINEDAY_EPOCH = "CINEDAY_EPOCH"; // Only for debug, if we get the same code twice on different days, it won't call onSharedPreferenceChanged because code is the same
    public static final String SHARED_PREF_KEY_CINEDAY_TO_SHARE = "CINEDAY_TO_SHARE";
    public static final String SHARED_PREF_KEY_SMS_ERROR = "SMS_ERROR";
    public static final String SHARED_PREF_KEY_SCHEDULED_ALARM_EPOCH = "SCHEDULED_ALARM_EPOCH";
    public static final String SHARED_PREF_KEY_SMS_SEND_EPOCH = "SMS_SEND_EPOCH";
    public static final String SHARED_PREF_KEY_CANCEL_SMS_SENDING_EPOCH = "CANCEL_SMS_SENDING_EPOCH";
    public static final String SHARED_PREF_KEY_CINEDAY_CODE_GIVEN_EPOCH = "CINEDAY_CODE_GIVEN_EPOCH";

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

    /**
     * DEBUG ONLY !
     */
    public void clear() {
        mSharedPreferences.edit().clear().apply();
    }

    public void setUserOkWithSmsSending(boolean ok) {
        mSharedPreferences.edit()
                .putBoolean(IS_USER_OK_WITH_SMS_SENDING, ok)
                .apply();
    }

    public boolean isUserOkWithSmsSending() {
        return mSharedPreferences.getBoolean(IS_USER_OK_WITH_SMS_SENDING, false);
    }

    public void setCinedayCode(@Nullable String cinedayCode) {
        if (cinedayCode == null) {
            mSharedPreferences.edit()
                    .remove(SHARED_PREF_KEY_CINEDAY)
                    .remove(SHARED_PREF_KEY_CINEDAY_EPOCH)
                    .apply();
        } else {
            mSharedPreferences.edit()
                    .putString(SHARED_PREF_KEY_CINEDAY, cinedayCode)
                    .putLong(SHARED_PREF_KEY_CINEDAY_EPOCH, CRDTimeManager.getNowTimeMilli())
                    .apply();
        }
    }

    @Nullable
    public String getCinedayCode() {
        return mSharedPreferences.getString(SHARED_PREF_KEY_CINEDAY, null);
    }

    @Nullable
    Long getCinedayCodeEpoch() {
        long cinedayCodeEpoch = mSharedPreferences.getLong(SHARED_PREF_KEY_CINEDAY_EPOCH, -1);

        if (cinedayCodeEpoch == -1) {
            return null;
        } else {
            return cinedayCodeEpoch;
        }
    }

    public void setCinedayCodeToShare(@Nullable String cinedayCodeToShare) {
        if (cinedayCodeToShare == null) {
            mSharedPreferences.edit()
                    .remove(SHARED_PREF_KEY_CINEDAY_TO_SHARE)
                    .remove(SHARED_PREF_KEY_CINEDAY_TO_SHARE_EPOCH)
                    .apply();
        } else {
            mSharedPreferences.edit()
                    .putString(SHARED_PREF_KEY_CINEDAY_TO_SHARE, cinedayCodeToShare)
                    .putLong(SHARED_PREF_KEY_CINEDAY_TO_SHARE_EPOCH, CRDTimeManager.getNowTimeMilli())
                    .apply();
        }
    }

    @Nullable
    public String getCinedayCodeToShare() {
        return mSharedPreferences.getString(SHARED_PREF_KEY_CINEDAY_TO_SHARE, null);
    }

    @Nullable
    Long getCinedayCodeToShareEpoch() {
        long codeToShareEpoch = mSharedPreferences.getLong(SHARED_PREF_KEY_CINEDAY_TO_SHARE_EPOCH, -1);

        if (codeToShareEpoch == -1) {
            return null;
        } else {
            return codeToShareEpoch;
        }
    }

    public void setLastAlarmTriggeredEpoch() {
        mSharedPreferences.edit()
                .putLong(SHARED_PREF_KEY_LAST_TRIGGERED_ALARM_EPOCH, CRDTimeManager.getNowTimeMilli())
                .apply();
    }

    @Nullable
    public Long getLastAlarmTriggeredEpoch() {
        long triggeredAlarmEpoch = mSharedPreferences.getLong(SHARED_PREF_KEY_LAST_TRIGGERED_ALARM_EPOCH, -1);

        if (triggeredAlarmEpoch == -1) {
            return null;
        } else {
            return triggeredAlarmEpoch;
        }
    }

    void setNextAlarmEpoch(long epoch) {
        mSharedPreferences.edit()
                .putLong(SHARED_PREF_KEY_SCHEDULED_ALARM_EPOCH, epoch)
                .apply();
    }

    @Nullable
    Long getNextAlarmEpoch() {
        long alarmEpoch = mSharedPreferences.getLong(SHARED_PREF_KEY_SCHEDULED_ALARM_EPOCH, -1);

        if (alarmEpoch == -1) {
            return null;
        } else {
            return alarmEpoch;
        }
    }

    public void setSendingSmsEpoch() {
        mSharedPreferences.edit()
                .putLong(SHARED_PREF_KEY_SMS_SEND_EPOCH, CRDTimeManager.getNowTimeMilli())
                .apply();
    }

    @Nullable
    Long getSendingSmsEpoch() {
        long sendingSmsEpoch = mSharedPreferences.getLong(SHARED_PREF_KEY_SMS_SEND_EPOCH, -1);

        if (sendingSmsEpoch == -1) {
            return null;
        } else {
            return sendingSmsEpoch;
        }
    }

    public void setSmsError(@Nullable String message) {
        if (message == null) {
            mSharedPreferences.edit()
                    .remove(SHARED_PREF_KEY_SMS_ERROR)
                    .remove(SHARED_PREF_KEY_SMS_ERROR_EPOCH)
                    .apply();
        } else {
            mSharedPreferences.edit()
                    .putString(SHARED_PREF_KEY_SMS_ERROR, message)
                    .putLong(SHARED_PREF_KEY_SMS_ERROR_EPOCH, CRDTimeManager.getNowTimeMilli())
                    .apply();
        }
    }

    @Nullable
    public String getSmsError() {
        return mSharedPreferences.getString(SHARED_PREF_KEY_SMS_ERROR, null);
    }

    @Nullable
    Long getSmsErrorEpoch() {
        long smsErrorEpoch = mSharedPreferences.getLong(SHARED_PREF_KEY_SMS_ERROR_EPOCH, -1);

        if (smsErrorEpoch == -1) {
            return null;
        } else {
            return smsErrorEpoch;
        }
    }

    public void setCancelNextSmsSendingEpoch(long epoch) { // TODO VOLKO ADD BUTTON TO STOP SMS SENDING FOR NEXT WEEK ON STATUS DASHBOARD
        mSharedPreferences.edit()
                .putLong(SHARED_PREF_KEY_CANCEL_SMS_SENDING_EPOCH, epoch)
                .apply();
    }

    @Nullable
    Long getCancelNextSmsSendingEpoch() {
        long cancelNextSmsSending = mSharedPreferences.getLong(SHARED_PREF_KEY_SMS_SEND_EPOCH, -1);

        if (cancelNextSmsSending == -1) {
            return null;
        } else {
            return cancelNextSmsSending;
        }
    }

    public void setCinedayCodeGivenEpoch() {
        mSharedPreferences.edit()
                .putLong(SHARED_PREF_KEY_CINEDAY_CODE_GIVEN_EPOCH, CRDTimeManager.getNowTimeMilli())
                .apply();
    }

    @Nullable
    Long getCinedayCodeGivenEpoch() {
        long cinedayCodeGivenEpoch = mSharedPreferences.getLong(SHARED_PREF_KEY_CINEDAY_CODE_GIVEN_EPOCH, -1);

        if (cinedayCodeGivenEpoch == -1) {
            return null;
        } else {
            return cinedayCodeGivenEpoch;
        }
    }

    public void addOnSharedPreferenceListener(@NonNull OnSharedPreferenceListener listener, @NonNull String... preferenceKeysToListen) {
        ArraySet<String> preferenceKeysListened = mSharedPreferenceListeners.get(listener);

        // One listener can listen to multiple keys changes
        if (preferenceKeysListened == null) {
            preferenceKeysListened = new ArraySet<>();
        }

        for (String key : preferenceKeysToListen) {
            if (key != null) { // varArgs accepts null values...
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
        for (OnSharedPreferenceListener onSharedPreferenceListener : mSharedPreferenceListeners.keySet()) { // Check if every listener...
            ArraySet<String> preferenceKeysListened = mSharedPreferenceListeners.get(onSharedPreferenceListener);

            if (preferenceKeysListened != null) {
                for (String preferenceKey : preferenceKeysListened) { // ... wants to listen to this key in particular
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
