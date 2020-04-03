package org.openhab.binding.sma.internal;

import org.openhab.binding.sma.internal.hardware.devices.SmaBluetoothAddress;

public class SmaBinding {

    public class Device {
        private final String plant;
        private final String userPassword;

        public Device(String plant, String userPassword) {
            this.plant = plant;
            this.userPassword = userPassword;
        }

        public SmaBluetoothAddress getBTAdress() {
            return null;
        }

        public String getPassword() {
            return "0000";
        }

        public SmaBluetoothAddress getPlant() {
            return new SmaBluetoothAddress("00:80:25:15:B6:06", 1);
        }

        public boolean isLoginAsInstaller() {
            return false;
        }

    }

    public Device createDevice(String plant, String userPassword) {
        return new Device(plant, userPassword);
    }
}
