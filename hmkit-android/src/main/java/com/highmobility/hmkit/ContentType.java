package com.highmobility.hmkit;

public enum ContentType {
    UNKNOWN(0), AUTO_API(1), VSS(2);

    public static ContentType fromInt(int intValue) {
        ContentType[] values = ContentType.values();

        for (int i = 0; i < values.length; i++) {
            ContentType state = values[i];
            if (state.asInt() == intValue) {
                return state;
            }
        }

        throw new IllegalArgumentException();
    }

    private int value;

    ContentType(int value) {
        this.value = value;
    }

    public int asInt() {
        return value;
    }
}