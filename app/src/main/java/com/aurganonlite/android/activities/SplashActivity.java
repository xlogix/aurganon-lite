package com.aurganonlite.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.aurganonlite.android.R;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by Abhish3k on 8/10/2016.
 */

public class SplashActivity extends Activity {

    public FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Thread timerThread = new Thread() {
            public void run() {
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        timerThread.start();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        finish();
    }
}