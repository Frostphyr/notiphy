package com.frostphyr.notiphy.notification;

import android.content.Context;
import android.content.Intent;

import com.frostphyr.notiphy.Message;
import com.frostphyr.notiphy.NsfwContent;

import java.util.ArrayList;

public class ListNotificationBroadcastReceiver extends NotificationBroadcastReceiver {

    public static final String EXTRA_MESSAGES = "com.frostphyr.notiphy.extra.MESSAGES";

    @Override
    protected NotificationViews createViews(Context context, Intent intent) {
        ArrayList<Message> messages = intent.getParcelableArrayListExtra(EXTRA_MESSAGES);
        return new ListNotificationViews(context, NsfwContent.values()[intent.getIntExtra(EXTRA_NSFW_CONTENT_ORDINAL, -1)],
                intent.getBooleanExtra(EXTRA_SHOW_MEDIA, false), intent.getIntExtra(EXTRA_ID, -1), messages);
    }

}
