package com.halleyassist.backgroundble;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.getcapacitor.Logger;
import com.getcapacitor.plugin.util.AssetUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BackgroundBLE {

    public static final String TAG = "BackgroundBLE";

    private final Context context;

    public BackgroundBLE(Context context) {
        this.context = context;
    }

    public String addDevice(String id, String name) {
        Logger.info(TAG, "AddDevice called with name: " + name + " and id: " + id);
        Map<String, String> devices = loadDevices();
        devices.put(id, name);
        saveDevices(devices);
        return name + " " + id;
    }

    public Set<String> addDevices(@NonNull ArrayList<Device> devices) {
        Logger.info(TAG, "AddDevices called with devices: " + devices);
        Map<String, String> deviceMap = loadDevices();
        Set<String> results = new HashSet<>();
        for (Device device : devices) {
            deviceMap.put(device.deviceName, device.displayName);
            results.add(device.deviceName + " " + device.displayName);
        }
        saveDevices(deviceMap);
        return results;
    }

    public String removeDevice(String id) {
        Logger.info(TAG, "RemoveDevice called with id: " + id);
        Map<String, String> devices = loadDevices();
        devices.remove(id);
        saveDevices(devices);
        return id;
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
        Logger.info("Wrapping: " + devices);
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
        //  create the BackgroundBLEService intent
        return BackgroundBLEService.isRunning;
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
