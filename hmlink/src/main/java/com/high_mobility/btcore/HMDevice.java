package com.high_mobility.btcore;

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
        copyBytesToJNI(mac,_mac);
    }

    public byte[] getSerial() {
        return _serial;
    }

    public void setSerial(byte[] serial) {
        copyBytesToJNI(serial,_serial);
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
        copyBytesToJNI(appId,_appId);
    }

    private void copyBytesToJNI(byte[] from, byte[] to) {
        for (int i = 0; i < from.length; i++) {
            to[i] = from[i];
        }
    }
}
