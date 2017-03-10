package fr.delcey.cinereminday;

import android.Manifest;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class CRDActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    public static final int REQUEST_CODE_SMS_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Button buttonStartJob = (Button) findViewById(R.id.buttonStartJob);

        buttonStartJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do we have the permission to send SMS ?
                if (ContextCompat.checkSelfPermission(CRDActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    scheduleAlarm();
                } else {
                    ActivityCompat.requestPermissions(CRDActivity.this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_CODE_SMS_PERMISSION);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_SMS_PERMISSION) {
            for (int i = 0; i < permissions.length; i++) {
                if (Manifest.permission.SEND_SMS.equalsIgnoreCase(permissions[i])) {
                    if (PackageManager.PERMISSION_GRANTED == grantResults[i]) {
                        scheduleAlarm();
                    } else {
                        Toast.makeText(CRDActivity.this, "SMS permission not granted !", Toast.LENGTH_LONG).show();
                    }

                    break;
                }
            }
        }
    }

    private void scheduleAlarm() {
        PendingIntent alarmIntent = CRDUtils.getAlarmIntent(CRDActivity.this);

        CRDUtils.scheduleWeeklyAlarm(CRDActivity.this, alarmIntent);

        Toast.makeText(this, "Alarm scheduled, everything is done !", Toast.LENGTH_LONG).show();
    }
}
