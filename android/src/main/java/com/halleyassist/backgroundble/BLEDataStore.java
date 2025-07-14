package com.halleyassist.backgroundble;

public class BLEDataStore {

    static final String DATA_STORE_NAME = "background_ble";

    static final String KEY_DEVICE_TIMEOUT = "deviceTimeout";
    static final String KEY_SCAN_MODE = "scanMode";
    static final String KEY_DEBUG = "debug";
    static final String KEY_THRESHOLD = "threshold";
    static final String KEY_STOPPED = "stopped";

    public static final String[] KEYS = new String[] { KEY_SCAN_MODE, KEY_DEBUG, KEY_DEVICE_TIMEOUT, KEY_STOPPED, KEY_THRESHOLD };
}
