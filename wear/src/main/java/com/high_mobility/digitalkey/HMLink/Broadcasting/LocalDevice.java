package com.high_mobility.digitalkey.HMLink.Broadcasting;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import com.high_mobility.digitalkey.HMLink.Broadcasting.Core.FakeCore;
import com.high_mobility.digitalkey.HMLink.Broadcasting.Core.HMDevice;
import com.high_mobility.digitalkey.HMLink.Constants;
import com.high_mobility.digitalkey.HMLink.Device;
import com.high_mobility.digitalkey.HMLink.LinkException;
import com.high_mobility.digitalkey.HMLink.Shared.AccessCertificate;
import com.high_mobility.digitalkey.HMLink.Shared.DeviceCertificate;
import com.high_mobility.digitalkey.Utils;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

/**
 * Created by ttiganik on 12/04/16.
 */
public class LocalDevice extends Device {
    private static final String TAG = LocalDevice.class.getSimpleName();

    static final boolean ALLOWS_MULTIPLE_LINKS = false;

    public enum State { BLUETOOTH_UNAVAILABLE, IDLE, BROADCASTING }

    Context ctx;
    Storage storage;
    byte[] privateKey;
    byte[] CAPublicKey;
    LocalDeviceCallback callback;

    BluetoothManager mBluetoothManager;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    BluetoothGattServer GATTServer;
    GATTServerCallback gattServerCallback;


    BluetoothGattCharacteristic readCharacteristic;
    BluetoothGattCharacteristic writeCharacteristic;
    Handler mainThreadHandler;

    BTCoreInterface coreInterface;
    FakeCore core = new FakeCore();

    static LocalDevice instance = null;
    public State state = State.IDLE;
    Link[] links = new Link[0];

    public static LocalDevice getInstance() {
        if (instance == null) {
            instance = new LocalDevice();
        }

        return instance;
    }

    public void registerCallback(LocalDeviceCallback callback) {
        this.callback = callback;
    }

    public void setDeviceCertificate(DeviceCertificate certificate, byte[] privateKey, byte[] CAPublicKey, Context ctx) {
        this.ctx = ctx;
        storage = new Storage(ctx);

        this.certificate = certificate;
        this.privateKey = privateKey;
        this.CAPublicKey = CAPublicKey;
        storage = new Storage(ctx);
        createAdapter();
        mainThreadHandler = new Handler(ctx.getMainLooper());
        coreInterface = new BTCoreInterface(this);
        core.HMBTCoreInit(coreInterface);
    }

    public AccessCertificate[] getRegisteredCertificates() {
        return storage.getRegisteredCertificates(certificate.getSerial());
    }

    public AccessCertificate[] getStoredCertificates() {
        return storage.getStoredCertificates(certificate.getSerial());
    }

    public Link[] getLinks() {
        return links;
    }

    public void startBroadcasting() throws LinkException {
        if (ALLOWS_MULTIPLE_LINKS == false && links.length != 0) {
            return;
        }

        checkIfBluetoothIsEnabled();

        createGATTServer();

        // start advertising
        if (mBluetoothLeAdvertiser == null) {
            mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        }

        final AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .build();

        final UUID advertiseUUID = Utils.UUIDFromByteArray(Utils.concatBytes(certificate.getIssuer(), certificate.getAppIdentifier()));

        final AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(new ParcelUuid(advertiseUUID))
                .build();

        mBluetoothLeAdvertiser.startAdvertising(settings, data, advertiseCallback);
    }

    public void stopBroadcasting() {
        // if (mBluetoothLeAdvertiser == null) return;
        // mBluetoothLeAdvertiser.stopAdvertising(advertiseCallback);

        // TODO:
        // this clears the GATT server as well and GATTServer.sendResponse fails with nullPointer.
        // or device disconnects automatically if on main thread
        // Figure out how to stop advertise with a continually working server
    }

    public void closeGATTServer() {
        if (GATTServer != null) {
            GATTServer.clearServices();
            GATTServer.close();
            GATTServer = null;
        }

        // TODO: delete when stop broadcasting is fixed (will be stopped at some other time)
        if (mBluetoothLeAdvertiser != null) {
            mBluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
        }
        //
    }

    public void registerCertificate(AccessCertificate certificate) throws LinkException {
        if (this.certificate == null) {
            throw new LinkException(LinkException.LinkExceptionCode.INTERNAL_ERROR);
        }

        if (Arrays.equals(this.certificate.getSerial(), certificate.getProviderSerial()) == false) {
            throw new LinkException(LinkException.LinkExceptionCode.INTERNAL_ERROR);
        }

        storage.storeCertificate(certificate);
    }

    public void storeCertificate(AccessCertificate certificate) throws LinkException {
        storage.storeCertificate(certificate);
    }

    public void revokeCertificate(byte[] serial) throws LinkException {
        if (storage.certWithGainingSerial(serial) == null
                || storage.certWithProvidingSerial(serial) == null) {
            throw new LinkException(LinkException.LinkExceptionCode.INTERNAL_ERROR);
        }

        storage.deleteCertificateWithGainingSerial(serial);
        storage.deleteCertificateWithProvidingSerial(serial);
    }

    public void reset() {
        storage.resetStorage();
        closeGATTServer();

        try {
            startBroadcasting();
        } catch (LinkException e) {
            e.printStackTrace();
        }
    }

    void didReceiveCustomCommand(HMDevice device, byte[] data, int length, int error) {
        // TODO: implement when cleared
        BluetoothDevice btDevice = mBluetoothAdapter.getRemoteDevice(device.getMac());
        int linkIndex = linkIndexForBTDevice(btDevice);

        if (linkIndex > -1) {
            Link link = links[linkIndex];
            link.callback.linkDidReceiveCustomCommand(link, data);
        }
        else {
            Log.e(TAG, "no link for custom command received");
        }
    }

    void didReceiveLink(HMDevice device) {
        if (LocalDevice.ALLOWS_MULTIPLE_LINKS == false) {
            stopBroadcasting();
        }

        // add a new link to the array
        BluetoothDevice btDevice = mBluetoothAdapter.getRemoteDevice(device.getMac());
        final Link link = new Link(btDevice, this);
        Link[] newLinks = new Link[links.length + 1];

        for (int i = 0; i < links.length; i++) {
            newLinks[i] = links[i];
        }

        newLinks[links.length] = link;
        links = newLinks;

        link.setState(Link.State.CONNECTED);

        final LocalDevice devicePointer = this;
        devicePointer.mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (devicePointer.callback != null) {
                    devicePointer .callback.localDeviceDidReceiveLink(link);
                }
            }
        });
    }

    void didLoseLink(HMDevice device) {
        Log.i(TAG, "lose link " + Utils.hexFromBytes(device.getMac()));

        BluetoothDevice btDevice = mBluetoothAdapter.getRemoteDevice(device.getMac());
        int linkIndex = linkIndexForBTDevice(btDevice);

        if (linkIndex > -1) {
            // remove the link from the array
            final Link link = links[linkIndex];
            Link[] newLinks = new Link[links.length - 1];

            for (int i = 0; i < links.length; i++) {
                if (i < linkIndex) {
                    newLinks[i] = links[i];
                }
                else if (i > linkIndex) {
                    newLinks[i - 1] = links[i];
                }
            }

            links = newLinks;

            // set new adapter name
            if (LocalDevice.ALLOWS_MULTIPLE_LINKS == false && getLinks() == null) {
                setAdapterName();
            }

            // invoke the listener callback
            if (callback != null) {
                final LocalDevice devicePointer = this;
                devicePointer.mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        devicePointer.callback.localDeviceDidLoseLink(link);
                    }
                });
            }

            link.setState(Link.State.DISCONNECTED);

            // start broadcasting again
            if (state != LocalDevice.State.BROADCASTING) {
                try {
                    startBroadcasting();
                } catch (LinkException e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            Log.e(TAG, "no link for lose device");
        }
    }

    int pairingResponse = -1;
    int didReceivePairingRequest(HMDevice device, byte[] serialNumber) {
        pairingResponse = -1;
        BluetoothDevice btDevice = mBluetoothAdapter.getRemoteDevice(device.getMac());
        int linkIndex = linkIndexForBTDevice(btDevice);

        if (linkIndex > -1) {
            Link link = links[linkIndex];
            link.callback.linkDidReceivePairingRequest(link, serialNumber, new Constants.ApprovedCallback() {
                @Override
                public void approve() {
                    pairingResponse = 0;
                }

                @Override
                public void decline() {
                    pairingResponse = 0;
                }
            }, 10f);
        }
        else {
            Log.e(TAG, "no link for pairing request");
            return 1;
        }

        while(pairingResponse < 0) {}

        return pairingResponse;
    }

    private int linkIndexForBTDevice(BluetoothDevice device) {
        for (int i = 0; i < links.length; i++) {
            Link link = links[i];

            if (link.btDevice.getAddress().equals(device.getAddress())) {
                return i;
            }
        }

        return -1;
    }


    void setAdapterName() {
        byte[] serialBytes = new byte[4];
        new Random().nextBytes(serialBytes);
        mBluetoothAdapter.setName(Utils.hexFromBytes(serialBytes));
    }

    void writeData(byte[] mac, byte[] value) {
        Link link = getLinkForMac(mac);
        if (link != null) {
            readCharacteristic.setValue(value);
            GATTServer.notifyCharacteristicChanged(link.btDevice, readCharacteristic, false);
        }
        else {
            Log.e(TAG, "link does not exist for write");
        }
    }

    private Link getLinkForMac(byte[] mac) {
        for (int i = 0; i < links.length; i++) {
            Link link = links[i];
            if (Arrays.equals(link.getAddressBytes(), mac)) {
                return link;
            }
        }

        return null;
    }

    private void createGATTServer() {
        if (GATTServer == null) {
            gattServerCallback = new GATTServerCallback(LocalDevice.getInstance());
            GATTServer = mBluetoothManager.openGattServer(ctx, gattServerCallback);

            Log.i(TAG, "createGATTServer");
            // create the service
            BluetoothGattService service = new BluetoothGattService(Constants.SERVICE_UUID,
                    BluetoothGattService.SERVICE_TYPE_PRIMARY);

            readCharacteristic =
                    new BluetoothGattCharacteristic(Constants.READ_CHAR_UUID,
                            BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                            BluetoothGattCharacteristic.PERMISSION_READ);

            UUID confUUUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");
            readCharacteristic.addDescriptor(new BluetoothGattDescriptor(confUUUID, BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE));

            writeCharacteristic =
                    new BluetoothGattCharacteristic(Constants.WRITE_CHAR_UUID,
                            BluetoothGattCharacteristic.PROPERTY_WRITE,
                            BluetoothGattCharacteristic.PERMISSION_WRITE);

            service.addCharacteristic(readCharacteristic);
            service.addCharacteristic(writeCharacteristic);

            GATTServer.addService(service);
        }
        else {
            Log.i(TAG, "createGATTServer: already exists");
        }
    }

    private void createAdapter() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            setAdapterName();
            Log.i(TAG, "Create adapter " + mBluetoothAdapter.getName());
        }
    }

    private void checkIfBluetoothIsEnabled() throws LinkException {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            setState(State.BLUETOOTH_UNAVAILABLE);
            throw new LinkException(LinkException.LinkExceptionCode.BLUETOOTH_OFF);
        }

        if (!ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            setState(State.BLUETOOTH_UNAVAILABLE);
            throw new LinkException(LinkException.LinkExceptionCode.UNSUPPORTED);
        }
    }

    private final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i(TAG, "Start advertise " + mBluetoothAdapter.getName());
            setState(State.BROADCASTING);
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.w(TAG, "Start Advertise Failed: " + errorCode);
            setState(State.IDLE);
        }
    };

    private void setState(State state) {
        if (this.state != state) {
            State oldState = this.state;
            this.state = state;
            Log.i(TAG, "set local device state from " + oldState + " to " + state);
            if (callback != null) {
                callback.localDeviceStateChanged(state, oldState);
            }
        }
    }

}
