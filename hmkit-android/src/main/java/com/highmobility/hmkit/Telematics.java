package com.highmobility.hmkit;

import android.util.Base64;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.highmobility.utils.Bytes;
import com.highmobility.crypto.AccessCertificate;
import com.highmobility.hmkit.Error.TelematicsError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ttiganik on 03/05/2017.
 *
 * Telematics provides the option to send commands via telematics.
 */
public class Telematics {
    static final String TAG = "Telematics";

    Manager manager;
    CommandCallback callback;
    boolean sendingCommand;

    /**
     * CommandCallback is used to notify the user about telematics command result.
     */
    public interface CommandCallback {
        /**
         * Invoked if the command was sent successfully and a response was received.
         * @param bytes the response bytes
         */
        void onCommandResponse(byte[] bytes);

        /**
         * Invoked if something went wrong.
         * @param error The error
         */
        void onCommandFailed(TelematicsError error);
    }

    Telematics(Manager manager) {
        this.manager = manager;
    }

    /**
     * Send a command to a device via telematics.
     *
     * @param command the bytes to send to the device
     * @param serial serial of the device
     * @param callback A {@link CommandCallback} object that is invoked with the command result.
     *
     */
    public void sendCommand(final byte[] command, byte[] serial, final CommandCallback callback) {
        if (command.length > Constants.MAX_COMMAND_LENGTH) {
            TelematicsError error = new TelematicsError(TelematicsError.Type.COMMAND_TOO_BIG, 0,
                    "Command size is bigger than " + Constants.MAX_COMMAND_LENGTH + " bytes");
            callback.onCommandFailed(error);
            return;
        }

        if (sendingCommand == true) {
            TelematicsError error = new TelematicsError(TelematicsError.Type.COMMAND_IN_PROGRESS, 0, "Already sending a command");
            callback.onCommandFailed(error);
            return;
        }

        final AccessCertificate certificate = manager.getCertificate(serial);

        if (certificate == null) {
            TelematicsError error = new TelematicsError(TelematicsError.Type.INVALID_SERIAL, 0,
                    "Access certificate with this serial does not exist");
            callback.onCommandFailed(error);
            return;
        }

        if (manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
            Log.d(TAG, "sendTelematicsCommand: " + Bytes.hexFromBytes(command));

        sendingCommand = true;

        manager.webService.getNonce(certificate.getProviderSerial(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonResponse) {
                try {
                    final byte[] nonce = Base64.decode(jsonResponse.getString("nonce"), Base64.DEFAULT);
                    Telematics.this.callback = callback;
                    manager.workHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            manager.core.HMBTCoreSendTelematicsCommand(manager.coreInterface, certificate.getGainerSerial(), nonce, command.length, command);
                        }
                    });
                } catch (JSONException e) {
                    dispatchError(TelematicsError.Type.INVALID_SERVER_RESPONSE, 0, "Invalid nonce response from server.");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    dispatchError(TelematicsError.Type.HTTP_ERROR, error.networkResponse.statusCode, new String(error.networkResponse.data));
                }
                else {
                    dispatchError(TelematicsError.Type.NO_CONNECTION, 0, "Cannot connect to the web service. Check your internet connection");
                }
            }
        });
    }

    void onTelematicsCommandEncrypted(byte[] serial, byte[] issuer, byte[] command) {
        manager.webService.sendTelematicsCommand(command, serial, issuer,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            String status = jsonObject.getString("status");

                            if (status.equals("ok")) {
                                // decrypt the data
                                final byte[] data = Base64.decode(jsonObject.getString("response_data"), Base64.NO_WRAP);

                                manager.workHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        manager.core.HMBTCoreTelematicsReceiveData(manager.coreInterface, data.length, data);
                                    }
                                });
                            }
                            else if (status.equals("timeout")) {
                                dispatchError(TelematicsError.Type.TIMEOUT, 0, jsonObject.getString("message"));
                            }
                            else if (status.equals("error")) {
                                dispatchError(TelematicsError.Type.SERVER_ERROR, 0, jsonObject.getString("message"));
                            }
                        } catch (JSONException e) {
                            dispatchError(TelematicsError.Type.INVALID_SERVER_RESPONSE, 0, "Invalid response from server.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            try {
                                JSONObject json = new JSONObject(new String(error.networkResponse.data));
                                if (json.has("message")) {
                                    dispatchError(TelematicsError.Type.HTTP_ERROR, error.networkResponse.statusCode, json.getString("message"));
                                }
                                else {
                                    dispatchError(TelematicsError.Type.HTTP_ERROR, error.networkResponse.statusCode, new String(error.networkResponse.data));
                                }
                            } catch (JSONException e) {
                                dispatchError(TelematicsError.Type.HTTP_ERROR, error.networkResponse.statusCode, "");
                            }
                        }
                        else {
                            dispatchError(TelematicsError.Type.NO_CONNECTION, 0, "Cannot connect to the web service. Check your internet connection");
                        }
                    }
                });
    }

    void onTelematicsResponseDecrypted(byte[] serial, byte id, final byte[] data) {
        if (id == 0x02) {
            dispatchError(TelematicsError.Type.INTERNAL_ERROR, 0, "Failed to decrypt web service response.");
        }
        else {
            if (manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
                Log.d(TAG, "onTelematicsResponseDecrypted: " + Bytes.hexFromBytes(data));

            sendingCommand = false;

            manager.postToMainThread(new Runnable() {
                @Override
                public void run() {
                    callback.onCommandResponse(data);
                }
            });
        }
    }

    void dispatchError(final TelematicsError.Type type, final int code, final String message) {
        manager.postToMainThread(new Runnable() {
            @Override
            public void run() {
                final TelematicsError error = new TelematicsError(type, code, message);
                sendingCommand = false;
                callback.onCommandFailed(error);
            }
        });
    }
}
