package com.high_mobility.digitalkey.HMLink.Broadcasting;

import android.bluetooth.BluetoothDevice;

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
    LocalDevice device;

    Link(BluetoothDevice btDevice, LocalDevice device) {
        this.btDevice = btDevice;
        this.device = device;
    }

    void setState(State state) {
        State oldState = this.state;
        this.state = state;
        if (callback != null) {
            callback.linkStateDidChange(this, oldState);
        }
    }

    public State getState() {
        return state;
    }

    void registerCallback(LinkCallback callback) {
        this.callback = callback;
    }

    void sendCustomCommand(byte[] bytes, boolean secureResponse, Constants.DataResponseCallback responseCallback) {
        device.core.HMBTCoreSendCustomCommand(bytes, bytes.length, getAddressBytes());
    }

    byte[] getAddressBytes() {
        return Utils.bytesFromMacString(btDevice.getAddress());
    }
}
