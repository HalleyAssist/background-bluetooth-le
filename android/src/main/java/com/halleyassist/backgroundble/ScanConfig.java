package com.halleyassist.backgroundble;

import androidx.annotation.NonNull;
import com.getcapacitor.JSObject;
import com.halleyassist.backgroundble.Device.Device;
import java.util.ArrayList;
import java.util.List;

public class ScanConfig {

    public static final String[] KEYS = new String[] { "mode", "debug", "deviceTimeout" };

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

    public ScanConfig(JSObject config) {
        this.mode = config.getInteger("mode", 0);
        this.debug = config.getBoolean("debug", false);
        this.deviceTimeout = config.getInteger("deviceTimeout", 30000);
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
        ret.put("mode", mode);
        ret.put("debug", debug);
        ret.put("deviceTimeout", deviceTimeout);
        return ret;
    }
}
