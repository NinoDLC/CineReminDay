package fr.delcey.cinereminday;

import android.util.Log;

/**
 * Created by Nino on 24/06/2017.
 */

public class CRDTimeManager {
    private static long sDeltaEpoch; // Delta of time between the desired epoch and when we set the desired epoch

    public static void setEpoch(long desiredEpoch) {
        Log.v(CRDTimeManager.class.getName(), "setEpoch() called with: " + "desiredEpoch = [" + desiredEpoch + "], human-readable date = [" + CRDUtils.epochToHumanReadableDate(desiredEpoch) + "]");

        long currentEpoch = System.currentTimeMillis();

        sDeltaEpoch = desiredEpoch - currentEpoch;
    }

    public static long getEpoch() {
        return System.currentTimeMillis() + sDeltaEpoch;
    }

    public static void reset() {
        sDeltaEpoch = 0;
    }
}