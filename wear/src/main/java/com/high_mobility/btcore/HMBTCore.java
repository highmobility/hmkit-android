package com.high_mobility.btcore;

/**
 * Created by ttiganik on 13/04/16.
 */
public class HMBTCore {
    //Init core
    //interface is class reference what implements HMBTCoreInterface
    //TT
    public native void HMBTCoreInit(HMBTCoreInterface coreInterface);
    //Send clock beat to core
    public native void HMBTCoreClock();

    //CORE SENSING

    public native void HMBTCoreSensingReadNotification(byte[] mac);
    public native void HMBTCoreSensingReadResponse(byte[] data, int size, int offset, byte[] mac);

    public native void HMBTCoreSensingWriteResponse(byte[] mac);

    public native void HMBTCoreSensingPingNotification(byte[] mac);

    public native void HMBTCoreSensingProcessAdvertisement(byte[] mac, byte[] data, int size);
    public native void HMBTCoreSensingDiscoveryEvent(byte[] mac);
    public native void HMBTCoreSensingScanStart();

    public native void HMBTCoreSensingConnect(byte[] mac);
    public native void HMBTCoreSensingDisconnect(byte[] mac);

    //CORE LINK

    //Initialize link object in core
    //TT
    public native void HMBTCorelinkConnect(byte[] mac);
    //Delete link object in core
    //TT
    public native void HMBTCorelinkDisconnect(byte[] mac);

    //Forward link incoming data to core
    //TT
    public native void HMBTCorelinkIncomingData(byte[] data, int size, byte[] mac);
}
