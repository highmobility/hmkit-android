package com.highmobility.hmkit

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.highmobility.crypto.Crypto
import com.highmobility.crypto.value.DeviceSerial
import com.highmobility.crypto.value.PrivateKey
import com.highmobility.hmkit.HMLog.d
import com.highmobility.utils.Base64
import org.json.JSONException
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

typealias CompletionHandler = (accessToken: String?, errorMessage: String?) -> Unit

/**
 * Used to open the web view to get the oauth access token code. Then requests the access token and
 * returns it to the user. User can then use the token to download the Access Certificate.
 */
class OAuth internal constructor(private val webService: WebService,
                                 private var privateKey: PrivateKey,
                                 private var deviceSerial: DeviceSerial) {
    // created at the beginning of oauth process
    internal lateinit var webUrl: String

    private lateinit var clientId: String
    private lateinit var redirectScheme: String
    private lateinit var tokenUrl: String
    private lateinit var nonceString: String
    private lateinit var completionHandler: CompletionHandler
    private lateinit var viewControllerCompletionHandler: CompletionHandler

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
     * @param completionHandler The completionHandler.
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
        createNonce()

        this.clientId = clientId
        this.redirectScheme = redirectScheme
        this.tokenUrl = tokenUrl
        this.completionHandler = completionHandler
        val nonceBytes = nonceString.toByteArray(Charset.forName("ASCII"))
        val nonceSha256 = Crypto.sha256(nonceBytes).byteArray
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

    fun onStartLoadingUrl(url: String?): UrlLoadResult {
        if (url != null && url.startsWith(redirectScheme)) {
            val uri = Uri.parse(url)
            code = uri?.getQueryParameter("code")

            if (code == null) {
                finishedDownloadingAccessToken(null, "Invalid redirect uri")
                return UrlLoadResult.INVALID_REDIRECT_URL
            }

            return UrlLoadResult.CODE_INTERCEPTED
        }

        return UrlLoadResult.UNKNOWN_URL
    }

    fun downloadAccessToken(completionHandler: CompletionHandler) {
        viewControllerCompletionHandler = completionHandler

        webService.downloadOauthAccessToken(tokenUrl, "authorization_code", code!!, redirectScheme, clientId, getJwt(),
                { jsonObject ->
                    try {
                        d("response " + jsonObject.toString(2))
                        val accessToken = jsonObject["access_token"] as String
                        finishedDownloadingAccessToken(accessToken, null)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        finishedDownloadingAccessToken(null, "invalid download access token response")
                    }

                }, { error ->
            if (error?.networkResponse != null) {
                finishedDownloadingAccessToken(null, "" + error.networkResponse.statusCode + ": " + String(error.networkResponse.data))
            }
            else {
                finishedDownloadingAccessToken(null, "no internet connection")
            }
        })
    }

    private fun getJwt(): String {
        val header = "{\"alg\":\"ES256\",\"typ\":\"JWT\"}"
        val body = "{\"code_verifier\":\"$nonceString\",\"serial_number\":\"${deviceSerial.hex}\"}"

        val headerBase64 = Base64.encodeUrlSafe(header.toByteArray())
        val bodyBase64 = Base64.encodeUrlSafe(body.toByteArray())

        val jwtContent = String.format("%s.%s", headerBase64, bodyBase64)
        val jwtSignature = Crypto.signJWT(jwtContent.toByteArray(), privateKey)

        return String.format("%s.%s", jwtContent, jwtSignature.base64UrlSafe)
    }

    fun setDeviceCertificate(privateKey: PrivateKey, deviceSerial: DeviceSerial) {
        this.privateKey = privateKey
        this.deviceSerial = deviceSerial
    }

    private fun finishedDownloadingAccessToken(accessToken: String?, errorMessage: String?) {
        viewControllerCompletionHandler(accessToken, errorMessage) // finish the view
        completionHandler(accessToken, errorMessage)
    }

    private fun createNonce() {
        nonceString = Crypto.createSerialNumber().hex
    }

    enum class UrlLoadResult {
        UNKNOWN_URL, CODE_INTERCEPTED, INVALID_REDIRECT_URL
    }
}