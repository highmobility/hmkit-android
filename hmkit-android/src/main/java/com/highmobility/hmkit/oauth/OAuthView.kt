package com.highmobility.hmkit.oauth


import android.os.Bundle
import android.view.Window
import androidx.fragment.app.FragmentActivity

import com.highmobility.hmkit.R
import com.highmobility.hmkit.oauth.navigation.NavigationManager

/**
 * Used to start the web browser to get access token code. Will show progress bar while access token
 * is being downloaded.
 *
 */
internal class OAuthActivity : FragmentActivity(), IOAuthView, IWebView {
    lateinit var webView: WebViewFragment
    var info: InfoFragment? = null

    lateinit var controller: OAuthViewController

    private var navigationManager: NavigationManager = NavigationManager(supportFragmentManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.activity_oauth)
        controller = OAuthViewController(this)
        controller.onViewLoad()
    }

    // MARK: IOAuthView

    override fun openWebView(url: String) {
        webView = navigationManager.startWebView(this, url)
    }

    override fun showInfo(text: String, error: Boolean) {
        if (info == null) info = navigationManager.startInfo()
        info!!.showText(text, error)
    }

    override fun closeActivity() {
        // TODO: 2018-12-05 test memory
        finish()
    }

    // MARK: IWebView

    override fun onStartedLoadingUrl(url: String?) {
        controller.onStartLoadingUrl(url)
    }

    override fun onReceivedError(error: String?) {
        controller.onReceivedError(error)
    }
}

interface IOAuthView {
    fun openWebView(url: String)
    fun showInfo(text: String, error: Boolean)
    fun closeActivity()
}