package com.halleyassist.backgroundble;

import static com.halleyassist.backgroundble.BackgroundBLE.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
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

    private Disposable devicesDisposable;
    private Disposable closeDevicesDisposable;

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
            if (implementation.canUseBluetooth()) {
                call.resolve();
            } else {
                call.reject("Bluetooth not available");
            }
        } else {
            call.reject("Permission denied.");
        }
    }

    @PluginMethod
    @SuppressLint("MissingPermission")
    public void enable(@NonNull PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent actionIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(call, actionIntent, "requestEnableCallback");
        } else {
            try {
                BluetoothAdapter bluetoothAdapter = implementation.getAdapter();
                if (bluetoothAdapter.enable()) {
                    call.resolve();
                } else {
                    call.reject("Bluetooth not enabled");
                }
            } catch (Exception e) {
                call.reject(e.getMessage(), e);
            }
        }
    }

    @ActivityCallback
    private void requestEnableCallback(PluginCall call, @NonNull ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            call.resolve();
        } else {
            call.reject("Bluetooth not enabled");
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
                (devices) -> call.resolve(resolveDevices(devices)),
                (throwable) -> call.reject("Failed to get devices", throwable.getMessage())
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
                    (devices1) -> call.resolve(resolveDevices(devices1)),
                    (throwable) -> call.reject("Failed to set devices", throwable.getMessage())
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
                (devices) -> call.resolve(resolveDevices(devices)),
                (throwable) -> call.reject("Failed to clear devices", throwable.getMessage())
            );
    }

    @PluginMethod
    public Disposable startForegroundService(@NonNull PluginCall call) {
        return implementation
            .startForegroundService()
            .subscribe(
                (result) -> {
                    // subscribe to the implementation's getDevicesObservable()
                    devicesDisposable = implementation
                        .getDevicesObservable()
                        .subscribe((devices) -> {
                            JSObject ret = new JSObject();
                            JSArray deviceArray = devicesToJSArray(devices);
                            ret.put("devices", deviceArray);
                            notifyListeners("devicesChanged", ret);
                        });

                    // subscribe to the implementation's getCloseDevicesObservable()
                    closeDevicesDisposable = implementation
                        .getCloseDevicesObservable()
                        .subscribe((closeDevices) -> {
                            JSObject ret = new JSObject();
                            JSArray closeDeviceArray = devicesToJSArray(closeDevices);
                            ret.put("devices", closeDeviceArray);
                            notifyListeners("closeDevicesChanged", ret);
                        });

                    JSObject ret = new JSObject();
                    ret.put("result", result);
                    call.resolve(ret);
                },
                (throwable) -> call.reject("Failed to start foreground service", throwable.getMessage())
            );
    }

    @PluginMethod
    public void stopForegroundService(@NonNull PluginCall call) {
        //  unsubscribe from the implementation's getDevicesObservable()
        if (devicesDisposable != null && !devicesDisposable.isDisposed()) {
            devicesDisposable.dispose();
        }
        //  unsubscribe from the implementation's getCloseDevicesObservable()
        if (closeDevicesDisposable != null && !closeDevicesDisposable.isDisposed()) {
            closeDevicesDisposable.dispose();
        }
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

    @PluginMethod
    public Disposable didUserStop(@NonNull PluginCall call) {
        return implementation
            .didUserStop()
            .subscribe(
                (result) -> {
                    JSObject ret = new JSObject();
                    ret.put("userStopped", result);
                    call.resolve(ret);
                },
                (throwable) -> call.reject("Failed to check if user stopped", throwable.getMessage())
            );
    }

    @PluginMethod
    public Disposable setConfig(@NonNull PluginCall call) {
        JSObject config = call.getObject("config");
        return implementation
            .setScanConfig(new ScanConfig(config))
            .subscribe(
                (result) -> {
                    JSObject ret = new JSObject();
                    ret.put("config", result.toJSObject());
                    call.resolve(ret);
                },
                (throwable) -> call.reject("Failed to set config", throwable.getMessage())
            );
    }

    @PluginMethod
    public Disposable getConfig(@NonNull PluginCall call) {
        return implementation
            .getScanConfig()
            .subscribe(
                (config) -> {
                    JSObject ret = new JSObject();
                    ret.put("config", config.toJSObject());
                    call.resolve(ret);
                },
                (throwable) -> call.reject("Failed to get config", throwable.getMessage())
            );
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
