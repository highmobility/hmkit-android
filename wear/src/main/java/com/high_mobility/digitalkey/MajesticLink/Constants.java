package com.high_mobility.digitalkey.MajesticLink;

import java.util.UUID;

/**
 * Created by ttiganik on 13/04/16.
 */
public class Constants {

    public static UUID SERVICE_UUID = UUID.fromString("713D0100-503E-4C75-BA94-3148F18D941E");

    public static UUID READ_CHAR_UUID = UUID.fromString("713D0102-503E-4C75-BA94-3148F18D941E");

    public static UUID WRITE_CHAR_UUID = UUID.fromString("713D0103-503E-4C75-BA94-3148F18D941E");

    public static UUID PING_CHAR_UUID = UUID.fromString("713D0104-503E-4C75-BA94-3148F18D941E");

    public static final float feedbackTimeout      = 30.0f;
    public static final float registerTimeout      = 6.0f;
    public static final float commandTimeout       = 10.0f;
    public static final float connectionTimeout    = 5.0f;

    public interface ApprovedCallback {
        void approve();
        void decline();
    }

    public interface DataResponseCallback {
        void response(byte[] bytes, Error error);
    }

    public interface ResponseCallback {
        void response(Error error);
    }

    /// The values representing an error encountered in the *MajesticLink*
    public enum Error {
        /// Bluetooth is turned off
        BLUETOOTHOFF,
        /// Bluetooth is not authorised for this framework (app)
        BLUETOOTH_UNAUTHORISED,
        /// A custom command has not yet received a response
        CUSTOM_COMMAND_IN_PROGRESS,
        /// Framework encountered an internal error (commonly releated to invalid data received)
        INTERNAL_ERROR,
        /// The signature for the command was invalid
        INVALID_SIGNATURE,
        /// The Certificates storage database is full
        STORAGE_FULL,
        /// Command timed out
        TIMEOUT,
        /// The Link is not connected
        NOT_CONNECTED,
        /// The app is not authorised with the connected link to perform the action
        UNAUTHORISED,
        /// Bluetooth Low Energy is unavailable for this device
        UNSUPPORTED
    }

    /// The values representing a *MajesticLink* command
    public enum Command {
        /// A Nonce is retrieved
        GET_NONCE,
        /// Device Certificate is retrieved
        GET_DEVICE_CERTIFICATE,
        /// Certificate is stored in the storage if it is valid
        REGISTER_CERT,
        /// Returns a Certificate that's saved in the storage
        GET_CERTIFICATE,
        /// Certificate is stored in the storage
        STORE_CERT,
        /// Authenticates the devices
        AUTHENTICATE,
        /// The storage is reset and the connected device is disconnected
        RESET,
        /// A Certificate is removed from the storage
        REVOKE,
        /// An Unknown command was executed
        UNKNOWN
    }
}
