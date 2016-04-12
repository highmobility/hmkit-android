package com.high_mobility.digitalkey;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

/**
 * Created by ttiganik on 08/04/16.
 */
public class Constants {
    private static final byte[] appID = hexToBytes("***REMOVED***");
    private static final byte[] issuer = hexToBytes("47494d4f");

    public static UUID SERVICE_UUID = UUID.fromString("713D0100-503E-4C75-BA94-3148F18D941E");

    public static UUID READ_CHAR_UUID = UUID.fromString("713D0102-503E-4C75-BA94-3148F18D941E");

    public static UUID WRITE_CHAR_UUID = UUID.fromString("713D0103-503E-4C75-BA94-3148F18D941E");

    public static UUID PING_CHAR_UUID = UUID.fromString("713D0104-503E-4C75-BA94-3148F18D941E");

    public static UUID ADVERTISE_UUID = UUIDFromByteArray(concatBytes(issuer, appID));



    public static String getStateDescription(int state) {

        switch (state) {
            case BluetoothProfile.STATE_CONNECTED:
                return "Connected";
            case BluetoothProfile.STATE_CONNECTING:
                return "Connecting";
            case BluetoothProfile.STATE_DISCONNECTED:
                return "Disconnected";
            case BluetoothProfile.STATE_DISCONNECTING:
                return "Disconnecting";
            default:
                return "Unknown State "+state;
        }
    }

    public static String getStatusDescription(int status) {
        switch (status) {
            case BluetoothGatt.GATT_SUCCESS:
                return "SUCCESS";
            default:
                return "Unknown Status "+status;
        }
    }

    public static UUID UUIDFromByteArray(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long high = bb.getLong();
        long low = bb.getLong();
        UUID uuid = new UUID(high, low);
        return uuid;
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String hexFromBytes(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    public static byte[] concatBytes(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }
}
