package com.halleyassist.backgroundble;

import static android.bluetooth.le.BluetoothLeScanner.EXTRA_ERROR_CODE;
import static android.bluetooth.le.BluetoothLeScanner.EXTRA_LIST_SCAN_RESULT;

import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                results = intent.getParcelableArrayListExtra(EXTRA_LIST_SCAN_RESULT, ScanResult.class);
            } else {
                results = intent.getParcelableArrayListExtra(EXTRA_LIST_SCAN_RESULT);
            }
            //  handle results here
            //  extract the device information from the results
            if (results == null) {
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Intent broadcastIntent = getBroadcastIntent(context, results);
                context.sendBroadcast(broadcastIntent);
            } else {
                Intent serviceIntent = getServiceIntent(context, results);
                context.startService(serviceIntent);
            }
        }
    }

    @NonNull
    private static Intent getServiceIntent(Context context, @NonNull ArrayList<ScanResult> results) {
        //  send the device information to the service via an intent
        Intent serviceIntent = new Intent(context, BackgroundBLEService.class);
        serviceIntent.setAction(BackgroundBLEService.ACTION_DEVICES_FOUND);
        serviceIntent.putParcelableArrayListExtra(BackgroundBLEService.EXTRA_DEVICES, results);
        return serviceIntent;
    }

    @NonNull
    private static Intent getBroadcastIntent(@NonNull Context context, @NonNull ArrayList<ScanResult> results) {
        Intent intent = new Intent(BackgroundBLEService.ACTION_DEVICES_FOUND);
        intent.setPackage(context.getPackageName()); // limit to your app
        intent.putParcelableArrayListExtra(BackgroundBLEService.EXTRA_DEVICES, results);
        return intent;
    }

    private static void handleError(Context context, int errorCode) {
        //  handle error here
        //  send the error code to the service via an intent
    }
}
