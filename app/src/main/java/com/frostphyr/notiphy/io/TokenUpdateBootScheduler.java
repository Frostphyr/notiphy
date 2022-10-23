package com.frostphyr.notiphy.io;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TokenUpdateBootScheduler extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            TokenUpdateWorker.schedule(context);
        }
    }

}
