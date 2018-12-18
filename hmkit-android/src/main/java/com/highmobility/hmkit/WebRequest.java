package com.highmobility.hmkit;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

class WebRequest extends Request<JSONObject> {
    private static final Map<String, String> headers;

    static {
        headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
    }

    private Response.Listener<JSONObject> listener;
    private Map<String, String> params;

    void print() {
        if (HMKit.loggingLevel.getValue() < HMLog.Level.DEBUG.getValue()) return;
        try {
            byte[] body = getBody();
            String bodyString = body != null ? "\nbody:\n" + new String(getBody()) : "";
            JSONObject headers = new JSONObject(getHeaders());
            String log = "\n" + getUrl() + "\nheaders:\n" + headers.toString(2) + bodyString;
            HMLog.d(URLDecoder.decode(log, "ASCII"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WebRequest(int method, String url, Map<String, String> params,
                      Response.Listener<JSONObject> responseListener,
                      Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.listener = responseListener;
        this.params = params;
    }

    protected Map<String, String> getParams() {
        return params;
    }

    @Override public Map<String, String> getHeaders() {
        return headers;
    }

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
        listener.onResponse(response);
    }
}