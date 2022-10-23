package com.frostphyr.notiphy.notification;

import android.content.Context;
import android.content.Intent;

import com.frostphyr.notiphy.AsyncBroadcastReceiver;

public class DeleteMessageBroadcastReceiver extends AsyncBroadcastReceiver {

    public static final String EXTRA_URL = "com.frostphyr.notiphy.extra.URL";

    @Override
    public void onReceiveAsync(Context context, Intent intent) {
        MessageDao.getInstance(context).delete(intent.getStringExtra(EXTRA_URL));
    }

}
