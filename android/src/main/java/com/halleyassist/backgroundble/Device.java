package com.halleyassist.backgroundble;

import static android.bluetooth.le.ScanResult.TX_POWER_NOT_PRESENT;

import androidx.annotation.NonNull;
import com.getcapacitor.JSObject;

public class Device {

    /**
     * The serial of the device. Used to filter devices when scanning.
     */
    String serial;
    /**
     * The name of the device. Used to display the device in the UI.
     */
    String name;

    float rssi = 0;
    int txPower = TX_POWER_NOT_PRESENT;

    // last updated time
    long lastUpdated = 0;

    public Device(String serial, String name) {
        this.serial = serial;
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    /*
      Estimate the distance to the device, based on the RSSI and TX power (not precise, but close enough)
    public String estimateDistance() {
        if (txPower == TX_POWER_NOT_PRESENT || rssi == 0) {
            return "rssi: " + rssi;
        }
        //  RSSI = -10n log10(d) + A
        //  d = 10 ^ ((A - RSSI) / (10 * n))
        double exponent = (txPower - rssi) / (10 * 2.0);
        Log.d(TAG, "txPower: " + txPower + ", rssi: " + rssi + ", exponent: " + exponent);
        return String.format(Locale.ENGLISH, "%.2fm", Math.pow(10, exponent));
    }
    */

    public void update(float rssi, int txPower) {
        this.rssi = rssi;
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
