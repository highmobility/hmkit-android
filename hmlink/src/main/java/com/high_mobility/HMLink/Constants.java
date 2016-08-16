package com.high_mobility.HMLink;

public class Constants {
    public static final float registerTimeout           = 6.0f;
    public static final float commandTimeout            = 10.0f;
    public static final int certificateStorageCount     = 30;

    public interface ApprovedCallback {
        void approve();
        void decline();
    }

    public interface DataResponseCallback {
        void response(byte[] bytes, LinkException exception);
    }

    public interface RSSICallback {
        void onRSSIRead(int RSSI);
    }
}
