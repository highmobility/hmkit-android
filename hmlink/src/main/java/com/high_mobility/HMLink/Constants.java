package com.high_mobility.HMLink;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class Constants {
    static UUID SERVICE_UUID = UUID.fromString("713D0100-503E-4C75-BA94-3148F18D941E");
    static UUID READ_CHAR_UUID = UUID.fromString("713D0102-503E-4C75-BA94-3148F18D941E");
    static UUID WRITE_CHAR_UUID = UUID.fromString("713D0103-503E-4C75-BA94-3148F18D941E");
    static UUID ALIVE_CHAR_UUID = UUID.fromString("713D0104-503E-4C75-BA94-3148F18D941E");
    static UUID INFO_CHAR_UUID = UUID.fromString("713D0105-503E-4C75-BA94-3148F18D941E");
    static UUID SENSING_READ_CHAR_UUID = UUID.fromString("713D0106-503E-4C75-BA94-3148F18D941E");
    static UUID SENSING_WRITE_CHAR_UUID = UUID.fromString("713D0107-503E-4C75-BA94-3148F18D941E");


    static UUID NOTIFY_DESC_UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");

    public static final float registerTimeout           = 10.0f;
    public static final float commandTimeout            = 10.0f;
    public static final int certificateStorageCount     = 30;

    public interface ApprovedCallback {
        void approve();
        void decline();
    }

    public interface DataResponseCallback {
        void response(byte[] bytes, int errorCode);
    }

    public interface ResponseCallback {
        void response(int errorCode);
    }
}