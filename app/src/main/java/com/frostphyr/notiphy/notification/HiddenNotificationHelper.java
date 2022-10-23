package com.frostphyr.notiphy.notification;

import android.content.Context;
import android.widget.RemoteViews;

import com.frostphyr.notiphy.R;

public class HiddenNotificationHelper extends MessageNotificationHelper {

    public HiddenNotificationHelper(Message message) {
        super(message);
    }

    @Override
    public RemoteViews createSmallView(Context context) {
        return new RemoteViews(context.getPackageName(), R.layout.layout_hidden_notification);
    }

    @Override
    public RemoteViews createBigView(Context context) {
        return null;
    }

}
