/*
 * The MIT License
 *
 * Copyright (c) 2014- High-Mobility GmbH (https://high-mobility.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.highmobility.hmkit;

import android.bluetooth.BluetoothDevice;

import com.highmobility.btcore.HMDevice;
import com.highmobility.crypto.value.DeviceSerial;
import com.highmobility.hmkit.error.AuthenticationError;
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
    private long authenticationStartTime;
    private RevokeCallback revokeCallback;
    private byte[] revokeData;

    Link(Core core, ThreadManager threadManager, BluetoothDevice btDevice) {
        this.btDevice = btDevice;
        this.core = core;
        this.threadManager = threadManager;
        mac = new Bytes(ByteUtils.bytesFromMacString(btDevice.getAddress()));
        authenticationStartTime = Calendar.getInstance().getTimeInMillis();
    }

    /**
     * @return The Links state.
     */
    public State getState() {
        return state;
    }

    void setState(final State state) {
        if (this.state != state) {
            final State oldState = this.state;

            if (state == State.AUTHENTICATING) {
                authenticationStartTime = Calendar.getInstance().getTimeInMillis();
            } else if (state == State.AUTHENTICATED) {
                d("authenticated in %d ms", (Calendar.getInstance().getTimeInMillis() -
                        authenticationStartTime));
            }

            this.state = state;

            final Link linkPointer = this;
            threadManager.postToMain(new Runnable() {
                @Override public void run() {
                    if (linkPointer.listener == null) return;
                    linkPointer.listener.onStateChanged(linkPointer, state, oldState);
                }
            });
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
        sendCommand(ContentType.AUTO_API, bytes, callback);
    }

    /**
     * Send a command to the Link.
     *
     * @param contentType The command content type. See {@link ContentType} for possible options.
     * @param bytes       The command bytes that will be sent to the link.
     * @param callback    A {@link CommandCallback} object that is invoked with the command result.
     */
    public void sendCommand(final ContentType contentType, final Bytes bytes,
                            CommandCallback callback) {
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
                core.HMBTCoreSendCustomCommand(contentType.asInt(), bytes.getByteArray(),
                        bytes.getLength(), getAddressBytes());
            }
        });
    }

    /**
     * Revoke authorisation for this device. {@link RevokeCallback} will be called with the result.
     * If successful, the {@link LinkListener#onStateChanged(Link, State, State)} will be called
     * with the {@link State#NOT_AUTHENTICATED} state.
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
        setState(State.REVOKING);

        this.revokeCallback = callback;

        threadManager.postToWork(new Runnable() {
            @Override
            public void run() {
                core.HMBTCoreSendRevoke(serial.getByteArray());
            }
        });
    }

    void onChangedAuthenticationState(HMDevice hmDevice) {
        // EnteredProximity cb
        if (serial == null || serial.equals(hmDevice.getSerial()) == false) {
            serial = new DeviceSerial(hmDevice.getSerial());
        }

        if (hmDevice.getIsAuthenticated() == 0) {
            if (state == State.AUTHENTICATING) {
                // authentication failed with wrong signature. no errorCommand
                setState(State.NOT_AUTHENTICATED);
                AuthenticationError error =
                        new AuthenticationError(AuthenticationError.Type.INTERNAL_ERROR, 0,
                                "Authentication failed.");
                callAuthenticationFailed(error);
            } else if (state == State.AUTHENTICATED || state == State.REVOKING) {
                // AUTHENTICATED = Called after car revoke
                // TODO: 05/11/2019 If car revoke has a cb, the state can be REVOKING only ^^
                setState(State.NOT_AUTHENTICATED);
                threadManager.postToMain(new Runnable() {
                    @Override public void run() {
                        if (revokeCallback == null) return;
                        // REVOKING = called after mobile revoke
                        revokeCallback.onRevokeSuccess(new Bytes(revokeData));
                        revokeData = null;
                    }
                });
            }
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

    void onCommandResponse(final byte[] data) {
        d("did receive command response %s from %s in %s ms", ByteUtils.hexFromBytes(data),
                mac, getCommandDuration());

        if (isSendingCommand()) sentCommand.dispatchResponse(data);
    }

    void onCommandError(int errorType) {
        d("did receive command error %d from %s in %s ms", errorType, mac, getCommandDuration());

        if (isSendingCommand()) sentCommand.dispatchError(errorType);
    }

    private long getCommandDuration() {
        return Calendar.getInstance().getTimeInMillis() - sentCommand.commandStartTime;
    }

    private boolean isSendingCommand() {
        if (sentCommand == null || sentCommand.finished) {
            w("can't dispatch command response: sentCommand = null || finished");
            return false;
        }

        return true;
    }

    void onRevokeResponse(final byte[] data, final int result) {
        threadManager.postToMain(new Runnable() {
            @Override public void run() {
                if (revokeCallback == null) return;

                if (result == 0) {
                    // remember the data. To be sent on EnteredProximity.
                    revokeData = data;
                } else {
                    setState(State.AUTHENTICATED);
                    revokeCallback.onRevokeFailed(new RevokeError(RevokeError.Type.FAILED, 0,
                            "Revoke failed."));
                }
            }
        });
    }

    void onErrorCommand(int commandId, int errorType) {
        if (getState() == State.AUTHENTICATING) {
            // this is only called when authenticating. After this EnteredProximity is not called,
            // so can set the state here.
            setState(State.NOT_AUTHENTICATED);
            AuthenticationError error =
                    new AuthenticationError(AuthenticationError.Type.INTERNAL_ERROR, errorType,
                            "Command " + commandId + " failed.");
            callAuthenticationFailed(error);
        }
    }

    private void callAuthenticationFailed(final AuthenticationError error) {
        if (listener != null) {
            final Link linkPointer = this;
            threadManager.postToMain(new Runnable() {
                @Override public void run() {
                    if (linkPointer.listener == null) return;
                    linkPointer.listener.onAuthenticationFailed(linkPointer, error);
                }
            });
        }
    }

    byte[] getAddressBytes() {
        return ByteUtils.bytesFromMacString(btDevice.getAddress());
    }

    void onRevokeIncoming() {
        setState(State.REVOKING);
    }

    void onRegisterCertificate() {
        // authentication after revoke
        setState(State.AUTHENTICATING);
    }

    /**
     * The possible states of the Link.
     * <p>
     * States can go from: AUTHENTICATING > AUTHENTICATED or AUTHENTICATING > AUTHENTICATION_FAILED
     * (then the LinkListener's authenticationFailed is called as well)
     * <p>
     * After this, it can go to REVOKING (if initiated) and then to
     * NOT_AUTHNETICATED/AUTHENTICATED(revoke
     * failed).
     *
     * @see LinkListener#onStateChanged(Link, State, State)
     */
    public enum State {
        AUTHENTICATING, NOT_AUTHENTICATED, AUTHENTICATED, REVOKING
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
         * Invoked when the revoke succeeded. Link will go to {@link State#NOT_AUTHENTICATED}
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
