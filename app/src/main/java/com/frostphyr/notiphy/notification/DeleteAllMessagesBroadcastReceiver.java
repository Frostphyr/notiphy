package com.frostphyr.notiphy.notification;

import android.content.Context;
import android.content.Intent;

import com.frostphyr.notiphy.AsyncBroadcastReceiver;

public class DeleteAllMessagesBroadcastReceiver extends AsyncBroadcastReceiver {

    public static final String EXTRA_URLS = "com.frostphyr.notiphy.extra.URLS";

    @Override
    public void onReceiveAsync(Context context, Intent intent) {
        MessageDao.getInstance(context).deleteAll(intent.getStringArrayListExtra(EXTRA_URLS));
    }

}
