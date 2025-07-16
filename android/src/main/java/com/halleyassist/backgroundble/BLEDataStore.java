package com.halleyassist.backgroundble;

import android.content.Context;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.rxjava2.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava2.RxDataStore;

public class BLEDataStore {

    // A singleton class to handle the data store for the plugin and service
    private static volatile BLEDataStore instance;
    private static RxDataStore<Preferences> dataStore = null;

    private static final String DATA_STORE_NAME = "background_ble";

    static final String KEY_DEVICE_TIMEOUT = "deviceTimeout";
    static final String KEY_SCAN_MODE = "scanMode";
    static final String KEY_DEBUG = "debug";
    static final String KEY_THRESHOLD = "threshold";
    static final String KEY_STOPPED = "stopped";

    public static final String[] KEYS = new String[] { KEY_SCAN_MODE, KEY_DEBUG, KEY_DEVICE_TIMEOUT, KEY_STOPPED, KEY_THRESHOLD };

    private BLEDataStore(Context context) {
        if (dataStore == null) {
            dataStore = new RxPreferenceDataStoreBuilder(context, DATA_STORE_NAME).build();
        }
    }

    //  get the instance of the data store
    public static BLEDataStore getInstance(Context context) {
        if (instance == null) {
            synchronized (BLEDataStore.class) {
                if (instance == null) {
                    instance = new BLEDataStore(context);
                }
            }
        }
        return instance;
    }

    public RxDataStore<Preferences> getDataStore() {
        return dataStore;
    }
}
