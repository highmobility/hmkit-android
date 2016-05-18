package com.high_mobility.HMLink.Broadcasting;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.high_mobility.HMLink.Utils;
import com.high_mobility.btcore.HMDevice;
import com.high_mobility.HMLink.Constants;
import com.high_mobility.HMLink.Shared.AccessCertificate;

import java.lang.ref.WeakReference;
import java.util.Calendar;

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

    WeakReference<Constants.DataResponseCallback> commandCallback;

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

    public void sendCustomCommand(byte[] bytes, boolean secureResponse, Constants.DataResponseCallback responseCallback) {
        Log.i(LocalDevice.TAG, "sendCustomCommand " + Utils.hexFromBytes(hmDevice.getMac()));
        commandCallback = new WeakReference<>(responseCallback);
        device.core.HMBTCoreSendCustomCommand(this.device.coreInterface, bytes, bytes.length, getAddressBytes());
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

    void didReceiveCustomCommandResponse(final byte[] data) {
        Log.i(LocalDevice.TAG, "didReceiveCustomCommandResponse " + Utils.hexFromBytes(hmDevice.getMac()));
        if (commandCallback != null && commandCallback.get() != null) {
            this.device.mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    commandCallback.get().response(data, null);
                }
            });
        }
    }

    int pairingResponse = -1;
    int didReceivePairingRequest() {
        if (callback == null) return 1;
        final Link reference = this;
        pairingResponse = -1;
        device.mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
            callback.linkDidReceivePairingRequest(reference, new Constants.ApprovedCallback() {
                @Override
                public void approve() {
                    pairingResponse = 0;
                }

                @Override
                public void decline() {
                    pairingResponse = 1;
                }
            });
            }
        });

        Calendar c = Calendar.getInstance();
        int startSeconds = c.get(Calendar.SECOND);

        while(pairingResponse < 0) {
            int passedSeconds = Calendar.getInstance().get(Calendar.SECOND);
            if (passedSeconds - startSeconds > Constants.registerTimeout) {
                device.mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.linkPairingDidTimeout(reference);
                    }
                });

                Log.i(LocalDevice.TAG, "pairing timer exceeded");
                return 1; // TODO: use correct code
            }
        }

        return pairingResponse;
    }

    byte[] getAddressBytes() {
        return Utils.bytesFromMacString(btDevice.getAddress());
    }
}
