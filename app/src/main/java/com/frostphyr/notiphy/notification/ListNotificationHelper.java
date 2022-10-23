package com.frostphyr.notiphy.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.content.ContextCompat;

import com.frostphyr.notiphy.IconResource;
import com.frostphyr.notiphy.MatureContent;
import com.frostphyr.notiphy.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.ListIterator;

public class ListNotificationHelper implements NotificationHelper {

    private static final int MAX_ITEMS_SMALL = 2;
    private static final int MAX_ITEMS_BIG = 6;

    private final ArrayList<Message> messages;
    private final ArrayList<String> urls;
    private final MatureContent matureContent;

    public ListNotificationHelper(ArrayList<Message> messages, MatureContent matureContent) {
        this.messages = messages;
        this.matureContent = matureContent;

        urls = new ArrayList<>(messages.size());
        for (Message m : messages) {
            urls.add(m.url);
        }
    }

    @Override
    public long getTimestamp() {
        return Calendar.getInstance().getTime().getTime();
    }

    @Override
    public PendingIntent createContentIntent(Context context, int flags) {
        return PendingIntent.getActivity(context, 1,
                new Intent(context, NotificationListActivity.class)
                        .setAction(Long.toString(System.currentTimeMillis()))
                        .putStringArrayListExtra(NotificationListActivity.EXTRA_URLS, urls),
                flags);
    }

    @Override
    public PendingIntent createDeleteIntent(Context context, int flags) {
        return PendingIntent.getBroadcast(context, 1,
                new Intent(context, DeleteAllMessagesBroadcastReceiver.class)
                        .putStringArrayListExtra(DeleteAllMessagesBroadcastReceiver.EXTRA_URLS, urls),
                flags);
    }

    @Override
    public RemoteViews createSmallView(Context context) {
        return createView(context, MAX_ITEMS_SMALL);
    }

    @Override
    public RemoteViews createBigView(Context context) {
        return messages.size() > MAX_ITEMS_SMALL ? createView(context, MAX_ITEMS_BIG) : null;
    }

    private RemoteViews createView(Context context, int maxItems) {
        RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.layout_list_notification);
        if (messages.size() > maxItems) {
            StringBuilder builder = new StringBuilder()
                    .append(context.getString(R.string.list_notification_more_arg,
                            Integer.toString(messages.size() - maxItems + 1)));
            if (messages.size() >= NotificationDispatcher.MAX_MESSAGES) {
                builder.append(" (")
                        .append(context.getString(R.string.maximum_reached))
                        .append(')');
            }
            view.setTextViewText(R.id.list_notification_more, builder.toString());
        } else {
            view.setViewVisibility(R.id.list_notification_more, View.GONE);
        }

        for (ListIterator<Message> it = messages.listIterator(); it.hasNext(); ) {
            int index = it.nextIndex();
            Message message = it.next();
            if (messages.size() == maxItems && index >= maxItems ||
                    messages.size() > maxItems && index >= maxItems - 1) {
                break;
            } else {
                if (matureContent == MatureContent.HIDE && message.mature) {
                    view.addView(R.id.list_notification_layout,
                            new RemoteViews(context.getPackageName(), R.layout.layout_hidden_notification));
                } else {
                    RemoteViews itemView = new RemoteViews(context.getPackageName(),
                            R.layout.layout_list_notification_item);
                    itemView.setImageViewResource(R.id.list_notification_item_icon,
                            message.type.getIconResource().getDrawableResId());
                    itemView.setContentDescription(R.id.list_notification_item_icon,
                            context.getString(message.type.getIconResource().getStringResId()));
                    itemView.setTextViewText(R.id.list_notification_item_text, getText(message));
                    if (message.media != null) {
                        IconResource iconResource = message.media.getIconResource();
                        itemView.setInt(R.id.list_notification_item_media_icon,
                                "setColorFilter", ContextCompat.getColor(context, R.color.cyan));
                        itemView.setImageViewResource(R.id.list_notification_item_media_icon,
                                iconResource.getDrawableResId());
                        itemView.setContentDescription(R.id.list_notification_item_media_icon,
                                context.getString(iconResource.getStringResId()));
                    } else {
                        itemView.setViewVisibility(R.id.list_notification_item_media_icon, View.GONE);
                    }
                    view.addView(R.id.list_notification_layout, itemView);
                }
            }
        }
        return view;
    }

    private String getText(Message message) {
        StringBuilder builder = new StringBuilder();
        builder.append(message.title);
        if (message.description != null) {
            builder.append(" \u00B7 ");
            builder.append(message.description);
        } else if (message.text != null) {
            builder.append(" \u00B7 ");
            builder.append(message.text);
        }
        return builder.toString();
    }

}
