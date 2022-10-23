package com.frostphyr.notiphy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class AsyncBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PendingResult result = goAsync();
        AsyncExecutor.execute(() -> {
            onReceiveAsync(context, intent);
            result.finish();
        });
    }

    protected abstract void onReceiveAsync(Context context, Intent intent);

}
