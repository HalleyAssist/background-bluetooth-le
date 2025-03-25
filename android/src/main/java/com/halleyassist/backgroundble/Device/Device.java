package com.halleyassist.backgroundble.Device;

import static android.bluetooth.le.ScanResult.TX_POWER_NOT_PRESENT;

import androidx.annotation.NonNull;
import com.getcapacitor.JSObject;

public class Device {

    /**
     * The serial of the device. Used to filter devices when scanning.
     */
    public String serial;
    /**
     * The name of the device. Used to display the device in the UI.
     */
    public String name;

    public float rssi = -127;
    int txPower = TX_POWER_NOT_PRESENT;

    // last updated time
    public long lastUpdated = 0;

    public Device(String serial, String name) {
        this.serial = serial;
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    public void update(float rssi, int txPower) {
        float smoothingFactor = 0.8f;
        this.rssi = smoothingFactor * rssi + (1 - smoothingFactor) * this.rssi;
        this.txPower = txPower;
        if (rssi != -127) {
            //  only update the last updated time if the rssi is not -127
            this.lastUpdated = System.currentTimeMillis();
        }
    }

    public JSObject toObject() {
        JSObject object = new JSObject();
        object.put("serial", serial);
        object.put("name", name);
        object.put("rssi", rssi);
        object.put("txPower", txPower);
        object.put("lastUpdated", lastUpdated);
        return object;
    }
}
