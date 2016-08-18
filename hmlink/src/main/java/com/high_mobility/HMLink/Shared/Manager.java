package com.high_mobility.HMLink.Shared;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.high_mobility.HMLink.DeviceCertificate;
import com.high_mobility.btcore.HMBTCore;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ttiganik on 03/08/16.
 */
public class Manager {
    public enum LoggingLevel {
        None(0), Debug(1), All(2);

        private Integer level;

        LoggingLevel(int level) {
            this.level = level;
        }

        public int getValue() {
            return level;
        }
    }

    public static LoggingLevel loggingLevel = LoggingLevel.All;

    HMBTCore core = new HMBTCore();
    SharedBle ble;

    static Manager instance;
    DeviceCertificate certificate;
    byte[] privateKey;

    BTCoreInterface coreInterface;

    private Scanner scanner;
    private Broadcaster broadcaster;

    Context ctx;
    Timer coreClockTimer;
    Handler mainThread;

    byte[] CAPublicKey;

    public static Manager getInstance() {
        if (instance == null) {
            instance = new Manager();
        }

        return instance;
    }

    /**
     * Set the broadcaster certificate and private key before using any other functionality.
     *
     * setContext() has to be called before this to initialize the database.
     *
     * @param certificate The broadcaster certificate.
     * @param privateKey 32 byte private key with elliptic curve Prime 256v1.
     * @param CAPublicKey 64 byte public key of the Certificate Authority.
     * @param applicationContext The application context
     */
    public void initialize(DeviceCertificate certificate, byte[] privateKey, byte[] CAPublicKey, Context applicationContext) {
        Log.i(Broadcaster.TAG, "Initialized High-Mobility SDK with certificate" + certificate.toString());
        ctx = applicationContext;
        mainThread = new Handler(ctx.getMainLooper());

        ble = new SharedBle(ctx);
        this.CAPublicKey = CAPublicKey;
        coreInterface = new BTCoreInterface(this);

        this.certificate = certificate;
        this.privateKey = privateKey;

        core.HMBTCoreInit(coreInterface);
        startClock();
    }

    public Broadcaster getBroadcaster() {
        if (broadcaster == null) broadcaster = new Broadcaster(this);
        return broadcaster;
    }

    public Scanner getScanner() {
        if (scanner == null) scanner = new Scanner(this);
        return scanner;
    }

    public DeviceCertificate getCertificate() {
        return certificate;
    }

    void startClock() {
        if (coreClockTimer != null) return;

        coreClockTimer = new Timer();
        coreClockTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        core.HMBTCoreClock(coreInterface);
                    }
                });
            }
        }, 0, 1000);
    }
}
