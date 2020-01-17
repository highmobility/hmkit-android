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

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.URLUtil
import com.android.volley.VolleyError
import com.highmobility.crypto.Crypto
import com.highmobility.crypto.value.DeviceSerial
import com.highmobility.crypto.value.PrivateKey
import com.highmobility.utils.Base64
import com.highmobility.value.Bytes
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

data class AccessTokenResponse(val accessToken: String, val refreshToken: String, val expiresIn: Int)
typealias CompletionHandler = (response: AccessTokenResponse?, errorMessage: String?) -> Unit

/**
 * OAuth is used to open a web view and get the access token that can be used to download an Access
 * Certificate for the vehicle.
 */
class OAuth internal constructor(private val webService: WebService,
                                 private val crypto: Crypto,
                                 private var privateKey: PrivateKey,
                                 private var deviceSerial: DeviceSerial) {
    // created at the beginning of oauth process
    internal lateinit var webUrl: String

    private lateinit var clientId: String
    private lateinit var redirectScheme: String
    private lateinit var tokenUrl: String
    private lateinit var nonce: Bytes
    private lateinit var completionHandler: CompletionHandler
    private var viewControllerCompletionHandler: CompletionHandler? = null

    private var code: String? = null


    /**
     * Get the access token for downloading an Access Certificate. An activity is started that uses
     * a web view and a http request to get the access token.
     *
     * @param activity The activity the oAuth process is started in.
     * @param appId The app ID.
     * @param authUrl The auth URL.
     * @param clientId The client ID.
     * @param redirectScheme The redirect scheme.
     * @param tokenUrl The token URL.
     * @param startDate The start date.
     * @param endDate The end date.
     * @param state The state.
     * @param completionHandler The completion handler.
     */
    fun getAccessToken(activity: Activity,
                       appId: String,
                       authUrl: String,
                       clientId: String,
                       redirectScheme: String,
                       tokenUrl: String,
                       startDate: Calendar? = null,
                       endDate: Calendar? = null,
                       state: String? = null,
                       completionHandler: CompletionHandler) {
        if (URLUtil.isValidUrl(authUrl) == false ||
            URLUtil.isValidUrl(tokenUrl) == false) {
            completionHandler(null, "Invalid OAuth parameters")
            return
        }

        this.clientId = clientId
        this.redirectScheme = redirectScheme
        this.tokenUrl = tokenUrl
        this.completionHandler = completionHandler

        nonce = crypto.createSerialNumber()
        val nonceSha256 = crypto.sha256(nonce.hex.toByteArray(Charset.forName("ASCII"))).byteArray
        val codeChallenge = Base64.encodeUrlSafe(nonceSha256)

        var webUrl = authUrl
        webUrl += "?app_id=$appId"
        webUrl += "&client_id=$clientId"
        webUrl += "&redirect_uri=$redirectScheme"
        webUrl += "&code_challenge=$codeChallenge"

        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", Locale.getDefault())
        if (startDate != null) webUrl += "&validity_start_date=${df.format(startDate)}"
        if (endDate != null) webUrl += "&validity_end_date=${df.format(endDate)}"
        if (state != null) webUrl += "&state=$state"
        this.webUrl = webUrl

        val intent = Intent(activity, OAuthActivity::class.java)
        activity.startActivity(intent)
    }

    /**
     * Refresh the access token with a previously acquired refresh token.
     *
     * @param tokenUrl The token URL.
     * @param clientId The client ID.
     * @param refreshToken The refresh token.
     * @param completionHandler The completion handler.
     */
    fun refreshAccessToken(tokenUrl: String, clientId: String, refreshToken: String, completionHandler: CompletionHandler) {
        webService.refreshOauthAccessToken(tokenUrl, clientId, refreshToken, object : WebRequestListener() {
            override fun onResponse(jsonObject: JSONObject?) {
                try {
                    val responseObject = parseAccessTokenResponse(jsonObject!!)
                    completionHandler(responseObject, null)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    finishedDownloadingAccessToken(null, "invalid refresh access token response")
                }
            }

            override fun onError(error: VolleyError?) {
                if (error?.networkResponse != null) {
                    completionHandler(null, "${error.networkResponse.statusCode}: ${String(error.networkResponse.data)}")
                }
                else {
                    completionHandler(null, "no internet connection")
                }

            }
        })
    }

    internal fun onStartLoadingUrl(url: String?): UrlLoadResult {
        if (url != null && url.startsWith(redirectScheme)) {
            val uri = Uri.parse(url)
            code = uri?.getQueryParameter("code")

            if (code == null) {
                var errorText = uri?.getQueryParameter("error")
                var errorDescription = uri?.getQueryParameter("error_description")
                if (errorDescription.isNullOrBlank() == false) errorText += ": $errorDescription"
                var error = if (errorText != null) "$errorText" else "Invalid redirect uri"
                finishedDownloadingAccessToken(null, error)
                return UrlLoadResult.INVALID_REDIRECT_URL
            }

            return UrlLoadResult.CODE_INTERCEPTED
        }

        return UrlLoadResult.UNKNOWN_URL
    }

    internal fun downloadAccessToken(completionHandler: CompletionHandler) {
        viewControllerCompletionHandler = completionHandler

        webService.downloadOauthAccessToken(tokenUrl, code!!, redirectScheme, clientId, getJwt(),
                object : WebRequestListener() {
                    override fun onResponse(jsonObject: JSONObject?) {
                        finishedDownloadingAccessToken(jsonObject, null)
                    }

                    override fun onError(error: VolleyError?) {
                        if (error?.networkResponse != null) {
                            finishedDownloadingAccessToken(null, "${error.networkResponse.statusCode}: ${String(error.networkResponse.data)}")
                        }
                        else {
                            finishedDownloadingAccessToken(null, "no internet connection")
                        }
                    }
                })
    }

    protected fun setDeviceCertificate(privateKey: PrivateKey, deviceSerial: DeviceSerial) {
        this.privateKey = privateKey
        this.deviceSerial = deviceSerial
    }

    private fun getJwt(): String {
        val header = "{\"alg\":\"ES256\",\"typ\":\"JWT\"}"
        val body = "{\"code_verifier\":\"${nonce.hex}\",\"serial_number\":\"${deviceSerial.hex}\"}"

        val headerBase64 = Base64.encodeUrlSafe(header.toByteArray())
        val bodyBase64 = Base64.encodeUrlSafe(body.toByteArray())

        val jwtContent = String.format("%s.%s", headerBase64, bodyBase64)
        val jwtSignature = crypto.signJWT(jwtContent.toByteArray(), privateKey)

        return String.format("%s.%s", jwtContent, jwtSignature.base64UrlSafe)
    }

    private fun finishedDownloadingAccessToken(jsonObject: JSONObject?, errorMessage: String?) {
        var responseContainer: AccessTokenResponse? = null

        var errorString: String? = errorMessage

        if (jsonObject != null) {
            try {
                responseContainer = parseAccessTokenResponse(jsonObject)
            } catch (e: JSONException) {
                e.printStackTrace()
                errorString = "invalid download access token response"
            }
        }

        viewControllerCompletionHandler?.invoke(responseContainer, errorString) // finish the view
        completionHandler(responseContainer, errorString)
    }

    private fun parseAccessTokenResponse(jsonObject: JSONObject): AccessTokenResponse {
        val responseContainer: AccessTokenResponse?

        val accessToken = jsonObject["access_token"] as String
        val refreshToken = jsonObject["refresh_token"] as String
        val expiresIn = jsonObject["expires_in"] as Int
        responseContainer = AccessTokenResponse(accessToken, refreshToken, expiresIn)

        return responseContainer
    }

    enum class UrlLoadResult {
        UNKNOWN_URL, CODE_INTERCEPTED, INVALID_REDIRECT_URL
    }
}