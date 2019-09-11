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