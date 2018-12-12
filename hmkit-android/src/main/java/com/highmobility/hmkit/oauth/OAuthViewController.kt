package com.highmobility.hmkit.oauth

import com.highmobility.hmkit.HMKit

internal class OAuthViewController(val view: IOAuthView) {
    val oauth: OAuth = HMKit.getInstance().oAuth

    fun onViewLoad() {
        view.openWebView(oauth.webUrl)
    }

    fun onStartLoadingUrl(url: String?) {
        val result = oauth.onStartLoadingUrl(url)

        if (result == OAuth.UrlLoadResult.CODE_INTERCEPTED) {
            view.showInfo("Downloading Access Token", false)
            oauth.downloadAccessToken { accessToken, errorMessage ->
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
        view.showInfo("Web error\n$error", true)
    }
}