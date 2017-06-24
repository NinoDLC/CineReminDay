package fr.delcey.cinereminday.sms_scheduler;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;

import fr.delcey.cinereminday.CRDUtils;

/**
 * Created by Nino on 01/03/2017.
 */

public class CRDSmsJobSchedulerService extends JobService {

    private TelephonyManager mTelephonyManager;
    private PhoneStateListener mPhoneStateListener;

    @Override
    public boolean onStartJob(final JobParameters params) {
        Log.v(CRDSmsJobSchedulerService.class.getName(), "onStartJob()");
        mTelephonyManager = (TelephonyManager) getApplicationContext().getSystemService(TELEPHONY_SERVICE);
        mPhoneStateListener = new PhoneStateListener() {
            // Fired when the service state changes or immediately after registration via .listen()
            @Override
            public void onServiceStateChanged(ServiceState serviceState) {
                super.onServiceStateChanged(serviceState);

                Log.v(CRDSmsJobSchedulerService.class.getName(), "onServiceStateChanged() => " + "serviceState = [" + serviceState.getState() + "]");

                stopListeningToCellularNetwork();

                if (serviceState.getState() == ServiceState.STATE_IN_SERVICE) {
                    CRDUtils.sendSmsToOrange(getApplicationContext());

                    jobFinished(params, false); // We did the best we could to send the SMS, job is done.
                } else {
                    jobFinished(params, true); // We couldn't launch the SMS (cellular network not reached / ok), reschedule job !
                }
            }
        };

        // We can't simply have the current state of the network. We have to register to its changes, then it will fire
        // immediately after the registration an event with the initial state. Super retarded imo.
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        stopListeningToCellularNetwork();

        return true; // Retry the job when possible, ie when cellular network is available
    }

    private void stopListeningToCellularNetwork() {
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
    }
}
