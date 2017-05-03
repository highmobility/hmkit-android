package com.high_mobility.HMLink;

import android.util.Base64;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.high_mobility.HMLink.Crypto.AccessCertificate;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by root on 03/05/2017.
 */

public class Telematics {
    public interface TelematicsResponseCallback {
        void response(TelematicsResponse response);
    }

    public enum TelematicsResponseStatus { OK, TIMEOUT, ERROR }

    Manager manager;
    TelematicsResponseCallback callback;
    AccessCertificate certificate; // TODO: delete if not necessary
    TelematicsResponse response;

    Telematics(Manager manager) {
        this.manager = manager;
    }

    // TODO: comment
    /**
     * response
     * @param command
     * @param certificate
     * @param callback
     */
    public void sendTelematicsCommand(byte[] command, AccessCertificate certificate, final TelematicsResponseCallback callback) {
        if (this.certificate != null) {
            TelematicsResponse response = new TelematicsResponse();
            response.status = TelematicsResponseStatus.ERROR;
            response.message = "Already sending a command";
            callback.response(response);
            return;
        }

        this.certificate = certificate;
        this.callback = callback;
        manager.core.HMBTCoreSendTelematicsCommand(certificate.getProviderSerial(), command.length, command);
    }

    void onTelematicsCommandEncrypted(byte[] serial, byte[] command) {
        manager.webService.sendTelematicsCommand(command, serial, certificate.getIssuer(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            TelematicsResponse response = TelematicsResponse.fromResponse(jsonObject);
                            manager.core.HMBTCoreTelematicsReceiveData(response.data.length, response.data);
                        } catch (JSONException e) {
                            TelematicsResponse response = new TelematicsResponse();
                            response.status = TelematicsResponseStatus.ERROR;
                            response.message = "Invalid response from server.";
                            callback.response(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        TelematicsResponse response = new TelematicsResponse();
                        response.status = TelematicsResponseStatus.ERROR;

                        if (error.networkResponse != null) {
                            response.message = "HTTP error " + error.networkResponse.statusCode;
                        }
                        else {
                            response.message = "Cannot connect to the web service. Check your internet connection";
                        }

                        callback.response(response);
                    }
                });
    }

    void onTelematicsResponseDecrypted(byte[] serial, byte id, byte[] data) {
        /*
        @Override
    public void HMApiCallbackTelematicsCommandIncoming(HMDevice device, int id, int length, byte[] data) {
        // TODO TELEMATICS
        //Siia tuleb sisse callback kus on data lahti krüptitud
        //device omab serial numbrit kust data tuli
        //id on sissetuleva commandi id. Hetkel on kas crypto container 0x36 või siis error 0x02
        //Data on sissetulev data. Kui error siis error pakett
         */
        if (id == 0x02) {
            response.status = TelematicsResponseStatus.ERROR;
            response.message = "Failed to decrypt web service response.";
        }
        else {
            response.status = TelematicsResponseStatus.OK;
            response.data = data;
        }

        this.certificate = null;
        callback.response(response);
    }

    public static class TelematicsResponse {
        public TelematicsResponseStatus status;
        public String message;
        public byte[] data;

        static TelematicsResponse fromResponse(JSONObject jsonObject) throws JSONException {
            TelematicsResponse response = new TelematicsResponse();
            String status = jsonObject.getString("status");
            if (status == "ok") {
                response.status = TelematicsResponseStatus.OK;
            }
            else if (status == "timeout") {
                response.status = TelematicsResponseStatus.TIMEOUT;
            }
            else if (status == "error") {
                response.status = TelematicsResponseStatus.ERROR;
            }

            response.message = jsonObject.getString("message");
            response.data = Base64.decode(jsonObject.getString("response-data"), Base64.DEFAULT);
            return response;
        }
    }
}
