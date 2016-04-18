package com.high_mobility.digitalkey.HMLink.Broadcasting;

import android.bluetooth.BluetoothDevice;

import com.high_mobility.digitalkey.HMLink.Constants;
import com.high_mobility.digitalkey.HMLink.Shared.AccessCertificate;
import com.high_mobility.digitalkey.HMLink.Shared.Parser;

/**
 * Created by ttiganik on 13/04/16.
 */
public class Link {
    public enum State { CONNECTED, AUTHENTICATED, DISCONNECTED }
    public State state;
    public AccessCertificate certificate;

    private LinkCallback callback;

    BluetoothDevice btDevice;
    LocalDevice device;
    Parser parser;

    Link(BluetoothDevice btDevice, LocalDevice device) {
        // TODO: create parser as well
        this.btDevice = btDevice;
        this.device = device;
    }

    void registerCallback(LinkCallback callback) {
        // TODO:
        this.callback = callback;
    }

    void sendCustomCommand(byte[] bytes, boolean secureResponse, Constants.DataResponseCallback responseCallback) {
        // TODO:
    }
}
