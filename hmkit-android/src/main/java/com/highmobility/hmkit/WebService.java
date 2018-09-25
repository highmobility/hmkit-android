package com.highmobility.hmkit;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.highmobility.crypto.Crypto;
import com.highmobility.crypto.value.DeviceSerial;
import com.highmobility.crypto.value.Issuer;
import com.highmobility.crypto.value.PrivateKey;
import com.highmobility.value.Bytes;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

class WebService {
    private static final String testBaseUrl = "https://limitless-gorge-44605.herokuapp.com"; // test
    private static final String productionBaseUrl = "https://developers.high-mobility.com"; //
    // production
    private static final String stagingBaseUrl = "https://developers.h-m.space"; // staging
    private static final String apiUrl = "/hm_cloud/api/v1";

    private static String telematicsUrl;

    private final RequestQueue queue;

    WebService(Context context) {
        // ignoreSslErrors();
        queue = Volley.newRequestQueue(context);
        if (HmKit.customEnvironmentBaseUrl != null) {
            telematicsUrl = HmKit.customEnvironmentBaseUrl + apiUrl;
        } else {
            switch (HmKit.environment) {
                case TEST:
                    telematicsUrl = testBaseUrl + apiUrl;
                    break;
                case STAGING:
                    telematicsUrl = stagingBaseUrl + apiUrl;
                    break;
                case PRODUCTION:
                    telematicsUrl = productionBaseUrl + apiUrl;
                    break;
            }
        }
    }

    void requestAccessCertificate(String accessToken,
                                  PrivateKey privateKey,
                                  DeviceSerial serialNumber,
                                  final Response.Listener<JSONObject> response,
                                  Response.ErrorListener error) throws IllegalArgumentException {
        String url = telematicsUrl + "/access_certificates";

        // headers
        final Map<String, String> headers = new HashMap<>(1);
        headers.put("Content-Type", "application/json");

        // payload
        JSONObject payload = new JSONObject();
        try {
            Bytes accessTokenBytes = new Bytes(accessToken.getBytes("UTF-8"));
            payload.put("serial_number", serialNumber.getHex());
            payload.put("access_token", accessToken);
            payload.put("signature", Crypto.sign(accessTokenBytes, privateKey).getBase64());
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, payload, new
                Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        if (HmKit.loggingLevel.getValue() >= HmKit.LoggingLevel.DEBUG
                                .getValue()) {
                            try {
                                HmLog.d("response " + jsonObject
                                        .toString(2));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        response.onResponse(jsonObject);
                    }
                }, error) {
            @Override
            public Map<String, String> getHeaders() {
                return headers;
            }
        };

        queueRequest(request);
    }

    void sendTelematicsCommand(Bytes command, DeviceSerial serial, Issuer issuer, final Response
            .Listener<JSONObject> response, Response.ErrorListener error) {
        String url = telematicsUrl + "/telematics_commands";
        // headers
        final Map<String, String> headers = new HashMap<>(1);
        headers.put("Content-Type", "application/json");

        // payload
        JSONObject payload = new JSONObject();
        try {
            payload.put("serial_number", serial.getHex());
            payload.put("issuer", issuer.getHex());
            payload.put("data", command.getBase64());
        } catch (JSONException e) {
            throw new IllegalArgumentException();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, payload, new
                Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        if (HmKit.loggingLevel.getValue() >= HmKit.LoggingLevel.DEBUG
                                .getValue()) {
                            try {
                                HmLog.d("response " + jsonObject
                                        .toString(2));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        response.onResponse(jsonObject);
                    }
                }, error) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };

        queueRequest(request);
    }

    void getNonce(DeviceSerial serial, final Response.Listener<JSONObject> response, Response
            .ErrorListener error) {
        String url = telematicsUrl + "/nonces";

        // headers
        final Map<String, String> headers = new HashMap<>(1);
        headers.put("Content-Type", "application/json");

        // payload
        JSONObject payload = new JSONObject();
        try {
            payload.put("serial_number", serial.getHex());
        } catch (JSONException e) {
            throw new IllegalArgumentException();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, payload, new
                Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        if (HmKit.loggingLevel.getValue() >= HmKit.LoggingLevel.DEBUG
                                .getValue()) {
                            try {
                                HmLog.d("response " + jsonObject
                                        .toString(2));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        response.onResponse(jsonObject);
                    }
                }, error) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };

        queueRequest(request);
    }

    private void queueRequest(JsonObjectRequest request) {
        request.setTag(this);
        printRequest(request);
        queue.add(request);
    }

    void cancelAllRequests() {
        queue.cancelAll(this);
    }

    private static void printRequest(JsonRequest request) {
        if (HmKit.loggingLevel.getValue() < HmKit.LoggingLevel.DEBUG.getValue()) return;
        try {
            byte[] body = request.getBody();
            String bodyString = body != null ? "\n" + new String(request.getBody()) : "";
            JSONObject headers = new JSONObject(request.getHeaders());

            try {
                HmLog.d(request.getUrl() + "\n" + headers.toString(2) + bodyString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
        }
    }
}
