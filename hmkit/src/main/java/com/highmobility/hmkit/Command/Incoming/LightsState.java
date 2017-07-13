package com.highmobility.hmkit.Command.Incoming;
import android.graphics.Color;
import android.util.Log;

import com.highmobility.hmkit.ByteUtils;
import com.highmobility.hmkit.Command.CommandParseException;

import static com.highmobility.hmkit.Command.Incoming.DeliveredParcels.TAG;


/**
 * Created by ttiganik on 13/09/16.
 */
public class LightsState extends IncomingCommand {
    public enum FrontExteriorLightState {
        INACTIVE, ACTIVE, ACTIVE_WITH_FULL_BEAM;

        public byte byteValue() {
            if (this == INACTIVE) return 0x00;
            else if (this == ACTIVE) return 0x01;
            else if (this == ACTIVE_WITH_FULL_BEAM) return 0x02;
            return 0x00;
        }
    }

    FrontExteriorLightState frontExteriorLightState;
    boolean rearExteriorLightActive;
    boolean interiorLightActive;
    int ambientColor;

    /**
     *
     * @return Front exterior light state
     */
    public FrontExteriorLightState getFrontExteriorLightState() {
        return frontExteriorLightState;
    }

    /**
     *
     * @return Rear exterior light state
     */
    public boolean isRearExteriorLightActive() {
        return rearExteriorLightActive;
    }

    /**
     *
     * @return Interior light state
     */
    public boolean isInteriorLightActive() {
        return interiorLightActive;
    }

    /**
     *
     * @return Ambient color in color-int
     */
    public int getAmbientColor() {
        return ambientColor;
    }

    public LightsState(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 9) throw new CommandParseException();

        if (bytes[3] == 0x00) {
            frontExteriorLightState = FrontExteriorLightState.INACTIVE;
        }
        else if (bytes[3] == 0x01) {
            frontExteriorLightState = FrontExteriorLightState.ACTIVE;
        }
        else if (bytes[3] == 0x02) {
            frontExteriorLightState = FrontExteriorLightState.ACTIVE_WITH_FULL_BEAM;
        }

        rearExteriorLightActive = ByteUtils.getBool(bytes[4]);
        interiorLightActive = ByteUtils.getBool(bytes[5]);

        ambientColor = Color.rgb(bytes[6] & 0xFF, bytes[7] & 0xFF, bytes[8] & 0xFF);
    }
}
