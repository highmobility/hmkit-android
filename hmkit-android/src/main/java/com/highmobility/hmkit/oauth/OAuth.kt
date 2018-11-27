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
import com.highmobility.value.Bytes
import org.json.JSONException
import org.json.JSONObject
import java.security.MessageDigest
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

        val webViewUrl = webViewUrl(appId, authUrl, clientId, redirectScheme, startDate,
                endDate, state)
        d("start browser:$webViewUrl")

        intent.putExtra(EXTRA_URI_KEY, webViewUrl)


        d("jwt: ${getJwt()}")

        context.startActivity(intent)
    }

    internal fun didReturnFromUri(uri: Uri?, activity: Activity) {
        oauthActivity = activity
        val code = uri?.getQueryParameter("code")

        if (code == null) {
            finishedDownloadingAccessToken(null, "Invalid redirect uri")
            return
        }

        /*
        grant_type: authorization_code
        code: the code that you get in the redirect
        redirect_uri: redirect_uri that you sent during the start of the flow
        client_id: client_id! static value
        code_verifier: a JWT token that is signed by device's private key
         */

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
        var body = "{serial_number=\"${deviceSerial.hex}\",code_verifier=\"$nonceString\"}"

        val jsonHeader = Base64.encodeUrlSafe(header.toByteArray())
        var jsonBody = Base64.encodeUrlSafe(body.toByteArray())

        var jwtContent = String.format("%s.%s", jsonHeader, jsonBody)
        var jwtSignature = Crypto.sign(sha256(jwtContent), privateKey)

        ///
        /*val miladPrivBytes = Bytes("***REMOVED***")
        val miladPrivBase64 = Base64.encode(miladPrivBytes.byteArray)

        val miladPublicBytes = Bytes("***REMOVED***")
        val miladPublicBase64 = Base64.encode(miladPublicBytes.byteArray)

        body = "{\"code_verifier\":\"tomoyo\",\"serial_number\":\"6A1A7C3494F0B01C7E\"}"
        d("header:$header body:$body")
        jsonBody = Base64.encode(body.toByteArray())
        jwtContent = String.format("%s.%s", jsonHeader, jsonBody)

        d("priv $miladPrivBytes $miladPrivBase64\npublic $miladPublicBytes $miladPublicBase64")

        jwtSignature = Crypto.sign(sha256(jwtContent), miladPrivBytes.byteArray)*/

        /// // TODO: delete

        return String.format("%s.%s", jwtContent, jwtSignature.base64UrlSafe)
    }

    fun setDeviceCertificate(privateKey: PrivateKey,
                             deviceSerial: DeviceSerial) {
        this.privateKey = privateKey
        this.deviceSerial = deviceSerial
    }

    private fun webViewUrl(appId: String,
                           authUrl: String,
                           clientId: String,
                           redirectScheme: String,
                           startDate: Calendar? = null,
                           endDate: Calendar? = null,
                           state: String? = null): String {
        var webUrl = authUrl
        webUrl += "?app_id=$appId"
        webUrl += "&client_id=$clientId"
        webUrl += "&redirect_uri=$redirectScheme"
        webUrl += "&code_challenge=${Base64.encode(sha256(nonceString))}"

        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ")
        if (startDate != null) webUrl += "&validity_start_date=${df.format(startDate)}"
        if (endDate != null) webUrl += "&validity_end_date=${df.format(endDate)}"
        if (state != null) webUrl += "&state=$state"
        return webUrl
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
        val range = ('a'..'z')
        nonceString = (1..9)
                .map {
                    (Random().nextInt(range.endInclusive.toInt() - range.start.toInt()) +
                            range.start.toInt()).toChar()
                }
                .joinToString("")
    }

    private fun sha256(input: String): ByteArray {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(bytes)
    }

    companion object {
        const val EXTRA_URI_KEY = "EXTRA_URI_KEY"
    }
}