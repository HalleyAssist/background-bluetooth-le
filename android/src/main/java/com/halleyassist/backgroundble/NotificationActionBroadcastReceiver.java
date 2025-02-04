package com.halleyassist.backgroundble;

import static com.halleyassist.backgroundble.BackgroundBLE.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.getcapacitor.Logger;

public class NotificationActionBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            int buttonId = intent.getIntExtra("buttonId", -1);
            Logger.info(TAG, "onReceive: " + buttonId);
        } catch (Exception exception) {
            Logger.error(TAG, exception.getMessage(), exception);
        }
    }
}
