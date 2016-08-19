package com.high_mobility.HMLink.Shared;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
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
    BTCoreInterface coreInterface;
    SharedBle ble;
    Context ctx;

    private static Manager instance;
    DeviceCertificate certificate;
    byte[] privateKey;
    byte[] CAPublicKey;

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
        mainHandler = new Handler(ctx.getMainLooper());

        workThread.start();
        workHandler = new Handler(workThread.getLooper());

        ble = new SharedBle(ctx);
        this.CAPublicKey = CAPublicKey;
        coreInterface = new BTCoreInterface(this);

        this.certificate = certificate;
        this.privateKey = privateKey;

        core.HMBTCoreInit(coreInterface);
        startClock();
    }

    /**
     *
     * @return The Link Broadcaster instance
     */
    public Broadcaster getBroadcaster() {
        if (broadcaster == null) broadcaster = new Broadcaster(this);
        return broadcaster;
    }

    /**
     *
     * @return The Link Scanner Instance
     */
    public Scanner getScanner() {
        if (scanner == null) scanner = new Scanner(this);
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
