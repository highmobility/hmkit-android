package com.high_mobility.hmkit;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.high_mobility.btcore.HMDevice;

import java.util.Calendar;

import static com.high_mobility.hmkit.Broadcaster.TAG;

/**
 * Created by ttiganik on 17/08/16.
 */
public class Link {
    /// Bluetooth is off
    public static final int BLUETOOTH_OFF = 1;
    /// A custom command has not yet received a response
    public static final int COMMAND_IN_PROGRESS = 2;
    /// Framework encountered an internal error (commonly releated to invalid data received)
    public static final int INTERNAL_ERROR = 3;
    /// Bluetooth failed to act as expected
    public static final int BLUETOOTH_FAILURE = 4;
    /// The signature for the command was invalid
    public static final int INVALID_SIGNATURE = 5;
    /// The Certificates storage database is full
    public static final int STORAGE_FULL = 6;
    /// Command timed out
    public static final int TIME_OUT = 7;
    /// The Link is not connected
    public static final int NOT_CONNECTED = 8;
    /// The app is not authorised with the connected link to perform the action
    public static final int UNAUTHORIZED = 9;
    /// Bluetooth Low Energy is unavailable for this device
    public static final int UNSUPPORTED = 10;

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
                Log.d(TAG, "authenticated in " + (Calendar.getInstance().getTimeInMillis() - connectionTime) + "ms");
            }

            this.state = state;

            if (listener != null) {
                final Link linkPointer = this;
                Runnable callback = new Runnable() {
                    @Override
                    public void run() {
                    linkPointer.listener.onStateChanged(linkPointer, oldState);
                    }
                };
                
                manager.mainHandler.post(callback);
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
     * @param responseCallback  ResponseCallback object that returns the getErrorCode if the command
     *                          failed or 0 if it succeeded.
     *                          Error codes could be UNAUTHORIZED, COMMAND_IN_PROGRESS, TIME_OUT from {@link Link}.
     */
    public void sendCommand(final byte[] bytes, boolean secureResponse, Constants.ResponseCallback responseCallback) {
        if (state != State.AUTHENTICATED) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
                Log.d(TAG, "cant send command, not authenticated");
            responseCallback.response(Link.UNAUTHORIZED);
            return;
        }

        if (sentCommand != null && sentCommand.finished == false) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
                Log.d(TAG, "cant send command, custom command in progress");
            responseCallback.response(Link.COMMAND_IN_PROGRESS);
            return;
        }

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
            Log.d(TAG, "send command " + ByteUtils.hexFromBytes(bytes)
                    + " to " + ByteUtils.hexFromBytes(hmDevice.getMac()));

        sentCommand = new SentCommand(responseCallback, manager.mainHandler);

        manager.workHandler.post(new Runnable() {
            @Override
            public void run() {
                manager.core.HMBTCoreSendCustomCommand(manager.coreInterface, bytes, bytes.length, getAddressBytes());
            }
        });
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

    void onCommandReceived(final byte[] bytes) {
        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
            Log.d(TAG, "did receive command " + ByteUtils.hexFromBytes(bytes)
                    + " from " + ByteUtils.hexFromBytes(hmDevice.getMac()));

        if (listener == null) {
            Log.d(TAG, "can't dispatch notification: no listener set");
            return;
        }
        manager.mainHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.onCommandReceived(Link.this, bytes);
            }
        });
    }

    void onCommandResponseReceived(final byte[] data) {
        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
            Log.d(TAG, "did receive command response " + ByteUtils.hexFromBytes(data)
                    + " from " + ByteUtils.hexFromBytes(hmDevice.getMac()) + " in " +
                    (Calendar.getInstance().getTimeInMillis() - sentCommand.commandStartTime) + "ms");

        if (sentCommand == null) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                Log.d(TAG, "can't dispatch command response: sentCommand = null");
            return;
        }

        sentCommand.dispatchResult(data);
    }

    byte[] getAddressBytes() {
        return ByteUtils.bytesFromMacString(btDevice.getAddress());
    }
}
