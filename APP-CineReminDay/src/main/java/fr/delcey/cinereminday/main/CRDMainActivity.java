package fr.delcey.cinereminday.main;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import fr.delcey.cinereminday.CRDAuthActivity;
import fr.delcey.cinereminday.CRDConstants;
import fr.delcey.cinereminday.CRDUtils;
import fr.delcey.cinereminday.R;
import fr.delcey.cinereminday.local_manager.CRDSharedPreferences;

import static fr.delcey.cinereminday.local_manager.CRDSharedPreferences.SHARED_PREF_KEY_CINEDAY;
import static fr.delcey.cinereminday.local_manager.CRDSharedPreferences.SHARED_PREF_KEY_SMS_SEND_EPOCH;

public class CRDMainActivity extends CRDAuthActivity implements ActivityCompat.OnRequestPermissionsResultCallback, CRDSharedPreferences.OnSharedPreferenceListener {

    // Status
    private ImageView mImageViewStatus;
    private TextView mTextViewStatusTitle;
    private TextView mTextViewStatusMessage;

    // Permission
    private CardView mCardviewSmsPermission;

    // Code
    private CardView mCardviewCinedayCode;
    private TextView mTextViewCinedayCode;

    // Share
    private CardView mCardviewShareCinedayCode;

    // Ask
    private CardView mCardviewAskCinedayCode;

    // Broadcast receiver about time
    private BroadcastReceiver mTimeTickingBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        // Status
        mImageViewStatus = (ImageView) findViewById(R.id.main_dashboard_item_status_iv_status);
        mTextViewStatusTitle = (TextView) findViewById(R.id.main_dashboard_item_status_tv_title);
        mTextViewStatusMessage = (TextView) findViewById(R.id.main_dashboard_item_status_tv_message);

        // Permission
        mCardviewSmsPermission = (CardView) findViewById(R.id.main_dashboard_item_cv_permission);
        Button buttonSmsPermissionGrant = (Button) findViewById(R.id.main_dashboard_item_permission_btn_grant_permission);
        buttonSmsPermissionGrant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAskSmsPermissionButtonClicked();
            }
        });

        // Code
        mCardviewCinedayCode = (CardView) findViewById(R.id.main_dashboard_item_cv_show_code);
        mTextViewCinedayCode = (TextView) findViewById(R.id.main_dashboard_item_show_code_tv_code);

        // Share
        mCardviewShareCinedayCode = (CardView) findViewById(R.id.main_dashboard_item_cv_share);
        Button buttonShareCinedayCodeCommunity = (Button) findViewById(R.id.main_dashboard_item_share_btn_share_code_community);
        buttonShareCinedayCodeCommunity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShareCinedayCodeCommunityButtonClicked();
            }
        });
        Button buttonShareCinedayCodeFriend = (Button) findViewById(R.id.main_dashboard_item_share_btn_share_code_friend);
        buttonShareCinedayCodeFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShareCinedayCodeFriendButtonClicked();
            }
        });

        // Ask
        mCardviewAskCinedayCode = (CardView) findViewById(R.id.main_dashboard_item_cv_ask_code);
        Button buttonAskCinedayCode = (Button) findViewById(R.id.main_dashboard_item_ask_code_btn);
        buttonAskCinedayCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAskCinedayCodeFirebaseButtonClicked();
            }
        });
    }

    private void manageCardviews() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            // Status
            mImageViewStatus.setImageResource(R.drawable.ic_error_outline_white_36dp);
            //mTextViewStatusTitle.setText(R.string.main_dashboard_status_error);
            //mTextViewStatusMessage.setText(R.string.main_dashboard_status_error_message_permission_missing);

            // Permission
            mCardviewSmsPermission.setVisibility(View.VISIBLE);

            // Cineday Code
            mCardviewCinedayCode.setVisibility(View.GONE);

            // Share code
            mCardviewShareCinedayCode.setVisibility(View.GONE);
        } else {
            mImageViewStatus.setImageResource(R.drawable.ic_done_white_36dp);

            // Permission
            mCardviewSmsPermission.setVisibility(View.GONE);

            if (CRDSharedPreferences.getInstance(this).isCinedayCodeValid()) {
                // Status
                mTextViewStatusTitle.setText(R.string.main_dashboard_status_code_ok);
                mTextViewStatusMessage.setText(R.string.main_dashboard_status_code_ok_message);

                // Cineday Code
                mCardviewCinedayCode.setVisibility(View.VISIBLE);
                mTextViewCinedayCode.setText(CRDSharedPreferences.getInstance(this).getCinedayCode());

                // Share code
                mCardviewShareCinedayCode.setVisibility(View.VISIBLE);
            } else {
                // Cineday Code
                mCardviewCinedayCode.setVisibility(View.GONE);

                // Share code
                mCardviewShareCinedayCode.setVisibility(View.GONE);

                if (CRDSharedPreferences.getInstance(this).isSmsSent1HourAgo()) {
                    // Status
                    mTextViewStatusTitle.setText(R.string.main_dashboard_status_waiting_for_orange);
                    mTextViewStatusMessage.setText(R.string.main_dashboard_status_waiting_for_orange_message);
                } else {
                    // Status
                    String howMuchTimeUntilSmsSending = CRDUtils.howMuchTimeUntilCineday(this);

                    mTextViewStatusTitle.setText(R.string.main_dashboard_status_scheduled);
                    mTextViewStatusMessage.setText(getString(R.string.main_dashboard_status_scheduled_message, howMuchTimeUntilSmsSending));
                }
            }

            CRDUtils.scheduleWeeklyAlarm(this);
        }

        if (CRDUtils.isTodayTuesday()) {
            mCardviewAskCinedayCode.setVisibility(View.VISIBLE);
        } else {
            mCardviewAskCinedayCode.setVisibility(View.GONE);
        }
    }

    private void onAskSmsPermissionButtonClicked() {
        ActivityCompat.requestPermissions(CRDMainActivity.this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS}, CRDConstants.REQUEST_CODE_SMS_PERMISSIONS);
    }

    private void onShareCinedayCodeCommunityButtonClicked() {
        // TODO VOLKO SHARE WITH FIREBASE
        Toast.makeText(this, "WIP... Stand by!", Toast.LENGTH_LONG).show();
    }

    private void onShareCinedayCodeFriendButtonClicked() {
        Uri smsUri = Uri.parse("smsto:");
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsUri);
        intent.putExtra("sms_body", getString(R.string.sms_default_message, CRDSharedPreferences.getInstance(this).getCinedayCode()));
        startActivity(intent);
    }

    private void onAskCinedayCodeFirebaseButtonClicked() {
        // TODO VOLKO QUERY ON FIREBASE
        Toast.makeText(this, "WIP... Stand by!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart() {
        super.onStart();

        mTimeTickingBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                    manageCardviews();
                }
            }
        };

        registerReceiver(mTimeTickingBroadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }


    @Override
    protected void onResume() {
        super.onResume();

        CRDSharedPreferences.getInstance(this).addOnSharedPreferenceListener(this, SHARED_PREF_KEY_CINEDAY, SHARED_PREF_KEY_SMS_SEND_EPOCH);

        manageCardviews();
    }

    @Override
    protected void onPause() {
        super.onPause();

        CRDSharedPreferences.getInstance(this).removeOnSharedPreferenceListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mTimeTickingBroadcastReceiver != null) {
            unregisterReceiver(mTimeTickingBroadcastReceiver);
        }
    }

    /**
     * We have new informations about the Permissions we asked !
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CRDConstants.REQUEST_CODE_SMS_PERMISSIONS) {
            for (int i = 0; i < permissions.length; i++) {
                if (Manifest.permission.SEND_SMS.equalsIgnoreCase(permissions[i])) {
                    if (PackageManager.PERMISSION_GRANTED == grantResults[i]) {
                        CRDUtils.scheduleWeeklyAlarm(this);
                    } else {
                        // TODO VOLKO Talk better / nicer / prettier to user !
                        Toast.makeText(CRDMainActivity.this, "I can't send SMS, I won't work correctly !", Toast.LENGTH_LONG).show();
                    }

                    break;
                }
            }

            // TODO VOLKO WHY NOT A NICE LITTLE ANIMATION ? :)
            manageCardviews();
        }
    }

    @Override
    public void onSharedPreferenceChanged(@NonNull String key, @Nullable String value) {
        // TODO VOLKO WHY NOT A NICE LITTLE ANIMATION FOR CODE ? OR EVEN SMS SENDING ^.^
        manageCardviews();
    }
}
