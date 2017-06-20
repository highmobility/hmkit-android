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

    public interface TelematicsResponseCallback {
        void response(TelematicsResponse response);
    }

    public enum TelematicsResponseStatus { OK, TIMEOUT, ERROR }

    Manager manager;
    TelematicsResponseCallback callback;
    TelematicsResponse response;
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
    public void sendTelematicsCommand(final byte[] command, final AccessCertificate certificate, final TelematicsResponseCallback callback) {
        if (sendingCommand == true) {
            TelematicsResponse response = new TelematicsResponse();
            response.status = TelematicsResponseStatus.ERROR;
            response.message = "Already sending a command";
            callback.response(response);
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
                    dispatchError("Invalid nonce response from server.", callback);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    dispatchError("HTTP error " + new String(error.networkResponse.data), callback);
                }
                else {
                    dispatchError("Cannot connect to the web service. Check your internet connection", callback);
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
                            response = TelematicsResponse.fromResponse(jsonObject);
                            manager.workHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    manager.core.HMBTCoreTelematicsReceiveData(manager.coreInterface, response.data.length, response.data);
                                }
                            });
                        } catch (JSONException e) {
                            dispatchError("Invalid response from server.", callback);
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
                                    dispatchError(message + json.getString("message"), callback);
                                }
                                else {
                                    dispatchError(message + new String(error.networkResponse.data), callback);
                                }
                            } catch (JSONException e) {
                                dispatchError(message + new String(error.networkResponse.data), callback);
                            }
                        }
                        else {
                            dispatchError("Cannot connect to the web service. Check your internet connection", callback);
                        }
                    }
                });
    }

    void onTelematicsResponseDecrypted(byte[] serial, byte id, byte[] data) {
        if (id == 0x02) {
            response.status = TelematicsResponseStatus.ERROR;
            response.message = "Failed to decrypt web service response.";
        }
        else {
            response.status = TelematicsResponseStatus.OK;
            response.data = data;
        }

        if (manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue())
            Log.d(TAG, "onTelematicsResponseDecrypted: " + ByteUtils.hexFromBytes(data));

        sendingCommand = false;
        manager.mainHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.response(response);
            }
        });
    }

    void dispatchError(String message, final TelematicsResponseCallback callback) {
        final TelematicsResponse response = new TelematicsResponse();
        response.status = TelematicsResponseStatus.ERROR;
        response.message = message;
        sendingCommand = false;
        manager.mainHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.response(response);
            }
        });
    }

    public static class TelematicsResponse {
        public TelematicsResponseStatus status;
        public String message;
        public byte[] data;

        static TelematicsResponse fromResponse(JSONObject jsonObject) throws JSONException {
            TelematicsResponse response = new TelematicsResponse();
            String status = jsonObject.getString("status");

            if (status.equals("ok")) {
                response.status = TelematicsResponseStatus.OK;
            }
            else if (status.equals("timeout")) {
                response.status = TelematicsResponseStatus.TIMEOUT;
            }
            else if (status.equals("error")) {
                response.status = TelematicsResponseStatus.ERROR;
            }

            response.message = jsonObject.getString("message");
            response.data = Base64.decode(jsonObject.getString("response_data"), Base64.NO_WRAP);

            return response;
        }
    }
}
