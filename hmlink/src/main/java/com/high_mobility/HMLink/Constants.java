package com.high_mobility.HMLink;

public class Constants {

    public static final float registerTimeout      = 6.0f;

    public interface ApprovedCallback {
        void approve();
        void decline();
    }

    public interface DataResponseCallback {
        void response(byte[] bytes, LinkException exception);
    }
}
