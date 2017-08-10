package com.highmobility.hmkit;

import com.highmobility.hmkit.Command.CommandParseException;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Created by ttiganik on 08/04/16.
 */
public class ByteUtils {
    final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String hexFromBytes(byte[] bytes) {
        if (bytes == null) return "(null)";
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] bytesFromHex(String s) {
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

    public static byte[] concatBytes(byte[] a, byte b) {
        int aLen = a.length;

        byte[] c = new byte[aLen + 1];
        System.arraycopy(a, 0, c, 0, aLen);
        c[c.length - 1] = b;

        return c;
    }

    public static void setBytes(byte[] inArray, byte[] toBytes, int offset) {
        for (int i = offset; i < offset + toBytes.length; i++) {
            if (i > inArray.length - 1) return;
            inArray[offset + (i - offset)] = toBytes[i - offset];
        }
    }

    /**
     * Does this byte array begin with match array content?
     *
     * @param source
     *          Byte array to examine
     * @param match
     *          Byte array to locate in <code>source</code>
     * @return true If the starting bytes are equal
     */
    public static boolean startsWith(byte[] source, byte[] match) {
        return startsWith(source, 0, match);
    }

    /**
     * Does this byte array begin with match array content?
     *
     * @param source
     *          Byte array to examine
     * @param offset
     *          An offset into the <code>source</code> array
     * @param match
     *          Byte array to locate in <code>source</code>
     * @return true If the starting bytes are equal
     */
    public static boolean startsWith(byte[] source, int offset, byte[] match) {

        if (match.length > (source.length - offset)) {
            return false;
        }

        for (int i = 0; i < match.length; i++) {
            if (source[offset + i] != match[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean getBit(int n, int k) {
        return ((n >> k) & 1) == 1;
    }

    public static float getFloat(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getFloat();
    }

    public static int getInt(byte[] bytes) {
        if (bytes.length == 3) {
            int result = (bytes[0] & 0xff) << 16 | (bytes[1] & 0xff) << 8 | (bytes[2] & 0xff);
            return result;
        }
        else if (bytes.length == 2) {
            int result = ((bytes[0] & 0xff) << 8) | (bytes[1] & 0xff);
            return result;
        }

        return ByteBuffer.wrap(bytes).getInt();
    }

    public static int getInt(byte value) {
        return (int)value;
    }

    public static byte[] intToTwoBytes(int value) throws IllegalArgumentException {
        byte[] bytes = BigInteger.valueOf(value).toByteArray();

        if (bytes.length > 2) {
            throw new IllegalArgumentException();
        }
        else if (bytes.length == 2) {
            return bytes;
        }
        else {
            byte[] result = new byte[2];
            result[1] = bytes[0];
            return result;
        }
    }

    public static boolean getBool(byte value) {
        return value == 0x00 ? false : true;
    }

    public static byte getByte(boolean value) {
        return (byte) (value == false ? 0x00 : 0x01);
    }

    public static String getString(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "parse error";
        }
    }

    // 5 bytes eg yy mm dd mm ss. year is from 2000
    public static Date getDate(byte[] bytes) throws CommandParseException {
        for (int i = 0; i < bytes.length; i++) {
            if (i == bytes.length - 1 && bytes[i] == 0x00) return null; // all bytes are 0x00
            else if (bytes[i] != 0x00) break; // one byte is not 0x00, some date is set
        }

        Calendar c = Calendar.getInstance();

        if (bytes.length == 5) {
            c.set(2000 + bytes[0], bytes[1] - 1, bytes[2], bytes[3], bytes[4], 0x00);
        }
        else if (bytes.length == 6) {
            c.set(2000 + bytes[0], bytes[1] - 1, bytes[2], bytes[3], bytes[4], bytes[5]);
        }
        else {
            throw new CommandParseException();
        }

        return c.getTime();
    }

    static UUID UUIDFromByteArray(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long high = bb.getLong();
        long low = bb.getLong();
        UUID uuid = new UUID(high, low);
        return uuid;
    }

    static byte[] bytesFromMacString(String mac) {
        String[] macAddressParts = mac.split(":");

        // convert hex string to byte values
        byte[] macAddressBytes = new byte[6];
        for(int i = 0; i < 6; i++){
            Integer hex = Integer.parseInt(macAddressParts[i], 16);
            macAddressBytes[i] = hex.byteValue();
        }

        return macAddressBytes;
    }

    static byte[] longToBytes(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= 8;
        }
        return result;
    }

    static long bytesToLong(byte[] b) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }


    static void reverse(byte[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }
}
