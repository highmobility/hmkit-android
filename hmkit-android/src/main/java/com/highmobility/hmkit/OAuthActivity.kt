/*
 * The MIT License
 *
 * Copyright (c) 2014- High-Mobility GmbH (https://high-mobility.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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