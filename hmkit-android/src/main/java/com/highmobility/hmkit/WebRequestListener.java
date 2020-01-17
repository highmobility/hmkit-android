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

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

import static com.highmobility.hmkit.HMLog.d;

// This class logs out the response for all requests
class WebRequestListener {
    InternalResponseListener response = new InternalResponseListener();
    InternalErrorListener error = new InternalErrorListener();

    public void onResponse(JSONObject jsonObject) {

    }

    public void onError(VolleyError error) {

    }

    class InternalResponseListener implements Response.Listener<JSONObject> {
        @Override public void onResponse(JSONObject jsonObject) {
            printResponse(jsonObject);
            WebRequestListener.this.onResponse(jsonObject);
        }

        void printResponse(JSONObject jsonObject) {
            if (Timber.treeCount() == 0) return;
            try {
                String message = jsonObject.toString(2);
                d("response %s", message);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class InternalErrorListener implements Response.ErrorListener {
        @Override public void onErrorResponse(VolleyError error) {
            printError(error);
            WebRequestListener.this.onError(error);
        }

        void printError(VolleyError error) {
            if (Timber.treeCount() == 0) return;
            if (error.networkResponse != null) {
                String responseData = "";
                if (error.networkResponse.data != null)
                    responseData = new String(error.networkResponse.data);
                d("\nerror %d, %s", error.networkResponse.statusCode, responseData);
            }
        }
    }
}