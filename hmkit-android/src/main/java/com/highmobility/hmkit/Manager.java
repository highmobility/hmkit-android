package com.highmobility.hmkit;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.highmobility.btcore.HMBTCore;
import com.highmobility.crypto.AccessCertificate;
import com.highmobility.crypto.DeviceCertificate;
import com.highmobility.crypto.value.DeviceSerial;
import com.highmobility.crypto.value.PrivateKey;
import com.highmobility.crypto.value.PublicKey;
import com.highmobility.hmkit.error.BleNotSupportedException;
import com.highmobility.hmkit.error.DownloadAccessCertificateError;
import com.highmobility.utils.Base64;
import com.highmobility.value.Bytes;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nullable;

/**
 * Manager is the entry point to the HMKit.
 */
public class Manager {
    private static final String TAG = "HMKit-Manager";

    /**
     * The logging level of HMKit.
     */
    public static LoggingLevel loggingLevel = LoggingLevel.ALL;

    /**
     * The environment of the Web Service. If initialised, call {@link #terminate()} before
     * changing.
     */
    public static Environment environment = Environment.PRODUCTION;

    /**
     * Custom web environment url. Will override { @link {@link #environment} }
     */
    public static String customEnvironmentBaseUrl = null;

    private static Manager instance;
    private DeviceCertificate certificate;
    PrivateKey privateKey;
    PublicKey caPublicKey;
    byte[] issuer, appId; // these are set from BTCoreInterface HMBTHalAdvertisementStart.
    HMBTCore core = new HMBTCore();
    BTCoreInterface coreInterface;
    Storage storage;
    Context context;

    private Scanner scanner;
    private Broadcaster broadcaster;
    private Telematics telematics;
    private WebService webService;
    private SharedBle ble;

    Handler mainHandler, workHandler;
    private HandlerThread workThread = new HandlerThread("BTCoreThread");
    private Timer coreClockTimer;

    @Nullable SharedBle getBle() {
        if (ble == null) {
            try {
                ble = new SharedBle(context); // could be that phone has no bluetooth.
            } catch (BleNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return ble;
    }

    /**
     * @return The Broadcaster instance. Null if BLE is not supported.
     */
    @Nullable public Broadcaster getBroadcaster() {
        if (broadcaster == null) {
            try {
                broadcaster = new Broadcaster(this);
            } catch (BleNotSupportedException e) {
                e.printStackTrace();
            }
        }

        return broadcaster;
    }

    /**
     * @return The Telematics instance.
     */
    public Telematics getTelematics() {
        if (telematics == null) telematics = new Telematics(this);

        return telematics;
    }

    /**
     * @return The Scanner Instance. Null if BLE is not supported.
     * @throws IllegalStateException when SDK is not initialized.
     */
    @Nullable Scanner getScanner() throws IllegalStateException {
        if (scanner == null) {
            try {
                scanner = new Scanner(this);
            } catch (BleNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return scanner;
    }

    /**
     * @return The device certificate that is used by the SDK to identify itself. Null if {@link
     * #initialise(DeviceCertificate, PrivateKey, PublicKey)} has not been called.
     */
    @Nullable public DeviceCertificate getDeviceCertificate() {
        return certificate;
    }

    /**
     * @return An SDK description string containing version name and type(mobile or wear).
     * @throws IllegalStateException when SDK is not initialized.
     */
    public String getInfoString() {
        String infoString = "Android ";
        infoString += BuildConfig.VERSION_NAME;

        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WATCH)) {
            infoString += " w";
        } else {
            infoString += " m";
        }

        return infoString;
    }

    WebService getWebService() {
        if (webService == null) webService = new WebService(context);
        return webService;
    }

    /**
     * Create the Manager instance with Application Context before accessing it.
     *
     * @param context The application context.
     */
    public static Manager createInstance(Context context) {
        if (instance == null) {
            instance = new Manager(context);
        }

        return instance;
    }

    /**
     * @return The singleton instance of Manager.
     */
    public static Manager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Call createInstance() before accessing the Manager " +
                    "instance.");
        }

        return instance;
    }

    Manager(Context context) {
        this.context = context.getApplicationContext();
        storage = new Storage(this.context);
        mainHandler = new Handler(this.context.getMainLooper());
        workThread.start();
        workHandler = new Handler(workThread.getLooper());
    }

    /**
     * Initialise the SDK with a certificate. Call this before using Broadcaster or Telematics.
     *
     * @param certificate The broadcaster certificate.
     * @param privateKey  32 byte private key with elliptic curve Prime 256v1.
     * @param caPublicKey 64 byte public key of the Certificate Authority.
     * @throws IllegalStateException if there are connected links with Bluetooth.
     * @deprecated Use {@link #initialise(DeviceCertificate, PrivateKey, PublicKey)} instead.
     */
    @Deprecated
    public void initialize(DeviceCertificate certificate,
                           PrivateKey privateKey,
                           PublicKey caPublicKey,
                           Context context) throws IllegalStateException {
        initialise(certificate, privateKey, caPublicKey);
    }

    /**
     * Initialise the SDK with a certificate. Call this before using Broadcaster or Telematics.
     *
     * @param certificate The broadcaster certificate.
     * @param privateKey  32 byte private key with elliptic curve Prime 256v1.
     * @param caPublicKey 64 byte public key of the Certificate Authority.
     * @throws IllegalStateException if there are connected links with Bluetooth.
     */
    public void initialise(DeviceCertificate certificate,
                           PrivateKey privateKey,
                           PublicKey caPublicKey) throws IllegalStateException {
        if (this.certificate != null) terminate(); // will throw if there are connected links

        this.caPublicKey = caPublicKey;
        this.certificate = certificate;
        this.privateKey = privateKey;

        if (coreInterface == null) {
            // core init needs to be done once, only initialises structs
            coreInterface = new BTCoreInterface(this);
            core.HMBTCoreInit(coreInterface);
        }

        startCoreClock();

        // initialise after terminate
        if (ble != null) ble.initialise();
        if (broadcaster != null) broadcaster.initialise();

        Log.i(TAG, "Initialized High-Mobility " + getInfoString() + certificate.toString());
    }

    /**
     * Initialise the SDK with a certificate. Call this before using Broadcaster or Telematics.
     *
     * @param certificate     The device certificate in Base64 or hex.
     * @param privateKey      32 byte private key with elliptic curve Prime 256v1 in Base64 or hex.
     * @param issuerPublicKey 64 byte public key of the Certificate Authority in Base64 or hex.
     * @param context         the application context
     * @throws IllegalArgumentException if the parameters are invalid.
     * @deprecated Use {@link #initialise(String, String, String)} instead.
     */
    @Deprecated
    public void initialize(String certificate, String privateKey, String issuerPublicKey, Context
            context) throws IllegalArgumentException {
        initialise(certificate, privateKey, issuerPublicKey);
    }

    /**
     * Initialise the SDK with a certificate. Call this before using Broadcaster or Telematics.
     *
     * @param certificate     The device certificate in Base64 or hex.
     * @param privateKey      32 byte private key with elliptic curve Prime 256v1 in Base64 or hex.
     * @param issuerPublicKey 64 byte public key of the Certificate Authority in Base64 or hex.
     * @throws IllegalArgumentException if the parameters are invalid.
     */
    public void initialise(String certificate, String privateKey, String issuerPublicKey) throws
            IllegalArgumentException {
        DeviceCertificate decodedCert = new DeviceCertificate(new Bytes(Base64.decode
                (certificate)));
        PrivateKey decodedPrivateKey = new PrivateKey(privateKey);
        PublicKey decodedIssuerPublicKey = new PublicKey(issuerPublicKey);
        initialise(decodedCert, decodedPrivateKey, decodedIssuerPublicKey);
    }

    /**
     * Call this function when the SDK is not used anymore - for instance when killing the app. It
     * clears all the internal processes, stops broadcasting, unregisters BroadcastReceivers and
     * enables re-initializing the SDK with new certificates.
     * <p>
     * Terminate will fail if a connected link still exists. Disconnect all the links before
     * terminating the SDK.
     * <p>
     * Stored certificates are not deleted.
     *
     * @throws IllegalStateException when there are links still connected.
     */
    public void terminate() throws IllegalStateException {
        if (certificate == null) return; // already not initialized

        /**
         * Broadcaster and ble are initialised once and then reused after other terminate/init-s.
         * This is because users wouldn't have to reset the listener after terminate/init.
         */
        if (broadcaster != null) broadcaster.terminate();
        if (ble != null) ble.terminate();
        webService.cancelAllRequests();

        coreClockTimer.cancel();
        coreClockTimer = null;

        this.caPublicKey = null;
        this.certificate = null;
        this.privateKey = null;
    }

    /**
     * Download and store a access certificate for the given access token. The access token needs to
     * be provided by the certificate provider.
     *
     * @param accessToken The token that is used to download the certificates.
     * @param callback    A {@link DownloadCallback} object that is invoked after the download is
     *                    finished or failed.
     * @throws IllegalStateException when SDK is not initialized.
     */
    public void downloadCertificate(String accessToken,
                                    final DownloadCallback callback) throws IllegalStateException {
        checkInitialised();
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
                            if (Manager.loggingLevel.getValue() >= Manager
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
     * @return All Access Certificates where this device is providing access.
     * @throws IllegalStateException when SDK is not initialized.
     */
    public AccessCertificate[] getCertificates() throws IllegalStateException {
        checkInitialised();
        return storage.getCertificatesWithProvidingSerial(getDeviceCertificate().getSerial()
                .getByteArray());
    }

    /**
     * @param context The application context.
     * @param serial  The serial of the device that is providing access (eg this device).
     * @return All stored Access Certificates where the device with the given serial is providing
     * access.
     * @deprecated Use {@link #getCertificates()} instead.
     */
    @Deprecated
    public AccessCertificate[] getCertificates(DeviceSerial serial, Context context) {
        if (storage == null) storage = new Storage(context);
        return storage.getCertificatesWithProvidingSerial(serial.getByteArray());
    }

    /**
     * Find an Access Certificate with the given serial number.
     *
     * @param serial The serial number of the device that is gaining access.
     * @return An Access Certificate for the given serial if one exists, otherwise null.
     * @throws IllegalStateException when SDK is not initialized.
     */
    public AccessCertificate getCertificate(DeviceSerial serial) {
        AccessCertificate[] certificates = storage.getCertificatesWithGainingSerial(serial
                .getByteArray());

        if (certificates != null && certificates.length > 0) {
            return certificates[0];
        }

        return null;
    }

    /**
     * Find a Access Certificate with the given serial number.
     *
     * @param serial  The serial number of the device that is gaining access.
     * @param context The application context.
     * @return An Access Certificate for the given serial if one exists, otherwise null.
     * @deprecated Use {@link #getCertificate(DeviceSerial)} instead.
     */
    @Deprecated
    public AccessCertificate getCertificate(DeviceSerial serial, Context context) {
        if (storage == null) storage = new Storage(context);
        AccessCertificate[] certificates = storage.getCertificatesWithGainingSerial(serial
                .getByteArray());

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
     * @throws IllegalStateException when SDK is not initialized.
     */
    public boolean deleteCertificate(DeviceSerial serial) throws IllegalStateException {
        checkInitialised();
        return storage.deleteCertificate(serial.getByteArray(), certificate.getSerial()
                .getByteArray());
    }

    /**
     * Delete an access certificate.
     *
     * @param serial  The serial of the device that is gaining access.
     * @param context The application context.
     * @return true if the certificate existed and was deleted successfully, otherwise false.
     * @deprecated Use {@link #deleteCertificate(DeviceSerial)} instead.
     */
    @Deprecated
    public boolean deleteCertificate(DeviceSerial serial, Context context) {
        checkInitialised();
        if (storage == null) storage = new Storage(context);
        return storage.deleteCertificate(serial.getByteArray(), certificate.getSerial()
                .getByteArray());
    }

    /**
     * Deletes all of the stored Access Certificates.
     */
    public void deleteCertificates() {
        storage.resetStorage();
    }

    /**
     * Deletes all the stored Access Certificates.
     *
     * @param context The application context.
     * @deprecated Use {@link #deleteCertificates()} instead.
     */
    @Deprecated
    public void deleteCertificates(Context context) {
        if (storage == null) storage = new Storage(context);
        storage.resetStorage();
    }

    void postToMainThread(Runnable runnable) {
        if (Looper.myLooper() != mainHandler.getLooper()) {
            mainHandler.post(runnable);
        } else {
            runnable.run();
        }
    }

    void checkInitialised() throws IllegalStateException {
        if (certificate == null) throw new IllegalStateException("SDK not initialized");
    }

    private void startCoreClock() {
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

    /**
     * The web environment.
     */
    public enum Environment {
        TEST, STAGING, PRODUCTION
    }

    /**
     * The logging level.
     */
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
     * {@link #downloadCertificate(String, DownloadCallback)} result.
     */
    public interface DownloadCallback {
        /**
         * Invoked if the certificate download was successful.
         *
         * @param serial the vehicle or charger serial.
         */
        void onDownloaded(DeviceSerial serial);

        /**
         * Invoked when there was an error with the certificate download.
         *
         * @param error The error
         */
        void onDownloadFailed(DownloadAccessCertificateError error);
    }
}
