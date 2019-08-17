package com.frostphyr.notiphy.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.frostphyr.notiphy.Message;
import com.frostphyr.notiphy.NsfwContent;
import com.frostphyr.notiphy.R;
import com.frostphyr.notiphy.StartupActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class ListNotificationViews extends NotificationViews {

    private static final int SMALL_ITEM_COUNT = 1;
    private static final int BIG_ITEM_COUNT = 5;

    private ArrayList<Message> messages;
    private RemoteViews smallView;
    private RemoteViews bigView;
    private boolean hidden;

    public ListNotificationViews(Context context, NsfwContent nsfwContent, boolean showMedia, int id, ArrayList<Message> messages) {
        super(context, nsfwContent, showMedia, id);

        this.messages = messages;

        init();
    }

    public ListNotificationViews(Context context, NsfwContent nsfwContent, boolean showMedia, int id, Message... messages) {
        this(context, nsfwContent, showMedia, id, new ArrayList<>(Arrays.asList(messages)));
    }

    private void init() {
        smallView = new RemoteViews(getContext().getPackageName(), R.layout.layout_list_notification);
        bigView = new RemoteViews(getContext().getPackageName(), R.layout.layout_list_notification);

        for (int i = 0; i < messages.size(); i++) {
            addToViews(messages.get(i), i);
        }
    }

    public void add(Message message) {
        messages.add(message);
        addToViews(message, messages.size() - 1);
    }

    @Override
    public long getWhen() {
        return Calendar.getInstance().getTime().getTime();
    }

    @Override
    public RemoteViews getSmallView() {
        return smallView;
    }

    @Override
    public RemoteViews getBigView() {
        return bigView;
    }

    @Override
    public Intent getIntent() {
        return new Intent(getContext(), StartupActivity.class)
                .putExtra(StartupActivity.EXTRA_FINISH_ACTIVITY_INTENT, new Intent(getContext(), NotificationListActivity.class)
                        .putExtra(NotificationListActivity.EXTRA_MESSAGES, messages)
                        .putExtra(NotificationListActivity.EXTRA_NSFW_CONTENT_ORDINAL, getNsfwContent().ordinal())
                        .putExtra(NotificationListActivity.EXTRA_SHOW_MEDIA, isShowMedia()));
    }

    private void add(RemoteViews view, RemoteViews itemView, int index, int itemCount) {
        if (index < itemCount) {
            view.addView(R.id.list_notification_layout, itemView);
            if (hidden) {
                view.setViewVisibility(R.id.list_notification_layout, View.INVISIBLE);
                view.setViewVisibility(R.id.list_notification_nsfw_overlay, View.VISIBLE);
            }
        } else {
            if (index == itemCount) {
                RemoteViews moreView = new RemoteViews(getContext().getPackageName(), R.layout.layout_list_notification_more);
                view.addView(R.id.list_notification_layout, moreView);
            }
            view.setTextViewText(R.id.list_notification_more_count, Integer.toString(index - itemCount + 1));
        }

        if (hidden) {
            view.setOnClickPendingIntent(R.id.list_notification_show_nsfw, PendingIntent.getBroadcast(getContext(), getId(),
                    new Intent(getContext(), ListNotificationBroadcastReceiver.class)
                            .putExtra(ListNotificationBroadcastReceiver.EXTRA_NSFW_CONTENT_ORDINAL, NsfwContent.SHOW.ordinal())
                            .putExtra(ListNotificationBroadcastReceiver.EXTRA_SHOW_MEDIA, isShowMedia())
                            .putExtra(ListNotificationBroadcastReceiver.EXTRA_ID, getId())
                            .putExtra(ListNotificationBroadcastReceiver.EXTRA_MESSAGES, messages),
                    PendingIntent.FLAG_UPDATE_CURRENT));
        }
    }

    private void addToViews(Message message, int index) {
        RemoteViews itemView = new RemoteViews(getContext().getPackageName(), R.layout.layout_list_notification_item);
        itemView.setImageViewResource(R.id.list_notification_item_icon, message.getType().getIconResourceId());
        itemView.setTextViewText(R.id.list_notification_item_text, getText(message));
        if (message.isNsfw() && getNsfwContent() == NsfwContent.HIDE) {
            hidden = true;
        }

        add(smallView, itemView, index, SMALL_ITEM_COUNT);
        add(bigView, itemView, index, BIG_ITEM_COUNT);
    }

    private String getText(Message message) {
        StringBuilder builder = new StringBuilder();
        builder.append(message.getTitle());
        if (message.getDescription() != null) {
            builder.append(" \u00B7 ");
            builder.append(message.getDescription());
        } else if (message.getText() != null) {
            builder.append(" \u00B7 ");
            builder.append(message.getText());
        }
        return builder.toString();
    }

}
