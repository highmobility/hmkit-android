package com.highmobility.bluetoothtest

import android.app.Activity
import android.os.Bundle
import com.highmobility.autoapi.GetVehicleStatus
import com.highmobility.crypto.AccessCertificate
import com.highmobility.crypto.DeviceCertificate
import com.highmobility.crypto.value.PrivateKey
import com.highmobility.crypto.value.PublicKey
import com.highmobility.hmkit.*
import com.highmobility.hmkit.error.BroadcastError
import com.highmobility.hmkit.error.LinkError
import com.highmobility.value.Bytes
import timber.log.Timber
import timber.log.Timber.d

class BluetoothTestActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth_test)
        Timber.plant(Timber.DebugTree())

        /*
        @param certificate     The broadcaster certificate.
***REMOVED***
      * @param privateKey      32 byte private key with elliptic curve Prime 256v1.
***REMOVED***
      * @param issuerPublicKey 64 byte public key of the Certificate Authority.
***REMOVED***
         */

        HMKit.getInstance().initialise(DeviceCertificate("***REMOVED***"),
                PrivateKey("***REMOVED***"),
                PublicKey("***REMOVED***"),
                this)

        var registeredCert =
                AccessCertificate("***REMOVED***")
        var storedCert =
                AccessCertificate("***REMOVED***")

        HMKit.getInstance().broadcaster?.registerCertificate(registeredCert)
        HMKit.getInstance().broadcaster?.storeCertificate(storedCert)

        var incomingLink:ConnectedLink
        HMKit.getInstance().broadcaster?.setListener(object : BroadcasterListener {
            override fun onStateChanged(oldState: Broadcaster.State?) {

            }

            override fun onLinkReceived(link: ConnectedLink?) {
                incomingLink = link!!
                link.setListener(object : ConnectedLinkListener {
                    override fun onAuthorizationRequested(link: ConnectedLink?, callback: ConnectedLinkListener.AuthorizationCallback?) {
                        callback?.approve()
                    }

                    override fun onAuthorizationTimeout(link: ConnectedLink?) {

                    }

                    override fun onStateChanged(link: Link?, oldState: Link.State?) {
                        d("%s", link?.state)
                        if (link?.state == Link.State.AUTHENTICATED) {
                            link.sendCommand(GetVehicleStatus(), object: Link.CommandCallback {

                                override fun onCommandSent() {
                                d("onCommandSent")
                                }

                                override fun onCommandFailed(error: LinkError?) {
                                    d("onCommandFailed")
                                }
                            })
                        }
                    }

                    override fun onCommandReceived(link: Link?, bytes: Bytes?) {

                    }
                })
            }

            override fun onLinkLost(link: ConnectedLink?) {

            }
        })

        HMKit.getInstance().broadcaster?.startBroadcasting(object : Broadcaster.StartCallback {
            override fun onBroadcastingStarted() {
                d("started")
            }

            override fun onBroadcastingFailed(error: BroadcastError?) {
                d("failed")
            }
        })
    }
}
