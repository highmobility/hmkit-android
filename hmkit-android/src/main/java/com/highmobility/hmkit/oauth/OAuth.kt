package com.highmobility.basicoauth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.Volley
import com.highmobility.crypto.Crypto
import com.highmobility.crypto.value.DeviceSerial
import com.highmobility.crypto.value.PrivateKey
import com.highmobility.hmkit.HMLog.d
import com.highmobility.hmkit.oauth.OAuthActivity
import com.highmobility.utils.Base64
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

typealias CompletionHandler = (accessToken: String?, errorMessage: String?) -> Unit

/**
 * Used to open the web view to get the oauth access token code. Then requests the access token and
 * returns it to the user.
 */
class OAuth internal constructor(private val context: Context,
                                 private var privateKey: PrivateKey,
                                 private var deviceSerial: DeviceSerial) {

    // created at the beginning of oauth process
    private lateinit var clientId: String
    private lateinit var redirectScheme: String
    private lateinit var tokenUrl: String
    private lateinit var nonceString: String
    private lateinit var completionHandler: CompletionHandler

    private var oauthActivity: Activity? = null

    fun getAccessToken(appId: String,
                       authUrl: String,
                       clientId: String,
                       redirectScheme: String,
                       tokenUrl: String,
                       startDate: Calendar? = null,
                       endDate: Calendar? = null,
                       state: String? = null,
                       completionHandler: CompletionHandler) {
        val intent = Intent(context, OAuthActivity::class.java)
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

        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ")
        if (startDate != null) webUrl += "&validity_start_date=${df.format(startDate)}"
        if (endDate != null) webUrl += "&validity_end_date=${df.format(endDate)}"
        if (state != null) webUrl += "&state=$state"

        intent.putExtra(EXTRA_URI_KEY, webUrl)

        context.startActivity(intent)
    }

    internal fun didReturnFromUri(uri: Uri?, activity: Activity) {
        oauthActivity = activity
        val code = uri?.getQueryParameter("code")

        if (code == null) {
            finishedDownloadingAccessToken(null, "Invalid redirect uri")
            return
        }

        var tokenUrl = tokenUrl

        tokenUrl += "?grant_type=authorization_code"
        tokenUrl += "&code=$code"
        tokenUrl += "&redirect_uri=$redirectScheme"
        tokenUrl += "&client_id=$clientId"
        tokenUrl += "&code_verifier=${getJwt()}"

        val request = JsonObjectRequest(Request.Method.POST, tokenUrl, null,
                Response.Listener { jsonObject ->
                    try {
                        d("response " + jsonObject.toString(2))
                        val accessToken = jsonObject["access_token"] as String
                        finishedDownloadingAccessToken(accessToken, null)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        finishedDownloadingAccessToken(null, "invalid download access token response")
                    }
                }, Response.ErrorListener { error: VolleyError? ->
            if (error?.networkResponse != null) {
                finishedDownloadingAccessToken(null, "" + error.networkResponse.statusCode + ": " + String(error.networkResponse.data))
            }
            else {
                finishedDownloadingAccessToken(null, "no internet connection")
            }
        })

        printRequest(request)
        Volley.newRequestQueue(context).add(request)
    }

    private fun getJwt(): String {
        val header = "{\"alg\":\"ES256\",\"typ\":\"JWT\"}"
        var body = "{\"code_verifier\":\"$nonceString\",\"serial_number\":\"${deviceSerial.hex}\"}"

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
        completionHandler(accessToken, errorMessage)
        oauthActivity?.finish()
    }

    private fun printRequest(request: JsonRequest<*>) {
        try {
            val body = request.body
            val bodyString = if (body != null) "\n" + String(request.body) else ""
            val headers = JSONObject(request.headers)

            try {
                d(request.url.toString() + "\n" + headers.toString(2) +
                        bodyString)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } catch (authFailureError: AuthFailureError) {
            authFailureError.printStackTrace()
        }
    }

    private fun createNonce() {
        nonceString = Crypto.createSerialNumber().hex
    }

    companion object {
        const val EXTRA_URI_KEY = "EXTRA_URI_KEY"
    }
}