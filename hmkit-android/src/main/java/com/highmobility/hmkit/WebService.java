package com.highmobility.hmkit;

import android.content.Context;
import android.net.Uri;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.highmobility.crypto.Crypto;
import com.highmobility.crypto.value.DeviceSerial;
import com.highmobility.crypto.value.Issuer;
import com.highmobility.crypto.value.PrivateKey;
import com.highmobility.value.Bytes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

class WebService {
    private static final String defaultUrl = "https://sandbox.api.high-mobility.com";
    private static final String testUrl = defaultUrl;
    private static final String hmxvUrl = "https://api.high-mobility.com";
    private static final String apiUrl = "/v1";

    private static final byte[] testIssuer = new byte[]{0x74, 0x65, 0x73, 0x74};
    private static final byte[] xvIssuer = new byte[]{0x78, 0x76, 0x68, 0x6D};

    private String baseUrl;
    private final RequestQueue queue;

    WebService(Context context, Issuer issuer, @Nullable String customUrl) {
        // ignoreSslErrors();
        queue = Volley.newRequestQueue(context);
        setIssuer(issuer, customUrl);
    }

    void setIssuer(Issuer issuer, @Nullable String customUrl) {
        if (customUrl != null) {
            baseUrl = customUrl;
        } else if (issuer.equals(testIssuer)) {
            baseUrl = testUrl;
        } else if (issuer.equals(xvIssuer)) {
            baseUrl = hmxvUrl;
        } else {
            baseUrl = defaultUrl;
        }

        baseUrl += apiUrl;
    }

    void requestAccessCertificate(final String accessToken,
                                  PrivateKey privateKey,
                                  final DeviceSerial serialNumber,
                                  final Response.Listener<JSONObject> response,
                                  Response.ErrorListener error) throws IllegalArgumentException {
        String url = baseUrl + "/access_certificates";
        Bytes accessTokenBytes = new Bytes(accessToken.getBytes());
        final String signature = Crypto.sign(accessTokenBytes, privateKey).getBase64();
        // headers
        final Map<String, String> headers = new HashMap<>(1);
        headers.put("Content-Type", "application/json");

        Uri uri = Uri.parse(url)
                .buildUpon()
                /*.appendQueryParameter("serial_number", serialNumber.getHex())
                .appendQueryParameter("access_token", accessToken)
                .appendQueryParameter("signature", signature)*/
                .build();

        final JSONObject body = new JSONObject();

        try {
            body.put("serial_number", serialNumber.getHex());
            body.put("access_token", accessToken);
            body.put("signature", signature);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Request<JSONObject> request = new JsonObjectRequest(Request.Method.POST, uri.toString(),
                body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (HMKit.loggingLevel.getValue() >= HMLog.Level.DEBUG.getValue()) {
                    try {
                        HMLog.d("response " + jsonObject.toString(2));
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

            /*@Override public byte[] getBody() {
                try {
                    return body.toString().getBytes("ASCII");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }*/

            /*@Override protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("serial_number", serialNumber.getHex());
                params.put("access_token", accessToken);
                params.put("signature", signature);
                try {
                    HMLog.d("getParams(): " + new JSONObject(params).toString(2));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return params;
            }*/
        };


        queueRequest(request);
    }

    void sendTelematicsCommand(Bytes command, DeviceSerial serial, Issuer issuer, final Response
            .Listener<JSONObject> response, Response.ErrorListener error) {
        String url = baseUrl + "/telematics_commands";
        // headers
        final Map<String, String> headers = new HashMap<>(1);
        headers.put("Content-Type", "application/json");

        Uri uri = Uri.parse(url)
                .buildUpon()
                /*.appendQueryParameter("serial_number", serial.getHex())
                .appendQueryParameter("issuer", issuer.getHex())
                .appendQueryParameter("data", command.getBase64())*/
                .build();

        JSONObject body = new JSONObject();
        try {
            body.put("serial_number", serial.getHex());
            body.put("issuer", issuer.getHex());
            body.put("data", command.getBase64());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, uri.toString(),
                body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (HMKit.loggingLevel.getValue() >= HMLog.Level.DEBUG.getValue()) {
                    try {
                        HMLog.d("response " + jsonObject.toString(2));
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

    void getNonce(DeviceSerial serial, final Response.Listener<JSONObject> response, Response
            .ErrorListener error) {
        String url = baseUrl + "/nonces";

        // headers
        final Map<String, String> headers = new HashMap<>(1);
        headers.put("Content-Type", "application/json");

        // query
        Uri uri = Uri.parse(url)
                .buildUpon()
                /*.appendQueryParameter("serial_number", serial.getHex())*/
                .build();

        JSONObject body = new JSONObject();
        try {
            body.put("serial_number", serial.getHex());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, uri.toString(),
                body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (HMKit.loggingLevel.getValue() >= HMLog.Level.DEBUG.getValue()) {
                    try {
                        HMLog.d("response " + jsonObject.toString(2));
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

    private void queueRequest(Request<JSONObject> request) {
        request.setTag(this);
        printRequest(request);
        queue.add(request);
    }

    void cancelAllRequests() {
        queue.cancelAll(this);
    }

    private static void printRequest(Request<JSONObject> request) {
        if (HMKit.loggingLevel.getValue() < HMLog.Level.DEBUG.getValue()) return;
        try {
            byte[] body = request.getBody();
            String bodyString = body != null ? "\n" + new String(request.getBody()) : "";
            JSONObject headers = new JSONObject(request.getHeaders());
            String log = request.getUrl() + "\n" + headers.toString(2) + bodyString;
            HMLog.d(URLDecoder.decode(log, "ASCII"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class CustomRequest extends Request<JSONObject> {

        private Listener<JSONObject> listener;
        private Map<String, String> params;

        public CustomRequest(String url, Map<String, String> params,
                             Listener<JSONObject> reponseListener, ErrorListener errorListener) {
            super(Method.GET, url, errorListener);
            this.listener = reponseListener;
            this.params = params;
        }

        public CustomRequest(int method, String url, Map<String, String> params,
                             Listener<JSONObject> reponseListener, ErrorListener errorListener) {
            super(method, url, errorListener);
            this.listener = reponseListener;
            this.params = params;
        }

        protected Map<String, String> getParams()
                throws com.android.volley.AuthFailureError {
            return params;
        }

        ;

        @Override
        protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
            try {
                String jsonString = new String(response.data,
                        HttpHeaderParser.parseCharset(response.headers));
                return Response.success(new JSONObject(jsonString),
                        HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (JSONException je) {
                return Response.error(new ParseError(je));
            }
        }

        @Override
        protected void deliverResponse(JSONObject response) {
            // TODO Auto-generated method stub
            listener.onResponse(response);
        }
    }
}
