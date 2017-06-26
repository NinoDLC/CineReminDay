package fr.delcey.cinereminday.cloud_manager;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

import fr.delcey.cinereminday.cloud_manager.beans.CinedayCode;
import fr.delcey.cinereminday.main.CRDMainActivity;

/**
 * Created by Nino on 25/06/2017.
 */

public class CRDCloudCodeManager {
    private boolean mIsSharingCode;
    private boolean mIsQueryingCode;

    public void shareCinedayCode(String cinedayCodeToShare, final OnCodeSharedCallback callback) {
        if (!mIsSharingCode) {
            mIsSharingCode = true;

            FirebaseDatabase.getInstance().getReference().child("codes").push().setValue(new CinedayCode(cinedayCodeToShare)); // Push code to database

            FirebaseDatabase.getInstance().getReference().child("codes").orderByChild("code").equalTo(cinedayCodeToShare).addListenerForSingleValueEvent(new ValueEventListener() { // Listen to this code being saved to Firebase
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(CRDCloudCodeManager.class.getName(), "shareCinedayCode.onDataChange() called with: " + "dataSnapshot = [" + dataSnapshot + "]");

                    mIsSharingCode = false;

                    callback.onCodeShared();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(CRDCloudCodeManager.class.getName(), "shareCinedayCode.onCancelled() called with: " + "databaseError = [" + databaseError + "]");

                    mIsSharingCode = false;

                    callback.onCodeSharingFailed(databaseError.getMessage());
                }
            });
        }
    }

    public void queryCinedayCode(final OnCodeQueriedCallback callback) {
        if (!mIsQueryingCode) {
            mIsQueryingCode = true;

            FirebaseDatabase.getInstance().getReference().child("codes").orderByChild("available").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.v(CRDMainActivity.class.getName(), "queryCinedayCode.onDataChange() called with: " + "dataSnapshot = [" + dataSnapshot + "]");

                    if (dataSnapshot.getChildrenCount() > 0) {
                        int randomChildren = new Random().nextInt((int) dataSnapshot.getChildrenCount()); // I hope that we won't reach 2^31 available Cineday codes :p
                        long i = 0;

                        for (DataSnapshot codeDataSnapshot : dataSnapshot.getChildren()) { // Ugly but Firebase doesn't have .get(i), only an iterator available...
                            if (i == randomChildren) {
                                final CinedayCode code = codeDataSnapshot.getValue(CinedayCode.class);
                                code.setAvailable(false);

                                FirebaseDatabase.getInstance().getReference().child("codes").child(codeDataSnapshot.getKey()).setValue(code) // Save to Firebase the fact we took this code
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.d(CRDCloudCodeManager.class.getName(), "queryCinedayCode.onComplete() called !");

                                                mIsQueryingCode = false;

                                                callback.onCodeQueried(code.getCode());
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e(CRDCloudCodeManager.class.getName(), "queryCinedayCode.onFailure() called !");

                                                e.printStackTrace();

                                                mIsQueryingCode = false;

                                                callback.onCodeQueryingFailed(e.getMessage());
                                            }
                                        });

                                break;
                            }

                            i++;
                        }
                    } else {
                        mIsQueryingCode = false;

                        callback.onCodeQueryingResultEmpty();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(CRDMainActivity.class.getName(), "queryCinedayCode.onCancelled() called with: " + "databaseError = [" + databaseError + "]");

                    mIsQueryingCode = false;

                    callback.onCodeQueryingFailed(databaseError.getMessage());
                }
            });
        }
    }

    public interface OnCodeSharedCallback {
        void onCodeShared();

        void onCodeSharingFailed(String error);
    }

    public interface OnCodeQueriedCallback {
        void onCodeQueried(String cinedayCode);

        void onCodeQueryingResultEmpty();

        void onCodeQueryingFailed(String error);
    }
}
