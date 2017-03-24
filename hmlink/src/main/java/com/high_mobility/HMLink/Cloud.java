package com.high_mobility.HMLink;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ttiganik on 24/03/2017.
 */

/*
// create access token
$ curl  -H "Content-Type: application/json"
        -H "Authorization: Bearer ***REMOVED***"
        -X POST
        -d '{"serial_number": "C178323BE047EA9581"}'
        https://api.high-mobility.com/hm_cloud/api/v1/38e3a98e-0c99-41ca-bbef-185822a3b431/access_certificates
{
    "data": {
        "vehicle_access_certificate":"***REMOVED***",
        "device_access_certificate":"***REMOVED***"
    }
}
*/

class Cloud {
    private static final String telematicsServiceIdentifier = "38e3a98e-0c99-41ca-bbef-185822a3b431";
    private static final String baseUrl = "https://api.high-mobility.com";
    private static final String apiUrl = "hm_cloud/api/v1";

    private static final Map<String, String> headers;
    static {
        headers = new HashMap<>(2);
        headers.put("Content-Type", "application/json");
        // TODO: get correct token
        headers.put("Authorization", "Bearer ***REMOVED***");
    }

    RequestQueue queue;

//    private static var kJWTPayloadDict: (String) -> [String : String]   = { ["access_token" : $0] }

    Cloud(Context context) {
        queue = Volley.newRequestQueue(context);
    }

    void requestAccessCertificate(String accessToken, String telematicsServiceIdentifier, Response.Listener<JSONObject> response, Response.ErrorListener error) throws IllegalArgumentException {
        String url = baseUrl + "/" + apiUrl + "/" + telematicsServiceIdentifier + "/access_certificates";

        JSONObject payload = new JSONObject();
        try {
            payload.put("access_token", accessToken);
        } catch (JSONException e) {
            throw new IllegalArgumentException();
        }

        JsonObjectRequest request = new JsonObjectRequest (Request.Method.POST, url, payload, response, error) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };

        queue.add(request);
    }
}
