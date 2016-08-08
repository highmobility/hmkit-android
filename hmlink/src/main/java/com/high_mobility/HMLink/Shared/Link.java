package com.high_mobility.HMLink.Shared;

import android.bluetooth.BluetoothDevice;
import android.os.CountDownTimer;
import android.util.Log;

import com.high_mobility.HMLink.LinkException;
import com.high_mobility.btcore.HMDevice;
import com.high_mobility.HMLink.Constants;

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

    SentCommand sentCommand;

    long connectionTime;

    Link(BluetoothDevice btDevice, LocalDevice device) {
        connectionTime = Calendar.getInstance().getTimeInMillis();

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
        if (state != State.AUTHENTICATED) {
            if (Device.loggingLevel.getValue() >= Device.LoggingLevel.All.getValue())
                Log.d(LocalDevice.TAG, "cant send command, not authenticated");
            responseCallback.response(null, new LinkException(LinkException.LinkExceptionCode.UNAUTHORISED));
            return;
        }

        if (sentCommand != null && sentCommand.finished == false) {
            if (Device.loggingLevel.getValue() >= Device.LoggingLevel.All.getValue())
                Log.d(LocalDevice.TAG, "cant send command, custom command in progress");
            responseCallback.response(null, new LinkException(LinkException.LinkExceptionCode.CUSTOM_COMMAND_IN_PROGRESS));
            return;
        }

        if (Device.loggingLevel.getValue() >= Device.LoggingLevel.Debug.getValue())
            Log.d(LocalDevice.TAG, "send command " + ByteUtils.hexFromBytes(bytes)
                    + " to " + ByteUtils.hexFromBytes(hmDevice.getMac()));

        sentCommand = new SentCommand(responseCallback);
        device.shared.core.HMBTCoreSendCustomCommand(this.device.shared.coreInterface, bytes, bytes.length, getAddressBytes());
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
            if (state == State.AUTHENTICATED && Device.loggingLevel.getValue() >= Device.LoggingLevel.Debug.getValue()) {
                Log.d(LocalDevice.TAG, "authenticated in " + (Calendar.getInstance().getTimeInMillis() - connectionTime) + "ms");
            }

            this.state = state;

            if (listener != null) {
                final Link linkPointer = this;

                this.device.shared.ble.mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        linkPointer.listener.onStateChanged(linkPointer, oldState);
                    }
                });
            }
        }
    }

    byte[] onCommandReceived(byte[] bytes) {
        if (Device.loggingLevel.getValue() >= Device.LoggingLevel.Debug.getValue())
            Log.d(LocalDevice.TAG, "did receive command " + ByteUtils.hexFromBytes(bytes)
                    + " from " + ByteUtils.hexFromBytes(hmDevice.getMac()));

        if (listener == null) {
            Log.d(LocalDevice.TAG, "can't dispatch notification: no listener set");
            return null;
        }

        return listener.onCommandReceived(this, bytes);
    }

    void onCommandResponseReceived(final byte[] data) {
        if (Device.loggingLevel.getValue() >= Device.LoggingLevel.Debug.getValue())
            Log.d(LocalDevice.TAG, "did receive command response " + ByteUtils.hexFromBytes(data)
                    + " from " + ByteUtils.hexFromBytes(hmDevice.getMac()) + " in " +
                    (Calendar.getInstance().getTimeInMillis() - sentCommand.commandStartTime) + "ms");

        if (sentCommand == null) {
            if (Device.loggingLevel.getValue() >= Device.LoggingLevel.Debug.getValue())
                Log.d(LocalDevice.TAG, "can't dispatch command response: sentCommand = null");
            return;
        }

        sentCommand.dispatchResult(data, null);
    }

    int pairingResponse = -1;
    int didReceivePairingRequest() {
        if (listener == null) {
            Log.e(LocalDevice.TAG, "link listener not set");
            return 1;
        }

        final Link reference = this;
        pairingResponse = -1;
        device.shared.ble.mainThreadHandler.post(new Runnable() {
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
                device.shared.ble.mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onPairingRequestTimeout(reference);
                    }
                });

                if (Device.loggingLevel.getValue() >= Device.LoggingLevel.All.getValue()) Log.d(LocalDevice.TAG, "pairing timer exceeded");
                return 1; // TODO: use correct code
            }
        }

        return pairingResponse;
    }

    byte[] getAddressBytes() {
        return ByteUtils.bytesFromMacString(btDevice.getAddress());
    }

    private class SentCommand {
        boolean finished;
        Constants.DataResponseCallback commandCallback;
        CountDownTimer timeoutTimer;
        Long commandStartTime;

        SentCommand(Constants.DataResponseCallback callback) {
            this.commandCallback = callback;
            startTimeoutTimer();
            commandStartTime = Calendar.getInstance().getTimeInMillis();
        }

        void dispatchResult(final byte[] response, final LinkException exception) {
            if (timeoutTimer != null) timeoutTimer.cancel();
            finished = true;
            if (commandCallback == null) {
                Log.d(LocalDevice.TAG, "cannot dispatch the result: no callback reference");
                return;
            }

            device.shared.ble.mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    commandCallback.response(response, exception);
                }
            });
        }

        void startTimeoutTimer() {
            timeoutTimer = new CountDownTimer((long)(Constants.commandTimeout * 1000), 15000) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    dispatchResult(null, new LinkException(LinkException.LinkExceptionCode.TIME_OUT));
                }
            }.start();
        }
    }
}
