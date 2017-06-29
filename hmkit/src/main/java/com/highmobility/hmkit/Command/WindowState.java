package com.highmobility.hmkit.Command;

/**
 * Created by root on 6/29/17.
 */

public class WindowState {
    public enum Location {
        FRONT_LEFT, FRONT_RIGHT, REAR_LEFT, REAR_RIGHT;

        byte getByte() {
            switch (this) {
                case FRONT_LEFT:
                    return 0x00;
                case FRONT_RIGHT:
                    return 0x01;
                case REAR_RIGHT:
                    return 0x02;
                case REAR_LEFT:
                    return 0x03;
            }

            return 0x00;
        }
    }

    public enum Position {
        OPEN, CLOSED;

        byte getByte() {
            switch (this) {
                case OPEN:
                    return 0x01;
                case CLOSED:
                    return 0x00;
            }

            return 0x00;
        }
    }

    Location location;
    Position position;

    public Location getLocation() {
        return location;
    }

    public Position getPosition() {
        return position;
    }

    public WindowState(Location location, Position position) {
        this.location = location;
        this.position = position;
    }

    public byte[] getBytes() {
        return new byte[] { location.getByte(), position.getByte() };
    }
}
