package com.halleyassist.backgroundble;

import static android.app.Notification.CATEGORY_SERVICE;
import static android.bluetooth.le.ScanResult.TX_POWER_NOT_PRESENT;
import static android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY;
import static android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_POWER;
import static android.bluetooth.le.ScanSettings.SCAN_MODE_OPPORTUNISTIC;
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
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import com.getcapacitor.Logger;
import com.halleyassist.backgroundble.Device.Device;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
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
    public static final String ACTION_DEVICES_FOUND = "DEVICES_UPDATE";

    //  service extras
    public static final String EXTRA_DEVICES = "devices";
    public static final String EXTRA_SCAN_MODE = "scanMode";
    public static final String EXTRA_ICON = "icon";
    public static final String EXTRA_DEBUG_MODE = "debugMode";
    public static final String EXTRA_DEVICE_TIMEOUT = "deviceTimeout";
    public static final String EXTRA_THRESHOLD = "threshold";

    private BluetoothLeScanner bluetoothLeScanner;

    private Notification.Builder builder;
    private NotificationManager notificationManager;
    private List<Device> devices;
    private final AtomicBoolean timerRunning = new AtomicBoolean(false);
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> timerFuture;

    private PendingIntent scanIntent;

    private BroadcastReceiver scanResultReceiver;

    private boolean isRunning = false;

    private boolean debugMode = false;
    private int deviceTimeout = 30000;
    private int threshold = -100;

    private BehaviorSubject<List<Device>> devicesSubject;
    private BehaviorSubject<List<Device>> closeDevicesSubject;

    //  singleton
    private static BackgroundBLEService instance;

    @Nullable
    public static BackgroundBLEService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        devicesSubject = BehaviorSubject.create();
        closeDevicesSubject = BehaviorSubject.create();

        instance = this;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_STOP -> {
                        stopForeground(STOP_FOREGROUND_REMOVE);
                        stopSelf();
                        //  notify plugin that the service has stopped
                        LocalMessaging.sendMessage("User Stopped");
                        return START_NOT_STICKY;
                    }
                    case ACTION_RENOTIFY -> {
                        checkClosestDevice();
                        return START_STICKY;
                    }
                    case ACTION_DEVICES_FOUND -> {
                        // update the devices with the extras provided through the action
                        ArrayList<Parcelable> scanResultExtras;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            scanResultExtras = intent.getParcelableArrayListExtra(EXTRA_DEVICES, ScanResult.class);
                        } else {
                            scanResultExtras = intent.getParcelableArrayListExtra(EXTRA_DEVICES);
                        }
                        if (scanResultExtras != null) {
                            for (Parcelable parcelable : scanResultExtras) {
                                if (parcelable instanceof ScanResult result) {
                                    String serial = result.getDevice().getName().substring(2); // remove the 'H-' prefix
                                    int rssi = result.getRssi();
                                    int txPower = result.getTxPower();
                                    devices
                                        .stream()
                                        .filter(d -> d.serial.equals(serial))
                                        .findFirst()
                                        .ifPresent(device -> device.update(rssi, txPower));
                                }
                            }
                            checkClosestDevice();
                        }
                        return START_STICKY;
                    }
                }
            }

            debugMode = intent.getBooleanExtra(EXTRA_DEBUG_MODE, false);
            deviceTimeout = intent.getIntExtra(EXTRA_DEVICE_TIMEOUT, 30000);
            threshold = intent.getIntExtra(EXTRA_THRESHOLD, -100);

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
            //  validate scan mode is in range, default to low power
            if (scanMode < SCAN_MODE_OPPORTUNISTIC || scanMode > SCAN_MODE_LOW_LATENCY) {
                scanMode = SCAN_MODE_LOW_POWER;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
            } else {
                startForeground(NOTIFICATION_ID, notification);
            }

            startScanning(devices, scanMode);
        } catch (SecurityException e) {
            Logger.error(TAG, "Missing permission: " + e.getMessage(), e);
            return START_NOT_STICKY;
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage(), e);
            return START_NOT_STICKY;
        }
        Logger.info(TAG, "BackgroundBLEService started");

        LocalMessaging.sendMessage("Started");

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //  register a receiver to handle returned scan results from the scan receiver
            scanResultReceiver = new BackgroundBLEReceiver() {
                @Override
                @SuppressLint("MissingPermission")
                public void onReceive(Context context, @NonNull Intent intent) {
                    if (ACTION_DEVICES_FOUND.equals(intent.getAction())) {
                        ArrayList<Parcelable> scanResultExtras = intent.getParcelableArrayListExtra(EXTRA_DEVICES, ScanResult.class);
                        if (scanResultExtras != null) {
                            for (Parcelable parcelable : scanResultExtras) {
                                if (parcelable instanceof ScanResult result) {
                                    String serial = result.getDevice().getName().substring(2); // remove the 'H-' prefix
                                    int rssi = result.getRssi();
                                    int txPower = result.getTxPower();
                                    devices
                                        .stream()
                                        .filter(d -> d.serial.equals(serial))
                                        .findFirst()
                                        .ifPresent(device -> device.update(rssi, txPower));
                                }
                            }
                            checkClosestDevice();
                        }
                    }
                }
            };
            //  register the receiver
            IntentFilter filter = new IntentFilter(ACTION_DEVICES_FOUND);
            registerReceiver(scanResultReceiver, filter, RECEIVER_NOT_EXPORTED);
        }

        //  create an intent to send to the receiver
        scanIntent = PendingIntent.getBroadcast(
            getApplicationContext(),
            300,
            new Intent(getApplicationContext(), BackgroundBLEReceiver.class),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );

        bluetoothLeScanner.startScan(filters, settings, scanIntent);
        Logger.info(TAG, "Background Scan Started");
        isRunning = true;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private void stopScanning() {
        // stop scanning
        try {
            bluetoothLeScanner.stopScan(scanIntent);
        } catch (Exception e) {
            Logger.error(TAG, "Error stopping scan: " + e.getMessage(), e);
        }
        //  unregister the receiver
        if (scanResultReceiver != null) {
            unregisterReceiver(scanResultReceiver);
            scanResultReceiver = null;
        }
        Logger.info(TAG, "Background Scan Stopped");
        isRunning = false;
    }

    //#endregion Scanning

    //#region Notification

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(DEFAULT_CHANNEL_ID, "Bluetooth Scanner", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Shows nearby Hubs, enabling you to quickly view the closest one.");
        channel.enableLights(false);
        channel.enableVibration(false);
        channel.setSound(null, null);

        notificationManager.createNotificationChannel(channel);
    }

    @NonNull
    private Notification createNotification(@NonNull Intent intent) {
        String body = "Looking for Nearby Hubs";
        int icon = intent.getIntExtra(EXTRA_ICON, 0);
        String title = "Hub Scan Active";

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

    private void updateNotification(String text, String deeplink, @Nullable Notification.Action[] actions) {
        //  update the notification body
        builder.setContentText(text).setOngoing(true).setAutoCancel(false);
        //  update the content intent to launch the app with the closest device
        PendingIntent pendingIntent = getPendingIntent(deeplink);
        builder.setContentIntent(pendingIntent);
        //  update the actions
        if (actions == null || actions.length == 0) {
            builder.setActions();
        } else {
            builder.setActions(actions);
        }
        //  update the notification
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    /**
     * Get the action for the notification
     */
    @NonNull
    private Notification.Action getDeviceAction(@NonNull Device device, String buttonText) {
        PendingIntent pendingIntent = getPendingIntent("halleyassist://app/clients/" + device.serial);
        return new Notification.Action.Builder(null, buttonText, pendingIntent).build();
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
            closeDevices = devices.stream().filter(d -> d.rssi > threshold).toList();
        } else {
            closeDevices = devices.stream().filter(d -> d.rssi > threshold).collect(Collectors.toList());
        }
        closeDevicesSubject.onNext(closeDevices);

        Device closestDevice = null;
        if (!closeDevices.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                closestDevice = closeDevices.getFirst();
            } else {
                //noinspection SequencedCollectionMethodCanBeUsed
                closestDevice = closeDevices.get(0);
            }
        }

        //  create a list of actions, one for stopping the scan, one for the closest device, and one for the second closest device
        //  if no devices are close, only show the stop action
        Notification.Action[] actions = null;
        //  get the 2 closest devices, not including the closest device
        List<Device> twoClosest;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            twoClosest = closeDevices.stream().skip(1).limit(2).toList();
        } else {
            twoClosest = closeDevices.stream().skip(1).limit(2).collect(Collectors.toList());
        }

        if (!twoClosest.isEmpty()) {
            int size = twoClosest.size();
            if (size > 3) {
                size = 3;
            }
            actions = new Notification.Action[size];
            for (int i = 0; i < size; i++) {
                actions[i + 1] = getDeviceAction(twoClosest.get(i), twoClosest.get(i).name);
            }
        }
        if (debugMode) {
            //  in debug mode, the notification will show the rssi of all devices sorted by closest first, separated by a newline
            StringBuilder debugText = new StringBuilder();
            devices
                .stream()
                .filter(d -> d.rssi > threshold)
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
                updateNotification("Looking for Nearby Hubs", "halleyassist://app", actions);
                return;
            }
            StringBuilder bodyText = getBodyText(twoClosest, closestDevice);

            //  update the notification
            updateNotification(bodyText.toString(), "halleyassist://app/clients/" + closestDevice.serial, actions);
        }
        //  start a timer to clear the notification text once no devices are in range
        startTimer();
    }

    @NonNull
    private static StringBuilder getBodyText(@NonNull List<Device> twoClosest, @NonNull Device closestDevice) {
        StringBuilder bodyText = new StringBuilder();
        //  if more than 1 device in range
        if (!twoClosest.isEmpty()) {
            bodyText.append("Are you providing care to any of the following clients?").append("\n");
            bodyText.append("Tap here to open ").append(closestDevice.name).append("\n");
            bodyText.append("Tap below to open other clients");
        } else {
            bodyText.append("Are you providing care for ").append(closestDevice.name).append("?\nTap to Open");
        }
        return bodyText;
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
        //  stop the timer if all devices have an rssi less than threshold - 1 (effectively out of range)
        return devices.stream().allMatch(d -> d.rssi < (threshold - 1));
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

    public Observable<List<Device>> getDevicesObservable() {
        // create an observable to return the devices list
        return devicesSubject.hide();
    }

    public Observable<List<Device>> getCloseDevicesObservable() {
        // create an observable to return the close devices list
        return closeDevicesSubject.hide();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //  stop the scanning and timer
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            //  scan can only be started if the permission is granted, this check should always pass
            stopScanning();
        }
        stopTimer();
        //  complete observables
        devicesSubject.onComplete();
        closeDevicesSubject.onComplete();
        // close the notification
        notificationManager.cancel(NOTIFICATION_ID);
        //  notify the plugin that the service has stopped
        LocalMessaging.sendMessage("Stopped");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sortDevices() {
        //  sort the devices by rssi, largest first
        devices.sort((device1, device2) -> (int) (device2.rssi - device1.rssi));
        devicesSubject.onNext(devices);
    }

    public List<Device> getDevices() {
        return devices;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
