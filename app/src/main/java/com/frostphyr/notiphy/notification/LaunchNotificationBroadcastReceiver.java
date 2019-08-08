package com.frostphyr.notiphy.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LaunchNotificationBroadcastReceiver extends BroadcastReceiver {

    public static final String EXTRA_LAUNCH_ACTIVITY_INTENT = "com.frostphyr.notiphy.extra.LAUNCH_ACTIVITY_INTENT";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent activity = intent.getParcelableExtra(EXTRA_LAUNCH_ACTIVITY_INTENT);
        if (activity != null) {
            context.startActivity(activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        int id = intent.getIntExtra(NotificationDispatcher.EXTRA_ID, -1);
        if (id != -1) {
            context.sendBroadcast(new Intent(NotificationDispatcher.ACTION_DISPOSE)
                    .putExtra(NotificationDispatcher.EXTRA_ID, id));
        }
    }

}
