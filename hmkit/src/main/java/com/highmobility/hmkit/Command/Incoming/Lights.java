package com.highmobility.hmkit.Command.Incoming;
import android.graphics.Color;

import com.highmobility.hmkit.ByteUtils;
import com.highmobility.hmkit.Command.CommandParseException;
import com.highmobility.hmkit.Command.Constants;

/**
 * Created by ttiganik on 13/09/16.
 */
public class Lights extends IncomingCommand {
    Constants.FrontExteriorLightState frontExteriorLightState;
    boolean rearExteriorLightActive;
    boolean interiorLightActive;
    int ambientColor;

    /**
     *
     * @return Front exterior light state
     */
    public Constants.FrontExteriorLightState getFrontExteriorLightState() {
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

    public Lights(byte[] bytes) throws CommandParseException {
        super(bytes);

        if (bytes.length != 9) throw new CommandParseException();

        if (bytes[3] == 0x00) {
            frontExteriorLightState = Constants.FrontExteriorLightState.INACTIVE;
        }
        else if (bytes[3] == 0x01) {
            frontExteriorLightState = Constants.FrontExteriorLightState.ACTIVE;
        }
        else if (bytes[3] == 0x02) {
            frontExteriorLightState = Constants.FrontExteriorLightState.ACTIVE_WITH_FULL_BEAM;
        }

        rearExteriorLightActive = ByteUtils.getBool(bytes[4]);
        interiorLightActive = ByteUtils.getBool(bytes[5]);

        ambientColor = Color.rgb((int)bytes[6], (int)bytes[7], (int)bytes[8]);
    }
}
