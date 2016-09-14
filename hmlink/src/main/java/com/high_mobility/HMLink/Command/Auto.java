package com.high_mobility.HMLink.Command;

/**
 * Created by ttiganik on 13/09/16.
 */
public class Auto {
    public enum Type {
        //General
        GET_VEHICLE_STATUS((byte)0x18),
        VEHICLE_STATUS((byte)0x19);

        private byte value;

        Type(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }
}
