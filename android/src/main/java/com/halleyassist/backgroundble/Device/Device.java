package com.halleyassist.backgroundble.Device;

import static android.bluetooth.le.ScanResult.TX_POWER_NOT_PRESENT;

import androidx.annotation.NonNull;
import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;

public class Device {

    /**
     * The serial of the device. Used to filter devices when scanning.
     */
    public String serial;
    /**
     * The name of the device. Used to display the device in the UI.
     */
    public String name;

    public float rssi = 0;
    int txPower = TX_POWER_NOT_PRESENT;

    // last updated time
    public long lastUpdated = 0;

    MedianSmoother medianSmoother = new MedianSmoother(5);

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
        float median = medianSmoother.smooth(rssi);
        float smoothingFactor = 0.8f;
        this.rssi = smoothingFactor * median + (1 - smoothingFactor) * this.rssi;
        Logger.debug("BackgroundBLE.Device", name + " RSSI: " + rssi + ", median: " + median + ", smoothed: " + this.rssi);
        this.txPower = txPower;
        this.lastUpdated = System.currentTimeMillis();
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
