package com.halleyassist.backgroundble;

import static android.app.Notification.CATEGORY_PROGRESS;
import static android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_POWER;
import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE;
import static com.halleyassist.backgroundble.BackgroundBLE.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import com.getcapacitor.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BackgroundBLEService extends Service {

    private BluetoothLeScanner bluetoothLeScanner;

    private Notification.Builder builder;
    private NotificationManager notificationManager;
    private ArrayList<Device> devices;

    @SuppressLint("MissingPermission")
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            // add or update the device list with the scanned device
            BluetoothDevice bDevice = result.getDevice();
            String name = bDevice.getName();
            // create a device if it does not exist in the array (should never happen)
            if (devices.stream().noneMatch(d -> d.deviceName.equals(name))) {
                devices.add(new Device(name, name));
            }
            //  find the device in the list of devices, and update the rssi of the device
            devices
                .stream()
                .filter(d -> d.deviceName.equals(name))
                .findFirst()
                .ifPresent(foundDevice -> {
                    foundDevice.rssi = result.getRssi();
                    foundDevice.txPower = result.getTxPower();
                });
            //  get the closest device from the list of found devices
            Device closestDevice = getClosestDevice();
            //  get the name of the device from the devices arrayList
            if (closestDevice == null) return;
            StringBuilder devicesList = new StringBuilder();
            // add each device to the devicesList string, separated by a new line
            for (Device device : devices) {
                devicesList.append(device).append("\n");
            }
            //  update the notification body
            builder.setContentText(devicesList.toString()).setOngoing(true).setAutoCancel(false);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Logger.error("BackgroundBLEService Scan failed with error code: " + errorCode);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("STOP")) {
                    stopForeground(STOP_FOREGROUND_REMOVE);
                    stopSelf();
                    return START_NOT_STICKY;
                } else if (action.equals("OPEN")) {
                    Logger.info(TAG, "Open tapped");
                    return START_STICKY;
                }
            }

            notificationManager = getSystemService(NotificationManager.class);
            createNotificationChannel();
            Notification notification = createNotification(intent);

            //  get the list of devices from the intent
            Bundle devicesBundle = intent.getBundleExtra("devices");
            assert devicesBundle != null;
            Set<String> names = devicesBundle.keySet();
            devices = new ArrayList<>();
            for (String name : names) {
                String displayName = devicesBundle.getString(name);
                devices.add(new Device(name, displayName));
            }

            int scanMode = intent.getIntExtra("scanMode", SCAN_MODE_LOW_POWER);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
            } else {
                startForeground(NOTIFICATION_ID, notification);
            }

            startScanning(devices, scanMode);
        } catch (SecurityException e) {
            Logger.error(TAG, e.getMessage(), e);
            // Handle the exception
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage(), e);
        }
        Logger.info(TAG, "BackgroundBLEService started");
        return START_STICKY;
    }

    private PendingIntent buildContentIntent() {
        String packageName = getApplicationContext().getPackageName();
        Intent intent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(packageName);
        int pendingIntentFlags = getIntentFlags(true);
        return PendingIntent.getActivity(getApplicationContext(), 1337, intent, pendingIntentFlags);
    }

    private int getIntentFlags(boolean mutable) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return PendingIntent.FLAG_CANCEL_CURRENT;
        }
        if (mutable) {
            return PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_MUTABLE;
        }
        return PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private void startScanning(@NonNull ArrayList<Device> devices, int scanMode) {
        //  start scanning, filtering for devices with a name that starts with "HalleyHub"
        List<ScanFilter> filters = new ArrayList<>();
        Logger.info(TAG, "Creating filters from " + devices);
        // add a filter for each device in the list
        for (Device device : devices) {
            ScanFilter filter = new ScanFilter.Builder().setDeviceName(device.deviceName).build();
            filters.add(filter);
        }
        ScanSettings settings = new ScanSettings.Builder().setScanMode(scanMode).build();
        bluetoothLeScanner.startScan(filters, settings, scanCallback);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private void stopScanning() {
        bluetoothLeScanner.stopScan(scanCallback);
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(DEFAULT_CHANNEL_ID, "Bluetooth Scanner", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Shows the nearest Hubs");
        channel.enableLights(false);
        channel.enableVibration(false);
        channel.setSound(null, null);

        notificationManager.createNotificationChannel(channel);
    }

    @NonNull
    private Notification createNotification(@NonNull Intent intent) {
        String body = "Scanning for Nearby Hubs";
        int icon = intent.getIntExtra("icon", 0);
        String title = "Nearby Hub Scanning Active";

        PendingIntent contentIntent = buildContentIntent();
        builder = new Notification.Builder(getApplicationContext(), DEFAULT_CHANNEL_ID);
        builder
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setCategory(CATEGORY_PROGRESS)
            .setSmallIcon(icon)
            .setOnlyAlertOnce(true);

        //  create actions: Stop, and Open
        int pendingIntentFlags = getIntentFlags(false);
        //  Stop stops the scan and closes the notification
        //  Open also stops the scan and closes the notification, but also opens the app
        Notification.Action[] actions = new Notification.Action[2];
        // Stop Action
        Intent stopIntent = new Intent(getApplicationContext(), BackgroundBLEService.class);
        stopIntent.setAction("STOP");
        stopIntent.putExtra("buttonId", 0);
        PendingIntent stopPendingIntent = PendingIntent.getService(getApplicationContext(), 111, stopIntent, pendingIntentFlags);
        actions[0] = new Notification.Action.Builder(null, "Stop", stopPendingIntent).build();
        // Open Action
        Intent openIntent = new Intent(getApplicationContext(), BackgroundBLEService.class);
        openIntent.setAction("OPEN");
        openIntent.putExtra("buttonId", 1);
        PendingIntent openPendingIntent = PendingIntent.getService(getApplicationContext(), 222, openIntent, pendingIntentFlags);
        actions[1] = new Notification.Action.Builder(null, "Open", openPendingIntent).build();
        // Set actions
        builder.setActions(actions);

        return builder.build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            stopScanning();
        } catch (SecurityException e) {
            Logger.error(TAG, e.getMessage(), e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static final String DEFAULT_CHANNEL_ID = "BluetoothScanner";
    private static final int NOTIFICATION_ID = 1;

    @Nullable
    @SuppressLint("MissingPermission")
    private Device getClosestDevice() {
        if (devices.isEmpty()) {
            return null;
        }
        //  get the closest device from the list of found devices
        devices.sort((device1, device2) -> {
            //  compare the signal strength of the two devices
            int rssi1 = device1.rssi;
            int rssi2 = device2.rssi;
            return Integer.compare(rssi1, rssi2);
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            return devices.getFirst();
        }
        return devices.get(0);
    }
}
