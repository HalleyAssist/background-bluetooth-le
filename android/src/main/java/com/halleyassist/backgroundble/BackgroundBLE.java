package com.halleyassist.backgroundble;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.getcapacitor.Logger;
import com.getcapacitor.plugin.util.AssetUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BackgroundBLE {

    public static final String TAG = "BackgroundBLE";

    private final Context context;

    public BackgroundBLE(Context context) {
        this.context = context;
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

    public String addDevice(String serial, String name) {
        Logger.info(TAG, "AddDevice called with " + serial + " and " + name);
        Map<String, String> devices = loadDevices();
        devices.put(serial, name);
        saveDevices(devices);
        return serial + " " + name;
    }

    public Set<String> addDevices(@NonNull ArrayList<Device> devices) {
        Logger.info(TAG, "AddDevices called with devices " + devices);
        Map<String, String> deviceMap = loadDevices();
        Set<String> results = new HashSet<>();
        for (Device device : devices) {
            deviceMap.put(device.serial, device.name);
            results.add(device.serial + " " + device.name);
        }
        saveDevices(deviceMap);
        return results;
    }

    public String removeDevice(String serial) {
        Logger.info(TAG, "RemoveDevice called with " + serial);
        Map<String, String> devices = loadDevices();
        devices.remove(serial);
        saveDevices(devices);
        return serial;
    }

    public String clearDevices() {
        Logger.info(TAG, "ClearDevices called");
        saveDevices(new HashMap<>());
        return "Cleared";
    }

    public String startForegroundService() {
        Logger.info(TAG, "Starting Foreground Service");
        //  load icon from name
        int iconResourceId = AssetUtil.getResourceID(context, AssetUtil.getResourceBaseName("ic_notification"), "drawable");
        int scanMode = context.getSharedPreferences("com.halley.backgroundble.ScanMode", Context.MODE_PRIVATE).getInt("mode", 0);

        // load device list from key store
        Map<String, String> devices = loadDevices();
        //  convert the devices to a format that can be passed to the service, preserving the key-value pairs
        Bundle devicesBundle = new Bundle();
        for (Map.Entry<String, String> entry : devices.entrySet()) {
            devicesBundle.putString(entry.getKey(), entry.getValue());
        }

        //  create the BackgroundBLEService intent
        Intent serviceIntent = new Intent(context, BackgroundBLEService.class);
        //  pass the device list to the service
        serviceIntent.putExtra("devices", devicesBundle);
        //  pass the icon id to the service
        serviceIntent.putExtra("icon", iconResourceId);
        //  pass the scan mode to the service
        serviceIntent.putExtra("scanMode", scanMode);
        //  start the service
        context.startForegroundService(serviceIntent);

        return "Started";
    }

    public String stopForegroundService() {
        Logger.info(TAG, "StopForegroundService called");
        //  create the BackgroundBLEService intent
        Intent serviceIntent = new Intent(context, BackgroundBLEService.class);
        //  stop the service
        context.stopService(serviceIntent);
        return "Stopped";
    }

    public boolean isRunning() {
        Logger.info(TAG, "IsRunning called");
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (BackgroundBLEService.class.getName().equals(service.service.getClassName())) {
                    Logger.info(TAG, "BackgroundBLEService is running");
                    return true;
                }
            }
        } else {
            Logger.warn(TAG, "ActivityManager is null, unable to determine if BackgroundBLEService is running");
            return false;
        }
        Logger.info(TAG, "BackgroundBLEService is not running");
        return false;
    }

    public List<Device> getDevices() {
        Logger.info(TAG, "getDevices called");
        //  check if the service is running
        if (!isRunning()) {
            Logger.warn(TAG, "BackgroundBLEService is not running, returning empty list");
            return new ArrayList<>();
        }
        //  get devices from the service
        BackgroundBLEService service = BackgroundBLEService.getInstance();
        //  return the devices if the service is not null
        if (service != null) {
            return service.getDevices();
        }
        return new ArrayList<>();
    }

    public void setScanMode(int mode) {
        Logger.info(TAG, "SetScanMode called with mode: " + mode);
        // save the scan mode to shared preferences
        SharedPreferences preferences = context.getSharedPreferences("com.halley.backgroundble.ScanMode", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("mode", mode);
        editor.apply();
    }

    //  load device list from key store
    @NonNull
    private Map<String, String> loadDevices() {
        SharedPreferences preferences = context.getSharedPreferences("com.halley.backgroundble.BLEDevices", Context.MODE_PRIVATE);
        Set<String> keys = preferences.getAll().keySet();
        Map<String, String> deviceMap = new HashMap<>();
        for (String key : keys) {
            deviceMap.put(key, preferences.getString(key, ""));
        }
        return deviceMap;
    }

    private void saveDevices(@NonNull Map<String, String> devices) {
        SharedPreferences preferences = context.getSharedPreferences("com.halley.backgroundble.BLEDevices", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        //  remove any existing devices
        editor.clear();
        for (Map.Entry<String, String> entry : devices.entrySet()) {
            editor.putString(entry.getKey(), entry.getValue());
        }
        editor.apply();
    }
}
