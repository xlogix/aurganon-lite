package com.aurganonlite.android.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle

import com.aurganonlite.android.R
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Created by Abhish3k on 8/10/2016.
 */

class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val FirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        val timerThread = object : Thread() {
            override fun run() {
                try {
                    Thread.sleep(2000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } finally {
                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
        timerThread.start()
    }

    override fun onPause() {
        // TODO Auto-generated method stub
        super.onPause()
        finish()
    }
}