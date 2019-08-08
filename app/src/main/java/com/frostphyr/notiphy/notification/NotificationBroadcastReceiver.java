package com.frostphyr.notiphy.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class NotificationBroadcastReceiver extends BroadcastReceiver {

    public static final String EXTRA_NSFW_CONTENT_ORDINAL = "com.frostphyr.notiphy.extra.NSFW_CONTENT_ORDINAL";
    public static final String EXTRA_SHOW_MEDIA = "com.frostphyr.notiphy.extra.SHOW_MEDIA";
    public static final String EXTRA_ID = "com.frostphyr.notiphy.extra.ID";

    @Override
    public void onReceive(final Context context, Intent intent) {
        NotificationDispatcher.dispatch(context, createViews(context, intent));
    }

    protected abstract NotificationViews createViews(Context context, Intent intent);

}
