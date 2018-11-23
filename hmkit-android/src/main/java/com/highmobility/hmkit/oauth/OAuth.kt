package com.highmobility.basicoauth

import android.content.Context
import android.net.Uri
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import com.highmobility.crypto.Crypto
import com.highmobility.crypto.value.DeviceSerial
import com.highmobility.crypto.value.PrivateKey
import com.highmobility.hmkit.HMLog.d
import com.highmobility.utils.Base64
import org.json.JSONException
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


typealias CompletionHandler = (accessToken: String?, errorMessage: String?) -> Unit

class OAuth internal constructor(private val context: Context,
                                 private val privateKey: PrivateKey,
                                 private val deviceSerial: DeviceSerial) {
    private lateinit var appId: String
    private lateinit var authUrl: String
    private lateinit var clientId: String
    private lateinit var redirectScheme: String
    private lateinit var tokenUrl: String

    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    private var state: String? = null

    private lateinit var nonce: String // create at the beginning of oauth process
    private lateinit var completionHandler: CompletionHandler // create at the beginning of oauth process

    fun getAccessToken(appId: String,
                       authUrl: String,
                       clientId: String,
                       redirectScheme: String,
                       tokenUrl: String,
                       startDate: Calendar? = null,
                       endDate: Calendar? = null,
                       state: String? = null,
                       completionHandler: CompletionHandler) {
        // TODO: 2018-11-23 should kill oauth activity as well
        // TODO: 2018-11-23 start the activity

    }

    internal fun webViewUrl(): String {
        // TODO: 2018-11-23 put this as intent extra
        createNonce()
        var webUrl = authUrl
        webUrl += "?client_id=" + clientId
        webUrl += "&redirect_uri=" + redirectScheme
        webUrl += "&code_challenge=" + Base64.encode(sha256(nonce))
        webUrl += "&app_id=" + appId

        if (state != null) webUrl += "&state=" + state

        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ")
        if (startDate != null) webUrl += "&validity_start_date=" + df.format(startDate)
        if (endDate != null) webUrl += "&validity_end_date=" + df.format(endDate)

        return webUrl
    }

    internal fun didReturnFromUri(uri: Uri?) {
        if (uri == null) return

        /*
        grant_type: authorization_code
        code: the code that you get in the redirect
        redirect_uri: redirect_uri that you sent during the start of the flow
        client_id: client_id! static value
        code_verifier: a JWT token that is signed by device's private key
         */

        val code = uri.getQueryParameter("code")
        var tokenUrl = tokenUrl

        val header = HashMap<String, Any>()
        val body = HashMap<String, Any>()

        header["alg"] = "ES256"
        header["typ"] = "JWT"

        body["serial_number"] = deviceSerial.hex
        body["code_verifier"] = nonce

        val gson = GsonBuilder().create()
        val jsonHeader = gson.toJson(header)
        val jsonBody = gson.toJson(body)

        var jwtContent = String.format("%s.%s", jsonHeader, jsonBody)
        var jwtSignature = Crypto.sign(jwtContent.toByteArray(), privateKey)

        val jwt = String.format("%s.%s", jwtContent, jwtSignature.base64)

        tokenUrl += "?grant_type=authorization_code"
        tokenUrl += "&code=$code"
        tokenUrl += "&redirect_uri=$redirectScheme"
        tokenUrl += "&client_id=$clientId"
        tokenUrl += "&code_verifier=$jwt"

        val request = JsonObjectRequest(Request.Method.POST, tokenUrl, null,
                Response.Listener { jsonObject ->
                    try {
                        d("response " + jsonObject.toString(2))
                        val accessToken = jsonObject["access_token"] as String
                        completionHandler(accessToken, null)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        completionHandler(null, "invalid download access token response")
                    }
                }, Response.ErrorListener { error: VolleyError? ->
            if (error?.networkResponse != null) {
                completionHandler(null, "" + error.networkResponse.statusCode + ": " + String(error.networkResponse.data))
            }
            else {
                completionHandler(null, "no internet connection")
            }
        })

        printRequest(request)
        Volley.newRequestQueue(context).add(request)
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
        nonce = (1..9)
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
}
