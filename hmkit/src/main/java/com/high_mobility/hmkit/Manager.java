package com.high_mobility.hmkit;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.high_mobility.hmkit.Crypto.AccessCertificate;
import com.high_mobility.hmkit.Crypto.DeviceCertificate;
import com.high_mobility.btcore.HMBTCore;
import com.highmobility.hmkit.BuildConfig;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ttiganik on 03/08/16.
 */
public class Manager {
    private static final String TAG = "Manager";

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
    Storage storage;
    Context context;

    private static Manager instance;
    DeviceCertificate certificate;
    byte[] privateKey;
    byte[] caPublicKey;

    private Scanner scanner;
    private Broadcaster broadcaster;
    WebService webService;
    Telematics telematics;

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
     * Initialize the SDK with the necessary properties. Call this before using any other functionality.
     *
     * @param certificate The broadcaster certificate.
     * @param privateKey 32 byte private key with elliptic curve Prime 256v1.
     * @param caPublicKey 64 byte public key of the Certificate Authority.
     * @param context the application context
     *
     * @throws IllegalArgumentException if the parameters are invalid.
     */
    public void initialize(DeviceCertificate certificate, byte[] privateKey, byte[] caPublicKey, Context context) throws IllegalArgumentException {
        if (privateKey.length != 32 || caPublicKey.length != 64 || certificate == null) {
            throw new IllegalArgumentException();
        }

        this.context = context;
        storage = new Storage(this.context);
        webService = new WebService(this.context);

        this.caPublicKey = caPublicKey;
        this.certificate = certificate;
        this.privateKey = privateKey;

        mainHandler = new Handler(this.context.getMainLooper());

        if (workThread.getState() == Thread.State.NEW)
            workThread.start();
        workHandler = new Handler(workThread.getLooper());

        ble = new SharedBle(this.context);

        broadcaster = new Broadcaster(this);
        scanner = new Scanner(this);
        telematics = new Telematics(this);

        coreInterface = new BTCoreInterface(this);
        core.HMBTCoreInit(coreInterface);
        startClock();
        Log.i(TAG, "Initialized High-Mobility " + getInfoString() + certificate.toString());
    }

    /**
     * Initialize the SDK with the necessary properties. Call this before using any other functionality.
     *
     * @param certificate The broadcaster certificate, in Base64.
     * @param privateKey 32 byte private key with elliptic curve Prime 256v1 in Base64.
     * @param issuerPublicKey 64 byte public key of the Certificate Authority in Base64.
     * @param context the application context
     *
     * @throws IllegalArgumentException if the parameters are invalid.
     */
    public void initialize(String certificate, String privateKey, String issuerPublicKey, Context context) throws IllegalArgumentException {
        DeviceCertificate decodedCert = new DeviceCertificate(Base64.decode(certificate, Base64.DEFAULT));

        byte[] decodedPrivateKey = Base64.decode(privateKey, Base64.DEFAULT);
        byte[] decodedIssuer= Base64.decode(issuerPublicKey, Base64.DEFAULT);
        initialize(decodedCert, decodedPrivateKey, decodedIssuer, context);
    }

    /**
     * Call this function when the SDK is not used anymore - for instance when killing the app.
     * This clears the Bluetooth service and unregisters all BroadcastReceivers.
     *
     * Stored certificates are not deleted.
     */
    public void terminate() {
        if (context == null) return;

        broadcaster.terminate();
        broadcaster = null;
        coreClockTimer.cancel();
        coreClockTimer = null;
        ble.terminate();
        ble = null;
        telematics = null;
        context = null;
    }

    /**
     *
     * @return The Broadcaster instance
     * @throws IllegalStateException when SDK is not initialized
     */
    public Broadcaster getBroadcaster() throws IllegalStateException {
        if (context == null) throw new IllegalStateException("SDK not initialized");
        return broadcaster;
    }

    /**
     *
     * @return The Telematics instance
     * @throws IllegalStateException when SDK is not initialized
     */
    public Telematics getTelematics() throws IllegalStateException {
        if (context == null) throw new IllegalStateException("SDK not initialized");
        return telematics;
    }

    /**
     *
     * @return The Scanner Instance
     * @throws IllegalStateException when SDK is not initialized
     */

    Scanner getScanner() throws IllegalStateException {
        if (context == null) throw new IllegalStateException("SDK not initialized");
        return scanner;
    }

    /**
     *
     * @return The device certificate that is used by the SDK to identify itself.
     * @throws IllegalStateException when SDK is not initialized
     */
    public DeviceCertificate getDeviceCertificate() throws IllegalStateException {
        if (context == null) throw new IllegalStateException("SDK not initialized");
        return certificate;
    }

    /**
     *
     * @return A description about the SDK version name and type(mobile or wear).
     * @throws IllegalStateException when SDK is not initialized
     */
    public String getInfoString() throws IllegalStateException {
        if (context == null) throw new IllegalStateException("SDK not initialized");

        String infoString = "Android ";
        infoString += BuildConfig.VERSION_NAME;

        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WATCH)) {
            infoString += " w";
        }
        else {
            infoString += " m";
        }

        return infoString;
    }

    /**
     * Get an access certificate
     *
     * @param gainingSerial the gaining serial for the access certificate
     * @param providingSerial the providing serial for the access certificate
     * @return the access certificate or null if it does not exist
     * @throws IllegalStateException when SDK is not initialized
     */
    public AccessCertificate getCertificate(byte[] gainingSerial, byte[] providingSerial) throws IllegalStateException {
        if (context == null) throw new IllegalStateException("SDK not initialized");

        AccessCertificate[] certs = storage.getCertificates();

        for (int i = 0; i < certs.length; i++) {
            AccessCertificate cert = certs[i];

            if (Arrays.equals(cert.getGainerSerial(), gainingSerial)
                    && Arrays.equals(cert.getProviderSerial(), providingSerial)) {
                return cert;
            }
        }

        return null;
    }

    /**
     * Delete an access certificate.
     *
     * @param gainingSerial the gaining serial for the access certificate
     * @param providingSerial the providing serial for the access certificate
     * @return true if the certificate existed and was deleted successfully
     * @throws IllegalStateException when SDK is not initialized
     */
    public boolean deleteCertificate(byte[] gainingSerial, byte[] providingSerial) throws IllegalStateException {
        if (context == null) throw new IllegalStateException("SDK not initialized");
        return storage.deleteCertificate(gainingSerial, providingSerial);
    }

    /**
     * Download and store the device and vehicle access certificates for the given access token.
     *
     * @param accessToken The token that is used to download the certificates
     * @param callback Invoked with 0 if everything is successful, otherwise with either a
     *                 http error code, 1 for a connection issue, 2 for invalid data received.
     * @throws IllegalStateException when SDK is not initialized
     */
    public void downloadAccessCertificate(String accessToken,
                                          final Constants.ResponseCallback callback) throws IllegalStateException {
        if (context == null) throw new IllegalStateException("SDK not initialized");
        webService.requestAccessCertificate(accessToken,
                privateKey,
                getDeviceCertificate().getSerial(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            storage.storeDownloadedCertificates(response);
                            callback.response(0);
                        } catch (Exception e) {
                            Log.e(TAG, "Can't store the certificate, invalid data");
                            callback.response(2);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        int errorCode;

                        if (error.networkResponse != null) {
                            errorCode = error.networkResponse.statusCode;
                        }
                        else {
                            errorCode = -1;
                        }

                        callback.response(errorCode);
                    }
                });
    }

    /**
     * Deletes all the stored certificates.
     * @throws IllegalStateException when SDK is not initialized
     */
    public void resetStorage() throws IllegalStateException {
        if (context == null) throw new IllegalStateException("SDK not initialized");
        storage.resetStorage();
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