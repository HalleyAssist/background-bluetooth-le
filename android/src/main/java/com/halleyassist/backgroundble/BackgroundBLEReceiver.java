package com.halleyassist.backgroundble;

import static android.bluetooth.le.BluetoothLeScanner.EXTRA_ERROR_CODE;
import static android.bluetooth.le.BluetoothLeScanner.EXTRA_LIST_SCAN_RESULT;

import android.annotation.SuppressLint;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import java.util.ArrayList;

public class BackgroundBLEReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, @NonNull Intent intent) {
        //  handle error codes first
        if (intent.hasExtra(EXTRA_ERROR_CODE)) {
            int errorCode = intent.getIntExtra(EXTRA_ERROR_CODE, 0);
            //  handle error here
            handleError(context, errorCode);
            return;
        }
        if (intent.hasExtra(EXTRA_LIST_SCAN_RESULT)) {
            ArrayList<ScanResult> results;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                results = intent.getParcelableArrayListExtra(EXTRA_LIST_SCAN_RESULT, ScanResult.class);
            } else {
                results = intent.getParcelableArrayListExtra(EXTRA_LIST_SCAN_RESULT);
            }
            //  handle results here
            //  extract the device information from the results
            assert results != null;
            for (ScanResult result : results) {
                //  get the device information
                Intent serviceIntent = getServiceIntent(context, result);
                context.startService(serviceIntent);
            }
        }
    }

    @NonNull
    @SuppressLint("MissingPermission")
    private static Intent getServiceIntent(Context context, @NonNull ScanResult result) {
        String deviceName = result.getDevice().getName();
        //  remove the 'H-' prefix from the device name
        String serial = deviceName.substring(2);
        int rssi = result.getRssi();
        int txPower = result.getTxPower();
        //  send the device information to the service via an intent
        Intent serviceIntent = new Intent(context, BackgroundBLEService.class);
        serviceIntent.setAction(BackgroundBLEService.ACTION_DEVICE_FOUND);
        serviceIntent.putExtra(BackgroundBLEService.EXTRA_DEVICE_SERIAL, serial);
        serviceIntent.putExtra(BackgroundBLEService.EXTRA_DEVICE_RSSI, rssi);
        serviceIntent.putExtra(BackgroundBLEService.EXTRA_DEVICE_TX_POWER, txPower);
        return serviceIntent;
    }

    private static void handleError(Context context, int errorCode) {
        //  handle error here
        //  send the error code to the service via an intent
    }
}
