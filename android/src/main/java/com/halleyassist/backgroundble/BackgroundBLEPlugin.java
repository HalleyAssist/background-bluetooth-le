package com.halleyassist.backgroundble;

import static com.halleyassist.backgroundble.BackgroundBLE.TAG;

import android.Manifest;
import android.os.Build;
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
import com.halleyassist.backgroundble.Device.Device;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

@CapacitorPlugin(
    name = "BackgroundBLE",
    permissions = {
        @Permission(alias = "BLUETOOTH", strings = { Manifest.permission.BLUETOOTH }),
        @Permission(alias = "ACCESS_FINE_LOCATION", strings = { Manifest.permission.ACCESS_FINE_LOCATION }),
        @Permission(alias = "BLUETOOTH_SCAN", strings = { Manifest.permission.BLUETOOTH_SCAN }),
        @Permission(alias = "BLUETOOTH_CONNECT", strings = { Manifest.permission.BLUETOOTH_CONNECT }),
        @Permission(alias = "POST_NOTIFICATIONS", strings = { Manifest.permission.POST_NOTIFICATIONS })
    }
)
public class BackgroundBLEPlugin extends Plugin {

    private BackgroundBLE implementation;
    private List<String> permissions;

    @Override
    public void load() {
        implementation = new BackgroundBLE(getContext());
        Logger.info(TAG, "Loaded BackgroundBLEPlugin");
    }

    @PluginMethod
    public void initialise(@NonNull PluginCall call) {
        String[] aliases = getRequiredPermissions().toArray(new String[0]);
        Logger.info(TAG, "requesting permissions for " + Arrays.toString(aliases));
        try {
            requestPermissionForAliases(aliases, call, "checkPermission");
        } catch (Exception e) {
            call.reject(e.getMessage());
        }
    }

    @NonNull
    private List<String> getRequiredPermissions() {
        permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add("BLUETOOTH_SCAN");
            permissions.add("BLUETOOTH_CONNECT");
        } else {
            permissions.add("ACCESS_FINE_LOCATION");
            permissions.add("BLUETOOTH");
        }
        permissions.add("POST_NOTIFICATIONS");
        return permissions;
    }

    @PermissionCallback
    private void checkPermission(PluginCall call) {
        String[] aliases = permissions.toArray(new String[0]);
        Logger.info(TAG, "checking permissions for " + Arrays.toString(aliases));
        List<Boolean> granted = new ArrayList<>();
        for (String alias : aliases) {
            granted.add(getPermissionState(alias) == PermissionState.GRANTED);
        }
        if (granted.stream().allMatch(Boolean::booleanValue)) {
            try {
                implementation.canUseBluetooth();
                call.resolve();
            } catch (Exception e) {
                call.reject(e.getMessage());
            }
        } else {
            call.reject("Permission denied.");
        }
    }

    /**
     * Gets the devices that have been found by the plugin.
     */
    @PluginMethod
    public Disposable getDevices(@NonNull PluginCall call) {
        return implementation
            .getDevices()
            .subscribe(
                devices -> call.resolve(resolveDevices(devices)),
                throwable -> call.reject("Failed to get devices", throwable.getMessage())
            );
    }

    @PluginMethod
    public Disposable setDevices(@NonNull PluginCall call) {
        JSArray devices = call.getArray("devices");
        try {
            //  extract the devices array
            List<JSONObject> deviceList = devices.toList();
            ArrayList<Device> list = new ArrayList<>();
            //  parse the devices array
            for (JSONObject object : deviceList) {
                try {
                    Device device = new Device(object.getString("serial"), object.getString("name"));
                    list.add(device);
                } catch (JSONException e) {
                    Logger.error("BackgroundBLE Error parsing device: " + e.getMessage());
                }
            }

            return implementation
                .setDevices(list)
                .subscribe(
                    devices1 -> call.resolve(resolveDevices(devices1)),
                    throwable -> call.reject("Failed to set devices", throwable.getMessage())
                );
        } catch (JSONException ex) {
            call.reject(ex.toString());
            return Single.just("").subscribe();
        }
    }

    @PluginMethod
    public Disposable clearDevices(@NonNull PluginCall call) {
        return implementation
            .clearDevices()
            .subscribe(
                devices -> call.resolve(resolveDevices(devices)),
                throwable -> call.reject("Failed to clear devices", throwable.getMessage())
            );
    }

    @PluginMethod
    public Disposable startForegroundService(@NonNull PluginCall call) {
        return implementation
            .startForegroundService()
            .subscribe(
                result -> {
                    JSObject ret = new JSObject();
                    ret.put("result", result);
                    call.resolve(ret);
                },
                throwable -> call.reject("Failed to start foreground service", throwable.getMessage())
            );
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
    public Disposable setScanMode(@NonNull PluginCall call) {
        Integer mode = call.getInt("mode");

        if (mode == null) {
            call.reject("Scan mode is required", "MISSING_PARAMETER");
        }
        // Validate the mode if necessary
        else if (!isValidScanMode(mode)) {
            call.reject("Invalid scan mode", "INVALID_PARAMETER_VALUE");
        } else {
            return implementation
                .setScanMode(mode)
                .subscribe(
                    result -> {
                        JSObject ret = new JSObject();
                        ret.put("result", result);
                        call.resolve(ret);
                    },
                    throwable -> call.reject("Failed to set scan mode", throwable.getMessage())
                );
        }

        return Single.just("").subscribe();
    }

    /**
     * Checks if the given scan mode is valid.
     *
     * @param mode The scan mode to check.
     * @return True if the mode is valid, false otherwise.
     */
    private boolean isValidScanMode(int mode) {
        // Example: Check if the mode is within a specific range or a known set of values
        return mode >= -1 && mode <= 2; // Example: Valid modes are -1, 0, 1, and 2
    }

    @NonNull
    private JSArray devicesToJSArray(@NonNull List<Device> devices) {
        JSArray deviceArray = new JSArray();
        for (Device device : devices) {
            deviceArray.put(device.toObject());
        }
        return deviceArray;
    }

    @NonNull
    private JSObject resolveDevices(List<Device> devices) {
        JSObject ret = new JSObject();
        JSArray deviceArray = devicesToJSArray(devices);
        ret.put("devices", deviceArray);
        return ret;
    }
}
