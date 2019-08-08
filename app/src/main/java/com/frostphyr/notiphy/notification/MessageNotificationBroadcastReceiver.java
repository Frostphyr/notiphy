package com.frostphyr.notiphy.notification;

import android.content.Context;
import android.content.Intent;

import com.frostphyr.notiphy.Message;
import com.frostphyr.notiphy.NsfwContent;

public class MessageNotificationBroadcastReceiver extends NotificationBroadcastReceiver {

    public static final String EXTRA_MESSAGE = "com.frostphyr.notiphy.extra.MESSAGE";
    public static final String EXTRA_MEDIA_INDEX = "com.frostphyr.notiphy.extra.MEDIA_INDEX";

    @Override
    protected NotificationViews createViews(Context context, Intent intent) {
        return new MessageNotificationViews(context, NsfwContent.values()[intent.getIntExtra(EXTRA_NSFW_CONTENT_ORDINAL, -1)],
                intent.getBooleanExtra(EXTRA_SHOW_MEDIA, false), intent.getIntExtra(EXTRA_ID, -1),
                (Message) intent.getParcelableExtra(EXTRA_MESSAGE), intent.getIntExtra(EXTRA_MEDIA_INDEX, -1));
    }

}
