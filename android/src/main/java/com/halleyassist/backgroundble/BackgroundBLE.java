package com.halleyassist.backgroundble;

import static com.halleyassist.backgroundble.BLEDataStore.KEYS;
import static com.halleyassist.backgroundble.BLEDataStore.KEY_ACTIVE_DEVICE;
import static com.halleyassist.backgroundble.BLEDataStore.KEY_DEBUG;
import static com.halleyassist.backgroundble.BLEDataStore.KEY_DEVICE_TIMEOUT;
import static com.halleyassist.backgroundble.BLEDataStore.KEY_PERSISTENT;
import static com.halleyassist.backgroundble.BLEDataStore.KEY_SCAN_MODE;
import static com.halleyassist.backgroundble.BLEDataStore.KEY_STOPPED;
import static com.halleyassist.backgroundble.BLEDataStore.KEY_THRESHOLD;
import static com.halleyassist.backgroundble.BackgroundBLEService.EXTRA_ACTIVE_DEVICE;
import static com.halleyassist.backgroundble.BackgroundBLEService.EXTRA_DEBUG_MODE;
import static com.halleyassist.backgroundble.BackgroundBLEService.EXTRA_DEVICES;
import static com.halleyassist.backgroundble.BackgroundBLEService.EXTRA_DEVICE_TIMEOUT;
import static com.halleyassist.backgroundble.BackgroundBLEService.EXTRA_ICON;
import static com.halleyassist.backgroundble.BackgroundBLEService.EXTRA_PERSISTENT;
import static com.halleyassist.backgroundble.BackgroundBLEService.EXTRA_SCAN_MODE;
import static com.halleyassist.backgroundble.BackgroundBLEService.EXTRA_THRESHOLD;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.rxjava2.RxDataStore;
import com.getcapacitor.Logger;
import com.getcapacitor.plugin.util.AssetUtil;
import com.halleyassist.backgroundble.Device.Device;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.Subject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kotlinx.coroutines.ExperimentalCoroutinesApi;

public class BackgroundBLE {

  public static final String TAG = "BackgroundBLE";

  private final Context context;

  private final Subject<LocalMessage> messageSubject;

  private final RxDataStore<Preferences> dataStore;

  Disposable messageDisposable;

  @OptIn(markerClass = ExperimentalCoroutinesApi.class)
  public BackgroundBLE(Context context) {
    this.context = context;

    dataStore = BLEDataStore.getInstance(context).getDataStore();

    messageSubject = LocalMessaging.getSubject();

    messageDisposable = messageSubject.subscribe((message) -> {
      if (message.type == LocalMessage.Type.Service) return;
      Log.d(TAG, "Message received: " + message);
      if (message.content.equals("Started")) {
        // Handle service started
        //  set the stopped flag to false
        dataStore
          .updateDataAsync((preferences) -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            //  set the stopped flag to false
            Preferences.Key<Boolean> key = PreferencesKeys.booleanKey(KEY_STOPPED);
            mutablePreferences.remove(key);
            Logger.info(TAG, "Stopped set to false");
            return Single.just(mutablePreferences);
          })
          .subscribe();
      } else if (message.content.equals("User Stopped")) {
        // Handle user stopped
        dataStore
          .updateDataAsync((preferences) -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            //  set the stopped flag to true
            Preferences.Key<Boolean> key = PreferencesKeys.booleanKey(KEY_STOPPED);
            mutablePreferences.set(key, true);
            Logger.info(TAG, "Stopped set to true");
            return Single.just(mutablePreferences);
          })
          .subscribe();
      }
    });
  }

  // dispose the messageDisposable on destroy
  public void destroy() {
    if (messageDisposable != null && !messageDisposable.isDisposed()) {
      messageDisposable.dispose();
    }
    messageSubject.onComplete();
    Log.d(TAG, "BackgroundBLE destroyed");
  }

  public Boolean canUseBluetooth() {
    if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
      return false;
    }
    //  get bluetooth adaptor
    BluetoothAdapter bluetoothAdapter = getAdapter();
    return bluetoothAdapter != null;
  }

  public BluetoothAdapter getAdapter() {
    BluetoothManager bluetoothManager = context.getSystemService(BluetoothManager.class);
    return bluetoothManager.getAdapter();
  }

  public Observable<List<Device>> getDevicesObservable() {
    if (!isRunning()) {
      Log.d(TAG, "getDevicesObservable: not running");
      return loadDevices().toObservable();
    } else {
      BackgroundBLEService service = BackgroundBLEService.getInstance();
      if (service == null) {
        Log.d(TAG, "getDevicesObservable: service is null");
        return loadDevices().toObservable();
      }
      Log.d(TAG, "getDevicesObservable: running");
      return service.getDevicesObservable();
    }
  }

  public Observable<List<Device>> getCloseDevicesObservable() {
    if (!isRunning()) {
      Log.d(TAG, "getCloseDevicesObservable: not running");
      return loadDevices().toObservable();
    } else {
      BackgroundBLEService service = BackgroundBLEService.getInstance();
      if (service == null) {
        Log.d(TAG, "getCloseDevicesObservable: service is null");
        return loadDevices().toObservable();
      }
      Log.d(TAG, "getCloseDevicesObservable: running");
      return service.getCloseDevicesObservable();
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

  @OptIn(markerClass = ExperimentalCoroutinesApi.class)
  public Single<String> startForegroundService() {
    return dataStore
      .updateDataAsync((preferences) -> {
        MutablePreferences mutablePreferences = preferences.toMutablePreferences();
        //  set the stopped flag to false
        Preferences.Key<Boolean> key = PreferencesKeys.booleanKey(KEY_STOPPED);
        mutablePreferences.remove(key);
        Logger.info(TAG, "Stopped set to false");
        return Single.just(mutablePreferences);
      })
      .map(Preferences::asMap)
      .map(this::getConfigWithDevices)
      .flatMap((config) -> {
        int iconResourceId = AssetUtil.getResourceID(
          context,
          AssetUtil.getResourceBaseName("ic_notification"),
          "drawable"
        );
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
        serviceIntent.putExtra(EXTRA_THRESHOLD, config.getThreshold());
        serviceIntent.putExtra(EXTRA_ACTIVE_DEVICE, config.activeDevice);
        serviceIntent.putExtra(EXTRA_PERSISTENT, config.isPersistent());
        context.startForegroundService(serviceIntent);

        //  return a single that emits when the messageSubject emits "Started"
        return messageSubject
          .filter((message) -> message.type == LocalMessage.Type.Service && message.content.equals("Started"))
          .firstOrError()
          .map((message) -> message.content);
      });
  }

  public Single<String> stopForegroundService() {
    //  create the BackgroundBLEService intent
    Intent serviceIntent = new Intent(context, BackgroundBLEService.class);
    //  stop the service
    context.stopService(serviceIntent);
    //  return a single that emits when the messageSubject emits "Stopped"
    return messageSubject
      .filter((message) -> message.type == LocalMessage.Type.Service && message.content.equals("Stopped"))
      .firstOrError()
      .map((message) -> message.content);
  }

  public boolean isRunning() {
    BackgroundBLEService service = BackgroundBLEService.getInstance();
    if (service != null) {
      return service.isRunning();
    }
    return false;
  }

  @OptIn(markerClass = ExperimentalCoroutinesApi.class)
  public Single<Boolean> didUserStop() {
    Single<Map<Preferences.Key<?>, ?>> preferences = dataStore.data().firstOrError().map(Preferences::asMap);
    return preferences.map((pref) -> {
      if (pref.containsKey(PreferencesKeys.booleanKey(KEY_STOPPED))) {
        return (boolean) pref.get(PreferencesKeys.booleanKey(KEY_STOPPED));
      }
      return false;
    });
  }

  @OptIn(markerClass = ExperimentalCoroutinesApi.class)
  public Single<Device> getActiveDevice() {
    return dataStore
      .data()
      .firstOrError()
      .map((preferences) -> {
        Preferences.Key<String> activeDeviceSerialKey = PreferencesKeys.stringKey(KEY_ACTIVE_DEVICE);
        String activeDeviceSerial = preferences.get(activeDeviceSerialKey);

        if (activeDeviceSerial == null) {
          return null; // Return null if no active device serial is stored
        }

        Preferences.Key<String> deviceNameKey = PreferencesKeys.stringKey(activeDeviceSerial);
        String deviceName = preferences.get(deviceNameKey);

        if (deviceName == null) {
          // This case might indicate inconsistent data,
          // you could log a warning here or handle it differently.
          return null; // Return null if the device name for the active serial is not found
        }
        return new Device(activeDeviceSerial, deviceName);
      });
  }

  @OptIn(markerClass = ExperimentalCoroutinesApi.class)
  public Single<Device> setActiveDevice(@Nullable Device device) {
    return dataStore
      .updateDataAsync((preferences) -> {
        MutablePreferences mutablePreferences = preferences.toMutablePreferences();
        if (device != null) {
          mutablePreferences.set(PreferencesKeys.stringKey(KEY_ACTIVE_DEVICE), device.serial);
        } else {
          mutablePreferences.remove(PreferencesKeys.stringKey(KEY_ACTIVE_DEVICE));
        }
        LocalMessaging.getSubject().onNext(
          new LocalMessage(LocalMessage.Type.Device, device != null ? device.serial : "null")
        );
        return Single.just(mutablePreferences);
      })
      .map((preferences) -> device);
  }

  @OptIn(markerClass = ExperimentalCoroutinesApi.class)
  public Single<ScanConfig> getScanConfig() {
    Single<Map<Preferences.Key<?>, ?>> preferences = dataStore.data().firstOrError().map(Preferences::asMap);
    return preferences.map((pref) -> {
      ScanConfig config = new ScanConfig();
      for (String key : KEYS) {
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
            case KEY_THRESHOLD:
              config.setThreshold((int) pref.get(PreferencesKeys.intKey(key)));
              break;
            case KEY_PERSISTENT:
              config.setPersistent((boolean) pref.get(PreferencesKeys.booleanKey(key)));
              break;
          }
        }
      }
      return config;
    });
  }

  @OptIn(markerClass = ExperimentalCoroutinesApi.class)
  public Single<ScanConfig> setScanConfig(@NonNull ScanConfig config) {
    return dataStore
      .updateDataAsync((preferences) -> {
        MutablePreferences mutablePreferences = preferences.toMutablePreferences();
        mutablePreferences.set(PreferencesKeys.intKey(KEY_SCAN_MODE), config.getMode());
        mutablePreferences.set(PreferencesKeys.booleanKey(KEY_DEBUG), config.isDebug());
        mutablePreferences.set(PreferencesKeys.intKey(KEY_DEVICE_TIMEOUT), config.getDeviceTimeout());
        mutablePreferences.set(PreferencesKeys.intKey(KEY_THRESHOLD), config.getThreshold());
        mutablePreferences.set(PreferencesKeys.booleanKey(KEY_PERSISTENT), config.isPersistent());
        return Single.just(mutablePreferences);
      })
      .map((preferences) -> config);
  }

  //  load device list from key store
  @NonNull
  @OptIn(markerClass = ExperimentalCoroutinesApi.class)
  private Single<List<Device>> loadDevices() {
    //  get all the keys values
    Single<Map<Preferences.Key<?>, ?>> preferences = dataStore.data().firstOrError().map(Preferences::asMap);
    //  iterate map
    return preferences.map((pref) -> {
      List<Device> deviceList = new ArrayList<>();
      // iterate map, if the key is in the ignore array skip
      for (Map.Entry<Preferences.Key<?>, ?> entry : pref.entrySet()) {
        if (Arrays.stream(KEYS).anyMatch((k) -> k.equals(entry.getKey().toString()))) {
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
  @OptIn(markerClass = ExperimentalCoroutinesApi.class)
  private Single<List<Device>> saveDevices(@NonNull List<Device> devices) {
    return dataStore
      .updateDataAsync((preferences) -> {
        MutablePreferences mutablePreferences = preferences.toMutablePreferences();
        // get all the keys in the preferences
        Set<Preferences.Key<?>> keys = mutablePreferences.asMap().keySet();
        // if any key is not in the proved device list, remove the entry
        for (Preferences.Key<?> key : keys) {
          if (Arrays.stream(KEYS).anyMatch((k) -> k.equals(key.toString()))) {
            continue;
          }
          if (devices.stream().noneMatch((device) -> device.serial.equals(key.toString()))) {
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
      .map((preferences) -> devices);
  }

  @NonNull
  private ScanConfig getConfigWithDevices(@NonNull Map<Preferences.Key<?>, ?> prefs) {
    //  return every key in the preferences
    Logger.info(TAG, "Preferences: " + prefs);
    ScanConfig config = new ScanConfig();
    for (Map.Entry<Preferences.Key<?>, ?> entry : prefs.entrySet()) {
      switch (entry.getKey().toString()) {
        case KEY_SCAN_MODE:
          config.setMode((int) entry.getValue());
          break;
        case KEY_DEBUG:
          config.setDebug((boolean) entry.getValue());
          break;
        case KEY_DEVICE_TIMEOUT:
          config.setDeviceTimeout((int) entry.getValue());
          break;
        case KEY_THRESHOLD:
          config.setThreshold((int) entry.getValue());
          break;
        case KEY_PERSISTENT:
          config.setPersistent((boolean) entry.getValue());
          break;
        case KEY_STOPPED:
          break;
        case KEY_ACTIVE_DEVICE:
          config.activeDevice = entry.getValue().toString();
        default:
          config.devices.add(new Device(entry.getKey().toString(), entry.getValue().toString()));
          break;
      }
    }
    return config;
  }
}
