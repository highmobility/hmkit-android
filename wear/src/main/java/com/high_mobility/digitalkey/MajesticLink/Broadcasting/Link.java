package com.high_mobility.digitalkey.MajesticLink.Broadcasting;

import com.high_mobility.digitalkey.MajesticLink.Constants;
import com.high_mobility.digitalkey.MajesticLink.Shared.AccessCertificate;

/**
 * Created by ttiganik on 13/04/16.
 */
public class Link {
    public enum State { CONNECTED, AUTHENTICATED, DISCONNECTED }
    public State state;
    public AccessCertificate certificate;

    private LinkCallback callback;

    void registerCallback(LinkCallback callback) {
        // TODO:
        this.callback = callback;
    }

    void sendCustomCommand(byte[] bytes, boolean secureResponse, Constants.DataResponseCallback responseCallback) {
        // TODO:
    }
}
