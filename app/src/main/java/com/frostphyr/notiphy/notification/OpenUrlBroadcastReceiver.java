package com.frostphyr.notiphy.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class OpenUrlBroadcastReceiver extends BroadcastReceiver {

    public static final String EXTRA_URL = "com.frostphyr.notiphy.extra.URL";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(intent.getStringExtra(EXTRA_URL)))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (context.getPackageManager().queryIntentActivities(viewIntent, 0).size() > 0) {
            context.startActivity(viewIntent);
        }
    }

}
