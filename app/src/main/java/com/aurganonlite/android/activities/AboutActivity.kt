package com.aurganonlite.android.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

import com.aurganonlite.android.R

/**
 * Created by Abhish3k on 8/21/2016.
 */

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
    }

    fun fb(view: View) {
        val fbIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/aurganon/"))
        startActivity(fbIntent)
    }

    fun tweet(view: View) {
        val tweetIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/Aurganon"))
        startActivity(tweetIntent)
    }

    fun insta(view: View) {
        val instaIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/aurganon/"))
        startActivity(instaIntent)
    }


    fun web(view: View) {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://aurganon.com"))
        startActivity(webIntent)
    }
}
