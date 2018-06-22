package com.highmobility.hmkit;

import android.content.Context;
import android.util.Log;

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

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by ttiganik on 24/03/2017.
 */
class WebService {
    private static final String TAG = "HMKit-WebService";

    private static final String testBaseUrl = "https://limitless-gorge-44605.herokuapp.com"; // test
    private static final String productionBaseUrl = "https://developers.high-mobility.com"; //
    // production
    private static final String stagingBaseUrl = "https://developers.h-m.space"; // staging
    private static final String apiUrl = "/hm_cloud/api/v1";

    private static String telematicsUrl;

    RequestQueue queue;

    WebService(Context context) {
        // ignoreSslErrors();
        queue = Volley.newRequestQueue(context);
        if (Manager.customEnvironmentBaseUrl != null) {
            telematicsUrl = Manager.customEnvironmentBaseUrl + apiUrl;
        } else {
            switch (Manager.environment) {
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
                if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue()) {
                    try {
                        Log.d(TAG, "response " + jsonObject.toString(2));
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
                if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue()) {
                    try {
                        Log.d(TAG, "response " + jsonObject.toString(2));
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
                if (Manager.loggingLevel.getValue() >= Manager.LoggingLevel.DEBUG.getValue()) {
                    try {
                        Log.d(TAG, "response " + jsonObject.toString(2));
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

    void queueRequest(JsonObjectRequest request) {
        request.setTag(this);
        printRequest(request);
        queue.add(request);
    }

    void cancelAllRequests() {
        queue.cancelAll(this);
    }

    private static void ignoreSslErrors() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                            return myTrustedAnchors;
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception e) {
        }
    }

    private static void printRequest(JsonRequest request) {
        if (Manager.loggingLevel.getValue() < Manager.LoggingLevel.DEBUG.getValue()) return;
        try {
            byte[] body = request.getBody();
            String bodyString = body != null ? "\n" + new String(request.getBody()) : "";
            JSONObject headers = new JSONObject(request.getHeaders());

            try {
                Log.d(TAG, request.getUrl().toString() + "\n" + headers.toString(2) + bodyString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
        }
    }
}
