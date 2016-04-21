package com.high_mobility.digitalkey.HMLink.Broadcasting.Core;

public class FakeCore extends HMBTCore {

    @Override
    public void HMBTCoreInit(HMBTCoreInterface coreInterface) {
        
    }

    @Override
    public void HMBTCoreClock() {

    }

    @Override
    public void HMBTCoreSensingReadNotification(byte[] mac) {

    }

    @Override
    public void HMBTCoreSensingReadResponse(byte[] data, int size, int offset, byte[] mac) {

    }

    @Override
    public void HMBTCoreSensingWriteResponse(byte[] mac) {

    }

    @Override
    public void HMBTCoreSensingPingNotification(byte[] mac) {

    }

    @Override
    public void HMBTCoreSensingProcessAdvertisement(byte[] mac, byte[] data, int size) {

    }

    @Override
    public void HMBTCoreSensingDiscoveryEvent(byte[] mac) {

    }

    @Override
    public void HMBTCoreSensingScanStart() {

    }

    @Override
    public void HMBTCoreSensingConnect(byte[] mac) {

    }

    @Override
    public void HMBTCoreSensingDisconnect(byte[] mac) {

    }

    @Override
    public void HMBTCorelinkConnect(byte[] mac) {

    }

    @Override
    public void HMBTCorelinkDisconnect(byte[] mac) {

    }

    @Override
    public void HMBTCorelinkIncomingData(byte[] data, int size, byte[] mac) {

    }

    @Override
    public void HMBTCoreSendCustomCommand(byte[] data, Integer size, byte[] mac) {

    }
}
