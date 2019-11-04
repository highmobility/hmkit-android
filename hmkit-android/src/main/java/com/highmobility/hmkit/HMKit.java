package com.highmobility.hmkit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.highmobility.crypto.AccessCertificate;
import com.highmobility.crypto.Crypto;
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

import javax.annotation.Nullable;

import static com.highmobility.hmkit.HMLog.e;
import static com.highmobility.hmkit.HMLog.i;
import static com.highmobility.hmkit.HMLog.w;

/**
 * HMKit is the entry point for the HMKit library. Use the singleton to access Broadcaster and
 * Telematics.
 */
public class HMKit {
    /**
     * Custom web environment url. If set, will override the default url or the url from the device
     * certificate.
     */
    @Nullable public static String webUrl = null;

    static {
        if (Build.DEVICE.equals("robolectric") == false)
            System.loadLibrary("hmbtcore");
    }

    // Using application context, no chance for leak.
    @SuppressLint("StaticFieldLeak") private static volatile HMKit instance;

    private Context context;

    // created with device cert
    private Broadcaster broadcaster;
    private Scanner scanner;
    private Telematics telematics;
    private OAuth oauth;
    private Core core;

    // created with context
    private WebService webService;
    @Nullable private SharedBle ble;
    private Storage storage;
    private ThreadManager threadManager;
    private Crypto crypto;

    /**
     * @return The Broadcaster instance. Null if BLE is not supported.
     */
    @Nullable public Broadcaster getBroadcaster() {
        throwIfDeviceCertificateNotSet();

        if (ble == null) return null;

        if (broadcaster == null) {
            broadcaster = new Broadcaster(core, storage, threadManager, ble);

        }

        return broadcaster;
    }

    /**
     * @return The Scanner Instance. Null if BLE is not supported.
     */
    @Nullable Scanner getScanner() {
        throwIfDeviceCertificateNotSet();

        if (ble == null) return null;

        if (scanner == null) {
            scanner = new Scanner(core, storage, threadManager, ble);
        }
        return scanner;
    }

    /**
     * @return The Telematics instance.
     */
    public Telematics getTelematics() {
        throwIfDeviceCertificateNotSet();

        if (telematics == null)
            telematics = new Telematics(core, storage, threadManager, webService);

        return telematics;
    }

    /**
     * @return The OAuth instance.
     */
    public OAuth getOAuth() {
        throwIfDeviceCertificateNotSet();

        if (oauth == null)
            oauth = new OAuth(webService, crypto, core.getPrivateKey(),
                    getDeviceCertificate().getSerial());

        return oauth;
    }

    /**
     * @return The device certificate that is used by the SDK to identify itself.
     */
    public DeviceCertificate getDeviceCertificate() {
        throwIfDeviceCertificateNotSet();
        return core.getDeviceCertificate();
    }

    /**
     * The Storage can be accessed before setting the device certificate.
     *
     * @return The storage for Access Certificates.
     */
    public Storage getStorage() {
        throwIfContextNotSet();
        return storage;
    }

    /**
     * @return The HM crypto.
     */
    public Crypto getCrypto() {
        return crypto;
    }

    /**
     * @return An SDK description string containing version name and type(mobile or wear).
     */
    public String getInfoString() {
        throwIfContextNotSet();

        // has bluetooth (shared with broadcaster)
        if (ble != null) return ble.getInfoString();

        String infoString = infoStringPrefix();
        final PackageManager pm = context.getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_EMBEDDED)) {
            return infoString + "t"; // android things
        } else if (isEmulator()) {
            return infoString + "e"; // emulator
        }

        return infoString + "unknown";
    }

    /**
     * Set the logging level of HMKit.
     *
     * @param level The logging level.
     */
    public static void setLoggingLevel(HMLog.Level level) {
        HMLog.level = level;
    }

    /**
     * Get the logging level of HMKit.
     *
     * @return The logging level
     */
    public static HMLog.Level getLoggingLevel() {
        return HMLog.level;
    }

    static String infoStringPrefix() {
        return "Android " + BuildConfig.VERSION_NAME + " ";
    }

    static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    /**
     * @return The instance of the HMKit.
     */
    public static HMKit getInstance() {
        if (instance == null) {
            synchronized (HMKit.class) {
                // If instance is null, make sure its created thread  safely.
                if (instance == null) instance = new HMKit();
            }
        }

        return instance;
    }

    private HMKit() {
        // protect against reflection where private is not respected.
        if (instance != null) {
            throw new RuntimeException("Use getInstance() to get the HMKit singleton");
        }
        crypto = new Crypto(Core.core);
    }

    /**
     * Initialise the SDK with context to get access to storage only. Call {@link
     * #setDeviceCertificate (DeviceCertificate, PrivateKey, PublicKey)} later to send Commands.
     *
     * @param context The context.
     * @return The HMKit instance.
     */
    public HMKit initialise(Context context) {
        // all initialises come to here. throw to make clear how the sdk is supposed to be used -
        // initialise(cert, ctx) or initialise(ctx) + setDeviceCert(cert).
        if (this.context != null) {
            throw new IllegalStateException("HMKit can be initialised once. Call " +
                    "setDeviceCertificate() to set new Device Certificate.");
        }
        setContextAndCreateStorage(context);
        i("Initialised: %s", getInfoString());
        return instance;
    }

    /**
     * Initialise the SDK with a Device Certificate. This is needed before sending Commands.
     *
     * @param certificate     The broadcaster certificate.
     * @param privateKey      32 byte private key with elliptic curve Prime 256v1.
     * @param issuerPublicKey 64 byte public key of the Certificate Authority.
     * @param context         The Application Context.
     * @return The HMKit instance.
     */
    public HMKit initialise(DeviceCertificate certificate, PrivateKey privateKey, PublicKey
            issuerPublicKey, Context context) {
        initialise(context);
        setDeviceCertificate(certificate, privateKey, issuerPublicKey);
        return this;
    }

    /**
     * Initialise the SDK with a Device Certificate. Call this before using the HMKit.
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
     * Initialise the SDK with a Device Certificate. Call this before using the HMKit.
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
     * Initialise the SDK with a Device Certificate. Call this before using the HMKit.
     *
     * @param certificate     The device certificate in Base64 or hex.
     * @param privateKey      32 byte private key with elliptic curve Prime 256v1 in Base64 or hex.
     * @param issuerPublicKey 64 byte public key of the Certificate Authority in Base64 or hex.
     * @param context         The Application Context.
     * @return The HMKit instance.
     */
    public HMKit initialise(String certificate, String privateKey, String
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
     * @throws IllegalStateException if there are connected links with the Broadcaster or ongoing
     *                               Telematics commands.
     */
    public void setDeviceCertificate(DeviceCertificate certificate, PrivateKey privateKey,
                                     PublicKey issuerPublicKey) throws IllegalStateException {
        throwIfContextNotSet(); // need to check that context is set(initialise called).

        if (broadcaster != null && broadcaster.getLinks().size() > 0) {
            throw new IllegalStateException("Cannot set a new Device Certificate if a connected " +
                    "link exists with the Broadcaster. Disconnect from all of the links.");
        }

        if (telematics != null && telematics.isSendingCommand()) {
            throw new IllegalStateException("Cannot set a new Device Certificate while sending " +
                    "a Telematics command. Wait for the command to finish.");
        }

        if (scanner != null && scanner.getLinks().size() > 0) {
            throw new IllegalStateException("Cannot set a new Device Certificate if a connected " +
                    "link exists with the Scanner. Disconnect from all of the links.");
        }

        if (core == null) {
            core = new Core(storage, threadManager, certificate, privateKey, issuerPublicKey,
                    getLoggingLevel());
        } else core.setDeviceCertificate(certificate, privateKey, issuerPublicKey);

        if (oauth != null) oauth.setDeviceCertificate(privateKey, certificate.getSerial());

        if (webService == null)
            webService = new WebService(context, crypto, certificate.getIssuer(), webUrl);
        else webService.setIssuer(certificate.getIssuer(), webUrl);

        i("Set certificate: %s", certificate.toString());
    }

    /**
     * Stop internal processes, unregister BroadcastReceivers, stop broadcasting, cancel web
     * requests. It is meant to be called once, when app is destroyed.
     * <p>
     * Stored certificates are not deleted.
     *
     * @throws IllegalStateException When there are connected links.
     */
    public void terminate() throws IllegalStateException {
        /*
          Broadcaster and ble need to be terminated on app kill. Currently they can be used
          again after terminate(they start the processes again automatically) but this is not a
          requirement since terminate is supposed to be called once.
         */
        if (broadcaster != null) broadcaster.terminate();
        if (ble != null) ble.terminate();
        // this terminates telematics as well because that uses the same web service.
        if (webService != null) webService.cancelAllRequests();
        core.stop();
    }

    /**
     * Download and store the access certificate for the given access token. The access token needs
     * to be provided by the certificate provider.
     *
     * @param accessToken The token that is used to download the certificates.
     * @param callback    A {@link DownloadCallback} object that is invoked after the download is
     *                    finished or failed.
     */
    public void downloadAccessCertificate(String accessToken, final DownloadCallback callback) {
        throwIfDeviceCertificateNotSet();
        webService.requestAccessCertificate(accessToken,
                core.getPrivateKey(),
                getDeviceCertificate().getSerial(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        AccessCertificate certificate = null;
                        try {
                            certificate = storage.storeDownloadedCertificates(response);
                        } catch (Exception ex) {
                            e("storeDownloadedCertificates error: " + ex.getMessage());

                            DownloadAccessCertificateError error = new
                                    DownloadAccessCertificateError(
                                    DownloadAccessCertificateError.Type.INVALID_SERVER_RESPONSE,
                                    0, ex.getMessage());
                            callback.onDownloadFailed(error);
                        }

                        if (certificate != null)
                            callback.onDownloaded(certificate.getGainerSerial());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        DownloadAccessCertificateError dispatchedError;

                        if (error.networkResponse != null) {
                            try {
                                JSONObject json = new JSONObject(new String(error.networkResponse
                                        .data));
                                w("onErrorResponse: " + json.toString());
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
                                    WebService.NO_CONNECTION_ERROR);
                        }

                        callback.onDownloadFailed(dispatchedError);
                    }
                });
    }

    /**
     * Download and store the access certificate for the given access token. The access token needs
     * to be provided by the certificate provider.
     *
     * @param accessToken The token that is used to download the certificates.
     * @param callback    A {@link DownloadCallback} object that is invoked after the download is
     *                    finished or failed.
     * @deprecated Use {@link #downloadAccessCertificate(String, DownloadCallback)} instead.
     */
    @Deprecated
    public void downloadCertificate(String accessToken, final DownloadCallback callback) {
        downloadAccessCertificate(accessToken, callback);
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
        return storage.deleteCertificate(serial.getByteArray(), core.getDeviceCertificate()
                .getSerial()
                .getByteArray());
    }

    /**
     * Deletes all of the stored Access Certificates.
     */
    public void deleteCertificates() {
        throwIfContextNotSet();
        storage.deleteCertificates();
    }

    /**
     * Delete an access certificate.
     *
     * @param serial  The serial of the device that is gaining access.
     * @param context The context.
     * @return true if the certificate existed and was deleted successfully, otherwise false.
     * @deprecated Use {@link #deleteCertificate(DeviceSerial)} instead.
     */
    @Deprecated
    public boolean deleteCertificate(DeviceSerial serial, Context context) {
        // this method should be deleted. cannot be initialised without context
        throwIfDeviceCertificateNotSet();
        return storage.deleteCertificate(serial.getByteArray(), core.getDeviceCertificate()
                .getSerial().getByteArray());
    }

    /**
     * @param context The context.
     * @param serial  The serial of the device that is providing access (eg this device).
     * @return All stored Access Certificates where the device with the given serial is providing
     * access.
     * @deprecated Use {@link #getStorage()#getCertificates(DeviceSerial)} instead.
     */
    @Deprecated
    public AccessCertificate[] getCertificates(DeviceSerial serial, Context context) {
        try {
            initialise(context);
        } finally {
            return storage.getCertificates(serial);
        }
    }

    /**
     * Find an Access Certificate with the given serial number.
     *
     * @param serial  The serial number of the device that is gaining access.
     * @param context The context.
     * @return An Access Certificate for the given serial if one exists, otherwise null.
     * @deprecated Use {@link #getStorage()#getCertificate(DeviceSerial)} instead.
     */
    @Deprecated
    @Nullable public AccessCertificate getCertificate(DeviceSerial serial, Context context) {
        try {
            initialise(context);
        } finally {
            return storage.getCertificate(serial);
        }
    }

    /**
     * Deletes all of the stored Access Certificates.
     *
     * @param context The context.
     * @deprecated Use {@link #getStorage()#deleteCertificates()} instead.
     */
    @Deprecated
    public void deleteCertificates(Context context) {
        try {
            initialise(context);
        } finally {
            storage.deleteCertificates();
        }
    }

    void throwIfDeviceCertificateNotSet() throws IllegalStateException {
        // if device cert exists, context has to exist as well.
        if (core == null) {
            throwIfContextNotSet();
            throw new IllegalStateException("Device certificate is not set. Call HMKit" +
                    ".setDeviceCertificate() first.");
        }
    }

    void throwIfContextNotSet() throws IllegalStateException {
        if (context == null) {
            throw new IllegalStateException("Context is not set. Call HMKit.initialise() first.");
        }
    }

    private void setContextAndCreateStorage(Context context) {
        // storage can be accessed with context only.
        if (storage == null) {
            this.context = context.getApplicationContext();
            storage = new Storage(this.context);
            threadManager = new ThreadManager(this.context);
            try {
                ble = new SharedBle(this.context);
            } catch (BleNotSupportedException e) {
                i("BLE not supported");
            }
        }
    }

    /**
     * {@link #downloadAccessCertificate(String, DownloadCallback)} result.
     */
    public interface DownloadCallback {
        /**
         * Invoked when the certificate download was successful.
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
