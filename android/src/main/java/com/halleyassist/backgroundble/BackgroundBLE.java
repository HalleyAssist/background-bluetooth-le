package com.halleyassist.backgroundble;

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
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BackgroundBLE {

    public static final String TAG = "BackgroundBLE";

    private final Context context;
    private final RxDataStore<Preferences> dataStore;

    public BackgroundBLE(Context context) {
        this.context = context;
        this.dataStore = new RxPreferenceDataStoreBuilder(context, "background_ble").build();
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

    public Single<List<Device>> addDevice(String serial, String name) {
        return loadDevices()
            .flatMap(devices -> {
                devices.add(new Device(serial, name));
                return saveDevices(devices);
            });
    }

    public Single<List<Device>> addDevices(@NonNull List<Device> devices) {
        return loadDevices()
            .flatMap(devices1 -> {
                // add any device that does not already exist
                for (Device device : devices) {
                    if (devices1.stream().noneMatch(d -> d.serial.equals(device.serial))) {
                        devices1.add(device);
                    }
                }
                return saveDevices(devices1);
            });
    }

    public Single<List<Device>> removeDevice(String serial) {
        return loadDevices()
            .flatMap(devices -> {
                devices.removeIf(device -> device.serial.equals(serial));
                return saveDevices(devices);
            });
    }

    public Single<List<Device>> clearDevices() {
        return saveDevices(new ArrayList<>());
    }

    public Single<String> startForegroundService() {
        int iconResourceId = AssetUtil.getResourceID(context, AssetUtil.getResourceBaseName("ic_notification"), "drawable");
        int scanMode = getScanMode();

        return loadDevices()
            .map(devices -> {
                Bundle devicesBundle = new Bundle();
                for (Device device : devices) {
                    devicesBundle.putString(device.serial, device.name);
                }

                Intent serviceIntent = new Intent(context, BackgroundBLEService.class);
                serviceIntent.putExtra("devices", devicesBundle);
                serviceIntent.putExtra("icon", iconResourceId);
                serviceIntent.putExtra("scanMode", scanMode);
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

    public Single<Integer> setScanMode(int mode) {
        Preferences.Key<Integer> key = PreferencesKeys.intKey("scanMode");
        return dataStore
            .updateDataAsync(preferences -> {
                MutablePreferences mutablePreferences = preferences.toMutablePreferences();
                mutablePreferences.set(key, mode);
                return Single.just(mutablePreferences);
            })
            .map(preferences -> preferences.get(key));
    }

    //  load device list from key store
    @NonNull
    private Single<List<Device>> loadDevices() {
        //  get all the keys values
        Single<Map<Preferences.Key<?>, ?>> preferences = dataStore.data().firstOrError().map(Preferences::asMap);
        //  iterate map
        return preferences.map(pref -> {
            List<Device> deviceList = new ArrayList<>();
            // iterate map, if the key is 'scanMode' skip
            for (Map.Entry<Preferences.Key<?>, ?> entry : pref.entrySet()) {
                if (entry.getKey().toString().equals("scanMode")) {
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
                // if any key is not in the proved device list, remove the entry
                for (Preferences.Key<?> key : keys) {
                    if (key.toString().equals("scanMode")) {
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

    private int getScanMode() {
        Preferences.Key<Integer> key = PreferencesKeys.intKey("scanMode");
        Single<Integer> value = dataStore.data().firstOrError().map(prefs -> prefs.get(key)).onErrorReturnItem(0);
        return value.blockingGet();
    }
}
