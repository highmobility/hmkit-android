package com.high_mobility.HMLink;

import com.high_mobility.HMLink.Shared.DeviceCertificate;

import java.util.UUID;

/**
 * Created by ttiganik on 13/04/16.
 */
public class Device {
    protected static UUID SERVICE_UUID = UUID.fromString("713D0100-503E-4C75-BA94-3148F18D941E");
    protected static UUID READ_CHAR_UUID = UUID.fromString("713D0102-503E-4C75-BA94-3148F18D941E");
    protected static UUID WRITE_CHAR_UUID = UUID.fromString("713D0103-503E-4C75-BA94-3148F18D941E");
    protected static UUID PING_CHAR_UUID = UUID.fromString("713D0104-503E-4C75-BA94-3148F18D941E");

    protected DeviceCertificate certificate;

    public String getName() {
        return null;
    }

    public DeviceCertificate getCertificate() {
        return certificate;
    }
}
