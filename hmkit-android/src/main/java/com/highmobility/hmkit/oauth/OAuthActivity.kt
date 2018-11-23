package com.highmobility.hmkit.oauth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle

import com.highmobility.hmkit.HMKit
import com.highmobility.hmkit.R

class OAuthActivity : Activity() {
    private val oauthManager = HMKit.getInstance().oAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oauth)

        val url = oauthManager.webViewUrl()
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        oauthManager.didReturnFromUri(intent?.data)
    }
}
