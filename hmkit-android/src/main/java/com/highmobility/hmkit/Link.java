package com.highmobility.hmkit;

import android.bluetooth.BluetoothDevice;

import com.highmobility.btcore.HMDevice;
import com.highmobility.crypto.value.DeviceSerial;
import com.highmobility.hmkit.error.LinkError;
import com.highmobility.hmkit.error.RevokeError;
import com.highmobility.utils.ByteUtils;
import com.highmobility.value.Bytes;

import java.util.Calendar;

import javax.annotation.Nullable;

import static com.highmobility.hmkit.HMLog.d;
import static com.highmobility.hmkit.HMLog.i;
import static com.highmobility.hmkit.HMLog.w;

public class Link {
    /**
     * The time after which HMKit will fail the command if there has been no response. In ms.
     */
    public static long commandTimeout = 120000;
    protected final Core core;
    protected final ThreadManager threadManager;

    LinkListener listener;
    BluetoothDevice btDevice;
    private Bytes mac;
    @Nullable private DeviceSerial serial; // set after authentication is finished by core

    private State state = State.AUTHENTICATING;

    private LinkCommand sentCommand;
    private final long connectionTime;
    private RevokeCallback revokeCallback;

    Link(Core core, ThreadManager threadManager, BluetoothDevice btDevice) {
        this.btDevice = btDevice;
        this.core = core;
        this.threadManager = threadManager;

        connectionTime = Calendar.getInstance().getTimeInMillis();
        mac = new Bytes(ByteUtils.bytesFromMacString(btDevice.getAddress()));
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
                d("authenticated in %d ms", (Calendar.getInstance().getTimeInMillis() -
                        connectionTime));
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
     * @return The link's serial. Set after authentication process has resolved the serial.
     */
    @Nullable public DeviceSerial getSerial() {
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
            w("custom command in progress");

            callback.onCommandFailed(new LinkError(LinkError.Type.COMMAND_IN_PROGRESS, 0, "custom" +
                    " command in progress"));
            return;
        }

        d("send command %s to %s", bytes, mac);

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
     * {@link State#REVOKED} state.
     * <p>
     * After this has succeeded it is up to the user to finish the flow related to this link -
     * disconnect, stop broadcasting or something else.
     *
     * @param callback Callback invoked in case of an error.
     */
    public void revoke(RevokeCallback callback) {
        if (state != State.AUTHENTICATED) {
            String failureMessage = "not authenticated";
            w(failureMessage);
            callback.onRevokeFailed(new RevokeError(RevokeError.Type.UNAUTHORIZED, 0,
                    failureMessage));
            return;
        }

        if (sentCommand != null && sentCommand.finished == false) {
            String failureMessage = "custom command in progress";
            w(failureMessage);
            callback.onRevokeFailed(new RevokeError(RevokeError.Type.COMMAND_IN_PROGRESS, 0,
                    failureMessage));
            return;
        }

        i("revoke %s", serial);

        this.revokeCallback = callback;

        threadManager.postToWork(new Runnable() {
            @Override
            public void run() {
                core.HMBTCoreSendRevoke(serial.getByteArray());
            }
        });
    }


    void onChangedAuthenticationState(HMDevice hmDevice) {
        if (serial == null || serial.equals(hmDevice.getSerial()) == false) {
            serial = new DeviceSerial(hmDevice.getSerial());
        }

        if (hmDevice.getIsAuthenticated() == 0) {
            // either authentication failed(wrong signature) or after revoke
            // TODO: 21/10/2019 if after revoke, should go to REVOKED state
            setState(State.AUTHENTICATION_FAILED);
        } else {
            setState(State.AUTHENTICATED);
        }
    }

    void onCommandReceived(final byte[] bytes) {
        d("did receive command %s from %s", ByteUtils.hexFromBytes(bytes), mac);

        if (listener == null) {
            w("can't dispatch notification: no listener set");
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
        d("did receive command response %s from %s in %s ms", ByteUtils.hexFromBytes(data),
                mac, (Calendar.getInstance().getTimeInMillis() - sentCommand.commandStartTime)
        );

        if (sentCommand == null || sentCommand.finished) {
            w("can't dispatch command response: sentCommand = null || " +
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
     * States can go from:
     * AUTHENTICATING > AUTHENTICATED
     * or
     * AUTHENTICATING > AUTHENTICATION_FAILED (then the LinkListener's authenticationFailed is called as well)
     *
     * After this, it can go to REVOKING (if initiated) and to REVOKED/AUTHENTICATED(if revoke failed)
     *
     * @see LinkListener#onStateChanged(Link, State)
     */
    public enum State {
        AUTHENTICATING, AUTHENTICATION_FAILED, AUTHENTICATED, REVOKING, REVOKED
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
         * Invoked when the revoke succeeded. After this the link will go to {@link State#REVOKED}
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
