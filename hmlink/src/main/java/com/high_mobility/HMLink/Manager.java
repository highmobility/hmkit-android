package com.high_mobility.HMLink;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Base64;
import android.util.Log;

import com.high_mobility.HMLink.Crypto.DeviceCertificate;
import com.high_mobility.btcore.HMBTCore;
import com.highmobility.hmlink.BuildConfig;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ttiganik on 03/08/16.
 */
public class Manager {
    public enum LoggingLevel {
        NONE(0), DEBUG(1), ALL(2);

        private Integer level;

        LoggingLevel(int level) {
            this.level = level;
        }

        public int getValue() {
            return level;
        }
    }

    public static LoggingLevel loggingLevel = LoggingLevel.ALL;

    HMBTCore core = new HMBTCore();
    BTCoreInterface coreInterface;
    SharedBle ble;
    Context ctx;

    private static Manager instance;
    DeviceCertificate certificate;
    byte[] privateKey;
    byte[] caPublicKey;

    private Scanner scanner;
    private Broadcaster broadcaster;

    Handler mainHandler;
    Handler workHandler = null;
    private Timer coreClockTimer;
    private HandlerThread workThread = new HandlerThread("BTCoreThread");

    /**
     *
     * @return The singleton instance of Manager.
     */
    public static Manager getInstance() {
        if (instance == null) {
            instance = new Manager();
        }

        return instance;
    }

    /**
     * Initialize the SDK with the necessary properties before using any other functionality.
     *
     * @param certificate The broadcaster certificate.
     * @param privateKey 32 byte private key with elliptic curve Prime 256v1.
     * @param caPublicKey 64 byte public key of the Certificate Authority.
     * @param applicationContext The application context
     *
     * @throws IllegalArgumentException if the parameters are invalid.
     */
    public void initialize(DeviceCertificate certificate, byte[] privateKey, byte[] caPublicKey, Context applicationContext) throws IllegalArgumentException {
        Log.i(Broadcaster.TAG, "Initialized High-Mobility SDK with certificate" + certificate.toString());
        if (privateKey.length != 32 || caPublicKey.length != 64 || certificate == null || applicationContext == null) {
            throw new IllegalArgumentException();
        }

        this.caPublicKey = caPublicKey;
        this.certificate = certificate;
        this.privateKey = privateKey;
        ctx = applicationContext;
        mainHandler = new Handler(ctx.getMainLooper());

        if (workThread.getState() == Thread.State.NEW)
            workThread.start();

        workHandler = new Handler(workThread.getLooper());

        ble = new SharedBle(ctx);
        coreInterface = new BTCoreInterface(this);

        core.HMBTCoreInit(coreInterface);
        startClock();

        broadcaster = new Broadcaster(this);
        scanner = new Scanner(this);
    }


    /**
     * Initialize the SDK with the necessary properties. This needs to be done before using any
     * other functionality.
     *
     * @param certificate The broadcaster certificate, in Base64.
     * @param privateKey 32 byte private key with elliptic curve Prime 256v1 in Base64.
     * @param issuerPublicKey 64 byte public key of the Certificate Authority in Base64.
     * @param applicationContext The application context
     * @throws IllegalArgumentException
     */
    public void initialize(String certificate, String privateKey, String issuerPublicKey,
                           Context applicationContext) throws IllegalArgumentException {
        DeviceCertificate decodedCert = new DeviceCertificate(Base64.decode(certificate, Base64.DEFAULT));

        byte[] decodedPrivateKey = Base64.decode(privateKey, Base64.DEFAULT);
        byte[] decodedIssuer= Base64.decode(issuerPublicKey, Base64.DEFAULT);
        initialize(decodedCert, decodedPrivateKey, decodedIssuer, applicationContext);
    }

    /**
     *
     * @return The Broadcaster instance
     */
    public Broadcaster getBroadcaster() {
        return broadcaster;
    }

    /**
     *
     * @return The Scanner Instance
     */

    Scanner getScanner() {
        return scanner;
    }

    /**
     *
     * @return The device certificate that is used by the broadcaster and scanner
     * to identify themselves.
     */
    public DeviceCertificate getCertificate() {
        return certificate;
    }

    String getInfoString() {
        String infoString = "Android SDK ";
        infoString += BuildConfig.VERSION_NAME;

        if (ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WATCH)) {
            infoString += " w";
        }
        else {
            infoString += " m";
        }

        return infoString;
    }

    private void startClock() {
        if (coreClockTimer != null) return;

        coreClockTimer = new Timer();
        coreClockTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                workHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        core.HMBTCoreClock(coreInterface);
                    }
                });
            }
        }, 0, 1000);
    }
}
