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
import android.net.Uri;
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
            // extract the serial number from the device name (H-{serial})
            String serial = name.split("-")[1];
            // create a device if it does not exist in the array (should never happen)
            if (devices.stream().noneMatch(d -> d.serial.equals(serial))) {
                devices.add(new Device(serial, name));
            }
            //  find the device in the list of devices, and update the rssi of the device
            devices
                .stream()
                .filter(d -> d.serial.equals(serial))
                .findFirst()
                .ifPresent(foundDevice -> {
                    foundDevice.rssi = result.getRssi();
                    foundDevice.txPower = result.getTxPower();
                });
            //  get the closest device from the list of found devices
            Device closestDevice = getClosestDevice();
            //  get the name of the device from the devices arrayList
            if (closestDevice == null) return;
            //  update the notification body
            builder.setContentText("Tap to open " + closestDevice).setOngoing(true).setAutoCancel(false);
            //  update the content intent to launch the app with the closest device
            Uri deepLink = Uri.parse("halleyassist://app/client/" + closestDevice.serial);
            Intent deepLinkIntent = new Intent(Intent.ACTION_VIEW, deepLink);
            deepLinkIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1337, deepLinkIntent, getIntentFlags());
            builder.setContentIntent(pendingIntent);
            //  update the notification
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
                    openToHub();
                    return START_STICKY;
                }
            }

            notificationManager = getSystemService(NotificationManager.class);
            createNotificationChannel();
            Notification notification = createNotification(intent);

            //  get the list of devices from the intent
            Bundle devicesBundle = intent.getBundleExtra("devices");
            assert devicesBundle != null;
            Set<String> keys = devicesBundle.keySet();
            devices = new ArrayList<>();
            for (String serial : keys) {
                String name = devicesBundle.getString(serial);
                devices.add(new Device(serial, name));
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

    private int getIntentFlags() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return PendingIntent.FLAG_UPDATE_CURRENT;
        }
        return PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private void startScanning(@NonNull ArrayList<Device> devices, int scanMode) {
        //  start scanning, filtering for devices with a name that starts with "HalleyHub"
        List<ScanFilter> filters = new ArrayList<>();
        Logger.info(TAG, "Creating filters from " + devices);
        // add a filter for each device in the list
        for (Device device : devices) {
            ScanFilter filter = new ScanFilter.Builder().setDeviceName("H-" + device.serial).build();
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

        int pendingIntentFlags = getIntentFlags();

        //  create a content intent for launching the app
        Uri deepLink = Uri.parse("halleyassist://app");
        Intent deepLinkIntent = new Intent(Intent.ACTION_VIEW, deepLink);
        deepLinkIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1337, deepLinkIntent, getIntentFlags());

        builder = new Notification.Builder(getApplicationContext(), DEFAULT_CHANNEL_ID);
        builder
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(CATEGORY_PROGRESS)
            .setSmallIcon(icon)
            .setOnlyAlertOnce(true);

        //  create actions: Stop, and Open
        //  Stop stops the scan and closes the notification
        Notification.Action[] actions = new Notification.Action[1];
        // Stop Action
        Intent stopIntent = new Intent(getApplicationContext(), BackgroundBLEService.class);
        stopIntent.setAction("STOP");
        stopIntent.putExtra("buttonId", 0);
        PendingIntent stopPendingIntent = PendingIntent.getService(getApplicationContext(), 111, stopIntent, pendingIntentFlags);
        actions[0] = new Notification.Action.Builder(null, "Stop Scan", stopPendingIntent).build();
        // Set actions
        builder.setActions(actions);
        //  return the notification
        return builder.build();
    }

    private void openToHub() {
        //  devices should already be sorted by closest
        //  we want to relaunch the app with the closest device
        //  as we already support deep linking we can simply use the url that will
        //  navigate to the device that is closest
        //  deep link format: halleyassist://app/client/{serial}
        Device closestDevice = getClosestDevice();
        if (closestDevice == null) {
            Logger.warn(TAG, "No devices found");
            return;
        }
        String url = "halleyassist://app/client/" + closestDevice.serial;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(android.net.Uri.parse(url));
        startActivity(intent);
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
