package com.frostphyr.notiphy.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import com.frostphyr.notiphy.Media;
import com.frostphyr.notiphy.MediaType;
import com.frostphyr.notiphy.Message;
import com.frostphyr.notiphy.NsfwContent;
import com.frostphyr.notiphy.R;
import com.frostphyr.notiphy.io.ImageDownloader;

public class MessageNotificationViews extends NotificationViews {

    private static final String ACTION_PREVIOUS_MEDIA = "com.frostphyr.notiphy.action.PREVIOUS_MEDIA";
    private static final String ACTION_NEXT_MEDIA = "com.frostphyr.notiphy.action.NEXT_MEDIA";

    private Message message;
    private RemoteViews smallView;
    private RemoteViews bigView;
    private int mediaIndex;

    public MessageNotificationViews(Context context, NsfwContent nsfwContent, boolean showMedia, int id, Message message, int mediaIndex) {
        super(context, nsfwContent, showMedia, id);

        this.message = message;
        this.mediaIndex = mediaIndex;

        init();
    }

    private void init() {
        createSmallView();
        createBigView();
    }

    public Message getMessage() {
        return message;
    }

    @Override
    public long getWhen() {
        return message.getCreatedAt().getTime();
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
        return new Intent(Intent.ACTION_VIEW, Uri.parse(message.getUrl()));
    }

    private Intent createBroadcastIntent() {
        return new Intent(getContext(), MessageNotificationBroadcastReceiver.class)
                .putExtra(MessageNotificationBroadcastReceiver.EXTRA_NSFW_CONTENT_ORDINAL, getNsfwContent().ordinal())
                .putExtra(MessageNotificationBroadcastReceiver.EXTRA_SHOW_MEDIA, isShowMedia())
                .putExtra(MessageNotificationBroadcastReceiver.EXTRA_ID, getId())
                .putExtra(MessageNotificationBroadcastReceiver.EXTRA_MESSAGE, message)
                .putExtra(MessageNotificationBroadcastReceiver.EXTRA_MEDIA_INDEX, mediaIndex);
    }

    private RemoteViews createBaseView() {
        RemoteViews view = new RemoteViews(getContext().getPackageName(), R.layout.layout_notification);
        view.setImageViewResource(R.id.notification_icon, message.getType().getIconResourceId());
        view.setCharSequence(R.id.notification_icon, "setContentDescription", message.getType().getName());
        view.setInt(R.id.notification_description, "setMaxLines", 1);
        view.setViewVisibility(R.id.notification_time, View.GONE);
        view.setViewVisibility(R.id.notification_expand, View.GONE);
        setText(view, R.id.notification_title, message.getTitle());
        setText(view, R.id.notification_description, message.getDescription());
        setText(view, R.id.notification_text, message.getText());
        if (getNsfwContent() == NsfwContent.HIDE && message.isNsfw()) {
            view.setViewVisibility(R.id.notification_content, View.INVISIBLE);
            view.setOnClickPendingIntent(R.id.notification_show_nsfw,
                    PendingIntent.getBroadcast(getContext(), getId(), createBroadcastIntent()
                            .putExtra(MessageNotificationBroadcastReceiver.EXTRA_NSFW_CONTENT_ORDINAL, NsfwContent.SHOW.ordinal()),
                    PendingIntent.FLAG_UPDATE_CURRENT));
        } else {
            view.setViewVisibility(R.id.notification_nsfw_overlay, View.GONE);
        }
        return view;
    }

    private void createSmallView() {
        smallView = createBaseView();
        smallView.setBoolean(R.id.notification_text, "setSingleLine", true);
    }

    private void createBigView() {
        bigView = createBaseView();
        bigView.setInt(R.id.notification_text, "setMaxLines", 4);
        if (!isShowMedia() || message.getMedia() == null || message.getMedia().length == 0) {
            bigView.setViewVisibility(R.id.notification_media_layout, View.GONE);
            bigView.setViewVisibility(R.id.notification_media_count_layout, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.notification_media_loading, View.VISIBLE);
            bigView.setViewVisibility(R.id.notification_media_image, View.INVISIBLE);
            bigView.setViewVisibility(R.id.notification_media_icon, View.INVISIBLE);
            bigView.setOnClickPendingIntent(R.id.notification_media_image, PendingIntent.getBroadcast(getContext(), getId(),
                    new Intent(getContext(), OpenUrlBroadcastReceiver.class)
                            .putExtra(OpenUrlBroadcastReceiver.EXTRA_URL, message.getMedia()[mediaIndex].getUrl()),
                    PendingIntent.FLAG_UPDATE_CURRENT));
            if (message.getMedia().length == 1) {
                bigView.setViewVisibility(R.id.notification_media_count_layout, View.GONE);
            } else {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    bigView.setImageViewResource(R.id.notification_media_previous_button, R.drawable.ic_previous_white);
                    bigView.setImageViewResource(R.id.notification_media_next_button, R.drawable.ic_next_white);
                } else {
                    bigView.setImageViewResource(R.id.notification_media_previous_button, R.drawable.ic_previous);
                    bigView.setImageViewResource(R.id.notification_media_next_button, R.drawable.ic_next);
                }
                bigView.setBoolean(R.id.notification_media_previous_button, "setEnabled", false);
                bigView.setBoolean(R.id.notification_media_next_button, "setEnabled", false);
                bigView.setTextViewText(R.id.notification_media_count, (mediaIndex + 1) + "/" + message.getMedia().length);

                int previousIndex = mediaIndex - 1;
                if (previousIndex < 0) {
                    previousIndex = message.getMedia().length - 1;
                }
                int nextIndex = mediaIndex + 1;
                if (nextIndex >= message.getMedia().length) {
                    nextIndex = 0;
                }
                bigView.setOnClickPendingIntent(R.id.notification_media_previous_button,
                        PendingIntent.getBroadcast(getContext(), getId(),
                                createBroadcastIntent()
                                .setAction(ACTION_PREVIOUS_MEDIA)
                                .putExtra(MessageNotificationBroadcastReceiver.EXTRA_MEDIA_INDEX, previousIndex),
                        PendingIntent.FLAG_UPDATE_CURRENT));
                bigView.setOnClickPendingIntent(R.id.notification_media_next_button,
                        PendingIntent.getBroadcast(getContext(), getId(),
                                createBroadcastIntent()
                                        .setAction(ACTION_NEXT_MEDIA)
                                        .putExtra(MessageNotificationBroadcastReceiver.EXTRA_MEDIA_INDEX, nextIndex),
                                PendingIntent.FLAG_UPDATE_CURRENT));
            }
            downloadImage();
        }
    }

    private void downloadImage() {
        final Media media = message.getMedia()[mediaIndex];
        ImageDownloader.execute(media.getThumbnailUrl(), new ImageDownloader.Callback() {

            @Override
            public void onDownload(Bitmap bitmap) {
                bigView.setImageViewBitmap(R.id.notification_media_image, bitmap);
                bigView.setViewVisibility(R.id.notification_media_image, View.VISIBLE);
                if (media.getType() == MediaType.VIDEO) {
                    bigView.setCharSequence(R.id.notification_media_image, "setContentDescription", getContext().getString(R.string.video));
                    bigView.setCharSequence(R.id.notification_media_icon, "setContentDescription", getContext().getString(R.string.play));
                    bigView.setImageViewResource(R.id.notification_media_icon, R.drawable.ic_play);
                    bigView.setViewVisibility(R.id.notification_media_icon, View.VISIBLE);
                } else {
                    bigView.setCharSequence(R.id.notification_media_image, "setContentDescription", getContext().getString(R.string.image));
                }
                onResult();
            }

            @Override
            public void onFailure(Exception e) {
                bigView.setCharSequence(R.id.notification_media_icon, "setContentDescription", getContext().getString(R.string.error));
                bigView.setImageViewResource(R.id.notification_media_icon, R.drawable.ic_error);
                bigView.setViewVisibility(R.id.notification_media_icon, View.VISIBLE);
                onResult();
            }

            private void onResult() {
                bigView.setViewVisibility(R.id.notification_media_loading, View.GONE);
                bigView.setBoolean(R.id.notification_media_previous_button, "setEnabled", true);
                bigView.setBoolean(R.id.notification_media_next_button, "setEnabled", true);
                onUpdate();
            }

        });
    }

    private void setText(RemoteViews views, int viewId, CharSequence text) {
        if (text != null) {
            views.setTextViewText(viewId, text);
        } else {
            views.setViewVisibility(viewId, View.GONE);
        }
    }

}
