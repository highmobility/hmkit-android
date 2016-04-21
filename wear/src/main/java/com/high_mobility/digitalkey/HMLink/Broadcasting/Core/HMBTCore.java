package com.high_mobility.digitalkey.HMLink.Broadcasting.Core;

/**
 * Created by ttiganik on 13/04/16.
 */
public abstract class HMBTCore {
    //Init core
    //interface is class reference what implements HMBTCoreInterface
    //TT
    abstract void HMBTCoreInit(HMBTCoreInterface coreInterface);
    //Send clock beat to core
    abstract void HMBTCoreClock();

    //CORE SENSING

    abstract void HMBTCoreSensingReadNotification(byte[] mac);
    abstract void HMBTCoreSensingReadResponse(byte[] data, int size, int offset, byte[] mac);

    abstract void HMBTCoreSensingWriteResponse(byte[] mac);

    abstract void HMBTCoreSensingPingNotification(byte[] mac);

    abstract void HMBTCoreSensingProcessAdvertisement(byte[] mac, byte[] data, int size);
    abstract void HMBTCoreSensingDiscoveryEvent(byte[] mac);
    abstract void HMBTCoreSensingScanStart();

    abstract void HMBTCoreSensingConnect(byte[] mac);
    abstract void HMBTCoreSensingDisconnect(byte[] mac);

    //CORE LINK

    //Initialize link object in core
    //TT
    abstract void HMBTCorelinkConnect(byte[] mac);
    //Delete link object in core
    //TT
    abstract void HMBTCorelinkDisconnect(byte[] mac);

    //Forward link incoming data to core
    //TT
    abstract void HMBTCorelinkIncomingData(byte[] data, int size, byte[] mac);
    abstract void HMBTCoreSendCustomCommand(byte[] data, Integer size, byte[] mac);
}
