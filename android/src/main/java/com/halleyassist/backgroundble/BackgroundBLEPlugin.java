package com.halleyassist.backgroundble;

import static com.halleyassist.backgroundble.BackgroundBLE.TAG;

import android.Manifest;
import androidx.annotation.NonNull;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

@CapacitorPlugin(
    name = "BackgroundBLE",
    permissions = {
        @Permission(alias = "bluetooth", strings = { Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_SCAN }),
        @Permission(alias = "notifications", strings = { Manifest.permission.POST_NOTIFICATIONS })
    }
)
public class BackgroundBLEPlugin extends Plugin {

    private BackgroundBLE implementation;

    @Override
    public void load() {
        implementation = new BackgroundBLE(getContext());
        Logger.info(TAG, "Loaded BackgroundBLEPlugin");
    }

    @PermissionCallback
    public void bluetoothPermissionCallback(PluginCall call) {
        if (getPermissionState("bluetooth") == PermissionState.GRANTED) {
            call.resolve();
        } else {
            call.reject("Bluetooth permission is required to use this plugin");
        }
    }

    @PermissionCallback
    public void notificationsPermissionCallback(PluginCall call) {
        if (getPermissionState("notifications") == PermissionState.GRANTED) {
            call.resolve();
        } else {
            call.reject("Notification permission is required to use this plugin");
        }
    }

    @PluginMethod
    public void addDevice(@NonNull PluginCall call) {
        String name = call.getString("name");
        String displayName = call.getString("displayName");
        String result = implementation.addDevice(name, displayName);
        JSObject ret = new JSObject();
        ret.put("result", result);
        call.resolve(ret);
    }

    @PluginMethod
    public void addDevices(@NonNull PluginCall call) {
        JSArray devices = call.getArray("devices");
        try {
            List<JSONObject> deviceList = devices.toList();
            ArrayList<Device> list = new ArrayList<>();
            //  parse the devices array
            for (JSONObject object : deviceList) {
                try {
                    Device device = new Device(object.getString("name"), object.getString("displayName"));
                    list.add(device);
                } catch (JSONException e) {
                    Logger.error("BackgroundBLE Error parsing device: " + e.getMessage());
                }
            }

            Set<String> result = implementation.addDevices(list);
            JSObject ret = new JSObject();
            ret.put("result", result.toString());
            call.resolve(ret);
        } catch (JSONException ex) {
            call.reject(ex.toString());
        }
    }

    @PluginMethod
    public void removeDevice(@NonNull PluginCall call) {
        String id = call.getString("id");
        String result = implementation.removeDevice(id);
        JSObject ret = new JSObject();
        ret.put("result", result);
        call.resolve(ret);
    }

    @PluginMethod
    public void clearDevices(@NonNull PluginCall call) {
        String result = implementation.clearDevices();
        JSObject ret = new JSObject();
        ret.put("result", result);
        call.resolve(ret);
    }

    @PluginMethod
    public void startForegroundService(@NonNull PluginCall call) {
        String result = implementation.startForegroundService();
        Logger.info(TAG, "Started Foreground Service: " + result);
        call.resolve();
    }

    @PluginMethod
    public void stopForegroundService(@NonNull PluginCall call) {
        String result = implementation.stopForegroundService();
        Logger.info(TAG, "Stopped Foreground Service: " + result);
        call.resolve();
    }

    @PluginMethod
    public void isRunning(@NonNull PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("running", implementation.isRunning());
        call.resolve(ret);
    }

    /**
     * Sets the scan mode for the plugin.
     *
     * @param call The plugin call containing the 'mode' parameter.
     */
    @PluginMethod
    public void setScanMode(@NonNull PluginCall call) {
        Integer mode = call.getInt("mode");

        if (mode == null) {
            call.reject("Scan mode is required", "MISSING_PARAMETER");
            return;
        }

        // Validate the mode if necessary
        if (!isValidScanMode(mode)) {
            call.reject("Invalid scan mode", "INVALID_PARAMETER_VALUE");
            return;
        }

        try {
            implementation.setScanMode(mode);
            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to set scan mode", e);
        }
    }

    /**
     * Checks if the given scan mode is valid.
     *
     * @param mode The scan mode to check.
     * @return True if the mode is valid, false otherwise.
     */
    private boolean isValidScanMode(int mode) {
        // Example: Check if the mode is within a specific range or a known set of values
        return mode >= -1 && mode <= 2; // Example: Valid modes are 0, 1, and 2
    }
}
