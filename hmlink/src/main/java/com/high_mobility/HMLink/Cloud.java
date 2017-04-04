package com.high_mobility.HMLink;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.high_mobility.HMLink.Crypto.Crypto;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
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
class Cloud {
    private static final String TAG = "Cloud";

    private static final String baseUrl = "https://console.h-m.space";
    private static final String apiUrl = "/api/v1";

    private static final Map<String, String> jwtHeaders;
    public static String telematicsServiceIdentifier = "38e3a98e-0c99-41ca-bbef-185822a3b431";

    static {
        jwtHeaders = new HashMap<>(1);
        jwtHeaders.put("alg", "ES256");
    }

    RequestQueue queue;

    Cloud(Context context) {
        ignoreSslErrors(); // TODO: delete at some point
        queue = Volley.newRequestQueue(context);
    }

    void requestAccessCertificate(String accessToken,
                                  String telematicsServiceIdentifier,
                                  byte[] privateKey,
                                  byte[] serialNumber,
                                  final Response.Listener<JSONObject> response,
                                  Response.ErrorListener error) throws IllegalArgumentException {
        String url = baseUrl + "/" + apiUrl + "/" + telematicsServiceIdentifier + "/access_certificates";

        // headers
        final Map<String, String> headers = new HashMap<>(2);
        headers.put("Content-Type", "application/json");
        try {
            headers.put("Authorization", "Bearer " + getJwtField(accessToken, privateKey));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            error.onErrorResponse(new VolleyError("cannot create jwt"));
        }

        // payload
        JSONObject payload = new JSONObject();
        try {
            payload.put("serial_number", ByteUtils.hexFromBytes(serialNumber));
        } catch (JSONException e) {
            throw new IllegalArgumentException();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, payload, new Response.Listener<JSONObject>() {
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

        printRequest(request);
        queue.add(request);
    }

    private String getJwtField(String accessToken, byte[] privateKey) throws UnsupportedEncodingException {
        String jwt = "";

        // add default headers
        JSONObject headers = new JSONObject(jwtHeaders);
        jwt += Base64.encodeToString(headers.toString().getBytes("utf-8"), Base64.NO_WRAP | Base64.NO_PADDING);

        // add payload
        String payLoad = "{\"access_token\":\"" + accessToken + "\"}";
        String payLoadEncoded = Base64.encodeToString(payLoad.getBytes("utf-8"), Base64.NO_WRAP | Base64.NO_PADDING);
        jwt += "." + payLoadEncoded;

        // add signature
        byte[] signedBytes = payLoadEncoded.getBytes("utf-8");
        byte[] signature = Crypto.sign(signedBytes, privateKey);
        String signatureString = Base64.encodeToString(signature, Base64.NO_WRAP | Base64.NO_PADDING);
        jwt += "." + signatureString;

        return jwt;
    }

    private static void ignoreSslErrors() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                            return myTrustedAnchors;
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
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
            String bodyString = body != null ? "\n" + new String(request.getBody()): "";
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
