package com.halleyassist.backgroundble;

import static com.halleyassist.backgroundble.BLEDataStore.KEY_DEBUG;
import static com.halleyassist.backgroundble.BLEDataStore.KEY_DEVICE_TIMEOUT;
import static com.halleyassist.backgroundble.BLEDataStore.KEY_SCAN_MODE;
import static com.halleyassist.backgroundble.BLEDataStore.KEY_THRESHOLD;

import androidx.annotation.NonNull;
import com.getcapacitor.JSObject;
import com.halleyassist.backgroundble.Device.Device;
import java.util.ArrayList;
import java.util.List;

public class ScanConfig {

    private static final int MAX_THRESHOLD = -10;
    private static final int MIN_THRESHOLD = -100;

    private Integer mode;
    private Boolean debug;
    private Integer deviceTimeout;
    private Integer threshold;

    public List<Device> devices = new ArrayList<>();
    public String activeDevice = null;

    public ScanConfig() {
        // Default values
        this.mode = 0; // ScanMode.LOW_POWER
        this.debug = false;
        this.deviceTimeout = 30000;
        this.threshold = -100;
    }

    public ScanConfig(@NonNull JSObject config) {
        this.mode = config.getInteger(KEY_SCAN_MODE, 0);
        this.debug = config.getBoolean(KEY_DEBUG, false);
        this.deviceTimeout = config.getInteger(KEY_DEVICE_TIMEOUT, 30000);
        this.threshold = config.getInteger(KEY_THRESHOLD, -100);
    }

    public Integer getMode() {
        return mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    public Boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public Integer getDeviceTimeout() {
        return deviceTimeout;
    }

    public void setDeviceTimeout(Integer deviceTimeout) {
        this.deviceTimeout = deviceTimeout;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        // Ensure threshold is within the range of -10 to -100
        this.threshold = Math.max(MIN_THRESHOLD, Math.min(threshold, MAX_THRESHOLD));
    }

    public JSObject toJSObject() {
        JSObject ret = new JSObject();
        ret.put(KEY_SCAN_MODE, mode);
        ret.put(KEY_DEBUG, debug);
        ret.put(KEY_DEVICE_TIMEOUT, deviceTimeout);
        ret.put(KEY_THRESHOLD, threshold);
        return ret;
    }
}
