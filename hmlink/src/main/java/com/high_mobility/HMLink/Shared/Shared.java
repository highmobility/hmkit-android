package com.high_mobility.HMLink.Shared;

import android.content.Context;
import android.util.Log;

import com.high_mobility.HMLink.DeviceCertificate;
import com.high_mobility.btcore.HMBTCore;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ttiganik on 03/08/16.
 */
public class Shared {
    public HMBTCore core = new HMBTCore();
    public SharedBle ble;
    static Shared instance;
    public BTCoreInterface coreInterface;

    private ExternalDeviceManager externalDeviceManager;
    private LocalDevice localDevice;
    Context ctx;
    Timer t;

    byte[] CAPublicKey;

    public static Shared getInstance() {
        if (instance == null) {
            instance = new Shared();
        }

        return instance;
    }

    /**
     * Set the device certificate and private key before using any other functionality.
     *
     * setContext() has to be called before this to initialize the database.
     *
     * @param certificate The device certificate.
     * @param privateKey 32 byte private key with elliptic curve Prime 256v1.
     * @param CAPublicKey 64 byte public key of the Certificate Authority.
     * @param applicationContext The application context
     */
    public void initialize(DeviceCertificate certificate, byte[] privateKey, byte[] CAPublicKey, Context applicationContext) {
        Log.i(LocalDevice.TAG, "Initialized High-Mobility SDK with certificate" + certificate.toString());
        ctx = applicationContext;
        ble = new SharedBle(ctx);
        this.CAPublicKey = CAPublicKey;
        coreInterface = new BTCoreInterface(this);

        getLocalDevice().certificate = certificate;
        getLocalDevice().privateKey = privateKey;

        core.HMBTCoreInit(coreInterface);
//        startClock(); // TODO: start clock?
    }

    public LocalDevice getLocalDevice() {
        if (localDevice == null) localDevice = new LocalDevice(this);
        return localDevice;
    }

    public ExternalDeviceManager getExternalDeviceManager() {
        if (externalDeviceManager == null) externalDeviceManager = new ExternalDeviceManager(this);
        return externalDeviceManager;
    }

    void startClock() {
        if (t != null) return;

        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                core.HMBTCoreClock(coreInterface);
            }
        }, 0, 1000);
    }
}
