package com.highmobility.hmkit.Command.Incoming;
import com.highmobility.hmkit.ByteUtils;
import com.highmobility.hmkit.Command.CommandParseException;
import com.highmobility.hmkit.Command.WindscreenDamagePosition;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by ttiganik on 13/09/16.
 */
public class WindscreenState extends IncomingCommand {
    public enum WiperState {
        INACTIVE, ACTIVE, AUTOMATIC;

        static WiperState fromByte(byte value) {
            switch (value) {
                case 0x00:
                    return INACTIVE;
                case 0x01:
                    return ACTIVE;
                case 0x02:
                    return AUTOMATIC;
                default: return INACTIVE;
            }
        }
    }
    public enum WiperIntensity {
        LEVEL_0, LEVEL_1, LEVEL_2, LEVEL_3;

        static WiperIntensity fromByte(byte value) {
            switch (value) {
                case 0x00:
                    return LEVEL_0;
                case 0x01:
                    return LEVEL_1;
                case 0x02:
                    return LEVEL_2;
                case 0x03:
                    return LEVEL_3;
                default: return LEVEL_0;
            }
        }
    }
    public enum WindscreenDamage {
        NO_IMPACT, IMPACT_NO_DAMAGE, DAMAGE_SMALLER_THAN_1, DAMAGE_LARGER_THAN_1;

        static WindscreenDamage fromByte(byte value) {
            switch (value) {
                case 0x00:
                    return NO_IMPACT;
                case 0x01:
                    return IMPACT_NO_DAMAGE;
                case 0x02:
                    return DAMAGE_SMALLER_THAN_1;
                case 0x03:
                    return DAMAGE_LARGER_THAN_1;
                default: return NO_IMPACT;
            }
        }

        public byte getByte() {
            switch (this) {
                case NO_IMPACT:
                    return 0x00;
                case IMPACT_NO_DAMAGE:
                    return 0x01;
                case DAMAGE_SMALLER_THAN_1:
                    return 0x02;
                case DAMAGE_LARGER_THAN_1:
                    return 0x03;
                default: return 0x00;
            }
        }
    }

    public enum WindscreenReplacementState {
        UNKNOWN, REPLACEMENT_NOT_NEEDED, REPLACEMENT_NEEDED;

        static WindscreenReplacementState fromByte(byte value) {
            switch (value) {
                case 0x00:
                    return UNKNOWN;
                case 0x01:
                    return REPLACEMENT_NOT_NEEDED;
                case 0x02:
                    return REPLACEMENT_NEEDED;
                default: return UNKNOWN;
            }
        }

        public byte getByte() {
            switch (this) {
                case UNKNOWN:
                    return 0x00;
                case REPLACEMENT_NOT_NEEDED:
                    return 0x01;
                case REPLACEMENT_NEEDED:
                    return 0x02;
                default: return 0x00;
            }
        }
    }

    WiperState wiperState;
    WiperIntensity wiperIntensity;
    WindscreenDamage windscreenDamage;
    WindscreenReplacementState windscreenReplacementState;
    WindscreenDamagePosition windscreenDamagePosition;
    float damageConfidence;
    Date damageDetectionTime;

    /**
     *
     * @return Wiper state
     */
    public WiperState getWiperState() {
        return wiperState;
    }

    /**
     *
     * @return Wiper intensity
     */
    public WiperIntensity getWiperIntensity() {
        return wiperIntensity;
    }

    /**
     *
     * @return Windscreen damage
     */
    public WindscreenDamage getWindscreenDamage() {
        return windscreenDamage;
    }

    /**
     *
     * @return Windscreen replacement state
     */
    public WindscreenReplacementState getWindscreenReplacementState() {
        return windscreenReplacementState;
    }

    /**
     *
     * @return Windscreen damage position, as viewed from inside the car.
     *          null if unavailable
     */
    public WindscreenDamagePosition getWindscreenDamagePosition() {
        return windscreenDamagePosition;
    }

    /**
     *
     * @return Damage confidence
     */
    public float getDamageConfidence() {
        return damageConfidence;
    }

    /**
     *
     * @return Damage detection time
     */
    public Date getDamageDetectionTime() {
        return damageDetectionTime;
    }

    public WindscreenState(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length < 16) throw new CommandParseException();

        wiperState = WiperState.fromByte(bytes[3]);
        wiperIntensity = WiperIntensity.fromByte(bytes[4]);
        windscreenDamage = WindscreenDamage.fromByte(bytes[5]);

        windscreenReplacementState = WindscreenReplacementState.fromByte(bytes[8]);
        damageConfidence = (float)ByteUtils.getInt(bytes[9]) / 100f;

        if (bytes[6] != 0x00) {
            int horizontalSize = bytes[6] >> 4;
            int verticalSize = bytes[6] & 0x0F;

            int horizontalDamagePosition = bytes[7] >> 4;
            int verticalDamagePosition = bytes[7] & 0x0F;

            windscreenDamagePosition = new WindscreenDamagePosition(horizontalSize,
                    verticalSize,
                    horizontalDamagePosition,
                    verticalDamagePosition);
        }

        damageDetectionTime = ByteUtils.getDate(Arrays.copyOfRange(bytes, 10, 10 + 6));
    }
}
