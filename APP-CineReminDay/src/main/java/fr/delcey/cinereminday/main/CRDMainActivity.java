package fr.delcey.cinereminday.main;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import fr.delcey.cinereminday.CRDAuthActivity;
import fr.delcey.cinereminday.CRDUtils;
import fr.delcey.cinereminday.R;
import fr.delcey.cinereminday.cloud_manager.CRDCodesActivity;

import static fr.delcey.cinereminday.CRDConstants.REQUEST_CODE_SMS_PERMISSIONS;
import static fr.delcey.cinereminday.R.id.button_sign_in;

public class CRDMainActivity extends CRDAuthActivity implements ActivityCompat.OnRequestPermissionsResultCallback, View.OnClickListener {

    private Button mButtonSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        Button buttonScheduleSmsJob = (Button) findViewById(R.id.button_start_job);
        buttonScheduleSmsJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do we have the permission to send SMS ?
                if (ContextCompat.checkSelfPermission(CRDMainActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    scheduleAlarm();
                } else {
                    ActivityCompat.requestPermissions(CRDMainActivity.this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS}, REQUEST_CODE_SMS_PERMISSIONS);
                }
            }
        });

        mButtonSignIn = (Button) findViewById(button_sign_in);
        mButtonSignIn.setOnClickListener(this);
    }

    /**
     * We have new informations about the Permissions we asked !
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_SMS_PERMISSIONS) {
            for (int i = 0; i < permissions.length; i++) {
                if (Manifest.permission.SEND_SMS.equalsIgnoreCase(permissions[i])) {
                    if (PackageManager.PERMISSION_GRANTED == grantResults[i]) {
                        scheduleAlarm();
                    } else {
                        // TODO VOLKO Talk better / nicer / prettier to user !
                        Toast.makeText(CRDMainActivity.this, "I can't send SMS, I won't work correctly !", Toast.LENGTH_LONG).show();
                    }

                    break;
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mButtonSignIn) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                startActivity(new Intent(this, CRDCodesActivity.class));
            } else {
                signInWithGoogle();
            }
        }
    }

    @Override
    protected void onFirebaseUserSignedIn(FirebaseUser user) {
        super.onFirebaseUserSignedIn(user);

        mButtonSignIn.setText(R.string.main_manage_codes);
    }

    @Override
    protected void onFirebaseUserSignedOut() {
        super.onFirebaseUserSignedOut();

        mButtonSignIn.setText(R.string.main_sign_in);
    }

    private void scheduleAlarm() {
        PendingIntent alarmIntent = CRDUtils.getAlarmIntent(CRDMainActivity.this);

        CRDUtils.scheduleWeeklyAlarm(CRDMainActivity.this, alarmIntent);

        Toast.makeText(this, "Okay, everything is correctly set !", Toast.LENGTH_LONG).show();
    }
}
