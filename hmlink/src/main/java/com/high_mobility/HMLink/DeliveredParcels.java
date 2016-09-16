package com.high_mobility.HMLink;

import android.util.Log;

import java.util.Arrays;

/**
 * Created by ttiganik on 13/09/16.
 */
public class DeliveredParcels extends Incoming {
    static final String TAG = "DeliveredParcels";
    String[] deliveredParcels;

    public DeliveredParcels(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length < 3) throw new CommandParseException();

        int count = bytes[2];

        if (count < 1) return;

        if (bytes.length < 3 + count * 8) throw new CommandParseException();

        deliveredParcels = new String[count];
        for (int i = 0; i < count; i++) {
            byte[] identifier = Arrays.copyOfRange(bytes, 3 + i * 8, 3 + i * 8 + 8);
            deliveredParcels[i] = ByteUtils.hexFromBytes(identifier);
        }
    }

    public String[] getDeliveredParcels() {
        return deliveredParcels;
    }
}
