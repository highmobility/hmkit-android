package com.high_mobility.HMLink.Shared;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.high_mobility.HMLink.LinkException;
import com.high_mobility.btcore.HMDevice;

import java.util.Calendar;

/**
 * Created by ttiganik on 17/08/16.
 */
public class Link {
    BluetoothDevice btDevice;
    HMDevice hmDevice;

    public enum State {
        DISCONNECTED, CONNECTED, AUTHENTICATED
    }

    State state = State.CONNECTED;
    SentCommand sentCommand;
    LinkListener listener;
    long connectionTime;

    Manager manager;
    Link(Manager manager, BluetoothDevice btDevice) {
        this.btDevice = btDevice;
        this.manager = manager;
        connectionTime = Calendar.getInstance().getTimeInMillis();
    }

    /**
     *
     * @return the Link's state
     */
    public State getState() {
        return state;
    }

    void setState(State state) {
        if (this.state != state) {
            final State oldState = this.state;
            if (state == State.AUTHENTICATED && Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue()) {
                Log.d(Broadcaster.TAG, "authenticated in " + (Calendar.getInstance().getTimeInMillis() - connectionTime) + "ms");
            }

            this.state = state;

            if (listener != null) {
                final Link linkPointer = this;

                manager.mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        linkPointer.listener.onStateChanged(linkPointer, oldState);
                    }
                });
            }
        }
    }

    /**
     *
     * @return the name of the Link's bluetooth peripheral
     */
    public String getName() {
        return btDevice.getName();
    }

    /**
     *
     * @return Link's serial
     */
    public byte[] getSerial() {
        return hmDevice != null ? hmDevice.getSerial() : null;
    }

    /**
     * Send command to the ConnectedLink inside a secure container.
     *
     * @param bytes             The bytes that will be sent inside the secure container.
     * @param secureResponse    Optional boolean defining if the response has a secure HMAC element
     *                       in it or not - defaults to true
     * @param responseCallback  DataResponseCallback object that returns the response's byte array
     *                          or a LinkException if unsuccessful
     */
    public void sendCommand(byte[] bytes, boolean secureResponse, Constants.DataResponseCallback responseCallback) {
        if (state != State.AUTHENTICATED) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
                Log.d(Broadcaster.TAG, "cant send command, not authenticated");
            responseCallback.response(null, new LinkException(LinkException.LinkExceptionCode.UNAUTHORISED));
            return;
        }

        if (sentCommand != null && sentCommand.finished == false) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
                Log.d(Broadcaster.TAG, "cant send command, custom command in progress");
            responseCallback.response(null, new LinkException(LinkException.LinkExceptionCode.CUSTOM_COMMAND_IN_PROGRESS));
            return;
        }

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
            Log.d(Broadcaster.TAG, "send command " + ByteUtils.hexFromBytes(bytes)
                    + " to " + ByteUtils.hexFromBytes(hmDevice.getMac()));

        sentCommand = new SentCommand(responseCallback, manager.mainHandler);
        manager.core.HMBTCoreSendCustomCommand(manager.coreInterface, bytes, bytes.length, getAddressBytes());
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

    byte[] onCommandReceived(byte[] bytes) {
        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
            Log.d(Broadcaster.TAG, "did receive command " + ByteUtils.hexFromBytes(bytes)
                    + " from " + ByteUtils.hexFromBytes(hmDevice.getMac()));

        if (listener == null) {
            Log.d(Broadcaster.TAG, "can't dispatch notification: no listener set");
            return null;
        }

        return listener.onCommandReceived(this, bytes);
    }

    void onCommandResponseReceived(final byte[] data) {
        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
            Log.d(Broadcaster.TAG, "did receive command response " + ByteUtils.hexFromBytes(data)
                    + " from " + ByteUtils.hexFromBytes(hmDevice.getMac()) + " in " +
                    (Calendar.getInstance().getTimeInMillis() - sentCommand.commandStartTime) + "ms");

        if (sentCommand == null) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                Log.d(Broadcaster.TAG, "can't dispatch command response: sentCommand = null");
            return;
        }

        sentCommand.dispatchResult(data, null);
    }

    byte[] getAddressBytes() {
        return ByteUtils.bytesFromMacString(btDevice.getAddress());
    }
}
