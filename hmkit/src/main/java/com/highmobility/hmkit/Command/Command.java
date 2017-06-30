package com.highmobility.hmkit.Command;

import com.highmobility.hmkit.ByteUtils;
import com.highmobility.hmkit.Command.Incoming.WindscreenState;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static com.highmobility.hmkit.Command.Command.Identifier.FAILURE;

/**
 * Created by ttiganik on 25/05/16.
 */
public class Command {
    public enum Identifier {
        FAILURE(new byte[] { 0x00, (byte)0x02 }),
        CAPABILITIES(new byte[] { 0x00, (byte)0x10 }),
        VEHICLE_STATUS(new byte[] { 0x00, (byte)0x11 }),
        DOOR_LOCKS(new byte[] { 0x00, (byte)0x20 }),
        TRUNK_ACCESS(new byte[] { 0x00, (byte)0x21 }),
        WAKE_UP(new byte[] { 0x00, (byte)0x22 }),
        CHARGING(new byte[] { 0x00, (byte)0x23 }),
        CLIMATE(new byte[] { 0x00, (byte)0x24 }),
        ROOFTOP(new byte[] { 0x00, (byte)0x25 }),
        HONK_FLASH(new byte[] { 0x00, (byte)0x26 }),
        REMOTE_CONTROL(new byte[] { 0x00, (byte)0x27 }),
        VALET_MODE(new byte[] { 0x00, (byte)0x28 }),
        HEART_RATE(new byte[] { 0x00, (byte)0x29 }),
        VEHICLE_LOCATION(new byte[] { 0x00, (byte)0x30 }),
        NAVI_DESTINATION(new byte[] { 0x00, (byte)0x31 }),
        DELIVERED_PARCELS(new byte[] { 0x00, (byte)0x32 }),
        DIAGNOSTICS(new byte[] { 0x00, (byte)0x33 }),
        MAINTENANCE(new byte[] { 0x00, (byte)0x34 }),
        ENGINE(new byte[] { 0x00, (byte)0x35 }),
        LIGHTS(new byte[] { 0x00, (byte)0x36 }),
        MESSAGING(new byte[] { 0x00, (byte)0x37 }),
        NOTIFICATIONS(new byte[] { 0x00, (byte)0x38 }),
        WINDOWS(new byte[] { 0x00, (byte)0x45 }),
        WINDSCREEN(new byte[] { 0x00, (byte)0x42 }),
        VIDEO_HANDOVER(new byte[] { 0x00, (byte)0x43 }),
        TEXT_INPUT(new byte[] { 0x00, (byte)0x44 }),
        FUELING(new byte[] { 0x00, (byte)0x40 }),
        DRIVER_FATIGUE(new byte[] { 0x00, (byte)0x41 }),
        THEFT_ALARM(new byte[] { 0x00, (byte)0x46 }),
        PARKING_TICKET(new byte[] { 0x00, (byte)0x47 }),
        KEYFOB_POSITION(new byte[] { 0x00, (byte)0x48 });

        public static Identifier fromIdentifier(byte[] bytes) {
            return fromIdentifier(bytes[0], bytes[1]);
        }

        public static Identifier fromIdentifier(byte firstByte, byte secondByte) {
            Identifier[] allValues = Identifier.values();

            for (int i = 0; i < allValues.length; i++) {
                Identifier identifier = allValues[i];
                if (is(identifier, firstByte, secondByte)) {
                    return identifier;
                }
            }

            return null;
        }

        Identifier(byte[] identifier) {
            this.identifier = identifier;
        }
        private byte[] identifier;
        public byte[] getIdentifier() {
            return identifier;
        }

        static boolean is (Identifier feature, byte firstByte, byte secondByte) {
            return feature.getIdentifier()[0] == firstByte && feature.getIdentifier()[1] == secondByte;
        }
    }

    public interface Type {
        byte getType();
        Identifier getIdentifier();
        byte[] getIdentifierAndType();
    }

    public static Type typeFromBytes(byte identifierByteOne, byte identifierByteTwo, byte type) throws CommandParseException {
        Type parsedType;

        parsedType = FailureMessage.fromBytes(identifierByteOne, identifierByteTwo, type);
        if (parsedType != null) return parsedType;

        parsedType = Capabilities.fromBytes(identifierByteOne, identifierByteTwo, type);
        if (parsedType != null) return parsedType;

        parsedType = VehicleStatus.fromBytes(identifierByteOne, identifierByteTwo, type);
        if (parsedType != null) return parsedType;

        parsedType = DoorLocks.fromBytes(identifierByteOne, identifierByteTwo, type);
        if (parsedType != null) return parsedType;

        parsedType = TrunkAccess.fromBytes(identifierByteOne, identifierByteTwo, type);
        if (parsedType != null) return parsedType;

        parsedType = WakeUp.fromBytes(identifierByteOne, identifierByteTwo, type);
        if (parsedType != null) return parsedType;

        parsedType = Charging.fromBytes(identifierByteOne, identifierByteTwo, type);
        if (parsedType != null) return parsedType;

        parsedType = Climate.fromBytes(identifierByteOne, identifierByteTwo, type);
        if (parsedType != null) return parsedType;

        parsedType = RooftopControl.fromBytes(identifierByteOne, identifierByteTwo, type);
        if (parsedType != null) return parsedType;

        parsedType = HonkFlash.fromBytes(identifierByteOne, identifierByteTwo, type);
        if (parsedType != null) return parsedType;

        parsedType = RemoteControl.fromBytes(identifierByteOne, identifierByteTwo, type);
        if (parsedType != null) return parsedType;

        parsedType = ValetMode.fromBytes(identifierByteOne, identifierByteTwo, type);
        if (parsedType != null) return parsedType;

        parsedType = HeartRate.fromBytes(identifierByteOne, identifierByteTwo, type);
        if (parsedType != null) return parsedType;

        parsedType = VehicleLocation.fromBytes(identifierByteOne, identifierByteTwo, type);
        if (parsedType != null) return parsedType;

        parsedType = NaviDestination.fromBytes(identifierByteOne, identifierByteTwo, type);
        if (parsedType != null) return parsedType;

        parsedType = DeliveredParcels.fromBytes(identifierByteOne, identifierByteTwo, type);
        if (parsedType != null) return parsedType;

        parsedType = Diagnostics.fromBytes(identifierByteOne, identifierByteTwo, type);
        if (parsedType != null) return parsedType;


        throw new CommandParseException();
    }

    public static Type typeFromBytes(byte[] bytes) throws CommandParseException {
        if (bytes.length < 3) throw new CommandParseException();
        return typeFromBytes(bytes[0], bytes[1], bytes[2]);
    }

    /**
     * Commands for the Failure Message category of the Auto API.
     */
    public enum FailureMessage implements Type {
        FAILURE_MESSAGE((byte)0x01);

        static FailureMessage fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.FAILURE.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            FailureMessage[] allValues = FailureMessage.values();

            for (int i = 0; i < allValues.length; i++) {
                FailureMessage command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        FailureMessage(byte messageType) {
            this.messageType = messageType;
        }

        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return FAILURE;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
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
         * Get the vehicle capabilities. The car will respond with the Capabilities message that
         * manifests all different APIs that are enabled on the specific car. It is good practice
         * to only inspect the vehicle capabilities the first time when access is gained. The
         * capabilities are fixed for each car type and will not change between every session
         * unless the user meanwhile receives new permissions (requires a whole new certificate).
         *
         * @return the command bytes
         */
        public static byte[] getCapabilities() {
            return GET_CAPABILITIES.getIdentifierAndType();
        }

        /**
         * Get the capability of a certain feature. The car will respond with the Capability command
         * - to what extent the capability is supported, if at all.
         * @return the command bytes
         */
        public static byte[] getCapability(Identifier identifier) {
            return ByteUtils.concatBytes(GET_CAPABILITY.getIdentifierAndType(), identifier.getIdentifier());
        }

        static Capabilities fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.CAPABILITIES.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            Capabilities[] allValues = Capabilities.values();

            for (int i = 0; i < allValues.length; i++) {
                Capabilities command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        Capabilities(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.CAPABILITIES;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
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
            return GET_VEHICLE_STATUS.getIdentifierAndType();
        }

        static VehicleStatus fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.VEHICLE_STATUS.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            VehicleStatus[] allValues = VehicleStatus.values();

            for (int i = 0; i < allValues.length; i++) {
                VehicleStatus command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        VehicleStatus(byte messageType) {
            this.messageType = messageType;
        }

        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.VEHICLE_STATUS;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
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
            return GET_LOCK_STATE.getIdentifierAndType();
        }

        /**
         * Attempt to lock or unlock the car. The result is received through the Lock State command.
         *
         * @param lock whether to lock or unlock the car
         * @return the command bytes
         */
        public static byte[] lockDoors(boolean lock) {
            return ByteUtils.concatBytes(LOCK_UNLOCK.getIdentifierAndType(), ByteUtils.getByte(lock));
        }

        static DoorLocks fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.DOOR_LOCKS.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            DoorLocks[] allValues = DoorLocks.values();

            for (int i = 0; i < allValues.length; i++) {
                DoorLocks command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        DoorLocks(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.DOOR_LOCKS;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
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
            return GET_TRUNK_STATE.getIdentifierAndType();
        }

        /**
         * Unlock/Lock and Open/Close the trunk. The result is received through the evented
         * Trunk State command.
         *
         * @param lockState whether to lock or unlock the trunk
         * @param position whether to open or close the trunk
         * @return the command bytes
         */
        public static byte[] setTrunkState(Constants.TrunkLockState lockState, Constants.TrunkPosition position) {
            byte[] bytes = new byte[5];
            bytes[0] = OPEN_CLOSE.getIdentifier().getIdentifier()[0];
            bytes[1] = OPEN_CLOSE.getIdentifier().getIdentifier()[1];
            bytes[2] = OPEN_CLOSE.getType();
            bytes[3] = (byte)(lockState == Constants.TrunkLockState.UNLOCKED ? 0x00 : 0x01);
            bytes[4] = (byte)(position == Constants.TrunkPosition.CLOSED ? 0x00 : 0x01);
            return bytes;
        }

        static TrunkAccess fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.TRUNK_ACCESS.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            TrunkAccess[] allValues = TrunkAccess.values();

            for (int i = 0; i < allValues.length; i++) {
                TrunkAccess command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        TrunkAccess(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.TRUNK_ACCESS;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

    /**
     * Commands for the Wake Up category of the Auto API.
     */
    public enum WakeUp implements Type {
        WAKE_UP((byte)0x02);

        /**
         * Wake up the car. This is necessary when the car has fallen asleep, in which case the car
         * responds with the Failure Message to all incoming messages.
         *
         * The car is also waken up by the Lock/Unlock Doors message.
         *
         * @return the command bytes
         */
        public static byte[] wakeUp() {
            return WAKE_UP.getIdentifierAndType();
        }

        static WakeUp fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.WAKE_UP.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            WakeUp[] allValues = WakeUp.values();

            for (int i = 0; i < allValues.length; i++) {
                WakeUp command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        WakeUp(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.WAKE_UP;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

    /**
     * Commands for the Charging category of the Auto API.
     */
    public enum Charging implements Type {
        GET_CHARGE_STATE((byte)0x00),
        CHARGE_STATE((byte)0x01),
        START_STOP_CHARGING((byte)0x02),
        SET_CHARGE_LIMIT((byte)0x03);

        /**
         * Get the charge state. The car will respond with the Charge State message.
         *
         * @return the command bytes
         */
        public static byte[] getChargeState() {
            return GET_CHARGE_STATE.getIdentifierAndType();
        }

        /**
         * Start or stop charging, which can only be controlled when the car is plugged in. The
         * result is sent through the evented Charge State message.
         *
         * @param start Set to true if charging should start
         * @return the command bytes
         */
        public static byte[] startCharging(boolean start) {
            return ByteUtils.concatBytes(START_STOP_CHARGING.getIdentifierAndType(), ByteUtils.getByte(start));
        }

        /**
         * Set the charge limit, to which point the car will charge itself. The result is sent
         * through the evented Charge State message.
         *
         * @param limit The charge limit as percentage between 0-1
         * @return the command bytes
         */
        public static byte[] setChargeLimit(float limit) throws IllegalArgumentException {
            if (limit < 0 || limit > 1) throw new IllegalArgumentException();
            byte limitByte = (byte)(int)(limit * 100);
            return ByteUtils.concatBytes(SET_CHARGE_LIMIT.getIdentifierAndType(), limitByte);
        }

        static Charging fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.CHARGING.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            Charging[] allValues = Charging.values();

            for (int i = 0; i < allValues.length; i++) {
                Charging command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        Charging(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.CHARGING;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

    /**
     * Commands for the Climate category of the Auto API.
     */
    public enum Climate implements Type {
        GET_CLIMATE_STATE((byte)0x00),
        CLIMATE_STATE((byte)0x01),
        SET_CLIMATE_PROFILE((byte)0x02),
        START_STOP_HVAC((byte)0x03),
        START_STOP_DEFOGGING((byte)0x04),
        START_STOP_DEFROSTING((byte)0x05);

        /**
         * Get the climate state. The car will respond with the Climate State message.
         *
         * @return The command bytes
         */
        public static byte[] getClimateState() {
            return GET_CLIMATE_STATE.getIdentifierAndType();
        }

        /**
         * Set the climate profile. The result is sent through the evented Climate State message
         * with the new state.
         *
         * @param states The Auto HVAC states for every weekday. States for all 7 days of the week are expected.
         * @param autoHvacConstant Whether the auto HVAC is constant.
         * @param driverTemperature The driver temperature in Celsius.
         * @param passengerTemperature The passenger temperature in Celsius.
         * @return The command bytes
         * @throws IllegalArgumentException When the input is incorrect
         */
        public static byte[] setClimateProfile(AutoHvacState[] states,
                                               boolean autoHvacConstant,
                                               float driverTemperature,
                                               float passengerTemperature) throws IllegalArgumentException {
            if (states.length != 7) throw new IllegalArgumentException();
            byte[] command = new byte[26];
            ByteUtils.setBytes(command, SET_CLIMATE_PROFILE.getIdentifierAndType(), 0);

            byte autoHvacDatesByte = 0x00;
            for (int i = 0; i < 7; i++) {
                if (states[i].isActive()) {
                    autoHvacDatesByte = (byte) (autoHvacDatesByte | (1 << i));

                }

                command[4 + i * 2] = (byte)states[i].getStartHour();
                command[4 + i * 2 + 1] = (byte)states[i].getStartMinute();
            }
            if (autoHvacConstant) autoHvacDatesByte = (byte)(autoHvacDatesByte | (1 << 7));
            command[3] = autoHvacDatesByte;

            byte[] driverTempByte = ByteBuffer.allocate(4).putFloat(driverTemperature).array();
            ByteUtils.setBytes(command, driverTempByte, 18);

            byte[] passengerTempByte = ByteBuffer.allocate(4).putFloat(passengerTemperature).array();
            ByteUtils.setBytes(command, passengerTempByte, 22);

            return command;
        }

        /**
         * Start or stop the HVAC system to reach driver and passenger set temperatures. The car
         * will use cooling, defrosting and defogging as appropriate. The result is sent through
         * the evented Climate State message.
         *
         * @param start Whether to start the HVAC
         * @return The command bytes
         */
        public static byte[] startHvac(boolean start) {
            return ByteUtils.concatBytes(START_STOP_HVAC.getIdentifierAndType(), ByteUtils.getByte(start));
        }

        /**
         * Manually start or stop defogging. The result is sent through the evented Climate State
         * message.
         *
         * @param start Whether to start the defog
         * @return The command bytes
         */
        public static byte[] startDefog(boolean start) {
            return ByteUtils.concatBytes(START_STOP_DEFOGGING.getIdentifierAndType(), ByteUtils.getByte(start));
        }

        /**
         * Manually start or stop defrosting. The result is sent through the evented Climate State
         * message.
         *
         * @param start Whether to start the defrost
         * @return The command bytes
         */
        public static byte[] startDefrost(boolean start) {
            return ByteUtils.concatBytes(START_STOP_DEFROSTING.getIdentifierAndType(), ByteUtils.getByte(start));
        }

        static Climate fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.CLIMATE.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            Climate[] allValues = Climate.values();

            for (int i = 0; i < allValues.length; i++) {
                Climate command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        Climate(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.CLIMATE;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

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
            return GET_ROOFTOP_STATE.getIdentifierAndType();
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

            byte[] identifier = CONTROL_ROOFTOP.getIdentifierAndType();
            byte[] message = new byte[5];
            message[0] = identifier[0];
            message[1] = identifier[1];
            message[2] = identifier[2];
            message[3] = (byte)(int)(dimPercentage * 100);
            message[4] = (byte)(int)(openPercentage * 100);
            return message;
        }

        static RooftopControl fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.ROOFTOP.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            RooftopControl[] allValues = RooftopControl.values();

            for (int i = 0; i < allValues.length; i++) {
                RooftopControl command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        RooftopControl(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.ROOFTOP;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

    /**
     * Commands for the Honk Horn & Flash Lights category of the Auto API.
     */
    public enum HonkFlash implements Type {
        HONK_FLASH((byte)0x00),
        EMERGENCY_FLASHER((byte)0x01);

        /**
         * Honk the horn and/or flash the lights. This can be done simultaneously or just one
         * action at the time. It is also possible to pass in how many times the lights should
         * be flashed and how many seconds the horn should be honked.
         *
         * @param seconds how many seconds the horn should be honked, between 0 and 5
         * @param lightFlashCount how many times the light should be flashed, between 0 and 10
         * @return the command bytes
         * @throws IllegalArgumentException when the seconds or lightCount parameter is not in the
         * valid range
         */
        public static byte[] honkFlash(int seconds, int lightFlashCount) throws IllegalArgumentException {
            if (seconds < 0 || seconds > 5 || lightFlashCount < 0 || lightFlashCount > 10) {
                throw new IllegalArgumentException();
            }

            byte[] payLoad = new byte[2];
            payLoad[0] = (byte)seconds;
            payLoad[1] = (byte)lightFlashCount;
            return ByteUtils.concatBytes(HONK_FLASH.getIdentifierAndType(), payLoad);
        }

        /**
         * This activates or deactivates the emergency flashers of the car, typically the blinkers
         * to alarm other drivers.
         *
         * @param start wheter to start the emergency flasher or not
         * @return the command bytes
         */
        public static byte[] startEmergencyFlasher(boolean start) {
            return ByteUtils.concatBytes(EMERGENCY_FLASHER.getIdentifierAndType(), ByteUtils.getByte(start));
        }

        static HonkFlash fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.HONK_FLASH.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            HonkFlash[] allValues = HonkFlash.values();

            for (int i = 0; i < allValues.length; i++) {
                HonkFlash command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        HonkFlash(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.HONK_FLASH;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

    /**
     * Commands for the Remote Control category of the Auto API.
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
            return GET_CONTROL_MODE.getIdentifierAndType();
        }

        /**
         * Attempt to start the control mode of the car. The result is sent through the
         * Control Mode command with either Control Started or Control Failed to Start mode.
         *
         * @return the command bytes
         */
        public static byte[] startControlMode() {
            return START_CONTROL_MODE.getIdentifierAndType();
        }

        /**
         * Attempt to stop the control mode of the car. The result is sent through the
         * Control Mode command with Control Ended mode.
         *
         * @return the command bytes
         */
        public static byte[] stopControlMode() {
            return STOP_CONTROL_MODE.getIdentifierAndType();
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

            return ByteUtils.concatBytes(CONTROL_COMMAND.getIdentifierAndType(), new byte[] {(byte)speed, msb, lsb});
        }

        static RemoteControl fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.REMOTE_CONTROL.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            RemoteControl[] allValues = RemoteControl.values();

            for (int i = 0; i < allValues.length; i++) {
                RemoteControl command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        RemoteControl(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.REMOTE_CONTROL;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

    /**
     * Commands for the Valet Mode category of the Auto API.
     */
    public enum ValetMode implements Type {
        GET_VALET_MODE((byte)0x00),
        VALET_MODE((byte)0x01),
        ACTIVATE_DEACTIVATE_VALET_MODE((byte)0x02);

        /**
         * Get the valet mode, which either activated or not. The car will respond with the Valet
         * Mode message.
         *
         * @return The command bytes
         */
        public static byte[] getValetMode() {
            return GET_VALET_MODE.getIdentifierAndType();
        }

        /**
         * Activate or deactivate the Valet Mode. The result is sent through the evented Valet Mode
         * message with either the mode Deactivated or Activated.
         *
         * @param activate Whether to activate the Valet Mode
         * @return The command bytes
         */
        public static byte[] activateValetMode(boolean activate) {
            return ByteUtils.concatBytes(ACTIVATE_DEACTIVATE_VALET_MODE.getIdentifierAndType(),
                    ByteUtils.getByte(activate));
        }

        static ValetMode fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.VALET_MODE.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            ValetMode[] allValues = ValetMode.values();

            for (int i = 0; i < allValues.length; i++) {
                ValetMode command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        ValetMode(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.VALET_MODE;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

    public enum HeartRate implements Type {
        SEND_HEART_RATE((byte)0x00);

        /**
         * Send the driver heart rate to the car.
         *
         * @param heartRate The heart rate in BPM.
         * @return the command bytes
         */
        public static byte[] sendHeartRate(int heartRate) {
            return ByteUtils.concatBytes(SEND_HEART_RATE.getIdentifierAndType(), (byte)heartRate);
        }

        static HeartRate fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.HEART_RATE.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            HeartRate[] allValues = HeartRate.values();

            for (int i = 0; i < allValues.length; i++) {
                HeartRate command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        HeartRate(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.HEART_RATE;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

    /**
     * Commands for the Vehicle Location category of the Auto API.
     */
    public enum VehicleLocation implements Type {
        GET_VEHICLE_LOCATION((byte)0x00),
        VEHICLE_LOCATION((byte)0x01);

        /**
         * Get the vehicle location, which will return the latest recorded coordinates of the car.
         * The car will respond with the Vehicle Location message.
         *
         * @return The command bytes
         */
        public static byte[] getLocation() {
            return GET_VEHICLE_LOCATION.getIdentifierAndType();
        }

        static VehicleLocation fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.VEHICLE_LOCATION.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            VehicleLocation[] allValues = VehicleLocation.values();

            for (int i = 0; i < allValues.length; i++) {
                VehicleLocation command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        VehicleLocation(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.VEHICLE_LOCATION;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

    /**
     * Commands for the Navi Destination category of the Auto API.
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
         * @throws UnsupportedEncodingException when the name string is in wrong format
         */
        public static byte[] setDestination(float latitude, float longitude, String name) throws UnsupportedEncodingException {
            byte[] latitudeBytes = ByteBuffer.allocate(4).putFloat(latitude).array();
            byte[] longitudeBytes = ByteBuffer.allocate(4).putFloat(longitude).array();
            byte[] nameBytes = name.getBytes("UTF-8");
            byte[] destinationBytes = new byte[3 + 8 + 1 + nameBytes.length];

            System.arraycopy(SET_DESTINATION.getIdentifierAndType(), 0, destinationBytes, 0, 3);
            System.arraycopy(latitudeBytes, 0, destinationBytes, 3, 4);
            System.arraycopy(longitudeBytes, 0, destinationBytes, 7, 4);
            destinationBytes[11] = (byte)nameBytes.length;
            System.arraycopy(nameBytes, 0, destinationBytes, 12, nameBytes.length);
            return destinationBytes;
        }

        static NaviDestination fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.NAVI_DESTINATION.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            NaviDestination[] allValues = NaviDestination.values();

            for (int i = 0; i < allValues.length; i++) {
                NaviDestination command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        NaviDestination(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.NAVI_DESTINATION;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
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
            return GET_DELIVERED_PARCELS.getIdentifierAndType();
        }

        static DeliveredParcels fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.DELIVERED_PARCELS.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            DeliveredParcels[] allValues = DeliveredParcels.values();

            for (int i = 0; i < allValues.length; i++) {
                DeliveredParcels command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        DeliveredParcels(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.DELIVERED_PARCELS;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

    // TODO: add comments, implement
    /**
     * Commands for the Diagnostics category of the Auto API.
     */
    public enum Diagnostics implements Type {
        GET_DIAGNOSTICS_STATE((byte)0x00),
        DIAGNOSTICS_STATE((byte)0x01);

        /**
         * Get the diagnostics state of the car. The car will respond with the Diagnostics State message.
         *
         * @return the command bytes
         */
        public static byte[] getDiagnosticsState() {
            return GET_DIAGNOSTICS_STATE.getIdentifierAndType();
        }

        static Diagnostics fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.DIAGNOSTICS.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            Diagnostics[] allValues = Diagnostics.values();

            for (int i = 0; i < allValues.length; i++) {
                Diagnostics command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        Diagnostics(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.DIAGNOSTICS;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

    /**
     * Commands for the Maintenance category of the Auto API.
     */
    public enum Maintenance implements Type {
        GET_MAINTENANCE_STATE((byte)0x00),
        MAINTENANCE_STATE((byte)0x01);

        /**
         * Get the maintenance state, which may include trouble codes. The car will respond with the Maintenance State message.
         *
         * @return the command bytes
         */
        public static byte[] getMaintenanceState() {
            return GET_MAINTENANCE_STATE.getIdentifierAndType();
        }

        static Maintenance fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.MAINTENANCE.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            Maintenance[] allValues = Maintenance.values();

            for (int i = 0; i < allValues.length; i++) {
                Maintenance command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        Maintenance(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.MAINTENANCE;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

    /**
     * Commands for the IgnitionState category of the Auto API.
     */
    public enum Engine implements Type {
        GET_IGNITION_STATE((byte)0x00),
        IGNITION_STATE((byte)0x01),
        TURN_ENGINE_ON_OFF((byte)0x02);

        public static byte[] getIgnitionState() {
            return GET_IGNITION_STATE.getIdentifierAndType();
        }

        public static byte[] turnEngineOn(boolean on) {
            return ByteUtils.concatBytes(TURN_ENGINE_ON_OFF.getIdentifierAndType(), ByteUtils.getByte(on));
        }

        static Engine fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.ENGINE.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            Engine[] allValues = Engine.values();

            for (int i = 0; i < allValues.length; i++) {
                Engine command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        Engine(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.ENGINE;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

    /**
     * Commands for the Lights category of the Auto API.
     */
    public enum Lights implements Type {
        GET_LIGHTS_STATE((byte)0x00),
        LIGHTS_STATE((byte)0x01),
        CONTROL_LIGHTS((byte)0x02);

        public static byte[] getLightsState() {
            return GET_LIGHTS_STATE.getIdentifierAndType();
        }

        /**
         * Set the lights state. The result is sent through the Lights State message.
         *
         * @param frontExteriorLightState Front exterior light state
         * @param rearExteriorLightActive Rear exterior light state
         * @param interiorLightActive Interior light state
         * @param ambientColor Ambient color
         *
         * @return the command bytes
         */
        public static byte[] controlLights(Constants.FrontExteriorLightState frontExteriorLightState,
                                           boolean rearExteriorLightActive,
                                           boolean interiorLightActive,
                                           int ambientColor) {
            byte[] command = CONTROL_LIGHTS.getIdentifierAndType();
            command =  ByteUtils.concatBytes(command, frontExteriorLightState.byteValue());
            command = ByteUtils.concatBytes(command, ByteUtils.getByte(rearExteriorLightActive));
            command = ByteUtils.concatBytes(command, ByteUtils.getByte(interiorLightActive));

            byte[] colorBytes = ByteBuffer.allocate(4).putInt(ambientColor).array();
            byte[] colorBytesWithoutAlpha = Arrays.copyOfRange(colorBytes, 1, 1 + 3);
            command = ByteUtils.concatBytes(command, colorBytesWithoutAlpha);

            return command;
        }

        static Lights fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.LIGHTS.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            Lights[] allValues = Lights.values();

            for (int i = 0; i < allValues.length; i++) {
                Lights command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        Lights(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.LIGHTS;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

    /**
     * Commands for the Windows category of the Auto API.
     */
    public enum Windows implements Type {
        OPEN_CLOSE_WINDOWS((byte)0x02);

        public static byte[] openCloseWindows(WindowState[] windowStates) {
            byte[] command = OPEN_CLOSE_WINDOWS.getIdentifierAndType();
            command = ByteUtils.concatBytes(command, (byte) windowStates.length);

            for (int i = 0; i < windowStates.length; i++) {
                command = ByteUtils.concatBytes(command, windowStates[i].getBytes());
            }

            return command;
        }

        static Windows fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.WINDOWS.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            Windows[] allValues = Windows.values();

            for (int i = 0; i < allValues.length; i++) {
                Windows command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        Windows(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.WINDOWS;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

    /**
     * Commands for the Windscreen category of the Auto API.
     */
    public enum Windscreen implements Type {
        GET_WINDSCREEN_STATE((byte)0x00),
        WINDSCREEN_STATE((byte)0x01),
        SET_WINDSCREEN_DAMAGE((byte)0x02);

        /**
         *
         * @return the command bytes for Get Windscreen State
         */
        public static byte[] getWindscreenState() {
            return GET_WINDSCREEN_STATE.getIdentifierAndType();
        }

        /**
         * Set the windscreen damage. This is for instance used to reset the glass damage or
         * correct it. The result is sent through the Windscreen State message. Damage confidence
         * percentage is automatically set to either 0% or 100%.
         *
         * @param damage Damage amount
         * @param position Damage position
         * @param replacementState The replacement state
         *
         * @return the command bytes
         */
        public static byte[] setWindscreenDamage(WindscreenState.WindscreenDamage damage,
                                                 WindscreenDamagePosition position,
                                                 WindscreenState.WindscreenReplacementState replacementState) {
            byte[] command = SET_WINDSCREEN_DAMAGE.getIdentifierAndType();

            command = ByteUtils.concatBytes(command, damage.getByte());
            command = ByteUtils.concatBytes(command, position.getPositionByte());
            command = ByteUtils.concatBytes(command, replacementState.getByte());

            return command;
        }

        static Windscreen fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.WINDSCREEN.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            Windscreen[] allValues = Windscreen.values();

            for (int i = 0; i < allValues.length; i++) {
                Windscreen command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        Windscreen(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.WINDSCREEN;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

    /**
     * Commands for the Notifications category of the Auto API.
     */
    public enum Notifications implements Type {
        NOTIFICATION((byte)0x00),
        NOTIFICATION_ACTION((byte)0x01);

        /**
         * Send a notification to the car or smart device. The notification can have action items
         * that the user can respond with.
         *
         * @param notification
         * @param actions
         * @return the command bytes
         * @throws UnsupportedEncodingException if the notification is not UTF-8
         */
        public static byte[] notification(String notification, NotificationAction[] actions) throws UnsupportedEncodingException {
            byte[] command = NOTIFICATION.getIdentifierAndType();
            command = ByteUtils.concatBytes(command, (byte)notification.length());
            command = ByteUtils.concatBytes(command, notification.getBytes("UTF-8"));
            command = ByteUtils.concatBytes(command, (byte)actions.length);

            for (int i = 0; i <actions.length; i++) {
                command = ByteUtils.concatBytes(command, actions[i].getBytes());
            }

            return command;
        }

        /**
         * Send an action to a previously received Notification message.
         *
         * @param actionIdentifier
         * @return the command bytes
         */
        public static byte[] notificationAction(int actionIdentifier) {
            return ByteUtils.concatBytes(NOTIFICATION_ACTION.getIdentifierAndType(), (byte)actionIdentifier);
        }

        static Notifications fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.NOTIFICATIONS.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            Notifications[] allValues = Notifications.values();

            for (int i = 0; i < allValues.length; i++) {
                Notifications command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        Notifications(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.NOTIFICATIONS;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

    /**
     * Commands for the Messaging category of the Auto API.
     */
    public enum Messaging implements Type {
        MESSAGE_RECEIVED((byte)0x00),
        SEND_MESSAGE((byte)0x01);

        /**
         *
         * @param handle The sender handle (e.g. phone number)
         * @param message The message text
         * @return the command bytes
         * @throws UnsupportedEncodingException when the text is not UTF-8
         */
        public static byte[] messageReceived(String handle, String message) throws UnsupportedEncodingException {
            byte[] command = MESSAGE_RECEIVED.getIdentifierAndType();

            byte handleLength = (byte)handle.length();
            byte[] handleBytes = handle.getBytes("UTF-8");

            byte messageLength = (byte)message.length();
            byte[] messageBytes = message.getBytes("UTF-8");

            command = ByteUtils.concatBytes(command, handleLength);
            command = ByteUtils.concatBytes(command, handleBytes);
            command = ByteUtils.concatBytes(command, messageLength);
            command = ByteUtils.concatBytes(command, messageBytes);

            return command;
        }

        static Messaging fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.MESSAGING.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            Messaging[] allValues = Messaging.values();

            for (int i = 0; i < allValues.length; i++) {
                Messaging command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        Messaging(byte messageType) {
            this.messageType = messageType;
        }
        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.MESSAGING;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

    /**
     * Commands for the Video Handover category of the Auto API.
     */
    public enum VideoHandover implements Type {
        VIDEO_HANDOVER((byte)0x00);

        /**
         * Hand over a video from smart device to car headunit to be shown in the car display.
         *
         * @param url The URL of the video stream, formatted in UTF-8
         * @param startingSecond The starting second of the video
         * @param location The screen on which to play the video
         * @return the command bytes
         * @throws UnsupportedEncodingException when URL is not UTF-8
         */
        public static byte[] videoHandover(String url, int startingSecond, Constants.ScreenLocation location) throws UnsupportedEncodingException {
            byte[] command = VIDEO_HANDOVER.getIdentifierAndType();

            byte[] urlBytes = url.getBytes("UTF-8");
            command = ByteUtils.concatBytes(command, (byte) url.length());
            command = ByteUtils.concatBytes(command, urlBytes);
            command = ByteUtils.concatBytes(command, (byte) startingSecond);
            command = ByteUtils.concatBytes(command, location.getByte());

            return command;
        }

        static VideoHandover fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.VIDEO_HANDOVER.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            VideoHandover[] allValues = VideoHandover.values();

            for (int i = 0; i < allValues.length; i++) {
                VideoHandover command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        VideoHandover(byte messageType) {
            this.messageType = messageType;
        }

        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.VIDEO_HANDOVER;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

    /**
     * Commands for the Text Input category of the Auto API.
     */
    public enum TextInput implements Type {
        TEXT_INPUT((byte)0x00);

        /**
         * Send a keystroke or entire sentences as input to the car headunit. This can act as an
         * alternative to the input devices that the car is equipped with.
         *
         * @param text The text to send
         * @return the command bytes
         * @throws UnsupportedEncodingException when text is not UTF-8
         */
        public static byte[] textInput(String text) throws UnsupportedEncodingException {
            byte[] command = TEXT_INPUT.getIdentifierAndType();
            command = ByteUtils.concatBytes(command, (byte) text.length());
            command = ByteUtils.concatBytes(command, text.getBytes("UTF-8"));
            return command;
        }

        static TextInput fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.TEXT_INPUT.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            TextInput[] allValues = TextInput.values();

            for (int i = 0; i < allValues.length; i++) {
                TextInput command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        TextInput(byte messageType) {
            this.messageType = messageType;
        }

        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.TEXT_INPUT;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

    /**
     * Commands for the Fueling category of the Auto API.
     */
    public enum Fueling implements Type {
        OPEN_GAS_FLAP((byte)0x02);

        /**
         *
         * @return Open the gas flap of the car. This is possible even if the car is locked.
         */
        public static byte[] openGasFlap() {
            return OPEN_GAS_FLAP.getIdentifierAndType();
        }

        static Fueling fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.FUELING.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            Fueling[] allValues = Fueling.values();

            for (int i = 0; i < allValues.length; i++) {
                Fueling command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        Fueling(byte messageType) {
            this.messageType = messageType;
        }

        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.FUELING;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

    /**
     * Commands for the Driver Fatigue category of the Auto API.
     */
    public enum DriverFatigue implements Type {
        DRIVER_FATIGUE_DETECTED((byte)0x01);

        static DriverFatigue fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.DRIVER_FATIGUE.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            DriverFatigue[] allValues = DriverFatigue.values();

            for (int i = 0; i < allValues.length; i++) {
                DriverFatigue command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        DriverFatigue(byte messageType) {
            this.messageType = messageType;
        }

        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.DRIVER_FATIGUE;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

    /**
     * Commands for the Theft Alarm category of the Auto API.
     */
    public enum TheftAlarm implements Type {
        GET_THEFT_ALARM_STATE((byte)0x00),
        THEFT_ALARM_STATE((byte)0x01),
        SET_THEFT_ALARM((byte)0x02);

        /**
         *
         * @return Get the theft alarm state. The car will respond with the Theft Alarm message.
         */
        public static byte[] getTheftAlarmState() {
            return GET_THEFT_ALARM_STATE.getIdentifierAndType();
        }

        static TheftAlarm fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.THEFT_ALARM.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            TheftAlarm[] allValues = TheftAlarm.values();

            for (int i = 0; i < allValues.length; i++) {
                TheftAlarm command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        TheftAlarm(byte messageType) {
            this.messageType = messageType;
        }

        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.THEFT_ALARM;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

    /**
     * Commands for the Parking Ticket category of the Auto API.
     */
    public enum ParkingTicket implements Type {
        GET_PARKING_TICKET((byte)0x00),
        PARKING_TICKET((byte)0x01),
        START_PARKING((byte)0x02),
        END_PARKING((byte)0x03);

        public static byte[] getParkingTicket() {
            return GET_PARKING_TICKET.getIdentifierAndType();
        }

        /**
         * Start parking. This clears the last parking ticket information and starts a new one.
         * The result is sent through the evented Parking Ticket message. The end time can be left
         * unset depending on the operator.
         *
         * @param operatorName The operator name
         * @param operatorIdentifier The operator identifier
         * @param startDate Ticket start date
         * @param endDate ticket end date
         *
         * @return the command bytes
         * @throws UnsupportedEncodingException when a string is not in UTF-8
         * @throws IllegalArgumentException when input is invalid
         */
        public static byte[] startParking(String operatorName, int operatorIdentifier,
                                          Date startDate, Date endDate) throws UnsupportedEncodingException {
            byte[] command = START_PARKING.getIdentifierAndType();

            command = ByteUtils.concatBytes(command, (byte) operatorName.length());
            command = ByteUtils.concatBytes(command, operatorName.getBytes("UTF-8"));

            byte[] operatorBytes = BigInteger.valueOf(operatorIdentifier).toByteArray();
            command = ByteUtils.concatBytes(command, (byte) operatorBytes.length);
            command = ByteUtils.concatBytes(command, operatorBytes);

            if (startDate == null) throw new IllegalArgumentException();

            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            int year = cal.get(Calendar.YEAR) - 2000;
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            int second = cal.get(Calendar.SECOND);

            command = ByteUtils.concatBytes(command, (byte)year);
            command = ByteUtils.concatBytes(command, (byte)month);
            command = ByteUtils.concatBytes(command, (byte)day);
            command = ByteUtils.concatBytes(command, (byte)hour);
            command = ByteUtils.concatBytes(command, (byte)minute);
            command = ByteUtils.concatBytes(command, (byte)second);

            if (endDate == null) {
                byte[] emptyDate = new byte[6];
                command = ByteUtils.concatBytes(command, emptyDate);
            }
            else {
                cal.setTime(startDate);
                year = cal.get(Calendar.YEAR) - 2000;
                month = cal.get(Calendar.MONTH) + 1;
                day = cal.get(Calendar.DAY_OF_MONTH);
                hour = cal.get(Calendar.HOUR_OF_DAY);
                minute = cal.get(Calendar.MINUTE);
                second = cal.get(Calendar.SECOND);

                command = ByteUtils.concatBytes(command, (byte)year);
                command = ByteUtils.concatBytes(command, (byte)month);
                command = ByteUtils.concatBytes(command, (byte)day);
                command = ByteUtils.concatBytes(command, (byte)hour);
                command = ByteUtils.concatBytes(command, (byte)minute);
                command = ByteUtils.concatBytes(command, (byte)second);
            }

            return command;
        }

        public static byte[] endParking() {
            return END_PARKING.getIdentifierAndType();
        }

        static ParkingTicket fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.PARKING_TICKET.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            ParkingTicket[] allValues = ParkingTicket.values();

            for (int i = 0; i < allValues.length; i++) {
                ParkingTicket command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        ParkingTicket(byte messageType) {
            this.messageType = messageType;
        }

        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.PARKING_TICKET;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }

    /**
     * Commands for the Keyfob position category of the Auto API.
     */
    public enum KeyfobPosition implements Type {
        GET_KEYFOB_POSITION((byte)0x00),
        KEYFOB_POSITION((byte)0x01);

        public static byte[] getKeyfobPosition() {
            return GET_KEYFOB_POSITION.getIdentifierAndType();
        }

        static KeyfobPosition fromBytes(byte firstIdentifierByte, byte secondIdentifierByte, byte typeByte) {
            byte[] identiferBytes = Identifier.KEYFOB_POSITION.getIdentifier();
            if (firstIdentifierByte != identiferBytes[0]
                    || secondIdentifierByte != identiferBytes[1]) {
                return null;
            }

            KeyfobPosition[] allValues = KeyfobPosition.values();

            for (int i = 0; i < allValues.length; i++) {
                KeyfobPosition command = allValues[i];
                byte commandType = command.getType();

                if (commandType == typeByte) {
                    return command;
                }
            }

            return null;
        }

        KeyfobPosition(byte messageType) {
            this.messageType = messageType;
        }

        private byte messageType;
        public byte getType() {
            return messageType;
        }

        @Override
        public Identifier getIdentifier() {
            return Identifier.KEYFOB_POSITION;
        }

        @Override
        public byte[] getIdentifierAndType() {
            return ByteUtils.concatBytes(getIdentifier().getIdentifier(), getType());
        }
    }
}