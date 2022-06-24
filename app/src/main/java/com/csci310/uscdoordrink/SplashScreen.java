package com.csci310.uscdoordrink;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.SplashTheme);
        Database.initvalueeventmap();
//        final View content = findViewById(android.R.id.content);
//        content.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//            @Override
//            public boolean onPreDraw() {
//                return false;
//            }
//        });

        callAutoLogin();

    }

    private void callAutoLogin(){
        LoginPage.gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("739359063909-tcdhqtrktqvnck3ccdfbq1us5qmfrl71.apps.googleusercontent.com")
                .requestEmail()
                .build();
        LoginPage.mGoogleSignInClient = GoogleSignIn.getClient(this, LoginPage.gso);
        SharedPreferences preferences = getSharedPreferences("info", MODE_PRIVATE);

        String accountType = preferences.getString("accountType", "");
        boolean accountExistence;
        boolean isCustomer = false;
        if(accountType.equals("")) {
            accountExistence = false;
        } else {
            accountExistence = true;
            isCustomer = (accountType.equals("customer") ? true : false);
        }

        autoLogin(accountExistence, isCustomer);
    }

    private void autoLogin(boolean isAccount, boolean isCustomer){
        if (Database.isLoggedin()) {
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
        } else {
            Intent intent = new Intent(getApplicationContext(), LoginPage.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        callAutoLogin();
    }
}