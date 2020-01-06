/*
 * The MIT License
 *
 * Copyright (c) 2014- High-Mobility GmbH (https://high-mobility.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.highmobility.hmkit;

import android.content.Context;
import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.highmobility.crypto.Crypto;
import com.highmobility.crypto.value.DeviceSerial;
import com.highmobility.crypto.value.Issuer;
import com.highmobility.crypto.value.PrivateKey;
import com.highmobility.value.Bytes;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

class WebService {
    private static final String defaultUrl = "https://sandbox.api.high-mobility.com";
    private static final String testUrl = defaultUrl;
    private static final String hmxvUrl = "https://api.high-mobility.com";
    private static final String apiUrl = "/v1";

    static final String NO_CONNECTION_ERROR = "Cannot connect to the web service. Check your " +
            "internet connection.";

    private static final byte[] testIssuer = new byte[]{0x74, 0x65, 0x73, 0x74};
    private static final byte[] xvIssuer = new byte[]{0x78, 0x76, 0x68, 0x6D};

    private String baseUrl;
    private final RequestQueue queue;
    private final Crypto crypto;

    WebService(Context context, Crypto crypto, Issuer issuer, @Nullable String customUrl) {
        // ignoreSslErrors();
        queue = Volley.newRequestQueue(context);
        this.crypto = crypto;
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

    void downloadOauthAccessToken(String url, String code, String redirectUri,
                                  String clientId, String jwt,
                                  final WebRequestListener response) {
        Uri uri = Uri.parse(url).buildUpon().build();
        Map<String, String> params = new HashMap();

        // payload
        params.put("grant_type", "authorization_code");
        params.put("code", code);
        params.put("redirect_uri", redirectUri);
        params.put("client_id", clientId);
        params.put("code_verifier", jwt);

        WebRequest request = new WebRequest(Request.Method.POST, uri.toString(), params,
                response.response, response.error);

        queueRequest(request);
    }

    void refreshOauthAccessToken(String url,
                                 String clientId,
                                 String refreshToken,
                                 WebRequestListener response) {
        Uri uri = Uri.parse(url).buildUpon().build();
        Map<String, String> params = new HashMap();

        params.put("grant_type", "refresh_token");
        params.put("client_id", clientId);
        params.put("refresh_token", refreshToken);

        WebRequest request = new WebRequest(Request.Method.POST, uri.toString(), params,
                response.response, response.error);

        queueRequest(request);
    }

    void requestAccessCertificate(final String accessToken,
                                  PrivateKey privateKey,
                                  final DeviceSerial serialNumber,
                                  WebRequestListener response) {
        String url = baseUrl + "/access_certificates";
        Bytes accessTokenBytes = new Bytes(accessToken.getBytes());
        final String signature = crypto.sign(accessTokenBytes, privateKey).getBase64();

        Uri uri = Uri.parse(url).buildUpon().build();

        Map<String, String> params = new HashMap<>();
        params.put("serial_number", serialNumber.getHex());
        params.put("access_token", accessToken);
        params.put("signature", signature);

        WebRequest request = new WebRequest(Request.Method.POST, uri.toString(), params,
                response.response, response.error);

        queueRequest(request);
    }

    void sendTelematicsCommand(Bytes command, DeviceSerial serial, Issuer issuer,
                               WebRequestListener response) {
        String url = baseUrl + "/telematics_commands";

        Uri uri = Uri.parse(url).buildUpon().build();

        Map<String, String> params = new HashMap<>();
        params.put("serial_number", serial.getHex());
        params.put("issuer", issuer.getHex());
        params.put("data", command.getBase64());

        WebRequest request = new WebRequest(Request.Method.POST, uri.toString(), params,
                response.response, response.error);

        queueRequest(request);
    }

    void getNonce(DeviceSerial serial, WebRequestListener response) {
        String url = baseUrl + "/nonces";

        // query
        Uri uri = Uri.parse(url).buildUpon().build();
        Map<String, String> params = new HashMap<>();
        params.put("serial_number", serial.getHex());

        WebRequest request = new WebRequest(Request.Method.POST, uri.toString(), params,
                response.response, response.error);

        queueRequest(request);
    }

    private void queueRequest(WebRequest request) {
        request.setTag(this);
        request.print();
        queue.add(request);
    }

    void cancelAllRequests() {
        queue.cancelAll(this);
    }
}