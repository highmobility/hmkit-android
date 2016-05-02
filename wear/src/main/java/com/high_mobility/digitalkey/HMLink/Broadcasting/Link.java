package com.high_mobility.digitalkey.HMLink.Broadcasting;

import android.bluetooth.BluetoothDevice;

import com.high_mobility.btcore.HMDevice;
import com.high_mobility.digitalkey.HMLink.Constants;
import com.high_mobility.digitalkey.HMLink.Shared.AccessCertificate;
import com.high_mobility.digitalkey.Utils;

/**
 * Created by ttiganik on 13/04/16.
 */
public class Link {
    public enum State { CONNECTED, AUTHENTICATED, DISCONNECTED }

    State state;
    public AccessCertificate certificate;

    LinkCallback callback;

    BluetoothDevice btDevice;

    HMDevice hmDevice;
    LocalDevice device;

    Link(BluetoothDevice btDevice, LocalDevice device) {
        this.btDevice = btDevice;
        this.device = device;
    }

    public byte[] getSerial() {
        return hmDevice != null ? hmDevice.getSerial() : null;
    }

    public State getState() {
        return state;
    }

    public void registerCallback(LinkCallback callback) {
        this.callback = callback;
    }

    void setHmDevice(final HMDevice hmDevice) {
        this.hmDevice = hmDevice;

        if (hmDevice.getIsAuthenticated() == 0) {
            setState(State.CONNECTED);
        }
        else {
            setState(State.AUTHENTICATED);
        }
    }

    void setState(State state) {
        if (this.state != state) {
            final State oldState = this.state;
            this.state = state;

            if (callback != null) {
                final Link linkPointer = this;

                this.device.mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        linkPointer.callback.linkStateDidChange(linkPointer, oldState);
                    }
                });
            }
        }
    }

    public void sendCustomCommand(byte[] bytes, boolean secureResponse, Constants.DataResponseCallback responseCallback) {
        device.core.HMBTCoreSendCustomCommand(this.device.coreInterface, bytes, bytes.length, getAddressBytes());
    }

    byte[] getAddressBytes() {
        return Utils.bytesFromMacString(btDevice.getAddress());
    }
}
