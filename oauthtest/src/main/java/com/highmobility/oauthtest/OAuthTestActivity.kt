package com.highmobility.oauthtest

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.highmobility.autoapi.*
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
//        HMKit.customEnvironmentBaseUrl = "https://xv-platform.h-m.space"
        HMKit.getInstance().initialise(
                "***REMOVED***",
                "***REMOVED***",
                "***REMOVED***",
                applicationContext)

        val serial = DeviceSerial("66261204D4609A72A5")
        val cert = HMKit.getInstance().getCertificate(serial)

        if (cert != null) getVs(cert.gainerSerial)

        button.setOnClickListener {
            HMKit.getInstance().oAuth.getAccessToken(
                    "***REMOVED***",
                    "https://owner-panel.h-m.space/oauth/new",
                    "***REMOVED***",
                    "com.hm.1542392929-z6dz5swisgnv://in-app",
                    "https://xv-platform.h-m.space/hm_cloud/api/v1/access_tokens",
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
                getVs(vehicleSerial)
            }

            override fun onDownloadFailed(error: DownloadAccessCertificateError) {
                onError("error downloading access certificate" + error.type + " " + error.message)
            }
        })
    }

    private fun getVs(vehicleSerial: DeviceSerial) {
        progressBar.visibility = View.VISIBLE
        textView.text = "Sending Get Diagnostics"
        // send a simple command to see everything worked
        HMKit.getInstance().telematics.sendCommand(GetDiagnosticsState(), vehicleSerial, object :
                Telematics.CommandCallback {
            override fun onCommandResponse(p0: com.highmobility.value.Bytes?) {
                progressBar.visibility = View.GONE
                val command = CommandResolver.resolve(p0)

                when (command) {
                    is DiagnosticsState -> textView.text = "Got Diagnostics,\nmileage: ${command.mileage}"
                    is Failure -> textView.text = "Get Diagnostics failure:\n\n${command.failureReason}\n${command.failureDescription}"
                    else -> textView.text = "Unknown command response"
                }
            }

            override fun onCommandFailed(p0: TelematicsError?) {
                onError("failed to get VS:\n" + p0?.type + " " + p0?.message)
            }
        })
    }

    private fun onError(msg: String) {
        progressBar.visibility = View.GONE
        textView.text = msg
        HMLog.e(msg)
    }
}
