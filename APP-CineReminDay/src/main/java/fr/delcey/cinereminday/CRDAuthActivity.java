package fr.delcey.cinereminday;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Activity that handles the authentification flow with Google accounts and Firebase.
 * <p>
 * 1. Sign in with Google account
 * 2. With that credentials, sign in to Firebase
 * 3. Enjoy !
 * <p>
 * Created by Nino on 12/03/2017.
 */
public abstract class CRDAuthActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private boolean mFirebaseAuthentificated = false;

    private FirebaseAuth.AuthStateListener mFirebaseAuthListener;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initGoogleSignInAuth();

        mFirebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    if (!mFirebaseAuthentificated) {
                        mFirebaseAuthentificated = true;

                        onFirebaseUserSignedIn(user);
                    }
                } else {
                    mFirebaseAuthentificated = false;
                    onFirebaseUserSignedOut();
                }
            }
        };
    }

    /**
     * Boilerplate code for Google Sign-in authentification feature
     */
    private void initGoogleSignInAuth() {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseAuth.getInstance().addAuthStateListener(mFirebaseAuthListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == CRDConstants.REQUEST_CODE_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                logInToFirebaseWithGoogleSignIn(account);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mFirebaseAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mFirebaseAuthListener);
        }
    }

    protected boolean isGoogleUserConnected() {
        return mGoogleApiClient != null && mGoogleApiClient.isConnected();
    }

    protected boolean isFirebaseAuthentificated() {
        return mFirebaseAuthentificated;
    }

    private void logInToFirebaseWithGoogleSignIn(GoogleSignInAccount googleSignInAccount) {
        Log.v(CRDAuthActivity.class.getName(), "logInToFirebaseWithGoogleSignIn() called with: " + "googleSignInAccount = [" + googleSignInAccount + "]");

        // TODO VOLKO MAKE USER WAIT

        AuthCredential credential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);

        FirebaseAuth.getInstance()
                .signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.v(CRDAuthActivity.class.getName(), "logInToFirebaseWithGoogleSignIn.onComplete() called with: " + "isSuccessful = [" + task.isSuccessful() + "]");

                        // TODO VOLKO MAKE USER DE-WAIT

                        // If sign in succeeds the auth state listener will be notified and logic to handle the signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            onFirebaseConnectionFailed();
                        }
                    }
                });
    }

    /**
     * Google sign in connection failed
     */
    @Override
    public final void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.v(CRDAuthActivity.class.getName(), "onConnectionFailed() called with: " + "connectionResult = [" + connectionResult.toString() + "]");

        onGoogleConnectionFailed();
    }

    /**
     * User has connected to Firebase.
     *
     * @param user the currently logged-in user
     */
    protected void onFirebaseUserSignedIn(FirebaseUser user) {
        Log.v(CRDAuthActivity.class.getName(), "onFirebaseUserSignedIn() called, USER IS SIGNED IN ! " + "firebaseAuth = [" + user.getUid() + "]");
    }

    /**
     * User has not / never connected to Firebase.
     */
    protected void onFirebaseUserSignedOut() {
        mFirebaseAuthentificated = false;

        Log.v(CRDAuthActivity.class.getName(), "onFirebaseUserSignedOut() called, USER IS SIGNED OUT ! ");
    }

    /**
     * Part 1 of sign in failed : user canceled the pop up to connect or used bad credentials
     */
    protected void onGoogleConnectionFailed() {
        mFirebaseAuthentificated = false;
    }

    /**
     * Part 2 of sign in failed : we couldn't auth the google user through firebase. This is bad and this is our fault
     */
    protected void onFirebaseConnectionFailed() {
        mFirebaseAuthentificated = false;
    }

    /**
     * Launch the auth flow to sign in with a Google account, that will request a FirebaseAuth later on with
     * Google's credentials.
     */
    public final void signInWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, CRDConstants.REQUEST_CODE_GOOGLE_SIGN_IN);
    }
}
