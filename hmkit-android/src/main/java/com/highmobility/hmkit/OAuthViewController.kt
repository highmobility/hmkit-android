package com.highmobility.hmkit

internal class OAuthViewController(val view: IOAuthView) {
    private val oauth: OAuth = HMKit.getInstance().oAuth

    fun onViewLoad() {
        view.openWebView(oauth.webUrl)
    }

    fun onStartLoadingUrl(url: String?) {
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
        view.showInfo("Web Error. Check your internet connection.\n$error", true)
    }

    fun onCloseClicked() {
        view.closeActivity()
    }
}