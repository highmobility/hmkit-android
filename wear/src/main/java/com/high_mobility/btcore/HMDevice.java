package com.high_mobility.btcore;

import java.util.Arrays;

/**
 * Created by ttiganik on 13/04/16.
 */
public class HMDevice {
    byte[] _mac = {0,0,0,0,0,0};
    byte[] _serial = {0,0,0,0,0,0,0,0,0};
    byte[] _appId = {0,0,0,0,0,0,0,0,0,0,0,0};
    int _isAuthorised = 0;

    public byte[] getMac() {
        return _mac;
    }

    public void setMac(byte[] mac) {
        _mac = Arrays.copyOf(mac,mac.length);
    }

    public byte[] getSerial() {
        return _serial;
    }

    public void setSerial(byte[] serial) {
        _serial = Arrays.copyOf(serial,serial.length);
    }

    public int getIsAuthenticated() {
        return _isAuthorised;
    }

    public void setIsAuthenticated(int isAuthorised) {
        _isAuthorised = isAuthorised;
    }

    public byte[] getAppId() {
        return _appId;
    }

    public void setAppId(byte[] appId) {
        _appId = Arrays.copyOf(appId,appId.length);
    }
}
