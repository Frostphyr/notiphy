package com.frostphyr.notiphy.notification;

import android.content.Context;
import android.content.Intent;

import com.frostphyr.notiphy.AndroidUtils;
import com.frostphyr.notiphy.AsyncBroadcastReceiver;

public class OpenMessageBroadcastReceiver extends AsyncBroadcastReceiver {

    public static final String EXTRA_URL = "com.frostphyr.notiphy.extra.URL";

    @Override
    public void onReceiveAsync(Context context, Intent intent) {
        String url = intent.getStringExtra(EXTRA_URL);
        MessageDao.getInstance(context).delete(url);
        AndroidUtils.openUri(context, url);
    }

}
