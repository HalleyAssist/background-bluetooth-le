package com.halleyassist.backgroundble;

import static android.app.Notification.CATEGORY_SERVICE;
import static android.bluetooth.le.ScanResult.TX_POWER_NOT_PRESENT;
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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BackgroundBLEService extends Service {

    private static final String DEFAULT_CHANNEL_ID = "BluetoothScanner";
    private static final int NOTIFICATION_ID = 1;

    private BluetoothLeScanner bluetoothLeScanner;

    private Notification.Builder builder;
    private NotificationManager notificationManager;
    private List<Device> devices;
    private final AtomicBoolean timerRunning = new AtomicBoolean(false);
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> timerFuture;

    private boolean isRunning = false;

    //  singleton
    private static BackgroundBLEService instance;

    public static BackgroundBLEService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        instance = this;
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

    //#region Scanning

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private void startScanning(@NonNull List<Device> devices, int scanMode) {
        //  start scanning, filtering for devices with a name that starts with "HalleyHub"
        List<ScanFilter> filters = new ArrayList<>();
        // add a filter for each device in the list
        for (Device device : devices) {
            ScanFilter filter = new ScanFilter.Builder().setDeviceName("H-" + device.serial).build();
            filters.add(filter);
        }
        ScanSettings settings = new ScanSettings.Builder().setScanMode(scanMode).build();
        bluetoothLeScanner.startScan(filters, settings, scanCallback);
        Logger.info(TAG, "Background Scan Started");
        isRunning = true;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private void stopScanning() {
        bluetoothLeScanner.stopScan(scanCallback);
        Logger.info(TAG, "Background Scan Stopped");
        isRunning = false;
    }

    @SuppressLint("MissingPermission")
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            // add or update the device list with the scanned device
            BluetoothDevice bDevice = result.getDevice();
            String name = bDevice.getName();
            //  name must be present and start with "H-" to be a valid device
            if (name == null || !name.startsWith("H-")) {
                return;
            }
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
                    //  smooth the rssi value
                    float rssi = (foundDevice.rssi + result.getRssi()) / 2;
                    foundDevice.update(rssi, result.getTxPower());
                });
            //  update the notification
            checkClosestDevice();
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Logger.error("BackgroundBLEService Scan failed with error code: " + errorCode);
        }
    };

    //#endregion Scanning

    //#region Notification

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(DEFAULT_CHANNEL_ID, "Bluetooth Scanner", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Shows the nearest Hub, Enabling you to quickly open the hub in the app");
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

        PendingIntent pendingIntent = getPendingIntent("halleyassist://app");

        builder = new Notification.Builder(getApplicationContext(), DEFAULT_CHANNEL_ID);
        builder
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(CATEGORY_SERVICE)
            .setSmallIcon(icon)
            .setOnlyAlertOnce(true);

        //  create actions: Stop, and Open
        //  Stop stops the scan and closes the notification
        Notification.Action[] actions = new Notification.Action[1];
        // Stop Action
        Intent stopIntent = new Intent(getApplicationContext(), BackgroundBLEService.class);
        stopIntent.setAction("STOP");
        stopIntent.putExtra("buttonId", 0);
        PendingIntent stopPendingIntent = PendingIntent.getService(getApplicationContext(), 111, stopIntent, getIntentFlags());
        actions[0] = new Notification.Action.Builder(null, "Stop Scan", stopPendingIntent).build();
        // Set actions
        builder.setActions(actions);
        //  return the notification
        return builder.build();
    }

    private int getIntentFlags() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return PendingIntent.FLAG_UPDATE_CURRENT;
        }
        return PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
    }

    private PendingIntent getPendingIntent(String deeplink) {
        Uri deepLinkUri = Uri.parse(deeplink);
        Intent deepLinkIntent = new Intent(Intent.ACTION_VIEW, deepLinkUri);
        deepLinkIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(getApplicationContext(), 1337, deepLinkIntent, getIntentFlags());
    }

    private void updateNotification(String text, String deeplink) {
        //  update the notification body
        builder.setContentText(text).setOngoing(true).setAutoCancel(false);
        //  update the content intent to launch the app with the closest device
        PendingIntent pendingIntent = getPendingIntent(deeplink);
        builder.setContentIntent(pendingIntent);
        //  update the notification
        notificationManager.notify(NOTIFICATION_ID, builder.build());
        Logger.info(TAG, "Notification updated: " + text);
    }

    //#endregion Notification

    private void checkClosestDevice() {
        //  get the closest device from the list of found devices
        Device closestDevice = getClosestDevice();
        //  get the name of the device from the devices arrayList
        if (closestDevice == null) {
            // when no devices are found, reset the notification to default
            updateNotification("No devices nearby", "halleyassist://app");
            return;
        }
        //  update the notification
        updateNotification("Tap to open " + closestDevice, "halleyassist://app/clients/" + closestDevice.serial);
        //  start a timer to clear the notification text once no devices are in range
        startTimer();
    }

    private void startTimer() {
        if (timerRunning.get()) return;
        timerRunning.set(true);

        executorService = Executors.newSingleThreadScheduledExecutor();
        long TIMER_INTERVAL_MS = 15000;
        timerFuture = executorService.scheduleWithFixedDelay(
            () -> {
                // update any devices that have not been updated in the last 30 seconds
                devices.forEach(device -> {
                    if (device.rssi != 0 && System.currentTimeMillis() - device.lastUpdated > 30000) {
                        device.update(0, TX_POWER_NOT_PRESENT);
                    }
                });

                checkClosestDevice();

                if (shouldStopTimer()) {
                    stopTimer();
                }
            },
            TIMER_INTERVAL_MS,
            TIMER_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );
    }

    private boolean shouldStopTimer() {
        return devices.stream().allMatch(d -> d.rssi == 0);
    }

    private void stopTimer() {
        if (timerFuture != null) {
            timerFuture.cancel(false);
            timerFuture = null;
        }
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
        timerRunning.set(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            stopScanning();
        } catch (SecurityException e) {
            Logger.error(TAG, e.getMessage(), e);
        }
        stopTimer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Nullable
    private Device getClosestDevice() {
        if (devices.isEmpty()) {
            return null;
        }
        //  sort the devices by rssi, smallest first
        devices.sort((device1, device2) -> {
            //  compare the signal strength of the two devices
            float rssi1 = device1.rssi;
            float rssi2 = device2.rssi;
            //  sort by rssi, largest first
            return Float.compare(rssi2, rssi1);
        });
        Optional<Device> closestDevice;
        // get the first devices with a non-zero rssi
        closestDevice = devices.stream().filter(d -> d.rssi != 0).findFirst();
        return closestDevice.orElse(null);
    }

    public List<Device> getDevices() {
        return devices;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
