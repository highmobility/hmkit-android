package com.highmobility.hmkit;

import android.util.Base64;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.highmobility.hmkit.Crypto.AccessCertificate;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ttiganik on 03/05/2017.
 */

public class Telematics {
    static final String TAG = "Telematics";

    public interface CommandCallback {
        void onCommandResponse(byte[] bytes);
        void onCommandFailed(TelematicsError error);
    }

    Manager manager;
    CommandCallback callback;
    boolean sendingCommand;

    Telematics(Manager manager) {
        this.manager = manager;
    }

    /**
     * Send a command to a device via telematics.
     *
     * @param command the bytes to send to the device.
     * @param certificate the certificate that authorizes the connection with the SDK and the device.
     * @param callback callback that is invoked with the command result
     */
    public void sendTelematicsCommand(final byte[] command, final AccessCertificate certificate, final CommandCallback callback) {
        if (sendingCommand == true) {
            TelematicsError error = new TelematicsError(TelematicsError.Type.ERROR, "Already sending a command");
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
                    dispatchError(TelematicsError.Type.ERROR, "Invalid nonce response from server.");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    dispatchError(TelematicsError.Type.ERROR, "HTTP error " + new String(error.networkResponse.data));
                }
                else {
                    dispatchError(TelematicsError.Type.ERROR, "Cannot connect to the web service. Check your internet connection");
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
                                dispatchError(TelematicsError.Type.TIMEOUT, jsonObject.getString("message"));
                            }
                            else if (status.equals("error")) {
                                dispatchError(TelematicsError.Type.ERROR, jsonObject.getString("message"));
                            }
                        } catch (JSONException e) {
                            dispatchError(TelematicsError.Type.ERROR, "Invalid response from server.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            String message = "HTTP error " + error.networkResponse.statusCode + ": ";
                            try {
                                JSONObject json = new JSONObject(new String(error.networkResponse.data));
                                if (json.has("message")) {
                                    dispatchError(TelematicsError.Type.ERROR, message + json.getString("message"));
                                }
                                else {
                                    dispatchError(TelematicsError.Type.ERROR, message + new String(error.networkResponse.data));
                                }
                            } catch (JSONException e) {
                                dispatchError(TelematicsError.Type.ERROR, "Error parse exception");
                            }
                        }
                        else {
                            dispatchError(TelematicsError.Type.ERROR, "Cannot connect to the web service. Check your internet connection");
                        }
                    }
                });
    }

    void onTelematicsResponseDecrypted(byte[] serial, byte id, final byte[] data) {
        if (id == 0x02) {
            dispatchError(TelematicsError.Type.ERROR, "Failed to decrypt web service response.");
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

    void dispatchError(final TelematicsError.Type type, final String message) {
        manager.mainHandler.post(new Runnable() {
            @Override
            public void run() {
                final TelematicsError error = new TelematicsError(type, message);
                sendingCommand = false;
                callback.onCommandFailed(error);
            }
        });
    }

    public static class TelematicsError {
        public enum Type { TIMEOUT, ERROR }
        Type type;
        String message;

        TelematicsError(Type type, String message) {
            this.type = type;
            this.message = message;
        }

        public Type getType() {
            return type;
        }

        public String getMessage() {
            return message;
        }
    }
}
