package com.highmobility.common;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

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
 * Created by ttiganik on 28/03/2017.
 */

public class Cloud {
    public interface Response {
        void error(int status, String message);
        void success(JSONObject jsonObject);
    }

    private static final String TAG = "Cloud";
//    private static final String baseUrl = "https://console.h-m.space"; // prod
    private static final String baseUrl = "https://od-console.h-m.space:4443"; // stage
    private static final String apiUrl = "api/v1";
    private static String url;

    RequestQueue queue;

    Cloud(Context context) {
        ignoreSslErrors(); // TODO: delete at some point
        queue = Volley.newRequestQueue(context);
        url = baseUrl + "/" + apiUrl;
    }

    public void login(String email, String password, final Response response) {
        String url = this.url + "/login";

        // headers
        final Map<String, String> headers = new HashMap<>(2);
        headers.put("Content-Type", "application/json");

        // payload
        JSONObject payload = new JSONObject();
        try {
            payload.put("email", email);
            payload.put("password", password);
        } catch (JSONException e) {
            response.error(-1, "invalid arguments");
        }

        com.android.volley.Response.Listener responseListener = new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                response.success(jsonObject);
            }
        };

        com.android.volley.Response.ErrorListener errorListener = new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    response.error(error.networkResponse.statusCode, "");
                }
                else {
                    response.error(-1, "connection error");
                }
            }
        };

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, payload, responseListener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };

        printRequest(request);
        queue.add(request);
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

    private static void printRequest(JsonObjectRequest request) {
        try {
            Log.d(TAG, request.getUrl().toString() + "\n" + request.getHeaders().toString() + "\n" + new String(request.getBody()));
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
        }
    }
}
