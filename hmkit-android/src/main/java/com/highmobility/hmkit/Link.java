package com.highmobility.hmkit;

import android.bluetooth.BluetoothDevice;

import com.highmobility.btcore.HMDevice;
import com.highmobility.crypto.value.DeviceSerial;
import com.highmobility.hmkit.error.LinkError;
import com.highmobility.hmkit.error.RevokeError;
import com.highmobility.utils.ByteUtils;
import com.highmobility.value.Bytes;

import java.util.Calendar;

public class Link {
    /**
     * The time after which HMKit will fail the command if there has been no response. In ms.
     */
    public static long commandTimeout = 10000;
    protected final Core core;
    protected final ThreadManager threadManager;

    LinkListener listener;
    BluetoothDevice btDevice;

    private HMDevice hmDevice;
    private DeviceSerial serial;
    private State state = State.CONNECTED;

    private LinkCommand sentCommand;
    private final long connectionTime;
    private RevokeCallback revokeCallback;

    Link(Core core, ThreadManager threadManager, BluetoothDevice btDevice) {
        this.btDevice = btDevice;
        this.core = core;
        this.threadManager = threadManager;

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
            if (state == State.AUTHENTICATED) {
                HmLog.d("authenticated in %s ms", (Calendar
                        .getInstance().getTimeInMillis() - connectionTime));
            }

            this.state = state;

            if (listener != null) {
                final Link linkPointer = this;
                threadManager.postToMain(new Runnable() {
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
     * @return The link's serial.
     */
    public DeviceSerial getSerial() {
        return serial;
    }

    /**
     * Send a command to the Link.
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
            callback.onCommandFailed(new LinkError(LinkError.Type.UNAUTHORIZED, 0, "not " +
                    "authenticated"));
            return;
        }

        if (sentCommand != null && sentCommand.finished == false) {
            HmLog.d(HmLog.Level.ALL, "custom command in progress");

            callback.onCommandFailed(new LinkError(LinkError.Type.COMMAND_IN_PROGRESS, 0, "custom" +
                    " command in progress"));
            return;
        }

        HmLog.d("send command %s to %s", bytes, ByteUtils
                .hexFromBytes(hmDevice.getMac()));

        sentCommand = new LinkCommand(callback, threadManager);

        threadManager.postToWork(new Runnable() {
            @Override
            public void run() {
                core.HMBTCoreSendCustomCommand(bytes.getByteArray(), bytes.getLength(),
                        getAddressBytes());
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
            HmLog.d("not authenticated");
            callback.onRevokeFailed(new RevokeError(RevokeError.Type.UNAUTHORIZED, 0, "not " +
                    "authenticated"));
            return;
        }

        if (sentCommand != null && sentCommand.finished == false) {
            HmLog.d(HmLog.Level.ALL, "custom command in progress");
            callback.onRevokeFailed(new RevokeError(RevokeError.Type.COMMAND_IN_PROGRESS, 0, "a " +
                    " command is in progress"));
            return;
        }

        HmLog.d("revoke " + serial);

        this.revokeCallback = callback;

        threadManager.postToWork(new Runnable() {
            @Override
            public void run() {
                core.HMBTCoreSendRevoke(serial.getByteArray());
            }
        });
    }

    void onChangedAuthenticationState(HMDevice hmDevice) {
        this.hmDevice = hmDevice;

        if (serial == null || serial.equals(hmDevice.getSerial()) == false) {
            serial = new DeviceSerial(hmDevice.getSerial());
        }

        if (hmDevice.getIsAuthenticated() == 0) {
            if (state == State.AUTHENTICATED) {
                // we were authenticated, go back to connected state
                setState(State.CONNECTED);
            } else {
                // otherwise state is connected and now authentication failed
                setState(State.AUTHENTICATION_FAILED);
            }
        } else {
            setState(State.AUTHENTICATED);
        }
    }

    void onCommandReceived(final byte[] bytes) {
        HmLog.d("did receive command %s from %s", ByteUtils.hexFromBytes
                (bytes), ByteUtils.hexFromBytes(hmDevice.getMac()));

        if (listener == null) {
            HmLog.d("can't dispatch notification: no listener set");
            return;
        }

        threadManager.postToMain(new Runnable() {
            @Override public void run() {
                if (listener == null) return;
                listener.onCommandReceived(Link.this, new Bytes(bytes));
            }
        });
    }

    void onCommandResponseReceived(final byte[] data) {
        HmLog.d("did receive command response %s from %s in %s ms", ByteUtils
                .hexFromBytes(data), ByteUtils.hexFromBytes(hmDevice.getMac()), (Calendar
                .getInstance().getTimeInMillis() - sentCommand.commandStartTime)
        );

        if (sentCommand == null || sentCommand.finished) {
            HmLog.d("can't dispatch command response: sentCommand = null || " +
                    "finished");
            return;
        }

        sentCommand.dispatchResult(data);
    }

    void onRevokeResponse(final byte[] data, final int result) {
        threadManager.postToMain(new Runnable() {
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

    /**
     * The possible states of the Link.
     *
     * @see LinkListener#onStateChanged(Link, State)
     */
    public enum State {
        DISCONNECTED, CONNECTED, AUTHENTICATION_FAILED, AUTHENTICATED
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
