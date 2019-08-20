package com.frostphyr.notiphy.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.frostphyr.notiphy.EntryType;
import com.frostphyr.notiphy.Message;
import com.frostphyr.notiphy.NsfwContent;
import com.frostphyr.notiphy.R;

public class NotificationDispatcher {

    public static final String ACTION_DISPOSE = "com.frostphyr.notiphy.action.DISPOSE";
    public static final String EXTRA_ID = "com.frostphyr.notiphy.action.ID";

    private static final String CHANNEL_ID_PREFIX = "com.frostphyr.notiphy.channel.";
    private static final String CHANNEL_ID_LIST = CHANNEL_ID_PREFIX + "LIST";

    private static int id;

    private NotificationViews activeViews;
    private NotificationManagerCompat manager;
    private Context context;
    private NsfwContent nsfwContent;
    private boolean showMedia;

    public NotificationDispatcher(Context context) {
        this.context = context;

        init();
    }

    public static void dispatch(final Context context, NotificationViews views) {
        if (views.getId() == id || id == 0) {
            String channelId = views instanceof MessageNotificationViews ?
                    CHANNEL_ID_PREFIX + ((MessageNotificationViews) views).getMessage().getType().getName() :
                    CHANNEL_ID_LIST;
            views.setOnUpdateListener(new NotificationViews.OnUpdateListener() {

                @Override
                public void onUpdate(NotificationViews views) {
                    dispatch(context, views);
                }

            });
            NotificationManagerCompat.from(context).notify(views.getId(), new NotificationCompat.Builder(context, channelId)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_notification_logo)
                    .setColor(ContextCompat.getColor(context, R.color.dark_text))
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setWhen(views.getWhen())
                    .setShowWhen(true)
                    .setCustomContentView(views.getSmallView())
                    .setCustomBigContentView(views.getBigView())
                    .setContentIntent(PendingIntent.getBroadcast(context, views.getId(),
                            new Intent(context, LaunchNotificationBroadcastReceiver.class)
                                    .putExtra(EXTRA_ID, views.getId())
                                    .putExtra(LaunchNotificationBroadcastReceiver.EXTRA_LAUNCH_ACTIVITY_INTENT, views.getIntent()),
                            PendingIntent.FLAG_UPDATE_CURRENT))
                    .setDeleteIntent(PendingIntent.getBroadcast(context, views.getId(),
                            new Intent(ACTION_DISPOSE), PendingIntent.FLAG_UPDATE_CURRENT))
                    .build());
        }
    }

    public void dispatch(Message message) {
        if (nsfwContent != NsfwContent.BLOCK || !message.isNsfw()) {
            if (activeViews == null) {
                activeViews = new MessageNotificationViews(context, nsfwContent, showMedia, ++id, message, 0);
            } else if (activeViews instanceof MessageNotificationViews) {
                manager.cancel(id);
                activeViews = new ListNotificationViews(context, nsfwContent, showMedia, ++id,
                        ((MessageNotificationViews) activeViews).getMessage(), message);
            } else if (activeViews instanceof ListNotificationViews) {
                ((ListNotificationViews) activeViews).add(message);
            }
            dispatch(context, activeViews);
        }
    }

    public void setNsfwContent(NsfwContent nsfwContent) {
        this.nsfwContent = nsfwContent;
    }

    public void setShowMedia(boolean showMedia) {
        this.showMedia = showMedia;
    }

    private void init() {
        context.registerReceiver(disposeBroadcastReceiver, new IntentFilter(ACTION_DISPOSE));

        manager = NotificationManagerCompat.from(context);
        manager.cancelAll();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            for (EntryType t : EntryType.values()) {
                createChannel(notificationManager, CHANNEL_ID_PREFIX + t.getName());
            }
            createChannel(notificationManager, CHANNEL_ID_LIST);
        }
    }

    private void dispose() {
        activeViews = null;
    }

    private void createChannel(NotificationManager notificationManager, String name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel(name, name, NotificationManager.IMPORTANCE_DEFAULT));
        }
    }

    private BroadcastReceiver disposeBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            dispose();
        }

    };

}
