package com.frostphyr.notiphy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.frostphyr.notiphy.io.ImageDownloader;

import java.text.DateFormat;

public class NotificationDispatcher {

    private static final String CHANNEL_ID_SUFFIX = "_CHANNEL_ID";

    private static final String ACTION_PREVIOUS_MEDIA = "notificationPreviousMedia";
    private static final String ACTION_NEXT_MEDIA = "notificationNextMedia";
    private static final String ACTION_OPEN_URL = "notificationOpenUrl";

    private NotificationManagerCompat notificationManager;
    private Context context;
    private int id;

    public NotificationDispatcher(Context context) {
        this.context = context;

        init();
    }

    public void dispatch(Message message) {
        String channelId = message.getType() + CHANNEL_ID_SUFFIX;
        RemoteViews smallView = createView(message, false);
        RemoteViews bigView = createView(message, true);

        finalizeNotification(channelId, message.getUrl(), id, smallView, bigView, message);

        id++;
    }

    private void init() {
        notificationManager = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            for (EntryType t : EntryType.values()) {
                NotificationChannel channel = new NotificationChannel(t + CHANNEL_ID_SUFFIX, t.getName(), NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
        }

        context.registerReceiver(new NavigateMediaBroadcastReceiver(), new IntentFilter(ACTION_PREVIOUS_MEDIA));
        context.registerReceiver(new NavigateMediaBroadcastReceiver(), new IntentFilter(ACTION_NEXT_MEDIA));
        context.registerReceiver(new OpenUrlBroadcastReceiver(), new IntentFilter(ACTION_OPEN_URL));
    }

    private Intent createMediaIntent(String action, int id, String channelId, String url, int iconResId, RemoteViews smallView, RemoteViews bigView, Parcelable[] media, int mediaIndex) {
        Intent intent = new Intent(action);
        intent.putExtra("id", id);
        intent.putExtra("channelId", channelId);
        intent.putExtra("url", url);
        intent.putExtra("iconResId", iconResId);
        intent.putExtra("smallView", smallView);
        intent.putExtra("bigView", bigView);
        intent.putExtra("media", media);
        intent.putExtra("mediaIndex", mediaIndex);
        return intent;
    }

    private void finalizeNotification(String channelId, String url, int iconResId, int id, RemoteViews smallView, RemoteViews bigView, Parcelable[] media, int mediaIndex) {
        if (media != null && media.length > 1) {
            bigView.setBoolean(R.id.notification_media_previous_button, "setEnabled", false);
            bigView.setBoolean(R.id.notification_media_next_button, "setEnabled", false);
            bigView.setTextViewText(R.id.notification_media_count, (mediaIndex + 1) + "/" + media.length);
            bigView.setViewVisibility(R.id.notification_media_loading, View.VISIBLE);
            bigView.setViewVisibility(R.id.notification_media_image_view, View.INVISIBLE);
            bigView.setViewVisibility(R.id.notification_media_icon_view, View.INVISIBLE);

            Intent previousIntent = createMediaIntent(ACTION_PREVIOUS_MEDIA, id, channelId, url, iconResId, smallView, bigView, media, mediaIndex);
            bigView.setOnClickPendingIntent(R.id.notification_media_previous_button, PendingIntent.getBroadcast(context, id, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT));

            Intent nextIntent = createMediaIntent(ACTION_NEXT_MEDIA, id, channelId, url, iconResId, smallView, bigView, media, mediaIndex);
            bigView.setOnClickPendingIntent(R.id.notification_media_next_button, PendingIntent.getBroadcast(context, id, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT));

            Intent openMediaIntent = new Intent(ACTION_OPEN_URL);
            openMediaIntent.putExtra("url", ((Media) media[mediaIndex]).getUrl());
            bigView.setOnClickPendingIntent(R.id.notification_media, PendingIntent.getBroadcast(context, id, openMediaIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        }

        notificationManager.notify(id, createNotification(channelId, url, id, iconResId, smallView, bigView));

        if (media != null && media.length > 0) {
            Media m = (Media) media[mediaIndex];
            downloadImage(m, channelId, url, iconResId, id, smallView, bigView);
        }
    }

    private void finalizeNotification(String channelId, String url, int id, RemoteViews smallView, RemoteViews bigView, Message message) {
        finalizeNotification(channelId, url, message.getType().getIconResourceId(), id, smallView, bigView, message.getMedia(), 0);
    }

    private Notification createNotification(String channelId, String url, int id, int iconResId, RemoteViews smallView, RemoteViews bigView) {
        Intent openUrlIntent = new Intent(ACTION_OPEN_URL);
        openUrlIntent.putExtra("url", url);

        return new NotificationCompat.Builder(context, channelId)
                .setOnlyAlertOnce(true)
                .setSmallIcon(iconResId)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(smallView)
                .setCustomBigContentView(bigView)
                .setContentIntent(PendingIntent.getBroadcast(context, id, openUrlIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(true)
                .build();
    }

    private RemoteViews createView(Message message, boolean big) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.layout_notification);
        views.setImageViewResource(R.id.notification_icon, message.getType().getIconResourceId());
        if (message.getCreatedAt() != null) {
            views.setTextViewText(R.id.notification_time, DateFormat.getTimeInstance().format(message.getCreatedAt()));
        } else {
            views.setViewVisibility(R.id.notification_time, View.GONE);
        }
        views.setTextViewText(R.id.notification_title, message.getUsername());
        views.setTextViewText(R.id.notification_text, message.getText());
        if (big) {
            if (message.getMedia() == null || message.getMedia().length == 0) {
                views.setViewVisibility(R.id.notification_media, View.GONE);
                views.setViewVisibility(R.id.notification_media_count_layout, View.GONE);
            } else {
                if (message.getMedia().length == 1) {
                    views.setViewVisibility(R.id.notification_media_count_layout, View.GONE);
                } else {
                    views.setImageViewResource(R.id.notification_media_previous_button, R.drawable.ic_previous);
                    views.setImageViewResource(R.id.notification_media_next_button, R.drawable.ic_next);
                }
            }
        } else {
            views.setBoolean(R.id.notification_text, "setSingleLine", true);
        }
        return views;
    }

    private void downloadImage(final Media media, final String channelId, final String url, final int iconResId, final int id, final RemoteViews smallView, final RemoteViews bigView) {
        ImageDownloader.execute(media.getThumbnailUrl(), new ImageDownloader.Callback() {

            @Override
            public void onDownload(Bitmap bitmap) {
                bigView.setImageViewBitmap(R.id.notification_media_image_view, bitmap);
                bigView.setViewVisibility(R.id.notification_media_image_view, View.VISIBLE);
                if (media.getType() != MediaType.IMAGE) {
                    bigView.setImageViewResource(R.id.notification_media_icon_view, R.drawable.ic_play);
                    bigView.setViewVisibility(R.id.notification_media_icon_view, View.VISIBLE);
                }

                onResult();
            }

            @Override
            public void onFailure(Exception e) {
                bigView.setImageViewResource(R.id.notification_media_icon_view, R.drawable.ic_error);
                bigView.setViewVisibility(R.id.notification_media_icon_view, View.VISIBLE);

                onResult();
            }

            private void onResult() {
                bigView.setViewVisibility(R.id.notification_media_loading, View.GONE);
                bigView.setBoolean(R.id.notification_media_previous_button, "setEnabled", true);
                bigView.setBoolean(R.id.notification_media_next_button, "setEnabled", true);

                notificationManager.notify(id, createNotification(channelId, url, id, iconResId, smallView, bigView));
            }

        });
    }

    private class NavigateMediaBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Parcelable[] media = intent.getParcelableArrayExtra("media");
            int mediaIndex = intent.getIntExtra("mediaIndex", -1);
            if (intent.getAction().equals(ACTION_PREVIOUS_MEDIA)) {
                if (--mediaIndex < 0) {
                    mediaIndex = media.length - 1;
                }
            } else if (intent.getAction().equals(ACTION_NEXT_MEDIA)) {
                if (++mediaIndex >= media.length) {
                    mediaIndex = 0;
                }
            } else {
                return;
            }

            finalizeNotification(intent.getStringExtra("chanelId"), intent.getStringExtra("url"), intent.getIntExtra("iconResId", -1), intent.getIntExtra("id", -1),
                    (RemoteViews) intent.getParcelableExtra("smallView"), (RemoteViews) intent.getParcelableExtra("bigView"), media, mediaIndex);
        }

    }

    private class OpenUrlBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(intent.getStringExtra("url")));
            context.startActivity(viewIntent);
        }

    }

}
