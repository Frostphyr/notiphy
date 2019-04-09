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
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.RemoteViews;

import com.frostphyr.notiphy.io.ImageDownloader;

import java.text.DateFormat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationDispatcher {

    private static final String CHANNEL_ID_SUFFIX = "_CHANNEL_ID";

    private static final String ACTION_PREVIOUS_MEDIA = "notificationPreviousMedia";
    private static final String ACTION_NEXT_MEDIA = "notificationNextMedia";
    private static final String ACTION_OPEN_URL = "notificationOpenUrl";
    private static final String ACTION_SHOW_NSFW = "notificationShowNsfw";

    private NsfwContent nsfwContent;
    private boolean showMedia;

    private NotificationManagerCompat notificationManager;
    private Context context;
    private int id;

    public NotificationDispatcher(Context context) {
        this.context = context;

        init();
    }

    public void dispatch(Message message) {
        if (nsfwContent != NsfwContent.BLOCK || !message.isNsfw()) {
            State state = new State();
            state.smallView = createView(message, false);
            state.bigView = createView(message, true);
            state.channelId = message.getType().toString() + CHANNEL_ID_SUFFIX;
            state.url = message.getUrl();
            state.media = message.getMedia();
            state.id = id++;
            state.iconResId = message.getType().getIconResourceId();
            state.hideNsfw = nsfwContent == NsfwContent.HIDE && message.isNsfw();

            finalizeNotification(state);
        }
    }

    public void setNsfwContent(NsfwContent nsfwContent) {
        this.nsfwContent = nsfwContent;
    }

    public void setShowMedia(boolean showMedia) {
        this.showMedia = showMedia;
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
        context.registerReceiver(new ShowNsfwBroadcastReceiver(), new IntentFilter(ACTION_SHOW_NSFW));
    }

    private void finalizeNotification(State state) {
        if (showMedia && state.media != null && state.media.length > 1) {
            state.bigView.setBoolean(R.id.notification_media_previous_button, "setEnabled", false);
            state.bigView.setBoolean(R.id.notification_media_next_button, "setEnabled", false);
            state.bigView.setTextViewText(R.id.notification_media_count, (state.mediaIndex + 1) + "/" + state.media.length);
            state.bigView.setViewVisibility(R.id.notification_media_loading, View.VISIBLE);
            state.bigView.setViewVisibility(R.id.notification_media_image_view, View.INVISIBLE);
            state.bigView.setViewVisibility(R.id.notification_media_icon_view, View.INVISIBLE);

            Intent previousIntent = new Intent(ACTION_PREVIOUS_MEDIA).putExtra("state", state);
            state.bigView.setOnClickPendingIntent(R.id.notification_media_previous_button, PendingIntent.getBroadcast(context, id, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT));

            Intent nextIntent = new Intent(ACTION_PREVIOUS_MEDIA).putExtra("state", state);
            state.bigView.setOnClickPendingIntent(R.id.notification_media_next_button, PendingIntent.getBroadcast(context, id, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT));

            Intent openMediaIntent = new Intent(ACTION_OPEN_URL);
            openMediaIntent.putExtra("url", state.media[state.mediaIndex].getUrl());
            state.bigView.setOnClickPendingIntent(R.id.notification_media, PendingIntent.getBroadcast(context, id, openMediaIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        }

        if (state.hideNsfw) {
            Intent nextIntent = new Intent(ACTION_SHOW_NSFW).putExtra("state", state);
            state.smallView.setOnClickPendingIntent(R.id.notification_show_nsfw, PendingIntent.getBroadcast(context, id, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT));
            state.bigView.setOnClickPendingIntent(R.id.notification_show_nsfw, PendingIntent.getBroadcast(context, id, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT));

            state.smallView.setViewVisibility(R.id.notification_content, View.INVISIBLE);
            state.bigView.setViewVisibility(R.id.notification_content, View.INVISIBLE);
        } else {
            state.smallView.setViewVisibility(R.id.notification_nsfw_layout, View.GONE);
            state.bigView.setViewVisibility(R.id.notification_nsfw_layout, View.GONE);
            state.smallView.setViewVisibility(R.id.notification_content, View.VISIBLE);
            state.bigView.setViewVisibility(R.id.notification_content, View.VISIBLE);
        }

        notificationManager.notify(id, createNotification(state));

        if (showMedia && state.media != null && state.media.length > 0) {
            Media m = (Media) state.media[state.mediaIndex];
            downloadImage(m, state);
        }
    }

    private Notification createNotification(State state) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, state.channelId)
                .setOnlyAlertOnce(true)
                .setSmallIcon(state.iconResId)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(state.smallView)
                .setCustomBigContentView(state.bigView)
                .setAutoCancel(true);
        if (!state.hideNsfw) {
            builder.setContentIntent(PendingIntent.getBroadcast(context, id,
                    new Intent(ACTION_OPEN_URL).putExtra("url", state.url), PendingIntent.FLAG_UPDATE_CURRENT));
        }
        return builder.build();
    }

    private RemoteViews createView(Message message, boolean big) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.layout_notification);
        views.setImageViewResource(R.id.notification_icon, message.getType().getIconResourceId());
        views.setTextViewText(R.id.notification_title, message.getTitle());
        setText(views, R.id.notification_description, message.getDescription());
        setText(views, R.id.notification_text, message.getText());
        setText(views, R.id.notification_time, DateFormat.getTimeInstance().format(message.getCreatedAt()));
        if (big) {
            if (!showMedia || message.getMedia() == null || message.getMedia().length == 0) {
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

    private void setText(RemoteViews views, int viewId, String text) {
        if (text != null) {
            views.setTextViewText(viewId, text);
        } else {
            views.setViewVisibility(viewId, View.GONE);
        }
    }

    private void downloadImage(final Media media, final State state) {
        ImageDownloader.execute(media.getThumbnailUrl(), new ImageDownloader.Callback() {

            @Override
            public void onDownload(Bitmap bitmap) {
                state.bigView.setImageViewBitmap(R.id.notification_media_image_view, bitmap);
                state.bigView.setViewVisibility(R.id.notification_media_image_view, View.VISIBLE);
                if (media.getType() != MediaType.IMAGE) {
                    state.bigView.setImageViewResource(R.id.notification_media_icon_view, R.drawable.ic_play);
                    state.bigView.setViewVisibility(R.id.notification_media_icon_view, View.VISIBLE);
                }

                onResult();
            }

            @Override
            public void onFailure(Exception e) {
                state.bigView.setImageViewResource(R.id.notification_media_icon_view, R.drawable.ic_error);
                state.bigView.setViewVisibility(R.id.notification_media_icon_view, View.VISIBLE);

                onResult();
            }

            private void onResult() {
                state.bigView.setViewVisibility(R.id.notification_media_loading, View.GONE);
                state.bigView.setBoolean(R.id.notification_media_previous_button, "setEnabled", true);
                state.bigView.setBoolean(R.id.notification_media_next_button, "setEnabled", true);

                notificationManager.notify(id, createNotification(state));
            }

        });
    }

    private class NavigateMediaBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            State state = intent.getParcelableExtra("state");
            if (intent.getAction().equals(ACTION_PREVIOUS_MEDIA)) {
                if (--state.mediaIndex < 0) {
                    state.mediaIndex = state.media.length - 1;
                }
            } else if (intent.getAction().equals(ACTION_NEXT_MEDIA)) {
                if (++state.mediaIndex >= state.media.length) {
                    state.mediaIndex = 0;
                }
            } else {
                return;
            }

            finalizeNotification(state);
        }

    }

    private class OpenUrlBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(intent.getStringExtra("url")));
            context.startActivity(viewIntent);
        }

    }

    private class ShowNsfwBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            State state = intent.getParcelableExtra("state");
            state.hideNsfw = false;
            finalizeNotification(state);
        }

    }

    private static class State implements Parcelable {

        private RemoteViews smallView;
        private RemoteViews bigView;
        private String channelId;
        private String url;
        private Media[] media;
        private int id;
        private int iconResId;
        private int mediaIndex;
        private boolean hideNsfw;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(smallView, 0);
            dest.writeParcelable(bigView, 0);
            dest.writeString(channelId);
            dest.writeString(url);
            dest.writeTypedArray(media, 0);
            dest.writeInt(id);
            dest.writeInt(iconResId);
            dest.writeInt(mediaIndex);
            dest.writeInt(hideNsfw ? 1 : 0);
        }

        public static final Parcelable.Creator<State> CREATOR = new Parcelable.Creator<State>() {

            @Override
            public State createFromParcel(Parcel in) {
                State state = new State();
                state.smallView = in.readParcelable(RemoteViews.class.getClassLoader());
                state.bigView = in.readParcelable(RemoteViews.class.getClassLoader());
                state.channelId = in.readString();
                state.url = in.readString();
                state.media = in.createTypedArray(Media.CREATOR);
                state.id = in.readInt();
                state.iconResId = in.readInt();
                state.mediaIndex = in.readInt();
                state.hideNsfw = in.readInt() == 1;
                return state;
            }

            @Override
            public State[] newArray(int size) {
                return new State[size];
            }

        };

    }

}
