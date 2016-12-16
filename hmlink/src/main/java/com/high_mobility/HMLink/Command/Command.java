package com.high_mobility.HMLink.Command;

import com.high_mobility.HMLink.ByteUtils;
import com.high_mobility.HMLink.Command.Incoming.RooftopState;
import com.high_mobility.HMLink.Command.Incoming.TrunkState;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Created by ttiganik on 25/05/16.
 */
public class Command {
    public static byte[] failureIdentifier = new byte[] { 0x00, 0x02 }; // TODO: this should ideally be internal
    static byte[] capabilitiesIdentifier = new byte[] { 0x00, 0x10 };
    static byte[] vehicleStatusIdentifier = new byte[] { 0x00, 0x11 };

    static byte[] doorLocksIdentifer = new byte[] { 0x00, 0x20 };
    static byte[] trunkAccessIdentifer = new byte[] { 0x00, 0x21 };
    static byte[] wakeupIdentifer = new byte[] { 0x00, 0x22 };
    static byte[] chargingIdentifer = new byte[] { 0x00, 0x23 };
    static byte[] climateIdentifer = new byte[] { 0x00, 0x24 };
    static byte[] rooftopControlIdentifer = new byte[] { 0x00, 0x25 };
    static byte[] honkFlashIdentifer = new byte[] { 0x00, 0x26 };
    static byte[] remoteControlIdentifer = new byte[] { 0x00, 0x27 };
    static byte[] valetModeIdentifer = new byte[] { 0x00, 0x28 };
    static byte[] heartRateIdentifer = new byte[] { 0x00, 0x29 };
    static byte[] vehicleLocationIdentifer = new byte[] { 0x00, 0x30 };
    static byte[] naviDestinationIdentifer = new byte[] { 0x00, 0x31 };
    static byte[] deliveredParcelsIdentifer = new byte[] { 0x00, 0x32 };

    public interface Type {
        byte getMessageType();
        byte[] getMessageIdentifier();
        byte[] getMessageIdentifierAndType();
    }

    /**
     * Commands for the Vehicle Status category of the Auto API.
     */
    public enum VehicleStatus implements Type {
        GET_VEHICLE_STATUS((byte)0x00),
        VEHICLE_STATUS((byte)0x01);

        /**
         * Get the vehicle status. The car will respond with the Vehicle Status command.
         *
         * @return the command bytes
         */
        public static byte[] getVehicleStatus() {
            return GET_VEHICLE_STATUS.getMessageIdentifierAndType();
        }

        VehicleStatus(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getMessageType() {
            return messageType;
        }

        @Override
        public byte[] getMessageIdentifier() {
            return vehicleStatusIdentifier;
        }

        @Override
        public byte[] getMessageIdentifierAndType() {
            return ByteUtils.concatBytes(getMessageIdentifier(), getMessageType());
        }
    }

    /**
     * Commands for the Vehicle Status category of the Auto API.
     */
    public enum Capabilities implements Type {
        GET_CAPABILITIES((byte)0x00),
        CAPABILITIES((byte)0x01),
        GET_CAPABILITY((byte)0x02),
        CAPABILITY((byte)0x03);

        /**
         * Get the vehicle capabilities. The car will respond with the Capabilities command that
         * manifests all different APIs that are enabled on the specific car. It is good practice
         * to only inspect the vehicle capabilities the first time when access is gained. The
         * capabilities are fixed for each car type and will not change between every session.
         *
         * @return the command bytes
         */
        public static byte[] getCapabilities() {
            return GET_CAPABILITIES.getMessageIdentifierAndType();
        }

        /**
         * Get the capability of a certain feature. The car will respond with the Capability command
         * - to what extent the capability is supported, if at all.
         * @return the command bytes
         */
        public static byte[] getCapability(VehicleFeature feature) {
            return ByteUtils.concatBytes(GET_CAPABILITY.getMessageIdentifierAndType(), feature.getIdentifier());
        }

        Capabilities(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getMessageType() {
            return messageType;
        }

        @Override
        public byte[] getMessageIdentifier() {
            return capabilitiesIdentifier;
        }

        @Override
        public byte[] getMessageIdentifierAndType() {
            return ByteUtils.concatBytes(getMessageIdentifier(), getMessageType());
        }
    }

    /**
     * Commands for the Door Locks category of the Auto API.
     */
    public enum DoorLocks implements Type {
        GET_LOCK_STATE((byte)0x00),
        LOCK_STATE((byte)0x01),
        LOCK_UNLOCK((byte)0x02);

        /**
         * Get the lock state, which either locked or unlocked. The car will respond with the
         * Lock State command.
         *
         * @return the command bytes
         */
        public static byte[] getLockState() {
            return GET_LOCK_STATE.getMessageIdentifierAndType();
        }

        /**
         * Attempt to lock or unlock the car. The result is received through the Lock State command.
         *
         * @param lock whether to lock or unlock the car
         * @return the command bytes
         */
        public static byte[] lockDoors(boolean lock) {
            return ByteUtils.concatBytes(LOCK_UNLOCK.getMessageIdentifierAndType(), (byte)(lock ? 0x01 : 0x00));
        }

        DoorLocks(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getMessageType() {
            return messageType;
        }

        @Override
        public byte[] getMessageIdentifier() {
            return doorLocksIdentifer;
        }

        @Override
        public byte[] getMessageIdentifierAndType() {
            return ByteUtils.concatBytes(getMessageIdentifier(), getMessageType());
        }
    }

    /**
     * Commands for the Trunk Access category of the Auto API.
     */
    public enum TrunkAccess implements Type {
        GET_TRUNK_STATE((byte)0x00),
        TRUNK_STATE((byte)0x01),
        OPEN_CLOSE((byte)0x02);

        /**
         * Get the trunk state, if it's locked/unlocked and closed/open. The car will respond with
         * the Trunk State command.
         *
         * @return the command bytes
         */
        public static byte[] getTrunkState() {
            return GET_TRUNK_STATE.getMessageIdentifierAndType();
        }

        /**
         * Unlock/Lock and Open/Close the trunk. The result is received through the evented
         * Trunk State command.
         *
         * @param lockState whether to lock or unlock the trunk
         * @param position whether to open or close the trunk
         * @return the command bytes
         */
        public static byte[] setTrunkState(TrunkState.LockState lockState, TrunkState.Position position) {
            byte[] bytes = new byte[5];
            bytes[0] = OPEN_CLOSE.getMessageIdentifier()[0];
            bytes[1] = OPEN_CLOSE.getMessageIdentifier()[1];
            bytes[2] = OPEN_CLOSE.getMessageType();
            bytes[3] = (byte)(lockState == TrunkState.LockState.UNLOCKED ? 0x00 : 0x01);
            bytes[4] = (byte)(position == TrunkState.Position.CLOSED ? 0x00 : 0x01);
            return bytes;
        }

        TrunkAccess(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getMessageType() {
            return messageType;
        }

        @Override
        public byte[] getMessageIdentifier() {
            return trunkAccessIdentifer;
        }

        @Override
        public byte[] getMessageIdentifierAndType() {
            return ByteUtils.concatBytes(getMessageIdentifier(), getMessageType());
        }
    }

    // TODO: add wakeup, charging, climate

    /**
     * Commands for the Rooftop Control category of the Auto API.
     */
    public enum RooftopControl implements Type {
        GET_ROOFTOP_STATE((byte)0x00),
        ROOFTOP_STATE((byte)0x01),
        CONTROL_ROOFTOP((byte)0x02);
        /**
         * Get the rooftop state. The car will respond with the Rooftop State command.
         *
         *  @return the command bytes
         */
        public static byte[] getRooftopState() {
            return GET_ROOFTOP_STATE.getMessageIdentifierAndType();
        }

        /**
         * Set the rooftop state. The result is sent through the evented Rooftop State command.
         *
         * @param dimPercentage 0 - 1 percentage of how dimmed the rooftop should be
         * @param openPercentage 0 - 1 percentage of how open the rooftop should be
         * @return the command bytes
         */
        public static byte[] controlRooftop(float dimPercentage, float openPercentage) throws IllegalArgumentException {
            if (dimPercentage < 0f || dimPercentage > 1f || openPercentage < 0f || openPercentage > 1f) {
                throw new IllegalArgumentException();
            }

            byte[] identifier = CONTROL_ROOFTOP.getMessageIdentifierAndType();
            byte[] message = new byte[5];
            message[0] = identifier[0];
            message[1] = identifier[1];
            message[2] = identifier[2];
            message[3] = (byte)(int)(dimPercentage * 100);
            message[4] = (byte)(int)(openPercentage * 100);
            return message;
        }

        RooftopControl(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getMessageType() {
            return messageType;
        }

        @Override
        public byte[] getMessageIdentifier() {
            return rooftopControlIdentifer;
        }

        @Override
        public byte[] getMessageIdentifierAndType() {
            return ByteUtils.concatBytes(getMessageIdentifier(), getMessageType());
        }
    }

    // TODO: add honkflash

    /**
     * Commands of the Remote Control category of the Auto API.
     */
    public enum RemoteControl implements Type {
        GET_CONTROL_MODE((byte)0x00),
        CONTROL_MODE((byte)0x01),
        START_CONTROL_MODE((byte)0x02),
        STOP_CONTROL_MODE((byte)0x03),
        CONTROL_COMMAND((byte)0x04);

        /**
         * Get the current remote control mode. The car will respond with the Control Mode command.
         *
         * @return the command bytes
         */
        public static byte[] getControlMode() {
            return GET_CONTROL_MODE.getMessageIdentifierAndType();
        }

        /**
         * Attempt to start the control mode of the car. The result is sent through the
         * Control Mode command with either Control Started or Control Failed to Start mode.
         *
         * @return the command bytes
         */
        public static byte[] startControlMode() {
            return START_CONTROL_MODE.getMessageIdentifierAndType();
        }

        /**
         * Stop the control mode of the car. The result is sent through the Control Mode
         * command with Control Ended mode.
         *
         * @return the command bytes
         */
        public static byte[] stopControlMode() {
            return STOP_CONTROL_MODE.getMessageIdentifierAndType();
        }

        /**
         * To be sent every time the controls for the car wants to be changed or once a second if
         * the controls remain the same. If the car does not receive the command every second it
         * will stop the control mode.
         *
         * @param speed Speed in km/h, can range between -5 to 5 whereas a negative speed will
         *              reverse the car.
         * @param angle angle of the car.
         * @return the command bytes
         */
        public static byte[] controlCommand(int speed, int angle) {
            byte msb = (byte) ((angle & 0xFF00) >> 8);
            byte lsb = (byte) (angle & 0xFF);

            return ByteUtils.concatBytes(CONTROL_COMMAND.getMessageIdentifierAndType(), new byte[] {(byte)speed, msb, lsb});
        }

        RemoteControl(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getMessageType() {
            return messageType;
        }

        @Override
        public byte[] getMessageIdentifier() {
            return remoteControlIdentifer;
        }

        @Override
        public byte[] getMessageIdentifierAndType() {
            return ByteUtils.concatBytes(getMessageIdentifier(), getMessageType());
        }
    }

    // TODO: add valet

    public enum HeartRate implements Type {
        SEND_HEART_RATE((byte)0x00);

        /**
         * Send the driver heart rate to the car.
         *
         * @param heartRate The heart rate in BPM.
         * @return the command bytes
         */
        public static byte[] sendHeartRate(int heartRate) {
            return ByteUtils.concatBytes(SEND_HEART_RATE.getMessageIdentifierAndType(), (byte)heartRate);
        }

        HeartRate(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getMessageType() {
            return messageType;
        }

        @Override
        public byte[] getMessageIdentifier() {
            return heartRateIdentifer;
        }

        @Override
        public byte[] getMessageIdentifierAndType() {
            return ByteUtils.concatBytes(getMessageIdentifier(), getMessageType());
        }
    }

    // TODO: add vehicle location

    /**
     * Commands of the Navi Destination category of the Auto API.
     */
    public enum NaviDestination implements Type {
        SET_DESTINATION((byte)0x00);

        /**
         * Set the navigation destination. This will be forwarded to the navigation system of the car.
         *
         * @param latitude the latitude of the destination
         * @param longitude the longitude of the destination
         * @param name the destination name
         * @return the command bytes
         */
        public static byte[] setDestination(float latitude, float longitude, String name) throws UnsupportedEncodingException {
            byte[] latitudeBytes = ByteBuffer.allocate(4).putFloat(latitude).array();
            byte[] longitudeBytes = ByteBuffer.allocate(4).putFloat(longitude).array();
            byte[] nameBytes = name.getBytes("UTF-8");
            byte[] destinationBytes = new byte[3 + 8 + 1 + nameBytes.length];

            System.arraycopy(SET_DESTINATION.getMessageIdentifierAndType(), 0, destinationBytes, 0, 3);
            System.arraycopy(latitudeBytes, 0, destinationBytes, 3, 4);
            System.arraycopy(longitudeBytes, 0, destinationBytes, 7, 4);
            destinationBytes[11] = (byte)nameBytes.length;
            System.arraycopy(nameBytes, 0, destinationBytes, 12, nameBytes.length);
            return destinationBytes;
        }

        NaviDestination(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getMessageType() {
            return messageType;
        }

        @Override
        public byte[] getMessageIdentifier() {
            return naviDestinationIdentifer;
        }

        @Override
        public byte[] getMessageIdentifierAndType() {
            return ByteUtils.concatBytes(getMessageIdentifier(), getMessageType());
        }
    }

    /**
     * Commands for the Delivered Parcels category of the Auto API.
     */
    public enum DeliveredParcels implements Type {
        GET_DELIVERED_PARCELS((byte)0x00),
        DELIVERED_PARCELS((byte)0x01);

        /**
         * Get information about all parcels that have been delivered to the car. The car will
         * respond with the Delivered Parcels command.
         *
         * @return the command bytes
         */
        public static byte[] getDeliveredParcels() {
            return GET_DELIVERED_PARCELS.getMessageIdentifierAndType();
        }

        DeliveredParcels(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getMessageType() {
            return messageType;
        }

        @Override
        public byte[] getMessageIdentifier() {
            return deliveredParcelsIdentifer;
        }

        @Override
        public byte[] getMessageIdentifierAndType() {
            return ByteUtils.concatBytes(getMessageIdentifier(), getMessageType());
        }
    }
}