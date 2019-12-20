package com.highmobility.hmkit;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

import static com.highmobility.hmkit.HMLog.d;

// this class is to log out the response for all requests
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
                // TODO: 19.12.2019 now dont have to log this out in callbacks.
            }
        }
    }
}