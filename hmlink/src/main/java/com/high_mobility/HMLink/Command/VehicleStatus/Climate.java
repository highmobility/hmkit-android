package com.high_mobility.HMLink.Command.VehicleStatus;

import com.high_mobility.HMLink.ByteUtils;
import com.high_mobility.HMLink.Command.Command.Identifier;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by ttiganik on 16/12/2016.
 */

public class Climate extends FeatureState {
    float insideTemperature;
    float outsideTemperature;

    boolean hvacActive;
    boolean defoggingActive;
    boolean defrostingActive;

    boolean isAutoHvacConstant;
    boolean[] hvacActiveOnDays;

    Climate(byte[] bytes) {
        super(Identifier.CLIMATE);

        insideTemperature = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 3, 3 + 4)).getFloat();
        outsideTemperature = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 7, 7 + 4)).getFloat();

        hvacActive = bytes[11] == 0x00 ? false : true;
        defoggingActive = bytes[12] == 0x00 ? false : true;
        defrostingActive = bytes[13] == 0x00 ? false : true;

        int hvacActiveOnDays = bytes[14];
        if (ByteUtils.getBit(hvacActiveOnDays, 7)) isAutoHvacConstant = true;

        this.hvacActiveOnDays = new boolean[7];
        for (int i = 0; i < 7; i++) {
            this.hvacActiveOnDays[i] = ByteUtils.getBit(hvacActiveOnDays, i);
        }
    }

    /**
     *
     * @return Inside temperature in celsius
     */
    public float getInsideTemperature() {
        return insideTemperature;
    }

    /**
     *
     * @return Outside temperature in celsius
     */
    public float getOutsideTemperature() {
        return outsideTemperature;
    }

    /**
     *
     * @return Whether the HVAC is active or not
     */
    public boolean isHvacActive() {
        return hvacActive;
    }

    /**
     *
     * @return Whether the Defogging is active or not
     */
    public boolean isDefoggingActive() {
        return defoggingActive;
    }

    /**
     *
     * @return Whether Defrosting is active or not
     */
    public boolean isDefrostingActive() {
        return defrostingActive;
    }

    /**
     *
     * @return Whether autoHVAC is constant(based on the car surroundings)
     */
    public boolean isAutoHvacConstant() {
        return isAutoHvacConstant;
    }

    /**
     *
     * @return Array of 7 booleans indicating whether the HVAC is active on a specific weekday.
     */
    public boolean[] getHvacActiveOnDays() {
        return hvacActiveOnDays;
    }
}
