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
    private DeviceCertificate certificate;
    PrivateKey privateKey;
    PublicKey caPublicKey;
    byte[] issuer, appId; // these are set from BTCoreInterface HMBTHalAdvertisementStart.
    HMBTCore core = new HMBTCore();
    BTCoreInterface coreInterface;
    static Storage storage;

    private Scanner scanner;
    private Broadcaster broadcaster;
    private Telematics telematics;
    private WebService webService;
    private SharedBle ble;

    Handler mainHandler;
    Handler workHandler;
    private final HandlerThread workThread = new HandlerThread("BTCoreThread");
    private Timer coreClockTimer;

    /**
     * @return The Application Context set in {@link #initialise(DeviceCertificate, PrivateKey,
     * PublicKey, Context)}.
     */
    public Context getContext() {
        checkInitialised();
        return context;
    }

    /**
     * @return The Broadcaster instance. Null if BLE is not supported.
     */
    @Nullable public Broadcaster getBroadcaster() {
        checkInitialised();

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
        checkInitialised();

        if (telematics == null) telematics = new Telematics(this);

        return telematics;
    }

    /**
     * @return The Scanner Instance. Null if BLE is not supported.
     */
    @Nullable Scanner getScanner() {
        checkInitialised();

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
        checkInitialised();
        return certificate;
    }

    /**
     * @return An SDK description string containing version name and type(mobile or wear).
     * @throws IllegalStateException when SDK is not initialised.
     */
    public String getInfoString() {
        checkInitialised();
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
     * @param context The application context.
     * @return The storage for Access Certificates.
     */
    public static Storage getStorage(Context context) {
        createStorage(context);
        return storage;
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
     * @param certificate The broadcaster certificate.
     * @param privateKey  32 byte private key with elliptic curve Prime 256v1.
     * @param caPublicKey 64 byte public key of the Certificate Authority.
     * @param context     The Application Context.
     * @return The Manager instance.
     */
    public Manager initialise(DeviceCertificate certificate, PrivateKey privateKey, PublicKey
            caPublicKey, Context context) {
        if (this.certificate != null) {
            // context could be set if Storage was accessed before. Need to check for certificate.
            throw new IllegalStateException("Manager can be initialised once. Call " +
                    "setDeviceCertificate() to set new Device Certificate.");
        }

        this.context = context.getApplicationContext();
        createStorage(this.context);

        this.caPublicKey = caPublicKey;
        this.certificate = certificate;
        this.privateKey = privateKey;

        // TODO: 29/08/2018 start core/broadcaster/telematics logic in telematics / broadcaster
        startCore();

        // initialise after terminate.
        // TODO: 29/08/2018 init never called after terminate
        if (ble != null) ble.initialise();
        if (broadcaster != null) broadcaster.initialise();

        Log.i(TAG, "Initialized High-Mobility " + getInfoString() + certificate.toString());

        return this;
    }

    /**
     * Initialise the SDK with a Device Certificate. Call this before using the Manager.
     *
     * @param certificate     The device certificate in Base64 or hex.
     * @param privateKey      32 byte private key with elliptic curve Prime 256v1 in Base64 or hex.
     * @param issuerPublicKey 64 byte public key of the Certificate Authority in Base64 or hex.
     * @param context         the application context
     * @throws IllegalArgumentException if the parameters are invalid.
     * @deprecated Use {@link #initialise(String, String, String, Context)} instead.
     */
    @Deprecated
    public void initialize(String certificate, String privateKey, String issuerPublicKey, Context
            context) throws IllegalArgumentException {
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
     * @throws IllegalArgumentException if the parameters are invalid.
     */
    public Manager initialise(String certificate, String privateKey, String
            issuerPublicKey, Context context) throws
            IllegalArgumentException {
        DeviceCertificate decodedCert = new DeviceCertificate(new Bytes(Base64.decode
                (certificate)));
        PrivateKey decodedPrivateKey = new PrivateKey(privateKey);
        PublicKey decodedIssuerPublicKey = new PublicKey(issuerPublicKey);
        initialise(decodedCert, decodedPrivateKey, decodedIssuerPublicKey, context);
        return this;
    }

    /**
     * @param certificate
     * @param privateKey
     * @param issuerPublicKey
     * @throws IllegalStateException if there are connected links with the Broadcaster or an ongoing
     *                               Telematics command.
     */
    public void setDeviceCertificate(String certificate, String privateKey, String
            issuerPublicKey) throws IllegalStateException {
        // TODO: 29/08/2018 implement and comment
    }

    public void setDeviceCertificate(DeviceCertificate certificate, PrivateKey privateKey,
                                     PublicKey caPublicKey) throws IllegalStateException {
        // TODO: 29/08/2018 implement and comment

    }

    /**
     * Terminate is meant to be called when the SDK is not used anymore(on app kill for example). It
     * stops internal processes, unregisters BroadcastReceivers, stops broadcasting, cancels
     * Telematics commands.
     * <p>
     * You can re use Broadcaster and Telematics after terminate without re initialise(Device
     * Certificate reference is retained).
     * <p>
     * Stored certificates are not deleted.
     *
     * @throws IllegalStateException when there are links still connected.
     */
    public void terminate() throws IllegalStateException {
        // TODO: use something to check that does not terminate twice. or check inside the methods

        /**
         * Broadcaster and ble are initialised once and then reused after other terminate/init-s.
         * This is because users wouldn't have to reset the listener after terminate/init.
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
        checkInitialised();
        getWebService().requestAccessCertificate(accessToken,
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
     */
    public AccessCertificate[] getCertificates() {
        checkInitialised();
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
        checkInitialised();
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
        checkInitialised();
        return storage.deleteCertificate(serial.getByteArray(), certificate.getSerial()
                .getByteArray());
    }

    /**
     * Deletes all of the stored Access Certificates.
     */
    public void deleteCertificates() {
        checkInitialised();
        storage.resetStorage();
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
        checkInitialised();
        return storage.deleteCertificate(serial.getByteArray(), certificate.getSerial()
                .getByteArray());
    }

    /**
     * @param context The application context.
     * @param serial  The serial of the device that is providing access (eg this device).
     * @return All stored Access Certificates where the device with the given serial is providing
     * access.
     * @deprecated Use {@link Storage#getCertificates(DeviceSerial)} instead.
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
     * @deprecated Use {@link Storage#getCertificate(DeviceSerial)} instead.
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
     * @deprecated Use {@link Storage#deleteCertificates()} instead.
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

    void checkInitialised() throws IllegalStateException {
        // if device cert exists, context has to exist as well.
        if (certificate == null)
            throw new IllegalStateException("Device certificate is not set. Call Manager" +
                    ".initialise() first.");
    }

    private static void createStorage(Context context) {
        // storage could be accessed before init.
        if (storage == null) storage = new Storage(context.getApplicationContext());
    }

    private void startCore() {
        // create the core if doesn't exist
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
