package com.highmobility.hmkit


import android.os.Bundle
import android.view.Window
import androidx.fragment.app.FragmentActivity

/**
 * Used to start the web browser to get access token code. Will show progress bar while access token
 * is being downloaded.
 *
 */
internal class OAuthActivity : FragmentActivity(), IOAuthView, IWebView, IInfoView {
    private lateinit var webView: WebViewFragment
    var info: OAuthInfoFragment? = null

    lateinit var controller: OAuthViewController

    private var navigationManager: OAuthNavigationManager = OAuthNavigationManager(supportFragmentManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.activity_oauth)
        controller = OAuthViewController(this)
        controller.onViewLoad(this)
    }

    // MARK: IOAuthView

    override fun openWebView(url: String) {
        webView = navigationManager.startWebView(this, url)
    }

    override fun showInfo(text: String, error: Boolean) {
        if (info == null) info = navigationManager.startInfo(this, text, error)
        else {
            info!!.showText(text, error)
        }
    }

    override fun closeActivity() {
        // TODO: 2018-12-05 test memory
        finish()
    }

    // MARK: IWebView

    override fun onStartedLoadingUrl(url: String?) {
        controller.onStartedLoadingUrl(url)
    }

    override fun onReceivedError(error: String?) {
        controller.onReceivedError(error)
    }

    // MARK: IInfoView

    override fun onCloseButtonClicked() {
        controller.onCloseClicked()
    }
}

interface IOAuthView {
    fun openWebView(url: String)
    fun showInfo(text: String, error: Boolean)
    fun closeActivity()
}