package com.highmobility.hmkit;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.ParcelUuid;

import com.highmobility.btcore.HMDevice;
import com.highmobility.crypto.AccessCertificate;
import com.highmobility.crypto.value.DeviceSerial;
import com.highmobility.hmkit.error.BroadcastError;
import com.highmobility.utils.ByteUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

/**
 * Broadcaster acts as a gateway to the application's capability to broadcast itself and handle
 * ConnectedLink connectivity.
 * <p>
 * Access the broadcaster from {@link HMKit#getBroadcaster()}. Broadcaster is created once and then
 * bound to HMKit instance.
 */
public class Broadcaster extends Core.Broadcaster {
    /**
     * This class keeps link references, advertises.
     */
    private final Core core;
    private final Storage storage;
    private final SharedBle ble;
    private final ThreadManager threadManager;

    private BroadcasterListener listener;

    private StartCallback startCallback;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private final GattServer gattServer;

    private boolean isAlivePinging;
    private long alivePingInterval = 500;

    private State state = State.IDLE;

    private final List<ConnectedLink> links = new ArrayList<>();
    private BroadcastConfiguration configuration;

    Broadcaster(Core core, Storage storage, ThreadManager threadManager, SharedBle ble) {
        this.core = core;
        this.storage = storage;
        this.ble = ble;
        this.threadManager = threadManager;
        core.broadcaster = this;
        gattServer = new GattServer(core, threadManager, ble, gattServerCallback);
        startBle(); // start listening for ble on/off
    }

    /**
     * The possible states of the broadcaster are represented by the enum {@link State}.
     *
     * @return The current state of the Broadcaster.
     */
    public State getState() {
        return state;
    }

    /**
     * @return The name of the advertised peripheral.
     */
    public String getName() {
        return ble.getName();
    }

    /**
     * @return indication of whether the alive pinging is active or not.
     */
    public boolean isAlivePinging() {
        return isAlivePinging;
    }

    /**
     * @return The certificates that are registered for the Broadcaster.
     */
    public AccessCertificate[] getRegisteredCertificates() {
        return storage.getCertificatesWithProvidingSerial(core.getDeviceCertificate().getSerial()
                .getByteArray());
    }

    /**
     * @return The certificates that are stored in the broadcaster's database for other devices.
     */
    public AccessCertificate[] getStoredCertificates() {
        return storage.getCertificatesWithoutProvidingSerial(core.getDeviceCertificate()
                .getSerial().getByteArray());
    }

    /**
     * @return The Links currently connected to the Broadcaster.
     */
    public List<ConnectedLink> getLinks() {
        return links;
    }

    /**
     * In order to receive Broadcaster events, a listener must be set.
     *
     * @param listener The listener instance to receive Broadcaster events.
     */
    public void setListener(BroadcasterListener listener) {
        this.listener = listener;
    }

    /**
     * Start broadcasting via BLE advertising.
     *
     * @param callback      This is invoked with the start broadcasting result.
     *                      onBroadcastingStarted is invoked if the broadcasting started.
     *                      onBroadcastingFailed is invoked if something went wrong.
     * @param configuration The broadcast configuration.
     */
    public void startBroadcasting(StartCallback callback, @Nullable BroadcastConfiguration
            configuration) {
        this.configuration = configuration;
        startBroadcasting(callback);
    }

    /**
     * Start broadcasting via BLE advertising.
     *
     * @param callback is invoked with the start broadcasting result. onBroadcastingStarted is
     *                 invoked if the broadcasting started. onBroadcastingFailed is invoked if
     *                 something went wrong.
     */
    public void startBroadcasting(StartCallback callback) {
        HMLog.d("startBroadcasting() called");

        if (state == State.BROADCASTING) {
            HMLog.d("will not start broadcasting: already " +
                    "broadcasting");

            callback.onBroadcastingStarted();
            return;
        }

        core.start();
        // if ble was stopped with terminate, we need to start it again.
        startBle();

        if (state == State.BLUETOOTH_UNAVAILABLE) {
            callback.onBroadcastingFailed(new BroadcastError(BroadcastError.Type.BLUETOOTH_OFF
                    , 0, "Bluetooth is turned off"));
            return;
        }

        // get the advertiser
        if (mBluetoothLeAdvertiser == null) {
            mBluetoothLeAdvertiser = ble.getAdapter().getBluetoothLeAdvertiser();

            if (mBluetoothLeAdvertiser == null) {
                // for unsupported devices the system does not return an advertiser
                setState(State.BLUETOOTH_UNAVAILABLE);
                callback.onBroadcastingFailed(new BroadcastError(BroadcastError.Type.UNSUPPORTED
                        , 0, "Bluetooth is no supported"));
                return;
            }
        }

        if (this.configuration == null) this.configuration = new BroadcastConfiguration();
        ble.setRandomAdapterName(configuration.isOverridingAdvertisementName());
        startCallback = callback;
        gattServer.open();
    }

    /**
     * Stops the BLE advertisements. This will also disconnect all of the BLE connections.
     */
    public void stopBroadcasting() {
        if (getState() != State.BROADCASTING) return; // we are not broadcasting

        // stopAdvertising cancels all the BT connections as well.
        if (mBluetoothLeAdvertiser != null) {
            HMLog.d("stopBroadcasting() called");
            mBluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
            mBluetoothLeAdvertiser = null;
        }

        this.configuration = null;
        setState(State.IDLE);
    }

    /**
     * Start the alive ping mode. This can be done after broadcasting has started, even if there are
     * no connected links. Call {@link #stopAlivePinging()} to stop.
     *
     * @param interval Interval of the ping, in ms.
     */
    public void startAlivePinging(long interval) {
        alivePingInterval = interval;
        if (isAlivePinging) return;
        isAlivePinging = true;
        sendAlivePing();
    }

    /**
     * Stop the alive pinging.
     */
    public void stopAlivePinging() {
        if (isAlivePinging == false) return;
        isAlivePinging = false;
        threadManager.cancelDelayed(alivePingRunnable);
    }

    /**
     * Registers the AccessCertificate for the broadcaster, enabling authenticated connection to
     * other devices.
     *
     * @param certificate The certificate that can be used by the Device to authorised Links
     * @return {@link Storage.Result#SUCCESS} on success or {@link Storage.Result#INTERNAL_ERROR} if
     * the given certificates providing serial doesn't match with broadcaster's serial. {@link
     * Storage.Result#STORAGE_FULL} if the storage is full.
     */
    public Storage.Result registerCertificate(AccessCertificate certificate) {
        if (core.getDeviceCertificate().getSerial().equals(certificate.getProviderSerial()) ==
                false) {
            return Storage.Result.INTERNAL_ERROR;
        }

        return storage.storeCertificate(certificate);
    }

    /**
     * Stores a Certificate to Device's storage. This certificate is usually read by other Devices.
     *
     * @param certificate The certificate that will be saved to the database
     * @return {@link Storage.Result#SUCCESS} on success or {@link Storage.Result#STORAGE_FULL} if
     * the storage is full. {@link Storage.Result#INTERNAL_ERROR} if certificate is null.
     */
    public Storage.Result storeCertificate(AccessCertificate certificate) {
        return storage.storeCertificate(certificate);
    }

    /**
     * Revokes a stored certificate from Device's storage. The stored certificate and its
     * accompanying registered certificate are deleted from the storage.
     *
     * @param serial The 9-byte serial number of the access providing broadcaster
     */
    public void revokeCertificate(DeviceSerial serial) {
        storage.deleteCertificate(serial.getByteArray(), null);
        storage.deleteCertificate(null, serial.getByteArray());
    }

    /**
     * Tries to disconnect all Bluetooth connections. This has proven being slow or not working at
     * all. Success may be related to the specific device or it's Android version.
     * <p><p>
     * {@link #stopBroadcasting()} can be called to improve the chance of disconnecting.
     * <p><p>
     * If successful, the link's state will change to disconnected and {@link
     * com.highmobility.hmkit.BroadcasterListener#onLinkLost(ConnectedLink)}} will be called.
     * <p>
     * The user is responsible for releasing the Link's BroadcasterListener.
     */
    public void disconnectAllLinks() {
        gattServer.disconnectAllLinks();
    }

    private void startBle() {
        ble.initialise();
        // add state listener
        ble.addListener(bleListener);
        // check for initial state
        if (ble.isBluetoothOn() == false) setState(State.BLUETOOTH_UNAVAILABLE);
    }

    private final BleListener bleListener = new BleListener();

    private class BleListener implements SharedBleListener {
        // we don't want this method to be publicly available, so we create the class here
        @Override public void bluetoothChangedToAvailable(boolean available) {
            HMLog.d("bluetoothChangedToAvailable(): available = %s",
                    available);

            if (available && getState() == State.BLUETOOTH_UNAVAILABLE) {
                setState(State.IDLE);
            } else if (!available && getState() != State.BLUETOOTH_UNAVAILABLE) {
                // manually clear the links because there is no connection state change callback
                // after turning ble off.
                if (links.size() > 0) {
                    for (ConnectedLink link : links) {
                        core.HMBTCorelinkDisconnect(ByteUtils.bytesFromMacString(link.btDevice
                                .getAddress()));
                    }
                }

                // Need to reset the service after bluetooth reset because otherwise
                // startBroadcasting will not include the service. Maybe internally the services are
                // reset and we keep the invalid pointer here.
                gattServer.close();
                setState(State.BLUETOOTH_UNAVAILABLE);
            }
        }
    }

    /**
     * Called with {@link HMKit#terminate()}. Broadcasting and alive pinging will be stopped because
     * ble will be stopped.
     *
     * @throws IllegalStateException when there are still connected links.
     */
    void terminate() throws IllegalStateException {
        if (getLinks().size() > 0) {
            // re startBle(new device cert) would mess up communication with previous links
            throw new IllegalStateException("Broadcaster cannot terminate if a connected " +
                    "link exists. Disconnect from all of the links.");
        }

        stopBroadcasting();
        stopAlivePinging();
        ble.removeListener(bleListener);
    }

    @Override boolean onChangedAuthenticationState(HMDevice device) {
        final ConnectedLink link = getLinkForMac(device.getMac());
        if (link == null) return false;
        link.onChangedAuthenticationState(device);
        return true;
    }

    @Override boolean onDeviceExitedProximity(byte[] mac) {
        HMLog.d("lose link " + ByteUtils.hexFromBytes(mac));

        final ConnectedLink link = getLinkForMac(mac);
        if (link == null) return false;

        links.remove(link);
        link.setState(Link.State.DISCONNECTED);

        // invoke the listener listener
        if (listener != null) {
            threadManager.postToMain(new Runnable() {
                @Override public void run() {
                    if (listener == null) return;
                    listener.onLinkLost(link);
                    link.listener = null; // nothing to do with the link anymore
                    // pointless to set random adapter name here because if already broadcasting
                    // android will not change the name and if not broadcasting then startBroadcast
                    // will change the name
                }
            });
        }

        return true;
    }

    @Override boolean onCommandResponseReceived(HMDevice device, byte[] data) {
        Link link = getLinkForMac(device.getMac());
        if (link == null) return false;
        link.onCommandResponseReceived(data);
        return true;
    }

    @Override boolean onCommandReceived(HMDevice device, byte[] data) {
        Link link = getLinkForMac(device.getMac());
        if (link == null) return false;
        link.onCommandReceived(data);
        return true;
    }

    @Override boolean onRevokeResult(HMDevice device, byte[] data, int result) {
        Link link = getLinkForMac(device.getMac());
        if (link == null) return false;
        link.onRevokeResponse(data, result);
        return true;
    }

    @Override int onReceivedPairingRequest(HMDevice device) {
        ConnectedLink link = getLinkForMac(device.getMac());
        if (link != null) {
            return link.didReceivePairingRequest();
        }

        return 1;
    }

    @Override boolean writeData(byte[] mac, byte[] value, int characteristicId) {
        ConnectedLink link = getLinkForMac(mac);
        if (link == null || link.btDevice == null) return false;
        return gattServer.writeData(link.btDevice, value, characteristicId);
    }

    private ConnectedLink getLinkForMac(byte[] mac) {
        for (int i = 0; i < links.size(); i++) {
            ConnectedLink link = links.get(i);

            if (Arrays.equals(link.getAddressBytes(), mac)) {
                return link;
            }
        }

        return null;
    }

    final Runnable alivePingRunnable = new Runnable() {
        @Override public void run() {
            sendAlivePing();
        }
    };

    private void sendAlivePing() {
        if (gattServer.isOpen()) {
            for (Link link : links) gattServer.sendAlivePing(link.btDevice);
        } else {
            HMLog.d("need to start broadcasting before pinging");
        }

        if (isAlivePinging) {
            threadManager.postDelayed(alivePingRunnable, alivePingInterval);
        }
    }

    final GattServer.Callback gattServerCallback = new GattServer.Callback() {
        @Override void onServiceAdded(boolean success) {
            HMLog.d("onServiceAdded: %s", success);

            if (success) {
                final AdvertiseSettings settings = new AdvertiseSettings.Builder()
                        .setAdvertiseMode(configuration.getAdvertiseMode())
                        .setConnectable(true)
                        .setTimeout(0)
                        .setTxPowerLevel(configuration.getTxPowerLevel())
                        .build();

                UUID advertiseUUID;
                byte[] uuidBytes;

                if (configuration.getBroadcastTarget() == null) {
                    uuidBytes = ByteUtils.concatBytes(core.getIssuer(), core.getAppId());
                } else {
                    uuidBytes = ByteUtils.concatBytes(new byte[]{0x00, 0x00, 0x00, 0x00},
                            configuration
                                    .getBroadcastTarget().getByteArray());
                    uuidBytes = ByteUtils.concatBytes(uuidBytes, new byte[]{0x00, 0x00, 0x00});
                }

                ByteUtils.reverse(uuidBytes);
                advertiseUUID = ByteUtils.UUIDFromByteArray(uuidBytes);

                final AdvertiseData data = new AdvertiseData.Builder()
                        .setIncludeDeviceName(configuration.isOverridingAdvertisementName() == true)
                        .addServiceUuid(new ParcelUuid(advertiseUUID))
                        .build();

                mBluetoothLeAdvertiser.startAdvertising(settings, data, advertiseCallback);
            } else {
                setState(State.BLUETOOTH_UNAVAILABLE);
                startCallback.onBroadcastingFailed(new BroadcastError(BroadcastError.Type
                        .BLUETOOTH_FAILURE
                        , 0, "Failed to create gatt server."));
            }
        }

        @Override
        boolean onNotificationsStartedForReadCharacteristic(BluetoothDevice device, byte[] mac) {
            if (getLinkForMac(mac) != null) return false;

            // need to dispatch the link before authenticating to forward pairing request for
            // instance
            final ConnectedLink link = new ConnectedLink(core, threadManager, device);
            links.add(link);

            if (listener != null) {
                threadManager.postToMain(new Runnable() {
                    @Override public void run() {
                        if (listener == null) return;
                        listener.onLinkReceived(link);
                    }
                });
            }
            return true;
        }
    };

    private final AdvertiseCb advertiseCallback = new AdvertiseCb(this);

    private static class AdvertiseCb extends AdvertiseCallback {
        final WeakReference<Broadcaster> broadcaster;

        AdvertiseCb(Broadcaster broadcaster) {
            this.broadcaster = new WeakReference<>(broadcaster);
        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            if (HMKit.loggingLevel.getValue() >= HMLog.Level.DEBUG.getValue()) {
                String name;
                if (broadcaster.get().configuration.isOverridingAdvertisementName()) {
                    name = broadcaster.get().getName();
                } else {
                    name = "not advertising name";
                }

                HMLog.d("Start advertise: " + name);
            }
            broadcaster.get().setState(State.BROADCASTING);
            if (broadcaster.get().startCallback != null) {
                broadcaster.get().startCallback.onBroadcastingStarted();
            }
        }

        @Override
        public void onStartFailure(int errorCode) {
            switch (errorCode) {
                case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                    HMLog.e("Advertise failed: data too large");
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                    HMLog.e("Advertise failed: feature unsupported");
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                    HMLog.e("Advertise failed: internal error");
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                    HMLog.e("Advertise failed: too many advertisers");
                    break;
            }

            if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED) {
                broadcaster.get().setState(State.BROADCASTING);
            } else {
                broadcaster.get().setState(State.IDLE);

                if (broadcaster.get().startCallback != null) {
                    broadcaster.get().startCallback.onBroadcastingFailed(new BroadcastError
                            (BroadcastError.Type.BLUETOOTH_FAILURE
                                    , 0, "Failed to start BLE advertisements"));
                }
            }
        }
    }

    private void setState(final State state) {
        if (this.state != state) {
            final State oldState = this.state;
            this.state = state;

            if (listener != null) {
                threadManager.postToMain(new Runnable() {
                    @Override public void run() {
                        if (listener == null) return;
                        listener.onStateChanged(oldState);
                    }
                });
            }
        }
    }

    /**
     * The Broadcaster state.
     */
    public enum State {
        BLUETOOTH_UNAVAILABLE, IDLE, BROADCASTING
    }

    /**
     * StartCallback is used to notify the user about the start broadcasting result
     */
    public interface StartCallback {
        /**
         * Invoked when the broadcasting was started.
         */
        void onBroadcastingStarted();

        /**
         * Invoked when there was an error with starting the broadcast.
         *
         * @param error The error
         */
        void onBroadcastingFailed(BroadcastError error);
    }
}
