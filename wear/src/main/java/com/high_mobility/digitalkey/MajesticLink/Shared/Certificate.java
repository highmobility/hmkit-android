package com.high_mobility.digitalkey.MajesticLink.Shared;

/**
 * Created by ttiganik on 13/04/16.
 */
public class Certificate {
    byte[] bytes;

    /// The certificate's data in binary format, without the signature
    public byte [] getCertificateData() {
        return null;
    }

    /// The certificate's signature
    public byte[] getSignature() {
        return null;
    }

    /// The full certificate data in binary format.
    public byte[] getBytes() {
        return bytes;
    }

    /// Checks the certificate's signature
    ///
    /// - parameter CAPublicKey: The public key that the signature is checked with
    /// - returns: True if the signature is valid for the provided public key
    public boolean isSignatureValid(byte[] CAPublicKey){
//        if let signature = signature {
//            return HMCryptor.verifySignature(NSData(bytes: signature), forData: NSData(bytes:certificateData), publicKey: NSData(bytes:CAPublicKey))
//        }
// TODO:
        return false;
    }

    // TODO:
    /*
    static func dateFromBytes(bytes: [UInt8]) -> NSDate {
        let year = bytes[0], month = bytes[1], day = bytes[2], hour = bytes[3], minute = bytes[4]

        let components = NSDateComponents()
        components.year = 2000 | Int(year)

        components.month = Int(month)
        components.day = Int(day)
        components.hour = Int(hour)
        components.minute = Int(minute)
        let date = NSCalendar.currentCalendar().dateFromComponents(components)!

        return date
    }

    static func bytesFromDate(date: NSDate) -> [UInt8] {
        let components = NSCalendar.currentCalendar().components([NSCalendarUnit.Year, NSCalendarUnit.Month, NSCalendarUnit.Day, NSCalendarUnit.Hour, NSCalendarUnit.Minute], fromDate: date)
        var bytes = [UInt8](count: 5, repeatedValue: 0x00)

        bytes[0] = UInt8(components.year & 0xFF)
        bytes[1] = UInt8(components.month)
        bytes[2] = UInt8(components.day)
        bytes[3] = UInt8(components.hour)
        bytes[4] = UInt8(components.minute)

        return bytes
    }
*/
}
