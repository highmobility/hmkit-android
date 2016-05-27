package com.high_mobility.HMLink.Broadcasting;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.high_mobility.HMLink.Device;
import com.high_mobility.btcore.HMDevice;
import com.high_mobility.HMLink.Constants;

import java.lang.ref.WeakReference;
import java.util.Calendar;

/**
 * The Link is a representation of the connection between the LocalDevice and a Device
 * that has connected to it. The Link is created by the other Device via discovering
 * and connecting to the LocalDevice. The Link's interface provides the user the ability
 * to send commands and handle incoming requests from the Link.
 *
 * Created by ttiganik on 13/04/16.
 */
public class Link {
    public enum State { CONNECTED, AUTHENTICATED, DISCONNECTED }

    State state;

    LinkListener listener;

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

    /**
     * The possible states of the link are represented by the enum Link.State.
     * @return The current state of the link
     */
    public State getState() {
        return state;
    }

    /**
     * In order to receive Link events, a listener must be set.
     *
     * @param listener The listener instance to receive Link events.
     */
    public void setListener(LinkListener listener) {
        this.listener = listener;
    }

    /**
     * Send custom command to the Link inside a secure container.
     *
     * @param bytes             The bytes that will be sent inside the secure container.
     * @param secureResponse    Optional boolean defining if the response has a secure HMAC element
        *                       in it or not - defaults to true
     * @param responseCallback  DataResponseCallback object that returns the response's byte array
     *                          or a LinkException if unsuccessful
     */
    public void sendCommand(byte[] bytes, boolean secureResponse, Constants.DataResponseCallback responseCallback) {
        if (Device.loggingLevel.getValue() >= Device.LoggingLevel.Debug.getValue()) Log.i(LocalDevice.TAG, "sendCommand " + ByteUtils.hexFromBytes(hmDevice.getMac()));
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

            if (listener != null) {
                final Link linkPointer = this;

                this.device.mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        linkPointer.listener.onStateChanged(linkPointer, oldState);
                    }
                });
            }
        }
    }

    byte[] onCommandReceived(byte[] bytes) {
        if (listener == null) return null;
        return listener.onCommandReceived(this, bytes);
    }

    void onCommandResponseReceived(final byte[] data) {
        if (Device.loggingLevel.getValue() >= Device.LoggingLevel.Debug.getValue()) Log.i(LocalDevice.TAG, "onCommandResponseReceived " + ByteUtils.hexFromBytes(hmDevice.getMac()));
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
        if (listener == null) {
            Log.e(LocalDevice.TAG, "link listener not set");
            return 1;
        }

        final Link reference = this;
        pairingResponse = -1;
        device.mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
            listener.onPairingRequested(reference, new Constants.ApprovedCallback() {
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
                        listener.onPairingRequestTimeout(reference);
                    }
                });

                if (Device.loggingLevel.getValue() >= Device.LoggingLevel.All.getValue()) Log.i(LocalDevice.TAG, "pairing timer exceeded");
                return 1; // TODO: use correct code
            }
        }

        return pairingResponse;
    }

    byte[] getAddressBytes() {
        return ByteUtils.bytesFromMacString(btDevice.getAddress());
    }
}
