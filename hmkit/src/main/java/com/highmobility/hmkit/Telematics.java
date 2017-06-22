package com.highmobility.hmkit;

import android.util.Base64;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.highmobility.hmkit.Crypto.AccessCertificate;
import com.highmobility.hmkit.Error.TelematicsError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ttiganik on 03/05/2017.
 */

public class Telematics {
    static final String TAG = "Telematics";

    Manager manager;
    CommandCallback callback;
    boolean sendingCommand;

    public interface CommandCallback {
        void onCommandResponse(byte[] bytes);
        void onCommandFailed(TelematicsError error);
    }

    Telematics(Manager manager) {
        this.manager = manager;
    }

    /**
     * Send a command to a device via telematics.
     *
     * @param command the bytes to send to the device.
     * @param certificate the certificate that authorizes the connection with the SDK and the device.
     * @param callback callback that is invoked with the command result
     *                 onCommandResponse is invoked with the response if thecommand was sent successfully.
     *                 onCommandFailed is invoked if something went wrong.
     */
    public void sendTelematicsCommand(final byte[] command, final AccessCertificate certificate, final CommandCallback callback) {
        // TODO: dont use cert but vehicleSerial
        if (sendingCommand == true) {
            TelematicsError error = new TelematicsError(TelematicsError.Type.COMMAND_IN_PROGRESS, 0, "Already sending a command");
            callback.onCommandFailed(error);
            return;
        }

        if (manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
            Log.d(TAG, "sendTelematicsCommand: " + ByteUtils.hexFromBytes(command));

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
                Log.d(TAG, "onTelematicsResponseDecrypted: " + ByteUtils.hexFromBytes(data));

            sendingCommand = false;
            manager.mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onCommandResponse(data);
                }
            });
        }
    }

    void dispatchError(final TelematicsError.Type type, final int code, final String message) {
        manager.mainHandler.post(new Runnable() {
            @Override
            public void run() {
                final TelematicsError error = new TelematicsError(type, code, message);
                sendingCommand = false;
                callback.onCommandFailed(error);
            }
        });
    }
}
