package com.highmobility.hmkit;

import android.util.Base64;
import android.util.Log;

import com.android.volley.Response;
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

/**
 * Telematics provides the option to send commands via Telematics.
 */
public class Telematics extends Core.Telematics {
    private static final String TAG = "HMKit-Telematics";
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
     * @param command  the bytes to send to the device
     * @param serial   serial of the device
     * @param callback A {@link CommandCallback} object that is invoked with the command result.
     */
    public void sendCommand(final Bytes command, DeviceSerial serial, final CommandCallback
            callback) {
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

        if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
            Log.d(TAG, "sendTelematicsCommand: " + command);

        final TelematicsCommand activeCommand = new TelematicsCommand(commandCallback, callback,
                threadManager);
        activeCommands.add(activeCommand);

        webService.getNonce(certificate.getProviderSerial(), new Response
                .Listener<JSONObject>() {
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
                                    .getByteArray(), nonce, command.getLength(), command
                                    .getByteArray());
                        }
                    });
                } catch (JSONException e) {
                    activeCommand.dispatchError(TelematicsError.Type
                                    .INVALID_SERVER_RESPONSE, 0,
                            "Invalid nonce response from server.");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    activeCommand.dispatchError(TelematicsError.Type.HTTP_ERROR, error
                            .networkResponse.statusCode, new String(error.networkResponse.data));
                } else {
                    activeCommand.dispatchError(TelematicsError.Type.NO_CONNECTION, 0, "Cannot " +
                            "connect to the web service. Check your internet connection");
                }
            }
        });
    }

    @Override
    public void onTelematicsCommandEncrypted(byte[] serial, byte[] issuer, byte[] command) {
        final TelematicsCommand commandSent = interactingCommand; // need this for command response
        webService.sendTelematicsCommand(new Bytes(command), new DeviceSerial(serial),
                new Issuer(issuer),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            String status = jsonObject.getString("status");

                            if (status.equals("ok")) {
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
                            } else if (status.equals("timeout")) {
                                commandSent.dispatchError(TelematicsError.Type.TIMEOUT, 0,
                                        jsonObject
                                                .getString("message"));
                            } else if (status.equals("error")) {
                                commandSent.dispatchError(TelematicsError.Type
                                                .SERVER_ERROR, 0,
                                        jsonObject
                                                .getString("message"));
                            }
                        } catch (JSONException e) {
                            commandSent.dispatchError(TelematicsError.Type
                                            .INVALID_SERVER_RESPONSE, 0,
                                    "Invalid response from server.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
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
                                    "Cannot connect " +
                                            "to the web service. Check your internet connection");
                        }
                    }
                });
    }

    @Override
    public void onTelematicsResponseDecrypted(byte[] serial, int resultCode, byte[] data) {
        if (resultCode == 0x02) {
            interactingCommand.dispatchError(TelematicsError.Type.INTERNAL_ERROR, 0, "Failed to " +
                    "decrypt web service response.");
        } else {
            final Bytes response = new Bytes(data);
            if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                Log.d(TAG, "onTelematicsResponseDecrypted: " + response);
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
