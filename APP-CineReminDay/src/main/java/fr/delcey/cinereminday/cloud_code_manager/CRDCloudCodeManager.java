package fr.delcey.cinereminday.cloud_code_manager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import fr.delcey.cinereminday.cloud_code_manager.beans.CinedayCode;
import fr.delcey.cinereminday.local_code_manager.CRDSharedPreferences;
import fr.delcey.cinereminday.main.CRDMainActivity;
import java.util.Random;

/**
 * Created by Nino on 25/06/2017.
 */

public class CRDCloudCodeManager {

    private boolean mIsSharingCode;
    private boolean mIsQueryingCode;
    private boolean mIsAccessingCode;

    public void shareCinedayCode(final Context context,
                                 String cinedayCodeToShare,
                                 final OnCodeSharedCallback callback) {
        if (!mIsSharingCode) {
            mIsSharingCode = true;

            FirebaseDatabase.getInstance()
                            .getReference()
                            .child("codes")
                            .push()
                            .setValue(new CinedayCode(cinedayCodeToShare)); // Push code to database

            FirebaseDatabase.getInstance()
                            .getReference()
                            .child("codes")
                            .orderByChild("code")
                            .equalTo(cinedayCodeToShare)
                            .addListenerForSingleValueEvent(new ValueEventListener() { // Listen to this code being
                                // saved to Firebase
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Log.d(CRDCloudCodeManager.class.getName(),
                                          "shareCinedayCode.onDataChange() called with: " + "dataSnapshot = ["
                                              + dataSnapshot + "]");

                                    mIsSharingCode = false;

                                    CRDSharedPreferences.getInstance(context).setCinedayCode(null);
                                    CRDSharedPreferences.getInstance(context).setCinedayCodeGivenEpoch();
                                    CRDSharedPreferences.getInstance(context).setCinedayCodeToShare(null);

                                    callback.onCodeShared();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e(CRDCloudCodeManager.class.getName(),
                                          "shareCinedayCode.onCancelled() called with: " + "databaseError = ["
                                              + databaseError + "]");

                                    mIsSharingCode = false;

                                    callback.onCodeSharingFailed(databaseError.getMessage());
                                }
                            });
        }
    }

    public void queryCinedayCode(final Context context, final OnCodeQueriedCallback callback) {
        if (!mIsQueryingCode) {
            mIsQueryingCode = true;

            FirebaseDatabase.getInstance()
                            .getReference()
                            .child("codes")
                            .orderByChild("available")
                            .equalTo(true)
                            .addListenerForSingleValueEvent(new CinedayCloudCodeEventListener(context, callback));
        }
    }

    private class CinedayCloudCodeEventListener implements ValueEventListener {

        private Context mContext;
        private OnCodeQueriedCallback mCallback;

        CinedayCloudCodeEventListener(Context context, OnCodeQueriedCallback callback) {
            mContext = context;
            mCallback = callback;
        }

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.v(CRDMainActivity.class.getName(),
                  "queryCinedayCode.onDataChange() called with: " + "dataSnapshot = [" + dataSnapshot + "]");

            if (dataSnapshot.getChildrenCount() > 0) {
                long randomChildren = (long) (new Random().nextDouble() * dataSnapshot.getChildrenCount());

                long i = 0;

                for (DataSnapshot codeDataSnapshot : dataSnapshot.getChildren()) {
                    // Ugly but Firebase doesn't have .get(i), only an iterator available...
                    if (i == randomChildren) {
                        final CinedayCode code = codeDataSnapshot.getValue(CinedayCode.class);
                        if (code == null || codeDataSnapshot.getKey() == null) {
                            continue;
                        }

                        mIsAccessingCode = true;

                        code.setAvailable(false);

                        FirebaseDatabase.getInstance()
                                        .getReference()
                                        .child("codes")
                                        .child(codeDataSnapshot.getKey())
                                        .setValue(code) // Save to Firebase the fact we took this code
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.d(CRDCloudCodeManager.class.getName(),
                                                      "queryCinedayCode.onComplete() called !");

                                                mIsQueryingCode = false;
                                                mIsAccessingCode = false;

                                                CRDSharedPreferences.getInstance(mContext)
                                                                    .setCinedayCode(code.getCode());

                                                mCallback.onCodeQueried();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e(CRDCloudCodeManager.class.getName(),
                                                      "queryCinedayCode.onFailure() called !");

                                                e.printStackTrace();

                                                mIsQueryingCode = false;
                                                mIsAccessingCode = false;

                                                mCallback.onCodeQueryingFailed(e.getMessage());
                                            }
                                        });

                        break;
                    }

                    i++;
                }

                if (!mIsAccessingCode) {
                    mIsQueryingCode = false;
                }
            } else {
                mIsQueryingCode = false;

                mCallback.onCodeQueryingResultEmpty();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(CRDMainActivity.class.getName(),
                  "queryCinedayCode.onCancelled() called with: " + "databaseError = ["
                      + databaseError + "]");

            mIsQueryingCode = false;

            mCallback.onCodeQueryingFailed(databaseError.getMessage());
        }
    }

    public interface OnCodeSharedCallback {

        void onCodeShared();

        void onCodeSharingFailed(String error);
    }

    public interface OnCodeQueriedCallback {

        void onCodeQueried();

        void onCodeQueryingResultEmpty();

        void onCodeQueryingFailed(String error);
    }
}
