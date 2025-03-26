package com.halleyassist.backgroundble;

import static com.halleyassist.backgroundble.BackgroundBLEService.EXTRA_DEBUG_MODE;
import static com.halleyassist.backgroundble.BackgroundBLEService.EXTRA_DEVICES;
import static com.halleyassist.backgroundble.BackgroundBLEService.EXTRA_DEVICE_TIMEOUT;
import static com.halleyassist.backgroundble.BackgroundBLEService.EXTRA_ICON;
import static com.halleyassist.backgroundble.BackgroundBLEService.EXTRA_SCAN_MODE;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava2.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava2.RxDataStore;
import com.getcapacitor.plugin.util.AssetUtil;
import com.halleyassist.backgroundble.Device.Device;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BackgroundBLE {

    public static final String TAG = "BackgroundBLE";

    public static final String KEY_SCAN_MODE = "mode";
    public static final String KEY_DEBUG = "debug";
    public static final String KEY_DEVICE_TIMEOUT = "deviceTimeout";

    private final Context context;
    private static RxDataStore<Preferences> dataStore = null;

    public BackgroundBLE(Context context) {
        this.context = context;
        if (dataStore == null) {
            dataStore = new RxPreferenceDataStoreBuilder(context, "background_ble").build();
        }
    }

    public void canUseBluetooth() {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            throw new RuntimeException("BLE is not supported.");
        }

        //  get bluetooth adaptor
        BluetoothManager bluetoothManager = context.getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            throw new RuntimeException("BLE is not available.");
        }
    }

    public Single<List<Device>> getDevices() {
        //  check if the service is running
        if (!isRunning()) {
            //  when not running, return the saved devices
            return loadDevices();
        }
        //  get devices from the service
        BackgroundBLEService service = BackgroundBLEService.getInstance();
        //  return the devices if the service is not null
        if (service != null) {
            return Single.just(service.getDevices());
        }
        return Single.just(new ArrayList<>());
    }

    public Single<List<Device>> setDevices(@NonNull List<Device> devices) {
        return saveDevices(devices);
    }

    public Single<List<Device>> clearDevices() {
        return saveDevices(new ArrayList<>());
    }

    public Single<String> startForegroundService() {
        int iconResourceId = AssetUtil.getResourceID(context, AssetUtil.getResourceBaseName("ic_notification"), "drawable");
        return getConfigWithDevices()
            .map(config -> {
                Bundle devicesBundle = new Bundle();
                for (Device device : config.devices) {
                    devicesBundle.putString(device.serial, device.name);
                }

                Intent serviceIntent = new Intent(context, BackgroundBLEService.class);
                serviceIntent.putExtra(EXTRA_DEVICES, devicesBundle);
                serviceIntent.putExtra(EXTRA_ICON, iconResourceId);
                serviceIntent.putExtra(EXTRA_SCAN_MODE, config.getMode());
                serviceIntent.putExtra(EXTRA_DEBUG_MODE, config.isDebug());
                serviceIntent.putExtra(EXTRA_DEVICE_TIMEOUT, config.getDeviceTimeout());
                context.startForegroundService(serviceIntent);

                return "Started";
            });
    }

    public String stopForegroundService() {
        //  create the BackgroundBLEService intent
        Intent serviceIntent = new Intent(context, BackgroundBLEService.class);
        //  stop the service
        context.stopService(serviceIntent);
        return "Stopped";
    }

    public boolean isRunning() {
        BackgroundBLEService service = BackgroundBLEService.getInstance();
        if (service != null) {
            return service.isRunning();
        }
        return false;
    }

    public Single<ScanConfig> getScanConfig() {
        Single<Map<Preferences.Key<?>, ?>> preferences = dataStore.data().firstOrError().map(Preferences::asMap);
        String[] keys = ScanConfig.KEYS;
        return preferences.map(pref -> {
            ScanConfig config = new ScanConfig();
            for (String key : keys) {
                if (pref.containsKey(PreferencesKeys.stringKey(key))) {
                    switch (key) {
                        case KEY_SCAN_MODE:
                            config.setMode((int) pref.get(PreferencesKeys.intKey(key)));
                            break;
                        case KEY_DEBUG:
                            config.setDebug((boolean) pref.get(PreferencesKeys.booleanKey(key)));
                            break;
                        case KEY_DEVICE_TIMEOUT:
                            config.setDeviceTimeout((int) pref.get(PreferencesKeys.intKey(key)));
                            break;
                    }
                }
            }
            return config;
        });
    }

    public Single<ScanConfig> setScanConfig(@NonNull ScanConfig config) {
        return dataStore
            .updateDataAsync(preferences -> {
                MutablePreferences mutablePreferences = preferences.toMutablePreferences();
                mutablePreferences.set(PreferencesKeys.intKey(KEY_SCAN_MODE), config.getMode());
                mutablePreferences.set(PreferencesKeys.booleanKey(KEY_DEBUG), config.isDebug());
                mutablePreferences.set(PreferencesKeys.intKey(KEY_DEVICE_TIMEOUT), config.getDeviceTimeout());
                return Single.just(mutablePreferences);
            })
            .map(preferences -> config);
    }

    //  load device list from key store
    @NonNull
    private Single<List<Device>> loadDevices() {
        //  get all the keys values
        Single<Map<Preferences.Key<?>, ?>> preferences = dataStore.data().firstOrError().map(Preferences::asMap);
        //  iterate map
        return preferences.map(pref -> {
            List<Device> deviceList = new ArrayList<>();
            String[] ignoreKeys = ScanConfig.KEYS;
            // iterate map, if the key is in the ignore array skip
            for (Map.Entry<Preferences.Key<?>, ?> entry : pref.entrySet()) {
                if (Arrays.stream(ignoreKeys).anyMatch(k -> k.equals(entry.getKey().toString()))) {
                    continue;
                }
                //  get the device name
                String name = entry.getValue().toString();
                //  add the device to the list
                deviceList.add(new Device(entry.getKey().toString(), name));
            }
            return deviceList;
        });
    }

    @NonNull
    private Single<List<Device>> saveDevices(@NonNull List<Device> devices) {
        return dataStore
            .updateDataAsync(preferences -> {
                MutablePreferences mutablePreferences = preferences.toMutablePreferences();
                // get all the keys in the preferences
                Set<Preferences.Key<?>> keys = mutablePreferences.asMap().keySet();
                String[] ignoreKeys = ScanConfig.KEYS;
                // if any key is not in the proved device list, remove the entry
                for (Preferences.Key<?> key : keys) {
                    if (Arrays.stream(ignoreKeys).anyMatch(k -> k.equals(key.toString()))) {
                        continue;
                    }
                    if (devices.stream().noneMatch(device -> device.serial.equals(key.toString()))) {
                        mutablePreferences.remove(key);
                    }
                }
                // iterate map
                for (Device device : devices) {
                    Preferences.Key<String> key = PreferencesKeys.stringKey(device.serial);
                    mutablePreferences.set(key, device.name);
                }
                return Single.just(mutablePreferences);
            })
            .map(preferences -> devices);
    }

    @NonNull
    private Single<ScanConfig> getConfigWithDevices() {
        //  return every key in the preferences
        Single<Map<Preferences.Key<?>, ?>> preferences = dataStore.data().firstOrError().map(Preferences::asMap);
        return preferences.map(prefs -> {
            ScanConfig config = new ScanConfig();
            for (Map.Entry<Preferences.Key<?>, ?> entry : prefs.entrySet()) {
                switch (entry.getKey().toString()) {
                    case "mode":
                        config.setMode((int) entry.getValue());
                        break;
                    case "debug":
                        config.setDebug((boolean) entry.getValue());
                        break;
                    case "deviceTimeout":
                        config.setDeviceTimeout((int) entry.getValue());
                        break;
                    default:
                        config.devices.add(new Device(entry.getKey().toString(), entry.getValue().toString()));
                        break;
                }
            }
            return config;
        });
    }
}
