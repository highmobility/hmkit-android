package com.highmobility.hmkit.Command;

import com.highmobility.hmkit.ByteUtils;

/**
 * Created by root on 6/29/17.
 */

public class WindscreenDamagePosition {
    int windscreenSizeHorizontal;
    int windscreenSizeVertical;
    int damagePositionX;
    int damagePositionY;

    public int getWindscreenSizeHorizontal() {
        return windscreenSizeHorizontal;
    }

    public int getWindscreenSizeVertical() {
        return windscreenSizeVertical;
    }

    public int getDamagePositionX() {
        return damagePositionX;
    }

    public int getDamagePositionY() {
        return damagePositionY;
    }

    public WindscreenDamagePosition(int windscreenSizeHorizontal, int windscreenSizeVertical, int damagePositionX, int damagePositionY) {
        this.windscreenSizeHorizontal = windscreenSizeHorizontal;
        this.windscreenSizeVertical = windscreenSizeVertical;
        this.damagePositionX = damagePositionX;
        this.damagePositionY = damagePositionY;
    }

    public byte getSizeByte() {
        return (byte) (((windscreenSizeHorizontal & 0x0F) << 4) | (windscreenSizeVertical & 0x0F));
    }

    public byte getPositionByte() {
        return (byte) (((damagePositionX & 0x0F) << 4) | (damagePositionY & 0x0F));
    }
}
