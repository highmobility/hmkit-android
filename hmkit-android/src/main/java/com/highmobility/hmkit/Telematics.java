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

import android.util.Base64;

import com.android.volley.VolleyError;
import com.highmobility.crypto.AccessCertificate;
import com.highmobility.crypto.value.DeviceSerial;
import com.highmobility.crypto.value.Issuer;
import com.highmobility.hmkit.error.TelematicsError;
import com.highmobility.value.Bytes;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.highmobility.hmkit.HMLog.d;

/**
 * Telematics provides the option to send commands via Telematics.
 */
public class Telematics extends Core.Telematics {
    private final Core core;
    private final WebService webService;
    private final Storage storage;
    private final ThreadManager threadManager;

    final List<TelematicsCommand> activeCommands = new ArrayList<>();
    TelematicsCommand interactingCommand; // reference between core interactions

    Telematics(Core core, Storage storage, ThreadManager threadManager, WebService webService) {
        this.core = core;
        this.storage = storage;
        this.threadManager = threadManager;
        this.webService = webService;
        core.telematics = this;
    }

    boolean isSendingCommand() {
        return activeCommands.size() > 0;
    }

    /**
     * Send a command to a device via telematics.
     *
     * @param contentType The content type. See {@link ContentType} for possible types.
     * @param command     the bytes to send to the device
     * @param serial      serial of the device
     * @param callback    A {@link CommandCallback} object that is invoked with the command result.
     */
    public void sendCommand(final ContentType contentType, final Bytes command, DeviceSerial serial,
                            final CommandCallback callback) {
        core.start();
        if (command.getLength() > Constants.MAX_COMMAND_LENGTH) {
            TelematicsError error = new TelematicsError(TelematicsError.Type.COMMAND_TOO_BIG, 0,
                    "Command size is bigger than " + Constants.MAX_COMMAND_LENGTH + " bytes");
            callback.onCommandFailed(error);
            return;
        }

        final AccessCertificate certificate = storage.getCertificate(serial);

        if (certificate == null) {
            TelematicsError error = new TelematicsError(TelematicsError.Type.INVALID_SERIAL, 0,
                    "Access certificate with this serial does not exist");
            callback.onCommandFailed(error);
            return;
        }

        d("sendTelematicsCommand: %s", command);

        final TelematicsCommand activeCommand = new TelematicsCommand(commandCallback, callback,
                threadManager);
        activeCommands.add(activeCommand);

        webService.getNonce(certificate.getProviderSerial(), new WebRequestListener() {
            @Override
            public void onResponse(JSONObject jsonResponse) {
                try {
                    final byte[] nonce = Base64.decode(jsonResponse.getString("nonce"),
                            Base64.DEFAULT);

                    threadManager.postToWork(new Runnable() {
                        @Override
                        public void run() {
                            interactingCommand = activeCommand;
                            core.HMBTCoreSendTelematicsCommand(certificate.getGainerSerial()
                                            .getByteArray(), nonce, contentType.asInt(),
                                    command.getLength(), command.getByteArray());
                        }
                    });
                } catch (JSONException e) {
                    activeCommand.dispatchError(TelematicsError.Type
                            .INVALID_SERVER_RESPONSE, 0, "Invalid nonce response from server.");
                }
            }

            @Override public void onError(VolleyError error) {
                if (error.networkResponse != null) {
                    activeCommand.dispatchError(TelematicsError.Type.HTTP_ERROR, error
                            .networkResponse.statusCode, new String(error.networkResponse.data));
                } else {
                    activeCommand.dispatchError(TelematicsError.Type.NO_CONNECTION, 0,
                            WebService.NO_CONNECTION_ERROR);
                }
            }
        });
    }

    /**
     * Send a command to a device via telematics.
     *
     * @param command  the bytes to send to the device
     * @param serial   serial of the device
     * @param callback A {@link CommandCallback} object that is invoked with the command result.
     */
    public void sendCommand(final Bytes command, DeviceSerial serial, final CommandCallback
            callback) {
        sendCommand(ContentType.AUTO_API, command, serial, callback);
    }

    @Override void onTelematicsCommandEncrypted(byte[] serial, byte[] issuer, byte[] command) {
        final TelematicsCommand commandSent = interactingCommand; // need this for command response
        webService.sendTelematicsCommand(new Bytes(command), new DeviceSerial(serial),
                new Issuer(issuer),
                new WebRequestListener() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            String status = jsonObject.getString("status");

                            switch (status) {
                                case "ok":
                                    // decrypt the data
                                    final byte[] data = Base64.decode(jsonObject.getString
                                            ("response_data"), Base64.NO_WRAP);

                                    threadManager.postToWork(new Runnable() {
                                        @Override
                                        public void run() {
                                            interactingCommand = commandSent;
                                            core.HMBTCoreTelematicsReceiveData(data.length, data);
                                        }
                                    });
                                    break;
                                case "timeout":
                                    commandSent.dispatchError(TelematicsError.Type.TIMEOUT, 0,
                                            jsonObject
                                                    .getString("message"));
                                    break;
                                case "error":
                                    commandSent.dispatchError(TelematicsError.Type
                                                    .SERVER_ERROR, 0,
                                            jsonObject
                                                    .getString("message"));
                                    break;
                            }
                        } catch (JSONException e) {
                            commandSent.dispatchError(TelematicsError.Type
                                            .INVALID_SERVER_RESPONSE, 0,
                                    "Invalid response from server.");
                        }
                    }

                    @Override public void onError(VolleyError error) {
                        if (error.networkResponse != null) {
                            try {
                                JSONObject json = new JSONObject(new String(error.networkResponse
                                        .data));
                                if (json.has("message")) {
                                    commandSent.dispatchError(TelematicsError.Type.HTTP_ERROR, error
                                            .networkResponse.statusCode, json.getString("message"));
                                } else {
                                    commandSent.dispatchError(TelematicsError.Type.HTTP_ERROR, error
                                            .networkResponse.statusCode, new String(error
                                            .networkResponse.data));
                                }
                            } catch (JSONException e) {
                                commandSent.dispatchError(TelematicsError.Type.HTTP_ERROR, error
                                        .networkResponse.statusCode, "");
                            }
                        } else {
                            commandSent.dispatchError(TelematicsError.Type.NO_CONNECTION, 0,
                                    WebService.NO_CONNECTION_ERROR);
                        }
                    }
                });
    }

    @Override void onTelematicsResponseDecrypted(byte[] serial, int resultCode, byte[] data) {
        if (resultCode == 0x02) {
            interactingCommand.dispatchError(TelematicsError.Type.INTERNAL_ERROR, 0, "Failed to " +
                    "decrypt web service response.");
        } else {
            final Bytes response = new Bytes(data);
            d("onTelematicsResponseDecrypted: " + response);
            interactingCommand.dispatchResult(response);
        }
    }

    final TelematicsCommand.Callback commandCallback = new TelematicsCommand.Callback() {
        @Override void onCommandFinished(TelematicsCommand command) {
            activeCommands.remove(command);
        }
    };

    /**
     * CommandCallback is used to notify the user about telematics command result.
     */
    public interface CommandCallback {
        /**
         * Invoked if the command was sent successfully and a response was received.
         *
         * @param bytes the response bytes
         */
        void onCommandResponse(Bytes bytes);

        /**
         * Invoked if something went wrong.
         *
         * @param error The error
         */
        void onCommandFailed(TelematicsError error);
    }

}
