package com.csci310.uscdoordrink;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Point;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginPage extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private SignInClient oneTapClient;
    private static final int REQ_ONE_TAP = 2;
    private FirebaseAuth fireDatabase;
    private volatile Boolean isCustomer = true;
    public static GoogleSignInClient mGoogleSignInClient;
    public static GoogleSignInOptions gso;

    @Override
    protected void onStart() {
        super.onStart();
        //autoLogin();
    }

    /*private void autoLogin(){
        if (Database.isLoggedin()) {
            //Database.testdrinkadd();
            Database.getUser(isCustomer, new SwitchDisplayCallback() {
                @Override
                public void switchToMerchantDisplay() {
                    Intent intent = new Intent(getApplicationContext(), MainPage.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void switchToCustomerDisplay() {
                    Intent intent = new Intent(getApplicationContext(), MainPage.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }*/

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        fireDatabase = FirebaseAuth.getInstance();

        Button signInButtonCustomer = (Button) findViewById(R.id.signInButtonCustomer);
        signInButtonCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                signInIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                isCustomer = true;
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        Button signInButtonMerchant = (Button) findViewById(R.id.signInButtonMerchant);
        signInButtonMerchant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                signInIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                isCustomer = false;
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                //Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately

                Log.e( "Google sign in failed", " " + e.getStatusCode());
                //Toast.makeText(this.getApplicationContext(), "", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        Boolean customerFlag = isCustomer;
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        fireDatabase.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = fireDatabase.getCurrentUser();
                            Database.hasSwitched = false;
                            //New Intent to direct to mapview
                            Database.getUser(customerFlag, new SwitchDisplayCallback() {
                                @Override
                                public void switchToMerchantDisplay() {
                                    Intent intent = new Intent(getApplicationContext(), MainPage.class);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void switchToCustomerDisplay() {
                                    Intent intent = new Intent(getApplicationContext(), MainPage.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

}
