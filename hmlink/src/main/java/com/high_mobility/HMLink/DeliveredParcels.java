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

        if (bytes.length < 3 + count * 8) throw new CommandParseException(); // TODO: test

        deliveredParcels = new String[count];
        for (int i = 0; i < count; i++) {
            byte[] identifier = Arrays.copyOfRange(bytes, 3 + i * 8, 3 + i * 8 + 8);
            deliveredParcels[i] = ByteUtils.hexFromBytes(identifier);
        }
    }

    public String[] getDeliveredParcels() {
        return deliveredParcels;
    }

    static void test() {
        // TODO: move to test target
        byte[] bytes = ByteUtils.bytesFromHex("***REMOVED***");

        DeliveredParcels parcels = null;

        try {
            parcels = new DeliveredParcels(bytes);
        } catch (CommandParseException e) {
            Log.e(TAG, "init parse error");
            e.printStackTrace();
        }

        if (parcels.deliveredParcels.length != 2) Log.e(TAG, "invalid parcel count");

        if (parcels.deliveredParcels[0].equals("4B87EFA8B4A6EC08") == false) Log.e(TAG, "invalid parcel 1 " + parcels.deliveredParcels[0]);
        if (parcels.deliveredParcels[1].equals("4B87EFA8B4A6EC09") == false) Log.e(TAG, "invalid parcel 2 " + parcels.deliveredParcels[1]);

        Log.d(TAG, "parcels pass");
    }
}
