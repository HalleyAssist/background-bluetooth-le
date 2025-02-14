package com.halleyassist.backgroundble;

import static android.bluetooth.le.ScanResult.TX_POWER_NOT_PRESENT;
import static com.halleyassist.backgroundble.BackgroundBLE.TAG;

import android.util.Log;
import androidx.annotation.NonNull;
import java.util.Locale;

public class Device {

    /**
     * The serial of the device. Used to filter devices when scanning.
     */
    String serial;
    /**
     * The name of the device. Used to display the device in the UI.
     */
    String name;

    int rssi = 0;
    int txPower = TX_POWER_NOT_PRESENT;

    public Device(String serial, String name) {
        this.serial = serial;
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return name + ": " + estimateDistance();
    }

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
}
