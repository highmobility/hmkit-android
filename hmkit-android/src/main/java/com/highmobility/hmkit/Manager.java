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

    private Context context;
    private Scanner scanner;
    private Broadcaster broadcaster;
    private Telematics telematics;
    private WebService webService;
    private SharedBle ble;
    private Storage storage;

    HMBTCore core = new HMBTCore();
    BTCoreInterface coreInterface;
    Handler mainHandler, workHandler;
    private final HandlerThread workThread = new HandlerThread("BTCoreThread");
    private Timer coreClockTimer;

    private DeviceCertificate certificate;
    byte[] issuer, appId; // these are set from BTCoreInterface HMBTHalAdvertisementStart.

    /**
     * @return The Application Context set in {@link #initialise(DeviceCertificate, PrivateKey,
     * PublicKey, Context)}.
     */
    public Context getContext() {
        throwIfContextNotSet();
        return context;
    }

    /**
     * @return The Broadcaster instance. Null if BLE is not supported.
     */
    @Nullable public Broadcaster getBroadcaster() {
        throwIfDeviceCertificateNotSet();

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
        throwIfDeviceCertificateNotSet();

        if (telematics == null) telematics = new Telematics(this);

        return telematics;
    }

    /**
     * @return The Scanner Instance. Null if BLE is not supported.
     */
    @Nullable Scanner getScanner() {
        throwIfDeviceCertificateNotSet();

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
     * @return The device certificate that is used by the SDK to identify itself.
     */
    public DeviceCertificate getDeviceCertificate() {
        throwIfDeviceCertificateNotSet();
        return certificate;
    }

    /**
     * @return An SDK description string containing version name and type(mobile or wear).
     * @throws IllegalStateException when SDK is not initialised.
     */
    public String getInfoString() {
        throwIfContextNotSet();

        String infoString = "Android ";
        infoString += BuildConfig.VERSION_NAME;

        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WATCH)) {
            infoString += " w";
        } else {
            infoString += " m";
        }

        return infoString;
    }

    // protected ivars are accessed when ctx is already checked
    @Nullable SharedBle getBle() {
        if (ble == null) {
            try {
                ble = new SharedBle(context); // could be that the device has no bluetooth.
            } catch (BleNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return ble;
    }

    WebService getWebService() {
        if (webService == null) webService = new WebService(context);
        return webService;
    }

    /**
     * @return The instance of the Manager.
     */
    public static Manager getInstance() {
        if (instance == null) instance = new Manager();

        return instance;
    }

    /**
     * The Storage can be accessed before initialise.
     *
     * @return The storage for Access Certificates.
     */
    public Storage getStorage() {
        throwIfContextNotSet();
        return storage;
    }

    /**
     * Initialise with context only. This allows access to storage. Call {@link
     * #setDeviceCertificate (DeviceCertificate, PrivateKey, PublicKey)} later to send Commands.
     *
     * @param context The application context.
     * @return The Manager instance.
     */
    public Manager initialise(Context context) {
        createStorage(context);
        return instance;
    }

    /**
     * Initialise the SDK with a Device Certificate. Call this before using the Manager.
     *
     * @param certificate     The broadcaster certificate.
     * @param privateKey      32 byte private key with elliptic curve Prime 256v1.
     * @param issuerPublicKey 64 byte public key of the Certificate Authority.
     * @param context         The Application Context.
     * @return The Manager instance.
     */
    public Manager initialise(DeviceCertificate certificate, PrivateKey privateKey, PublicKey
            issuerPublicKey, Context context) {
        if (this.certificate != null) {
            // context could be set if Storage was accessed before. Need to check for certificate.
            throw new IllegalStateException("Manager can be initialised once. Call " +
                    "setDeviceCertificate() to set new Device Certificate.");
        }

        initialise(context);

        this.certificate = certificate;
        coreInterface.caPublicKey = issuerPublicKey;
        coreInterface.privateKey = privateKey;

        Log.i(TAG, "Initialised High-Mobility " + getInfoString() + certificate.toString());

        return this;
    }

    /**
     * Initialise the SDK with a Device Certificate. Call this before using the Manager.
     *
     * @param certificate The broadcaster certificate.
     * @param privateKey  32 byte private key with elliptic curve Prime 256v1.
     * @param caPublicKey 64 byte public key of the Certificate Authority.
     * @param context     The Application Context.
     * @deprecated Use {@link #initialise(DeviceCertificate, PrivateKey, PublicKey, Context)}
     * instead.
     */
    @Deprecated
    public void initialize(DeviceCertificate certificate,
                           PrivateKey privateKey,
                           PublicKey caPublicKey,
                           Context context) {
        initialise(certificate, privateKey, caPublicKey, context);
    }

    /**
     * Initialise the SDK with a Device Certificate. Call this before using the Manager.
     *
     * @param certificate     The device certificate in Base64 or hex.
     * @param privateKey      32 byte private key with elliptic curve Prime 256v1 in Base64 or hex.
     * @param issuerPublicKey 64 byte public key of the Certificate Authority in Base64 or hex.
     * @param context         the application context
     * @deprecated Use {@link #initialise(String, String, String, Context)} instead.
     */
    @Deprecated
    public void initialize(String certificate, String privateKey, String issuerPublicKey, Context
            context) {
        initialise(certificate, privateKey, issuerPublicKey, context);
    }

    /**
     * Initialise the SDK with a Device Certificate. Call this before using the Manager.
     *
     * @param certificate     The device certificate in Base64 or hex.
     * @param privateKey      32 byte private key with elliptic curve Prime 256v1 in Base64 or hex.
     * @param issuerPublicKey 64 byte public key of the Certificate Authority in Base64 or hex.
     * @param context         The Application Context.
     * @return The Manager instance.
     */
    public Manager initialise(String certificate, String privateKey, String
            issuerPublicKey, Context context) {
        DeviceCertificate decodedCert = new DeviceCertificate(new Bytes(Base64.decode
                (certificate)));
        PrivateKey decodedPrivateKey = new PrivateKey(privateKey);
        PublicKey decodedIssuerPublicKey = new PublicKey(issuerPublicKey);
        initialise(decodedCert, decodedPrivateKey, decodedIssuerPublicKey, context);
        return this;
    }

    /**
     * Set a new Device Certificate.
     *
     * @param certificate     The device certificate in Base64 or hex.
     * @param privateKey      32 byte private key with elliptic curve Prime 256v1 in Base64 or hex.
     * @param issuerPublicKey 64 byte public key of the Certificate Authority in Base64 or hex.
     * @throws IllegalStateException if there are connected links with the Broadcaster or an ongoing
     *                               Telematics command.
     */
    public void setDeviceCertificate(String certificate, String privateKey, String
            issuerPublicKey) throws IllegalStateException {
        DeviceCertificate decodedCert = new DeviceCertificate(new Bytes(Base64.decode
                (certificate)));
        PrivateKey decodedPrivateKey = new PrivateKey(privateKey);
        PublicKey decodedIssuerPublicKey = new PublicKey(issuerPublicKey);
        setDeviceCertificate(decodedCert, decodedPrivateKey, decodedIssuerPublicKey);
    }

    /**
     * Set a new Device Certificate.
     *
     * @param certificate     The device certificate.
     * @param privateKey      32 byte private key with elliptic curve Prime 256v1.
     * @param issuerPublicKey 64 byte public key of the Certificate Authority.
     * @throws IllegalStateException if there are connected links with the Broadcaster or an ongoing
     *                               Telematics command.
     */
    public void setDeviceCertificate(DeviceCertificate certificate, PrivateKey privateKey,
                                     PublicKey issuerPublicKey) throws IllegalStateException {
        throwIfContextNotSet(); // need to check that context is set(initialise called).
        // TODO: 31/08/2018 test with HMKit-sandbox app.

        if (broadcaster != null && broadcaster.getLinks().size() > 0) {
            throw new IllegalStateException("Cannot set a new Device Certificate if a connected " +
                    "link exists with the Broadcaster. Disconnect from all of the links.");
        }

        if (telematics != null && telematics.isSendingCommand()) {
            throw new IllegalStateException("Cannot set a new Device Certificate sending " +
                    "a Telematics command. Wait for the commands to finish.");
        }

        if (scanner != null && scanner.getLinks().size() > 0) {
            throw new IllegalStateException("Cannot set a new Device Certificate if a connected " +
                    "link exists with the Scanner. Disconnect from all of the links.");
        }

        coreInterface.caPublicKey = issuerPublicKey;
        coreInterface.privateKey = privateKey;
        this.certificate = certificate;
    }

    /**
     * Terminate is meant to be called when the SDK is not used anymore(on app kill for example). It
     * stops internal processes, unregisters BroadcastReceivers, stops broadcasting, cancels web
     * requests.
     * <p>
     * Stored certificates are not deleted.
     *
     * @throws IllegalStateException when there are links still connected.
     */
    public void terminate() throws IllegalStateException {
        // TODO: 30/08/2018 try to call terminate 2x

        /**
         * Broadcaster and ble need to be terminated on app kill. Currently they can be used
         * again after terminate(they start the processes again automatically) but this is not a
         * requirement.
         */
        if (broadcaster != null) broadcaster.terminate();
        if (ble != null) ble.terminate();
        // this terminates telematics as well because that uses the same web service.
        if (webService != null) webService.cancelAllRequests();

        stopCore();
    }

    /**
     * Download and store a access certificate for the given access token. The access token needs to
     * be provided by the certificate provider.
     *
     * @param accessToken The token that is used to download the certificates.
     * @param callback    A {@link DownloadCallback} object that is invoked after the download is
     *                    finished or failed.
     */
    public void downloadCertificate(String accessToken, final DownloadCallback callback) {
        throwIfDeviceCertificateNotSet();
        getWebService().requestAccessCertificate(accessToken,
                coreInterface.privateKey,
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
     */
    public AccessCertificate[] getCertificates() {
        throwIfDeviceCertificateNotSet();
        return storage.getCertificatesWithProvidingSerial(getDeviceCertificate().getSerial()
                .getByteArray());
    }

    /**
     * Find an Access Certificate with the given serial number.
     *
     * @param serial The serial number of the device that is gaining access.
     * @return An Access Certificate for the given serial if one exists, otherwise null.
     */
    @Nullable public AccessCertificate getCertificate(DeviceSerial serial) {
        throwIfDeviceCertificateNotSet();
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
     * @return true if the certificate existed and was deleted successfully, otherwise false.
     */
    public boolean deleteCertificate(DeviceSerial serial) {
        throwIfDeviceCertificateNotSet();
        return storage.deleteCertificate(serial.getByteArray(), certificate.getSerial()
                .getByteArray());
    }

    /**
     * Deletes all of the stored Access Certificates.
     */
    public void deleteCertificates() {
        throwIfDeviceCertificateNotSet();
        storage.deleteCertificates();
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
        // this method should be deleted. cannot be initialised without context
        throwIfDeviceCertificateNotSet();
        return storage.deleteCertificate(serial.getByteArray(), certificate.getSerial()
                .getByteArray());
    }

    /**
     * @param context The application context.
     * @param serial  The serial of the device that is providing access (eg this device).
     * @return All stored Access Certificates where the device with the given serial is providing
     * access.
     * @deprecated Use {@link #getStorage()#getCertificates(DeviceSerial)} instead.
     */
    @Deprecated
    public AccessCertificate[] getCertificates(DeviceSerial serial, Context context) {
        createStorage(context);
        return storage.getCertificates(serial);
    }

    /**
     * Find an Access Certificate with the given serial number.
     *
     * @param serial  The serial number of the device that is gaining access.
     * @param context The application context.
     * @return An Access Certificate for the given serial if one exists, otherwise null.
     * @deprecated Use {@link #getStorage()#getCertificate(DeviceSerial)} instead.
     */
    @Deprecated
    @Nullable public AccessCertificate getCertificate(DeviceSerial serial, Context context) {
        createStorage(context);
        return storage.getCertificate(serial);
    }

    /**
     * Deletes all of the stored Access Certificates.
     *
     * @param context The application context.
     * @deprecated Use {@link #getStorage()#deleteCertificates()} instead.
     */
    @Deprecated
    public void deleteCertificates(Context context) {
        createStorage(context);
        storage.deleteCertificates();
    }

    void postToMainThread(Runnable runnable) {
        if (Looper.myLooper() != mainHandler.getLooper()) {
            mainHandler.post(runnable);
        } else {
            runnable.run();
        }
    }

    void throwIfDeviceCertificateNotSet() throws IllegalStateException {
        // if device cert exists, context has to exist as well.
        if (certificate == null)
            throw new IllegalStateException("Device certificate is not set. Call Manager" +
                    ".initialise() first.");
    }

    void throwIfContextNotSet() throws IllegalStateException {
        if (context == null) {
            throw new IllegalStateException("Context is not set. Call Manager" +
                    ".initialise() first.");
        }
    }

    void startCore() {
        throwIfDeviceCertificateNotSet();

        // create once if doesn't exist
        if (coreInterface == null) {
            mainHandler = new Handler(this.context.getMainLooper());
            workThread.start();
            workHandler = new Handler(workThread.getLooper());

            // core init needs to be done once, only initialises structs(but requires device cert)
            coreInterface = new BTCoreInterface(this);
            core.HMBTCoreInit(coreInterface);
        }

        // start the core clock if is not running already
        if (coreClockTimer == null) {
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

    private void createStorage(Context context) {
        // storage could be accessed before init.
        if (storage == null) {
            this.context = context.getApplicationContext();
            storage = new Storage(this.context);
        }
    }

    private void stopCore() {
        if (coreClockTimer != null) {
            coreClockTimer.cancel();
            coreClockTimer = null;
        }
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
