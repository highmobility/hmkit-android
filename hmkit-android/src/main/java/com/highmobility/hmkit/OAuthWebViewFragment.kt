package com.highmobility.hmkit

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_oauth_web_view.*

internal class WebViewFragment : Fragment() {
    private lateinit var iWebView: IWebView
    private lateinit var url: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_oauth_web_view, container, false)
        return view!!

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = webViewClient
        webView.loadUrl(url)
    }

    private val webViewClient: WebViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url.toString()

            if (URLUtil.isNetworkUrl(url) == false) {
                iWebView.onStartedLoadingUrl(url)
                return true
            }

            return false
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                HMLog.d("error ${error?.description}")
                iWebView.onReceivedError(error?.description as String?)
            }
            else {
                iWebView.onReceivedError(error.toString())
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(webView: IWebView, url: String): WebViewFragment {
            val fragment = WebViewFragment()
            fragment.iWebView = webView
            fragment.url = url
            return fragment
        }
    }
}

interface IWebView {
    fun onStartedLoadingUrl(url: String?)
    fun onReceivedError(error: String?)
}