package fr.delcey.cinereminday.cloud_manager;

import android.widget.Toast;

import fr.delcey.cinereminday.CRDAuthActivity;

/**
 * Created by Nino on 12/03/2017.
 */

public class CRDCodesActivity extends CRDAuthActivity {
    /*
        buttonAddTuesday.setText("add tuesday to tuesdays");
                    buttonAddTuesday.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String randomString = String.valueOf(new Random().nextInt());

                            Tuesday tuesday = new Tuesday(randomString);

                            mDatabase.child("tuesdays").push().child("tuesday").setValue(tuesday);
                        }
                    });

                    mDatabase.child("tuesdays").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Toast.makeText(getApplicationContext(), "NOUVELLE STRING : " + dataSnapshot.child("tuesday").getValue(), Toast.LENGTH_LONG ).show();
                            //adapter.add((String) dataSnapshot.child("title").getValue());
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                            Toast.makeText(getApplicationContext(), "STRING DELETED : " + dataSnapshot.child("tuesday").getValue(), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
         */

    // TODO VOLKO CREATE ACTIVITY TO MANAGE CODES WITH FIREBASE !

    @Override
    protected void onFirebaseUserSignedOut() {
        super.onFirebaseUserSignedOut();

        Toast.makeText(this, "You have been disconnected from Firebase !", Toast.LENGTH_LONG).show();

        finish();
    }

    @Override
    protected void onFirebaseConnectionFailed() {
        super.onFirebaseConnectionFailed();

        Toast.makeText(this, "You have been disconnected from Firebase !", Toast.LENGTH_LONG).show();

        finish();
    }
}
