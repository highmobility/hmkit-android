package com.high_mobility.HMLink;

/**
 * Created by ttiganik on 25/05/16.
 */
public class Command {
    interface Type {
        byte[] getIdentifier();
    }

    /**
     * Commands of the General category of the Auto API.
     */
    public enum General implements Type {
        GET_CAPABILITIES(new byte[] { 0x00, (byte)0x10 }),
        CAPABILITIES(new byte[] { 0x00, (byte)0x11 }),
        GET_VEHICLE_STATUS(new byte[] { 0x00, (byte)0x12 }),
        VEHICLE_STATUS(new byte[] { 0x00, (byte)0x13 });

        /**
         * Get the vehicle capabilities. The car will respond with the Capabilities command that
         * manifests all different APIs that are enabled on the specific car.
         *
         * @return the command bytes
         */
        public static byte[] getCapabilities() {
            return GET_VEHICLE_STATUS.getIdentifier();
        }

        /**
         * Get the vehicle status. The car will respond with the Vehicle Status command.
         *
         * @return the command bytes
         */
        public static byte[] getVehicleStatus() {
            return GET_VEHICLE_STATUS.getIdentifier();
        }

        General(byte[] identifier) {
            this.identifier = identifier;
        }
        private byte[] identifier;
        public byte[] getIdentifier() {
            return identifier;
        }
    }

    /**
     * Commands of the Digital Key category of the Auto API.
     */
    public enum DigitalKey implements Type {
        GET_LOCK_STATE(new byte[] { 0x00, (byte)0x20 }),
        LOCK_STATE(new byte[] { 0x00, (byte)0x21 }),
        LOCK_UNLOCK(new byte[] { 0x00, (byte)0x22 });

        /**
         * Get the lock state, which either locked or unlocked. The car will respond with the
         * Lock State command.
         *
         * @return the command bytes
         */
        public static byte[] getLockState() {
            return GET_LOCK_STATE.getIdentifier();
        }

        /**
         * Attempt to lock or unlock the car. The result is received through the Lock State command.
         *
         * @param lock whether to lock or unlock the car
         * @return the command bytes
         */
        public static byte[] lockDoors(boolean lock) {
            byte[] bytes = new byte[3];
            bytes[0] = LOCK_UNLOCK.getIdentifier()[0];
            bytes[1] = LOCK_UNLOCK.getIdentifier()[1];
            bytes[2] = (byte)(lock ? 0x01 : 0x00);
            return bytes;
        }

        DigitalKey(byte[] identifier) {
            this.identifier = identifier;
        }
        private byte[] identifier;
        public byte[] getIdentifier() {
            return identifier;
        }
    }

    /**
     * Commands of the Chassis category of the Auto API.
     */
    public enum Chassis implements Type {
        GET_WINDSHIELD_HEATING_STATE(new byte[] { 0x00, (byte)0x5A }),
        WINDSHIELD_HEATING_STATE(new byte[] { 0x00, (byte)0x5B }),
        SET_WINDSHIELD_HEATING(new byte[] { 0x00, (byte)0x5C }),
        GET_ROOFTOP_STATE(new byte[] { 0x00, (byte)0x5D }),
        ROOFTOP_STATE(new byte[] { 0x00, (byte)0x5E }),
        SET_ROOFTOP_TRANSPARENCY(new byte[] { 0x00, (byte)0x5F });

        /**
         * Get the windshield heating state. The car will respond with the Windshield Heating State
         * command.
         *
         * @return the command bytes
         */
        public static byte[] getWindshieldHeatingState() {
            return GET_WINDSHIELD_HEATING_STATE.getIdentifier();
        }

        /**
         * Set the windshield heating state. The result is sent through the Windshield Heating State
         * command.
         *
         * @param active whether the heating should be active or not
         * @return the command bytes
         */
        public static byte[] setWindshieldHeating(boolean active) {
            return ByteUtils.concatBytes(SET_WINDSHIELD_HEATING.getIdentifier(), (byte)(active ? 0x01 : 0x00));
        }

        /**
         * Get the rooftop state. The car will respond with the Rooftop State command.
         *
         *  @return the command bytes
         */
        public static byte[] getRooftopState() {
            return GET_ROOFTOP_STATE.getIdentifier();
        }

        /**
         * Set the rooftop state. The result is sent through the evented Rooftop State command.
         *
         * @param state the rooftop transparency state
         * @return the command bytes
         */
        public static byte[] controlRooftop(RooftopState.State state) {
            return ByteUtils.concatBytes(SET_ROOFTOP_TRANSPARENCY.getIdentifier(), (byte)(state == RooftopState.State.OPAQUE ? 0x01 : 0x00));
        }

        Chassis(byte[] identifier) {
            this.identifier = identifier;
        }
        private byte[] identifier;
        public byte[] getIdentifier() {
            return identifier;
        }
    }

    /**
     * Commands of the Remote Control category of the Auto API.
     */
    public enum RemoteControl implements Type {
        GET_CONTROL_MODE(new byte[] { 0x00, (byte)0x41 }),
        CONTROL_MODE(new byte[] { 0x00, (byte)0x42 }),
        START_CONTROL_MODE(new byte[] { 0x00, (byte)0x43 }),
        STOP_CONTROL_MODE(new byte[] { 0x00, (byte)0x44 }),
        CONTROL_COMMAND(new byte[] { 0x00, (byte)0x45 });

        /**
         * Get the current remote control mode. The car will respond with the Control Mode command.
         *
         * @return the command bytes
         */
        public static byte[] getControlMode() {
            return GET_CONTROL_MODE.getIdentifier();
        }

        /**
         * Attempt to start the control mode of the car. The result is sent through the
         * Control Mode command with either Control Started or Control Failed to Start mode.
         *
         * @return the command bytes
         */
        public static byte[] startControlMode() {
            return START_CONTROL_MODE.getIdentifier();
        }

        /**
         * Stop the control mode of the car. The result is sent through the Control Mode
         * command with Control Ended mode.
         *
         * @return the command bytes
         */
        public static byte[] stopControlMode() {
            return STOP_CONTROL_MODE.getIdentifier();
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

            return ByteUtils.concatBytes(CONTROL_COMMAND.getIdentifier(), new byte[] {(byte)speed, msb, lsb});
        }

        RemoteControl(byte[] identifier) {
            this.identifier = identifier;
        }
        private byte[] identifier;
        public byte[] getIdentifier() {
            return identifier;
        }
    }

    /**
     * Commands of the Health category of the Auto API.
     */
    public enum Health implements Type {
        HEART_RATE(new byte[] { 0x00, (byte)0x60 });

        /**
         * Send the driver heart rate to the car.
         *
         * @param heartRate The heart rate in BPM.
         * @return the command bytes
         */
        public static byte[] heartRate(int heartRate) {
            return ByteUtils.concatBytes(HEART_RATE.getIdentifier(), (byte)heartRate);
        }

        Health(byte[] identifier) {
            this.identifier = identifier;
        }
        private byte[] identifier;
        public byte[] getIdentifier() {
            return identifier;
        }
    }

    /**
     * Commands of the Point Of Interest category of the Auto API.
     */
    public enum PointOfInterest implements Type {
        SET_DESTINATION(new byte[] { 0x00, (byte)0x70 });

        /**
         * Set the navigation destination. This will be forwarded to the navigation system of the car.
         *
         * @param destination the destination
         * @return the command bytes
         */
        public static byte[] setDestination(String destination) {
            return ByteUtils.concatBytes(SET_DESTINATION.getIdentifier(), destination.getBytes());
        }

        PointOfInterest(byte[] identifier) {
            this.identifier = identifier;
        }
        private byte[] identifier;
        public byte[] getIdentifier() {
            return identifier;
        }
    }

    /**
     * Commands of the Parcel Delivery category of the Auto API.
     */
    public enum ParcelDelivery implements Type {
        GET_DELIVERED_PARCELS(new byte[] { 0x00, (byte)0x60 }),
        DELIVERED_PARCELS(new byte[] { 0x00, (byte)0x61 });

        /**
         * Get information about all parcels that have been delivered to the car. The car will
         * respond with the Delivered Parcels command.
         *
         * @return the command bytes
         */
        public static byte[] getDeliveredParcels() {
            return GET_DELIVERED_PARCELS.getIdentifier();
        }

        ParcelDelivery(byte[] identifier) {
            this.identifier = identifier;
        }
        private byte[] identifier;
        public byte[] getIdentifier() {
            return identifier;
        }
    }
}