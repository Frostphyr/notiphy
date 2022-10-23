package com.frostphyr.notiphy.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.widget.RemoteViews;

public interface NotificationHelper {

    long getTimestamp();

    PendingIntent createContentIntent(Context context, int flags);

    PendingIntent createDeleteIntent(Context context, int flags);

    RemoteViews createSmallView(Context context);

    RemoteViews createBigView(Context context);

}
