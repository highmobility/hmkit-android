package com.highmobility.hmkit;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.highmobility.btcore.HMDevice;
import com.highmobility.crypto.value.DeviceSerial;
import com.highmobility.hmkit.error.LinkError;
import com.highmobility.hmkit.error.RevokeError;
import com.highmobility.utils.ByteUtils;
import com.highmobility.value.Bytes;

import java.util.Calendar;

import static com.highmobility.hmkit.Broadcaster.TAG;

/**
 * Created by ttiganik on 17/08/16.
 */
public class Link {
    /**
     * The time after which HMKit will fail the command if there has been no response. In ms.
     */
    public static long commandTimeout = 10000;

    BluetoothDevice btDevice;
    HMDevice hmDevice;

    State state = State.CONNECTED;
    DeviceSerial serial;

    LinkCommand sentCommand;
    LinkListener listener;
    long connectionTime;
    RevokeCallback revokeCallback;
    Manager manager;

    Link(Manager manager, BluetoothDevice btDevice) {
        this.btDevice = btDevice;
        this.manager = manager;
        connectionTime = Calendar.getInstance().getTimeInMillis();
    }

    /**
     * @return The Links state.
     */
    public State getState() {
        return state;
    }

    void setState(State state) {
        if (this.state != state) {
            final State oldState = this.state;
            if (state == State.AUTHENTICATED && Manager.loggingLevel.getValue() >= Manager
                    .LoggingLevel.DEBUG.getValue()) {
                Log.d(TAG, "authenticated in " + (Calendar.getInstance().getTimeInMillis() -
                        connectionTime) + "ms");
            }

            this.state = state;

            if (listener != null) {
                final Link linkPointer = this;
                manager.postToMainThread(new Runnable() {
                    @Override public void run() {
                        if (linkPointer.listener == null) return;
                        linkPointer.listener.onStateChanged(linkPointer, oldState);
                    }
                });
            }
        }
    }

    /**
     * @return the name of the Links bluetooth peripheral.
     */
    public String getName() {
        return btDevice.getName();
    }

    /**
     * @return The links serial.
     */
    public DeviceSerial getSerial() {
        return serial;
    }

    /**
     * Send command to the Link.
     *
     * @param bytes    The command bytes that will be sent to the link.
     * @param callback A {@link CommandCallback} object that is invoked with the command result.
     */
    public void sendCommand(final Bytes bytes, CommandCallback callback) {
        if (bytes.getLength() > Constants.MAX_COMMAND_LENGTH) {
            LinkError error = new LinkError(LinkError.Type.COMMAND_TOO_BIG, 0,
                    "Command size is bigger than " + Constants.MAX_COMMAND_LENGTH + " bytes");
            callback.onCommandFailed(error);
            return;
        }

        if (state != State.AUTHENTICATED) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
                Log.d(TAG, "not authenticated");
            callback.onCommandFailed(new LinkError(LinkError.Type.UNAUTHORIZED, 0, "not " +
                    "authenticated"));
            return;
        }

        if (sentCommand != null && sentCommand.finished == false) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
                Log.d(TAG, "custom command in progress");

            callback.onCommandFailed(new LinkError(LinkError.Type.COMMAND_IN_PROGRESS, 0, "custom" +
                    " command in progress"));
            return;
        }

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
            Log.d(TAG, "send command " + bytes
                    + " to " + ByteUtils.hexFromBytes(hmDevice.getMac()));

        sentCommand = new LinkCommand(callback, manager.mainHandler);

        manager.workHandler.post(new Runnable() {
            @Override
            public void run() {
                manager.core.HMBTCoreSendCustomCommand(manager.coreInterface, bytes.getByteArray
                        (), bytes.getLength(), getAddressBytes());
            }
        });
    }

    /**
     * Revoke authorisation for this device. {@link RevokeCallback} will be called with the result.
     * If successful, the {@link LinkListener#onStateChanged(Link, State)} will be called with the
     * {@link State#CONNECTED} state.
     * <p>
     * After this has succeeded it is up to the user to finish the flow related to this link -
     * disconnect, stop broadcasting or something else.
     *
     * @param callback Callback invoked in case of an error.
     */
    public void revoke(RevokeCallback callback) {
        if (state != State.AUTHENTICATED) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
                Log.d(TAG, "not authenticated");
            callback.onRevokeFailed(new RevokeError(RevokeError.Type.UNAUTHORIZED, 0, "not " +
                    "authenticated"));
            return;
        }

        if (sentCommand != null && sentCommand.finished == false) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.ALL.getValue())
                Log.d(TAG, "custom command in progress");

            callback.onRevokeFailed(new RevokeError(RevokeError.Type.COMMAND_IN_PROGRESS, 0, "a " +
                    " command is in progress"));
            return;
        }

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
            Log.d(TAG, "revoke " + serial);

        this.revokeCallback = callback;

        manager.workHandler.post(new Runnable() {
            @Override
            public void run() {
                manager.core.HMBTCoreSendRevoke(manager.coreInterface, serial.getByteArray());
            }
        });
    }

    void setHmDevice(HMDevice hmDevice) {
        this.hmDevice = hmDevice;
        if (serial == null || serial.equals(hmDevice.getSerial()) == false) {
            serial = new DeviceSerial(hmDevice.getSerial());
        }

        if (hmDevice.getIsAuthenticated() == 0) {
            setState(State.CONNECTED);
        } else {
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

        manager.postToMainThread(new Runnable() {
            @Override public void run() {
                if (listener == null) return;
                listener.onCommandReceived(Link.this, new Bytes(bytes));
            }
        });
    }

    void onCommandResponseReceived(final byte[] data) {
        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())

            Log.d(TAG, "did receive command response " + ByteUtils.hexFromBytes(data)
                    + " from " + ByteUtils.hexFromBytes(hmDevice.getMac()) + " in " +
                    (Calendar.getInstance().getTimeInMillis() - sentCommand.commandStartTime) +
                    "ms");

        if (sentCommand == null || sentCommand.finished) {
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                Log.d(TAG, "can't dispatch command response: sentCommand = null || finished");
            return;
        }

        sentCommand.dispatchResult(data);
    }

    void onRevokeResponse(final byte[] data, final int result) {
        manager.postToMainThread(new Runnable() {
            @Override public void run() {
                if (revokeCallback == null) return;

                if (result == 0) {
                    revokeCallback.onRevokeSuccess(new Bytes(data));
                } else {
                    revokeCallback.onRevokeFailed(new RevokeError(RevokeError.Type.FAILED, 0,
                            "Revoke failed."));
                }
            }
        });
    }

    byte[] getAddressBytes() {
        return ByteUtils.bytesFromMacString(btDevice.getAddress());
    }

    public enum State {
        DISCONNECTED, CONNECTED, AUTHENTICATED
    }

    /**
     * CommandCallback is used to notify the user about the command result.
     */
    public interface CommandCallback {
        /**
         * Invoked when the command was successfully sent.
         */
        void onCommandSent();

        /**
         * Invoked when there was an issue with the command.
         *
         * @param error The command error.
         */
        void onCommandFailed(LinkError error);
    }

    /**
     * RevokeCallback is used to notify the user if the revoke failed.
     */
    public interface RevokeCallback {

        /**
         * Invoked when the revoke succeeded. After this the link will go to {@link State#CONNECTED}
         * state.
         *
         * @param customData The customer specific data, if exists.
         */
        void onRevokeSuccess(Bytes customData);

        /**
         * Invoked when there was an issue with the revoke.
         *
         * @param error The revoke error.
         */
        void onRevokeFailed(RevokeError error);
    }
}
