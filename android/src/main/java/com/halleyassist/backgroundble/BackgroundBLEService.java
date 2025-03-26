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
import androidx.annotation.RequiresPermission;
import com.getcapacitor.Logger;
import com.halleyassist.backgroundble.Device.Device;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class BackgroundBLEService extends Service {

    //  constants
    //  notifications
    private static final String DEFAULT_CHANNEL_ID = "BluetoothScanner";
    private static final int NOTIFICATION_ID = 1;

    //  actions
    public static final String ACTION_STOP = "STOP";
    public static final String ACTION_RENOTIFY = "RENOTIFY";
    public static final String ACTION_DEVICE_FOUND = "DEVICE_UPDATE";
    public static final String EXTRA_DEVICE_SERIAL = "serial";
    public static final String EXTRA_DEVICE_RSSI = "rssi";
    public static final String EXTRA_DEVICE_TX_POWER = "txPower";

    //  service extras
    public static final String EXTRA_DEVICES = "devices";
    public static final String EXTRA_SCAN_MODE = "scanMode";
    public static final String EXTRA_ICON = "icon";
    public static final String EXTRA_DEBUG_MODE = "debugMode";
    public static final String EXTRA_DEVICE_TIMEOUT = "deviceTimeout";

    private BluetoothLeScanner bluetoothLeScanner;

    private Notification.Builder builder;
    private NotificationManager notificationManager;
    private List<Device> devices;
    private final AtomicBoolean timerRunning = new AtomicBoolean(false);
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> timerFuture;

    private PendingIntent resultIntent;

    private boolean isRunning = false;

    private boolean debugMode = false;

    private int deviceTimeout = 30000;

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
                if (action.equals(ACTION_STOP)) {
                    stopForeground(STOP_FOREGROUND_REMOVE);
                    stopSelf();
                    return START_NOT_STICKY;
                } else if (action.equals(ACTION_RENOTIFY)) {
                    checkClosestDevice();
                    return START_STICKY;
                } else if (action.equals(ACTION_DEVICE_FOUND)) {
                    //  update the device with the extras provided through the action
                    String serial = intent.getStringExtra("serial");
                    int rssi = intent.getIntExtra("rssi", -127);
                    int txPower = intent.getIntExtra("txPower", TX_POWER_NOT_PRESENT);
                    devices.stream().filter(d -> d.serial.equals(serial)).findFirst().ifPresent(device -> device.update(rssi, txPower));
                    checkClosestDevice();
                    return START_STICKY;
                }
            }

            debugMode = intent.getBooleanExtra(EXTRA_DEBUG_MODE, false);

            deviceTimeout = intent.getIntExtra(EXTRA_DEVICE_TIMEOUT, 30000);

            notificationManager = getSystemService(NotificationManager.class);
            createNotificationChannel();
            Notification notification = createNotification(intent);

            //  get the list of devices from the intent
            Bundle devicesBundle = intent.getBundleExtra(EXTRA_DEVICES);
            assert devicesBundle != null;
            Set<String> keys = devicesBundle.keySet();
            devices = new ArrayList<>();
            for (String serial : keys) {
                String name = devicesBundle.getString(serial);
                devices.add(new Device(serial, name));
            }

            int scanMode = intent.getIntExtra(EXTRA_SCAN_MODE, SCAN_MODE_LOW_POWER);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
            } else {
                startForeground(NOTIFICATION_ID, notification);
            }

            resultIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                300,
                new Intent(getApplicationContext(), BackgroundBLEReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
            );

            startScanning(devices, scanMode);
        } catch (SecurityException e) {
            Logger.error(TAG, "Missing permission: " + e.getMessage(), e);
            return START_NOT_STICKY;
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage(), e);
            return START_NOT_STICKY;
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
        //  create an intent to send to the service when a device is scanned

        bluetoothLeScanner.startScan(filters, settings, resultIntent);
        Logger.info(TAG, "Background Scan Started");
        isRunning = true;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private void stopScanning() {
        bluetoothLeScanner.stopScan(resultIntent);
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
                .ifPresent(foundDevice -> foundDevice.update(result.getRssi(), result.getTxPower()));
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
        channel.setDescription("Shows the nearest Hub, Enabling you to quickly open the app to the Hub");
        channel.enableLights(false);
        channel.enableVibration(false);
        channel.setSound(null, null);

        notificationManager.createNotificationChannel(channel);
    }

    @NonNull
    private Notification createNotification(@NonNull Intent intent) {
        String body = "Scanning for Nearby Hubs";
        int icon = intent.getIntExtra(EXTRA_ICON, 0);
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
        actions[0] = getStopAction();
        // Set actions
        builder.setActions(actions);

        //  if the notification is dismissed, re notify
        Intent reNotifyIntent = new Intent(getApplicationContext(), BackgroundBLEService.class);
        reNotifyIntent.setAction(ACTION_RENOTIFY);
        PendingIntent reNotifyPendingIntent = PendingIntent.getService(getApplicationContext(), 200, reNotifyIntent, getIntentFlags());
        builder.setDeleteIntent(reNotifyPendingIntent);

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

    private void updateNotification(String text, String deeplink, Notification.Action[] actions) {
        //  update the notification body
        builder.setContentText(text).setOngoing(true).setAutoCancel(false);
        //  update the content intent to launch the app with the closest device
        PendingIntent pendingIntent = getPendingIntent(deeplink);
        builder.setContentIntent(pendingIntent);
        //  update the actions
        builder.setActions(actions);
        //  update the notification
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    @NonNull
    private Notification.Action getStopAction() {
        Intent stopIntent = new Intent(getApplicationContext(), BackgroundBLEService.class);
        stopIntent.setAction(ACTION_STOP);
        stopIntent.putExtra("buttonId", 0);
        PendingIntent stopPendingIntent = PendingIntent.getService(getApplicationContext(), 100, stopIntent, getIntentFlags());
        return new Notification.Action.Builder(null, "Stop Scan", stopPendingIntent).build();
    }

    /**
     * Get the action for the notification
     */
    @NonNull
    private Notification.Action getDeviceAction(@NonNull Device device) {
        PendingIntent pendingIntent = getPendingIntent("halleyassist://app/clients/" + device.serial);
        return new Notification.Action.Builder(null, "Open " + device.name, pendingIntent).build();
    }

    //#endregion Notification

    /**
     * Check the closest device from the list of found devices.
     * Updates the notification with the closest device
     */
    private void checkClosestDevice() {
        sortDevices();
        //  get the closest devices
        List<Device> closeDevices;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            closeDevices = devices.stream().filter(d -> d.rssi > -100).toList();
        } else {
            closeDevices = devices.stream().filter(d -> d.rssi > -100).collect(Collectors.toList());
        }
        //  create a list of actions, one for stopping the scan, one for the closest device, and one for the second closest device
        //  if no devices are close, only show the stop action
        Notification.Action[] actions;
        //  get the 2 closest devices, not including the closest device
        List<Device> twoClosest;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            twoClosest = closeDevices.stream().skip(1).limit(2).toList();
        } else {
            twoClosest = closeDevices.stream().skip(1).limit(2).collect(Collectors.toList());
        }
        if (twoClosest.isEmpty()) {
            actions = new Notification.Action[] { getStopAction() };
        } else {
            int size = twoClosest.size() + 1;
            if (size > 3) {
                size = 3;
            }
            actions = new Notification.Action[size];
            actions[0] = getStopAction();
            for (int i = 0; i < size - 1; i++) {
                actions[i + 1] = getDeviceAction(twoClosest.get(i));
            }
        }

        Device closestDevice = null;
        if (!closeDevices.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                closestDevice = closeDevices.getFirst();
            } else {
                closestDevice = closeDevices.get(0);
            }
        }
        if (debugMode) {
            //  in debug mode, the notification will show the rssi of all devices sorted by closest first, separated by a newline
            StringBuilder debugText = new StringBuilder();
            devices
                .stream()
                .filter(d -> d.rssi > -100)
                .forEach(device -> debugText.append(device.name).append(": ").append(device.rssi).append("\n"));
            //  deep link to the closest device, if present
            StringBuilder deepLink = new StringBuilder();
            if (closestDevice != null) {
                deepLink.append("halleyassist://app/clients/").append(closestDevice.serial);
            } else {
                deepLink.append("halleyassist://app");
            }
            updateNotification(debugText.toString(), deepLink.toString(), actions);
        } else {
            //  get the name of the device from the devices arrayList
            if (closestDevice == null) {
                // when no devices are found, reset the notification to default
                updateNotification("No devices nearby", "halleyassist://app", actions);
                return;
            }
            //  update the notification
            updateNotification("Tap to open " + closestDevice, "halleyassist://app/clients/" + closestDevice.serial, actions);
        }
        //  start a timer to clear the notification text once no devices are in range
        startTimer();
    }

    private void startTimer() {
        if (timerRunning.get()) return;
        timerRunning.set(true);

        executorService = Executors.newSingleThreadScheduledExecutor();
        timerFuture = executorService.scheduleWithFixedDelay(
            () -> {
                // update any devices that have not been updated in the last (timeout) seconds
                devices
                    .stream()
                    .filter(d -> System.currentTimeMillis() - d.lastUpdated > deviceTimeout)
                    .forEach(d -> d.update(-127, TX_POWER_NOT_PRESENT));

                checkClosestDevice();

                if (shouldStopTimer()) {
                    stopTimer();
                }
            },
            5,
            5,
            TimeUnit.SECONDS
        );
    }

    private boolean shouldStopTimer() {
        //  stop the timer if all devices have an rssi less than -101 (effectively out of range)
        return devices.stream().allMatch(d -> d.rssi < -101);
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

    private void sortDevices() {
        //  sort the devices by rssi, largest first
        devices.sort((device1, device2) -> (int) (device2.rssi - device1.rssi));
    }

    public List<Device> getDevices() {
        return devices;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
