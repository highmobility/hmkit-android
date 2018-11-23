package com.highmobility.oauthtest

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.highmobility.autoapi.HonkAndFlash
import com.highmobility.crypto.value.DeviceSerial
import com.highmobility.hmkit.HMKit
import com.highmobility.hmkit.HMLog
import com.highmobility.hmkit.Telematics
import com.highmobility.hmkit.error.DownloadAccessCertificateError
import com.highmobility.hmkit.error.TelematicsError

class OAuthTestActivity : Activity() {
    private lateinit var textView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oauth_test)

        textView = findViewById(R.id.text_view)
        progressBar = findViewById(R.id.progress_bar)
        button = findViewById(R.id.access_token_text_button)

        HMKit.getInstance().initialise(
                "***REMOVED***",
                "***REMOVED***",
                "***REMOVED***",
                applicationContext)

        button.setOnClickListener {
            HMKit.getInstance().oAuth.getAccessToken(
                    "***REMOVED***",
                    "",
                    "",
                    "com.hm.1542392929-z6dz5swisgnv://in-app",
                    "",
                    null,
                    null,
                    null
            ) { accessToken, errorMessage ->
                if (accessToken != null) {
                    onAccessTokenDownloaded(accessToken)
                }
                else {
                    onError(errorMessage!!)
                }
            }
        }
    }

    private fun onAccessTokenDownloaded(accessToken: String) {
        HMKit.getInstance().downloadAccessCertificate(accessToken, object : HMKit.DownloadCallback {
            override fun onDownloaded(vehicleSerial: DeviceSerial) {
                sendHonkFlash(vehicleSerial)
            }

            override fun onDownloadFailed(error: DownloadAccessCertificateError) {
                onError("error downloading access certificate" + error.type + " " + error.message)
            }
        })
    }

    private fun sendHonkFlash(vehicleSerial: DeviceSerial) {
        // send a simple command to see everything worked
        val command = HonkAndFlash(5, 1)
        HMKit.getInstance().telematics.sendCommand(command, vehicleSerial,
                object : Telematics.CommandCallback {
                    override fun onCommandResponse(p0: com.highmobility.value.Bytes?) {
                        progressBar.visibility = View.GONE
                        textView.text = "Successfully sent honk and flash."
                    }

                    override fun onCommandFailed(p0: TelematicsError?) {
                        onError("failed to send honk and flash: " + p0?.type + " " + p0?.message)
                    }
                })
    }

    private fun onError(msg: String) {
        progressBar.visibility = View.GONE
        textView.text = msg
        HMLog.e(msg)
    }
}
