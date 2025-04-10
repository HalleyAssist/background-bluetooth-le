package com.halleyassist.backgroundble;

import static com.halleyassist.backgroundble.BLEDataStore.KEY_DEBUG;
import static com.halleyassist.backgroundble.BLEDataStore.KEY_DEVICE_TIMEOUT;
import static com.halleyassist.backgroundble.BLEDataStore.KEY_SCAN_MODE;

import androidx.annotation.NonNull;
import com.getcapacitor.JSObject;
import com.halleyassist.backgroundble.Device.Device;
import java.util.ArrayList;
import java.util.List;

public class ScanConfig {

    private Integer mode;
    private Boolean debug;
    private Integer deviceTimeout;

    public List<Device> devices = new ArrayList<>();

    public ScanConfig() {
        // Default values
        this.mode = 0; // ScanMode.LOW_POWER
        this.debug = false;
        this.deviceTimeout = 30000;
    }

    public ScanConfig(Integer mode, Boolean debug, Integer deviceTimeout) {
        this.mode = mode;
        this.debug = debug;
        this.deviceTimeout = deviceTimeout;
    }

    public ScanConfig(@NonNull JSObject config) {
        this.mode = config.getInteger(KEY_SCAN_MODE, 0);
        this.debug = config.getBoolean(KEY_DEBUG, false);
        this.deviceTimeout = config.getInteger(KEY_DEVICE_TIMEOUT, 30000);
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

    public JSObject toJSObject() {
        JSObject ret = new JSObject();
        ret.put(KEY_SCAN_MODE, mode);
        ret.put(KEY_DEBUG, debug);
        ret.put(KEY_DEVICE_TIMEOUT, deviceTimeout);
        return ret;
    }
}
