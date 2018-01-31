package com.highmobility.hmkit;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.highmobility.crypto.AccessCertificate;
import com.highmobility.crypto.DeviceCertificate;
import com.highmobility.btcore.HMBTCore;
import com.highmobility.hmkit.Error.DownloadAccessCertificateError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ttiganik on 03/08/16.
 */
public class Manager {
    private static final String TAG = "Manager";

    public enum Environment {
        TEST, STAGING, PRODUCTION
    }

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

    /**
     * DownloadCallback is used to notify the user about the certificate download result.
     */
    public interface DownloadCallback {
        /**
         * Invoked if the certificate download was successful.
         *
         * @param vehicleSerial the certificate's gainer serial
         */
        void onDownloaded(byte[] vehicleSerial);

        /**
         * Invoked when there was an error with the certificate download.
         *
         * @param error The error
         */
        void onDownloadFailed(DownloadAccessCertificateError error);
    }

    public static LoggingLevel loggingLevel = LoggingLevel.ALL;

    /**
     * The environment of the Web Service. If initialized, call {@link #terminate()} before
     * changing it.
     */
    public static Environment environment = Environment.PRODUCTION;

    /**
     * Set a custom environment url.
     */
    public static String customEnvironmentBaseUrl = null;

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
     * @return The singleton instance of Manager.
     */
    public static Manager getInstance() {
        if (instance == null) {
            instance = new Manager();
        }

        return instance;
    }

    /**
     * Initialize the SDK with the necessary properties. Call this before using any other
     * functionality.
     *
     * @param certificate The broadcaster certificate.
     * @param privateKey  32 byte private key with elliptic curve Prime 256v1.
     * @param caPublicKey 64 byte public key of the Certificate Authority.
     * @param context     the application context
     * @throws IllegalArgumentException if the parameters are invalid.
     */
    public void initialize(DeviceCertificate certificate,
                           byte[] privateKey,
                           byte[] caPublicKey,
                           Context context) throws IllegalArgumentException {
        if (this.context != null) {
            throw new IllegalStateException("HMKit is already initialized. Call terminate() first" +
                    ".");
        }

        if (privateKey == null
                || privateKey.length != 32
                || caPublicKey == null
                || caPublicKey.length != 64
                || certificate == null) {
            throw new IllegalArgumentException("HMKit initialization parameters are invalid.");
        }

        this.context = context;
        storage = new Storage(context);
        webService = new WebService(context);

        this.caPublicKey = caPublicKey;
        this.certificate = certificate;
        this.privateKey = privateKey;

        mainHandler = new Handler(context.getMainLooper());

        if (workThread.getState() == Thread.State.NEW) {
            workThread.start();
            workHandler = new Handler(workThread.getLooper());
        }

        if (coreInterface == null) {
            coreInterface = new BTCoreInterface(this);
            core.HMBTCoreInit(coreInterface);
        }

        startClock();

        Log.i(TAG, "Initialized High-Mobility " + getInfoString() + certificate.toString());
    }

    /**
     * Initialize the SDK with the necessary properties. Call this before using any other
     * functionality.
     *
     * @param certificate     The broadcaster certificate, in Base64.
     * @param privateKey      32 byte private key with elliptic curve Prime 256v1 in Base64.
     * @param issuerPublicKey 64 byte public key of the Certificate Authority in Base64.
     * @param context         the application context
     * @throws IllegalArgumentException if the parameters are invalid.
     */
    public void initialize(String certificate, String privateKey, String issuerPublicKey, Context
            context) throws IllegalArgumentException {
        DeviceCertificate decodedCert = new DeviceCertificate(Base64.decode(certificate,
                Base64.DEFAULT));

        byte[] decodedPrivateKey = Base64.decode(privateKey, Base64.DEFAULT);
        byte[] decodedIssuer = Base64.decode(issuerPublicKey, Base64.DEFAULT);
        initialize(decodedCert, decodedPrivateKey, decodedIssuer, context);
    }

    /**
     * Call this function when the SDK is not used anymore - for instance when killing the app.
     * It clears all the internal processes and unregisters all BroadcastReceivers.
     *
     * Stored certificates are not deleted.
     */
    public void terminate() {
        if (context == null) return;

        broadcaster.terminate();

        coreClockTimer.cancel();
        coreClockTimer = null;

        if (ble != null) {
            ble.terminate();
            ble = null;
        }

        webService.cancelAllRequests();
        webService = null;

        if (ble != null) {
            ble.terminate();
        }

        context = null;
    }

    /**
     * @return The Broadcaster instance
     * @throws IllegalStateException when SDK is not initialized
     */
    public Broadcaster getBroadcaster() throws IllegalStateException {
        if (context == null) throw new IllegalStateException("SDK not initialized");
        if (broadcaster == null) broadcaster = new Broadcaster(this);

        return broadcaster;
    }

    /**
     * @return The Telematics instance
     * @throws IllegalStateException when SDK is not initialized
     */
    public Telematics getTelematics() throws IllegalStateException {
        if (context == null) throw new IllegalStateException("SDK not initialized");
        if (telematics == null) telematics = new Telematics(this);

        return telematics;
    }

    /**
     * @return The Scanner Instance
     * @throws IllegalStateException when SDK is not initialized
     */

    Scanner getScanner() throws IllegalStateException {
        if (context == null) throw new IllegalStateException("SDK not initialized");
        if (scanner == null) scanner = new Scanner(this);
        return scanner;
    }

    /**
     * @return The device certificate that is used by the SDK to identify itself.
     * @throws IllegalStateException when SDK is not initialized
     */
    public DeviceCertificate getDeviceCertificate() throws IllegalStateException {
        if (context == null) throw new IllegalStateException("SDK not initialized");
        return certificate;
    }

    /**
     * @return An SDK description string containing version name and type(mobile or wear).
     * @throws IllegalStateException when SDK is not initialized
     */
    public String getInfoString() throws IllegalStateException {
        if (context == null) throw new IllegalStateException("SDK not initialized");

        String infoString = "Android ";
        infoString += BuildConfig.VERSION_NAME;

        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WATCH)) {
            infoString += " w";
        } else {
            infoString += " m";
        }

        return infoString;
    }

    /**
     * Download and store a access certificate for the given access token. The access token needs to
     * be provided by the certificate provider.
     *
     * @param accessToken The token that is used to download the certificates
     * @param callback    A {@link DownloadCallback} object that is invoked after the download is
     *                   finished or failed
     * @throws IllegalStateException when SDK is not initialized
     */
    public void downloadCertificate(String accessToken,
                                    final DownloadCallback callback) throws IllegalStateException {
        if (context == null) throw new IllegalStateException("SDK not initialized");
        webService.requestAccessCertificate(accessToken,
                privateKey,
                getDeviceCertificate().getSerial(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        AccessCertificate certificate = null;
                        try {
                            certificate = storage.storeDownloadedCertificates(response);
                        } catch (Exception e) {
                            if (Manager.getInstance().loggingLevel.getValue() >= Manager
                                    .LoggingLevel.DEBUG.getValue()) {
                                Log.d(TAG, "storeDownloadedCertificates error: " + e.getMessage());
                            }
                            DownloadAccessCertificateError error = new
                                    DownloadAccessCertificateError(
                                    DownloadAccessCertificateError.Type.INVALID_SERVER_RESPONSE,
                                    0, e.getMessage());
                            callback.onDownloadFailed(error);
                        }

                        if (certificate != null)
                            callback.onDownloaded(certificate.getGainerSerial());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        DownloadAccessCertificateError dispatchedError = null;

                        if (error.networkResponse != null) {
                            try {
                                JSONObject json = new JSONObject(new String(error.networkResponse
                                        .data));
                                Log.d(TAG, "onErrorResponse: " + json.toString());
                                if (json.has("message")) {
                                    dispatchedError = new DownloadAccessCertificateError(
                                            DownloadAccessCertificateError.Type.HTTP_ERROR,
                                            error.networkResponse.statusCode,
                                            json.getString("message"));
                                } else {
                                    dispatchedError = new DownloadAccessCertificateError(
                                            DownloadAccessCertificateError.Type.HTTP_ERROR,
                                            error.networkResponse.statusCode,
                                            new String(error.networkResponse.data));
                                }
                            } catch (JSONException e) {
                                dispatchedError = new DownloadAccessCertificateError(
                                        DownloadAccessCertificateError.Type.HTTP_ERROR,
                                        error.networkResponse.statusCode,
                                        "");
                            }
                        } else {
                            dispatchedError = new DownloadAccessCertificateError(
                                    DownloadAccessCertificateError.Type.NO_CONNECTION,
                                    -1,
                                    "Cannot connect to the web service. Check your internet " +
                                            "connection");
                        }

                        callback.onDownloadFailed(dispatchedError);
                    }
                });
    }

    /**
     * @return All Access Certificates where this device's serial is providing access.
     * @throws IllegalStateException when SDK is not initialized
     */
    public AccessCertificate[] getCertificates() throws IllegalStateException {
        if (context == null) throw new IllegalStateException("SDK not initialized");
        return storage.getCertificatesWithProvidingSerial(getDeviceCertificate().getSerial());
    }

    /**
     * Find an Access Certificate with the given serial number.
     *
     * @param serial The serial number of the device that is gaining access.
     * @return An Access Certificate for the given serial if one exists, otherwise null.
     * @throws IllegalStateException when SDK is not initialized
     */
    public AccessCertificate getCertificate(byte[] serial) throws IllegalStateException {
        if (context == null) throw new IllegalStateException("SDK not initialized");
        AccessCertificate[] certificates = storage.getCertificatesWithGainingSerial(serial);

        if (certificates != null && certificates.length > 0) {
            return certificates[0];
        }

        return null;
    }

    /**
     * Delete an access certificate.
     *
     * @param serial The serial of the device that is gaining access.
     * @return true if the certificate existed and was deleted successfully, otherwise false
     * @throws IllegalStateException when SDK is not initialized
     */
    public boolean deleteCertificate(byte[] serial) throws IllegalStateException {
        if (context == null) throw new IllegalStateException("SDK not initialized");
        return storage.deleteCertificate(serial, certificate.getSerial());
    }

    /**
     * Deletes all the stored Access Certificates.
     *
     * @throws IllegalStateException when SDK is not initialized
     */
    public void deleteCertificates() throws IllegalStateException {
        if (context == null) throw new IllegalStateException("SDK not initialized");
        storage.resetStorage();
    }

    void postToMainThread(Runnable runnable) {
        if (Looper.myLooper() != mainHandler.getLooper()) {
            mainHandler.post(runnable);
        } else {
            runnable.run();
        }
    }

    void initializeBle() throws IllegalStateException {
        // we only want to initialize ble when we start using it
        if (context == null) throw new IllegalStateException("SDK not initialized");
        if (ble == null) ble = new SharedBle(context);
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
