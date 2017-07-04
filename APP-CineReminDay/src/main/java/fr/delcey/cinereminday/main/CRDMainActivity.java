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
import android.support.v7.widget.CardView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.wang.avi.AVLoadingIndicatorView;

import fr.delcey.cinereminday.BuildConfig;
import fr.delcey.cinereminday.CRDAuthActivity;
import fr.delcey.cinereminday.CRDConstants;
import fr.delcey.cinereminday.CRDDebug;
import fr.delcey.cinereminday.CRDUtils;
import fr.delcey.cinereminday.R;
import fr.delcey.cinereminday.cloud_code_manager.CRDCloudCodeManager;
import fr.delcey.cinereminday.local_code_manager.CRDSharedPreferences;
import fr.delcey.cinereminday.local_code_manager.CRDTimeManager;

public class CRDMainActivity extends CRDAuthActivity implements ActivityCompat.OnRequestPermissionsResultCallback, CRDSharedPreferences.OnSharedPreferenceListener {

    // ProgressBar
    private AVLoadingIndicatorView mCustomProgressBar;
    private View mCustomProgressBarBackground;

    // Status
    private Button mButtonStatusStore;
    private Button mButtonStatusRetry;
    private ImageView mImageViewStatus;
    private TextView mTextViewStatusTitle;
    private TextView mTextViewStatusMessage;

    // Permission
    private CardView mCardviewSmsPermission;

    // Code
    private CardView mCardviewCinedayCode;
    private TextView mTextViewCinedayCode;

    // Shared - awesome
    private CardView mCardviewSharedCinedayCode;

    // Share
    private CardView mCardviewShareCinedayCode;

    // Ask
    private CardView mCardviewAskCinedayCode;

    // Telephone carrier
    private CardView mCardviewWrongCarrier;

    // Broadcast receiver about time changing
    private BroadcastReceiver mTimeChangedBroadcastReceiver;

    // Code cloud manager
    private CRDCloudCodeManager mCinedayCodeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        // ProgressBar
        mCustomProgressBar = (AVLoadingIndicatorView) findViewById(R.id.main_pb);
        mCustomProgressBarBackground = findViewById(R.id.main_pb_background);

        // Status
        mButtonStatusRetry = (Button) findViewById(R.id.main_dashboard_item_status_btn_retry);
        mButtonStatusRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRetryButtonClicked();
            }
        });
        mButtonStatusStore = (Button) findViewById(R.id.main_dashboard_item_status_btn_store);
        mButtonStatusStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onVisitStorePageButtonClicked();
            }
        });
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

        // Shared - awesome
        mCardviewSharedCinedayCode = (CardView) findViewById(R.id.main_dashboard_item_cv_awesome);

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

        // Telephone carrier
        mCardviewWrongCarrier = (CardView) findViewById(R.id.main_dashboard_item_cv_wrong_carrier);

        mCinedayCodeManager = new CRDCloudCodeManager();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (BuildConfig.DEBUG) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main_menu, menu);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_menu_show_debug:
                CRDDebug.addDebugMenu(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void manageCardviews() {
        mButtonStatusRetry.setVisibility(View.GONE);
        mButtonStatusStore.setVisibility(View.GONE);

        if (!CRDUtils.isSmsPermissionOK(this)) {
            // Status
            mImageViewStatus.setImageResource(R.drawable.ic_error_outline_white_36dp);
            mTextViewStatusTitle.setText(R.string.main_dashboard_status_error_permission_missing);
            mTextViewStatusMessage.setText(R.string.main_dashboard_status_error_permission_missing_message);

            // Permission
            mCardviewSmsPermission.setVisibility(View.VISIBLE);
        } else {
            Integer secondsBeforeNextAlarm = CRDTimeManager.getSecondsBeforeNextAlarm(this);

            // Status
            String howMuchTimeUntilSmsSending = CRDUtils.secondsToHumanReadableCountDown(this, secondsBeforeNextAlarm);

            mImageViewStatus.setImageResource(R.drawable.ic_done_white_36dp);
            mTextViewStatusTitle.setText(R.string.main_dashboard_status_scheduled);
            mTextViewStatusMessage.setText(getString(R.string.main_dashboard_status_scheduled_message, howMuchTimeUntilSmsSending));

            // Permission
            mCardviewSmsPermission.setVisibility(View.GONE);
        }

        if (CRDTimeManager.isCinedayCodeValid(this)) {
            // Status
            mTextViewStatusTitle.setText(R.string.main_dashboard_status_code_ok);
            mTextViewStatusMessage.setText(R.string.main_dashboard_status_code_ok_message);
            mButtonStatusStore.setVisibility(View.VISIBLE);

            // Cineday Code
            mCardviewCinedayCode.setVisibility(View.VISIBLE);
            mTextViewCinedayCode.setText(CRDSharedPreferences.getInstance(this).getCinedayCode());

            // Shared - awesome
            mCardviewSharedCinedayCode.setVisibility(View.GONE);

            // Share code
            mCardviewShareCinedayCode.setVisibility(View.VISIBLE);
        } else {
            // Cineday Code
            mCardviewCinedayCode.setVisibility(View.GONE);

            // Share code
            mCardviewShareCinedayCode.setVisibility(View.GONE);

            // Shared - awesome
            if (CRDTimeManager.isCinedayCodeGivenToday(this)) {
                mCardviewSharedCinedayCode.setVisibility(View.VISIBLE);
            } else {
                mCardviewSharedCinedayCode.setVisibility(View.GONE);

                // Status
                if (CRDTimeManager.isSmsSentOneHourAgo(this)) {
                    mImageViewStatus.setImageResource(R.drawable.ic_done_white_36dp);
                    mTextViewStatusTitle.setText(R.string.main_dashboard_status_waiting_for_orange);
                    mTextViewStatusMessage.setText(R.string.main_dashboard_status_waiting_for_orange_message);
                } else if (CRDTimeManager.isSmsErrorOccuredToday(this)) {
                    mButtonStatusRetry.setVisibility(View.VISIBLE);
                    mImageViewStatus.setImageResource(R.drawable.ic_error_outline_white_36dp);
                    mTextViewStatusTitle.setText(R.string.main_dashboard_status_unknown_error);
                    mTextViewStatusMessage.setText(CRDSharedPreferences.getInstance(this).getSmsError());
                }
            }
        }

        // Telephone carrier
        TelephonyManager telephonyManager = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        String carrierName = telephonyManager.getNetworkOperatorName();

        if ("Orange F".equalsIgnoreCase(carrierName)) {
            mCardviewWrongCarrier.setVisibility(View.GONE);
        } else {
            mCardviewWrongCarrier.setVisibility(View.VISIBLE);
        }

        if (CRDTimeManager.isTodayTuesday()
                && !CRDTimeManager.isCinedayCodeValid(this)) {
            mCardviewAskCinedayCode.setVisibility(View.VISIBLE);
        } else {
            mCardviewAskCinedayCode.setVisibility(View.GONE);
        }
    }

    private void onRetryButtonClicked() {
        CRDSharedPreferences.getInstance(this).setSmsError(null);

        CRDUtils.sendSmsToOrange(this);
    }

    private void onVisitStorePageButtonClicked() {
        CRDUtils.redirectToStore(this);
    }

    private void onAskSmsPermissionButtonClicked() {
        ActivityCompat.requestPermissions(CRDMainActivity.this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS}, CRDConstants.REQUEST_CODE_SMS_PERMISSIONS);
    }

    private void onShareCinedayCodeCommunityButtonClicked() {
        String cinedayCode = CRDSharedPreferences.getInstance(this).getCinedayCode();

        if (cinedayCode != null && !cinedayCode.isEmpty()) {
            if (isGoogleUserConnected() && isFirebaseAuthentificated()) {
                shareCinedayCode(cinedayCode);
            } else {
                CRDSharedPreferences.getInstance(this).setCinedayCodeToShare(cinedayCode);

                signInWithGoogle();
            }
        }
    }

    private void onShareCinedayCodeFriendButtonClicked() {
        Uri smsUri = Uri.parse("smsto:");
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsUri);
        intent.putExtra("sms_body", getString(R.string.sms_default_message, CRDSharedPreferences.getInstance(this).getCinedayCode()));
        startActivity(intent);
    }

    private void onAskCinedayCodeFirebaseButtonClicked() {
        if (isGoogleUserConnected() && isFirebaseAuthentificated()) {
            getCinedayCode();
        } else {
            signInWithGoogle();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mTimeChangedBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                manageCardviews();
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED); // Hour and minute manually set (not through tick)
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED); // Timezone changed
        intentFilter.addAction(Intent.ACTION_DATE_CHANGED); // Date manually set (not through tick)

        registerReceiver(mTimeChangedBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        CRDSharedPreferences.getInstance(this).addOnSharedPreferenceListener(this,
                CRDSharedPreferences.SHARED_PREF_KEY_CINEDAY,
                CRDSharedPreferences.SHARED_PREF_KEY_CINEDAY_EPOCH,
                CRDSharedPreferences.SHARED_PREF_KEY_SCHEDULED_ALARM_EPOCH,
                CRDSharedPreferences.SHARED_PREF_KEY_SMS_SEND_EPOCH,
                CRDSharedPreferences.SHARED_PREF_KEY_SMS_ERROR,
                CRDSharedPreferences.SHARED_PREF_KEY_CINEDAY_CODE_GIVEN_EPOCH);

        if (CRDUtils.isSmsPermissionOK(this)) {
            CRDTimeManager.scheduleWeeklyAlarm(this);
        }

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

        if (mTimeChangedBroadcastReceiver != null) {
            unregisterReceiver(mTimeChangedBroadcastReceiver);
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
                    if (PackageManager.PERMISSION_GRANTED != grantResults[i]) {
                        // TODO VOLKO Talk better / nicer / prettier to user !
                        Toast.makeText(CRDMainActivity.this, "I can't send SMS, I won't work correctly !", Toast.LENGTH_LONG).show();
                    }

                    break;
                }
            }

            CRDTimeManager.scheduleWeeklyAlarm(CRDMainActivity.this);

            // TODO VOLKO WHY NOT A NICE LITTLE ANIMATION ? :)
            manageCardviews();
        }
    }

    private void shareCinedayCode(String cinedayCodeToShare) {
        mCustomProgressBar.setVisibility(View.VISIBLE);
        mCustomProgressBarBackground.setVisibility(View.VISIBLE);

        mCinedayCodeManager.shareCinedayCode(this, cinedayCodeToShare, new CRDCloudCodeManager.OnCodeSharedCallback() {
            @Override
            public void onCodeShared() {
                mCustomProgressBar.setVisibility(View.GONE);
                mCustomProgressBarBackground.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSharingFailed(String error) {
                Log.e(CRDMainActivity.class.getName(), "shareCinedayCode.onCodeSharingFailed() called with: " + "error = [" + error + "]");

                Toast.makeText(CRDMainActivity.this, error, Toast.LENGTH_LONG).show();

                mCustomProgressBar.setVisibility(View.GONE);
                mCustomProgressBarBackground.setVisibility(View.GONE);
            }
        });
    }

    private void getCinedayCode() {
        mCustomProgressBar.setVisibility(View.VISIBLE);
        mCustomProgressBarBackground.setVisibility(View.VISIBLE);

        mCinedayCodeManager.queryCinedayCode(this, new CRDCloudCodeManager.OnCodeQueriedCallback() {
            @Override
            public void onCodeQueried() {
                mCustomProgressBar.setVisibility(View.GONE);
                mCustomProgressBarBackground.setVisibility(View.GONE);
            }

            @Override
            public void onCodeQueryingResultEmpty() {
                Toast.makeText(CRDMainActivity.this, R.string.no_available_cineday_code, Toast.LENGTH_LONG).show();

                mCustomProgressBar.setVisibility(View.GONE);
                mCustomProgressBarBackground.setVisibility(View.GONE);
            }

            @Override
            public void onCodeQueryingFailed(String error) {
                Toast.makeText(CRDMainActivity.this, error, Toast.LENGTH_LONG).show();

                mCustomProgressBar.setVisibility(View.GONE);
                mCustomProgressBarBackground.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(@NonNull String key, @Nullable Object value) {
        // TODO VOLKO WHY NOT A NICE LITTLE ANIMATION FOR CODE ? OR EVEN SMS SENDING ^.^
        manageCardviews();
    }

    @Override
    protected void onFirebaseUserSignedIn(FirebaseUser user) {
        super.onFirebaseUserSignedIn(user);

        if (CRDTimeManager.isCinedayCodeToShareEpochValid(this)) {
            String codeToShare = CRDSharedPreferences.getInstance(this).getCinedayCodeToShare();

            if (codeToShare != null) {
                shareCinedayCode(codeToShare);
            }
        }
    }
}
