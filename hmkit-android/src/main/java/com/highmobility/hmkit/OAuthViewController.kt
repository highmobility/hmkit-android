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

import android.content.Context
import android.net.ConnectivityManager

/**
 * OAuth flow: open webview. If successful url intercepted, start
 *
 */
internal class OAuthViewController(val view: IOAuthView) {
    private val oauth: OAuth = HMKit.getInstance().oAuth

    fun onViewLoad(context: Context) {
        var connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (connectivityManager.activeNetworkInfo != null
            && connectivityManager.activeNetworkInfo.isConnected) {
            view.openWebView(oauth.webUrl)
        }
        else {
            view.showInfo("No internet connection", true)
        }
    }

    fun onStartedLoadingUrl(url: String?) {
        val result = oauth.onStartLoadingUrl(url)

        if (result == OAuth.UrlLoadResult.CODE_INTERCEPTED) {
            view.showInfo("Downloading Access Token", false)
            oauth.downloadAccessToken { _, _ ->
                // we have the token, close the activity
                view.closeActivity()
            }
        }
        else if (result == OAuth.UrlLoadResult.INVALID_REDIRECT_URL) {
            // something went wrong, close the activity
            view.closeActivity()
        }
    }

    fun onReceivedError(error: String?) {
        view.showInfo("Web Error:\n$error", true)
    }

    fun onCloseClicked() {
        view.closeActivity()
    }
}