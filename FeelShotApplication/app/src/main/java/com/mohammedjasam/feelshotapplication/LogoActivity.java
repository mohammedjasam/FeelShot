package com.mohsinhaider.feelshotapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
//import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.ActionBarActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LogoActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

        private static final String TAG = "SignInActivity";
        private static final int RC_SIGN_IN = 9001;

        private GoogleApiClient mGoogleApiClient;
        private TextView mStatusTextView;
        private ProgressDialog mProgressDialog;
        private FirebaseAuth mAuth;

        private FirebaseAuth.AuthStateListener mAuthListener;

        private DatabaseReference mReference;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_logo);

            if (getString(R.string.subscription_key).startsWith("Please")) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.add_subscription_key_tip_title))
                        .setMessage(getString(R.string.add_subscription_key_tip))
                        .setCancelable(false)
                        .show();
            }


            // References ... what to put for the sub-child !??!!?
            mReference = FirebaseDatabase.getInstance().getReference();

            findViewById(R.id.sign_in_button).setOnClickListener(this);

            // Mohsin: The GoogleSignInOptions is requesting basic info with the default
            // "DEFAULT_SIGN_IN" option, and we call .requestEmail() and .build() for the email
            // and building the structure
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    // in the case below doesn't work, try:
                    //.requestIdToken("527626849767-rdmvgvsu0g8m4fen602rjupl5c7to812.apps.googleusercontent.com")
                    .requestIdToken(getString(R.string.web_client_id))
                    .requestEmail()
                    .build();

            // Mohsin: Getting access to the GoogleApiClient (so we can log in)
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();

            // Authentica with Firebase
            mAuth = FirebaseAuth.getInstance();

            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        // User is signed in
                        Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    } else {
                        // User is signed out
                        Log.d(TAG, "onAuthStateChanged:signed_out");
                    }
                    updateUI(user);
                }
            };

            SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
            signInButton.setSize(SignInButton.SIZE_WIDE);
            signInButton.setScopes(gso.getScopeArray());

        }

        private void signIn() {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }

        @Override
        public void onStart() {
            super.onStart();
            mAuth.addAuthStateListener(mAuthListener);
        }

        @Override
        public void onStop() {
            super.onStop();
            if (mAuthListener != null) {
                mAuth.removeAuthStateListener(mAuthListener);
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.sign_in_button:
                    signIn();
                    break;
                // Other cases for if they sign out or disconnect can be included.
            }
        }

        // HERE IS WHERE THE THING ENDS +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result.isSuccess()) {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = result.getSignInAccount();
                    firebaseAuthWithGoogle(account);
                    Log.d("STATUS", "SUCCESS");
                    Toast.makeText(this, "Signing In...", Toast.LENGTH_SHORT).show();

                    // REACCESS "RecognizeActivity"....
//                    Intent myIntent = new Intent(this, RecognizeActivity.class);
//                    startActivity(myIntent);

                    // Attempting to check if user exists



                } else {
                    // Google Sign In failed, update UI appropriately
                    // ...
                    Log.d("STATUS", "ERROR");
                    Toast.makeText(this, "Incorrect Username or Password", Toast.LENGTH_SHORT).show();
                }
            }
        }

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

        private void handleSignInResult(GoogleSignInResult result) {
            Log.d(TAG, "handleSignInResult:" + result.isSuccess());
            if (result.isSuccess()) {
                // Signed in successfully, show authenticated UI.
                GoogleSignInAccount acct = result.getSignInAccount();
                mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
                //updateUI(true);
            } else {
                // Signed out, show unauthenticated UI.
                //updateUI(false);
            }
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            // An unresolvable error has occurred and Google APIs (including Sign-In) will not
            // be available.
            Log.d(TAG, "onConnectionFailed:" + connectionResult);
            Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
        }

        private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
            Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
            Log.d(TAG, "firebaseAuthID:" + acct.getEmail());

            checkIfNew(acct);

            // reference a branch on firebase and if that node exists the user is there...
            // if not



            AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "signInWithCredential", task.getException());
                                Toast.makeText(LogoActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            // ...
                        }
                    });
        }

        private void updateUI(FirebaseUser user) {
            TextView view = (TextView) findViewById(R.id.theview);
            if (user != null) {
                view.setText("Welcome back, " + user.getDisplayName() + "!");
//            mStatusTextView.setText(getString(R.string.google_status_fmt, user.getEmail()));
//            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));
//
//            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
//            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
            } else {
                view.setText("");
//            mStatusTextView.setText(R.string.signed_out);
//            mDetailTextView.setText(null);
//
//            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
//            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
            }
        }

    private boolean userExists;

    public void checkIfNew(GoogleSignInAccount myAcct) {
        String userID = myAcct.getId();

//        String path = "https://feelshotapplication.firebaseio.com/users/" + userID;
        String path = "users/" + userID;

        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
               // String id = dataSnapshot.getValue(String.class);
                userExists = dataSnapshot.exists();
                //Log.d("SNAPSHOT_STATUS", id);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mReference.child(path).addValueEventListener(userListener);

        if(userExists) {
            // start the calendar intent
            Log.d("WOOO", "user exists");
            //mReference.child("users").child(userID).child("username").setValue(myAcct.getDisplayName());
            Intent calendarIntent = new Intent(this, CalendarActivity.class);
            calendarIntent.putExtra("GoogleID", userID);
//            Bundle myBundle = new Bundle();
//            myBundle.pu
//            calendarIntent.putExtras()
            startActivity(calendarIntent);
        }
        else {
            // start thr registration intent
            Log.d("NOOO", "user DOESNT exists");
            mReference.child("users").child(userID).child("username").setValue(myAcct.getDisplayName());
            //mReference.child("users").child(userID).child("done-today").setValue(false);
        }
    }



}
