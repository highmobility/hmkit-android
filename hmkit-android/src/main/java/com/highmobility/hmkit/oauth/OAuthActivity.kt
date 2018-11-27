package com.highmobility.hmkit.oauth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.highmobility.basicoauth.OAuth
import com.highmobility.hmkit.HMKit
import com.highmobility.hmkit.HMLog.d

import com.highmobility.hmkit.R

/**
 * Used to start the web browser to get access token code. Will show progress bar while access token
 * is being downloaded.
 */
internal class OAuthActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oauth)
        val uri = intent.getStringExtra(OAuth.EXTRA_URI_KEY)
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startActivity(browserIntent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        HMKit.getInstance().oAuth.didReturnFromUri(intent?.data, this)
    }

    override fun onResume() {
        super.onResume()
        d("onResume ${intent.data} $this")
        // TODO: 2018-11-26 if browser was started and came back with back button, fail the oauth
        // process
    }
}
