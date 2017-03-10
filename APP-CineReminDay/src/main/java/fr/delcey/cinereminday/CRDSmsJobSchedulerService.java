package fr.delcey.cinereminday;

import android.Manifest;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Nino on 01/03/2017.
 */

public class CRDSmsJobSchedulerService extends JobService {

    private TelephonyManager mTelephonyManager;
    private PhoneStateListener mPhoneStateListener;

    @Override
    public boolean onStartJob(final JobParameters params) {
        Toast.makeText(getApplicationContext(), "JOB SCHEDULED !", Toast.LENGTH_LONG).show();

        mTelephonyManager = (TelephonyManager) getApplicationContext().getSystemService(TELEPHONY_SERVICE);
        mPhoneStateListener = new PhoneStateListener() {
            // Fired when the service state changes or immediately after registration via .listen()
            @Override
            public void onServiceStateChanged(ServiceState serviceState) {
                super.onServiceStateChanged(serviceState);

                Log.d(CRDSmsJobSchedulerService.class.getName(), "onServiceStateChanged() called with: " + "serviceState = [" + serviceState.getState() + "]");

                stopListeningToCellularNetwork();

                if (serviceState.getState() == ServiceState.STATE_IN_SERVICE) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(CRDUtils.ORANGE_CINEDAY_NUMBER, null, CRDUtils.ORANGE_CINEDAY_KEYWORD, null, null);
                    } else {
                        // TODO VOLKO Display notification to tell him he should give us the right to send SMS !
                    }

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
