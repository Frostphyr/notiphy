package com.frostphyr.notiphy.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.content.ContextCompat;

import com.frostphyr.notiphy.AndroidUtils;
import com.frostphyr.notiphy.IconResource;
import com.frostphyr.notiphy.R;
import com.frostphyr.notiphy.settings.Setting;

public class MessageNotificationHelper implements NotificationHelper {

    private final Message message;

    public MessageNotificationHelper(Message message) {
        this.message = message;
    }

    @Override
    public long getTimestamp() {
        return message.timestamp.getTime();
    }

    @Override
    public PendingIntent createContentIntent(Context context, int flags) {
        return PendingIntent.getBroadcast(context, 1,
                new Intent(context, OpenMessageBroadcastReceiver.class)
                        .putExtra(OpenMessageBroadcastReceiver.EXTRA_URL, message.url),
                flags);
    }

    @Override
    public PendingIntent createDeleteIntent(Context context, int flags) {
        return PendingIntent.getBroadcast(context, 1,
                new Intent(context, DeleteMessageBroadcastReceiver.class)
                        .putExtra(DeleteMessageBroadcastReceiver.EXTRA_URL, message.url),
                flags);
    }

    @Override
    public RemoteViews createSmallView(Context context) {
        RemoteViews smallView = createBaseView(context);
        smallView.setViewVisibility(R.id.notification_media_icon, View.GONE);
        if (message.description != null) {
            smallView.setViewVisibility(R.id.notification_text, View.GONE);
        } else {
            setText(smallView, R.id.notification_text, message.text);
            smallView.setInt(R.id.notification_text, "setMaxLines", 1);
        }
        return smallView;
    }

    @Override
    public RemoteViews createBigView(Context context) {
        RemoteViews bigView = createBaseView(context);
        setText(bigView, R.id.notification_text, message.text);
        bigView.setInt(R.id.notification_text, "setMaxLines", 4);
        if (!Setting.SHOW_MEDIA.get(context) || message.media == null) {
            bigView.setViewVisibility(R.id.notification_media_layout, View.GONE);
        } else {
            Bitmap image = AndroidUtils.downloadMedia(message.media, 1024, 512);
            if (image != null) {
                bigView.setImageViewBitmap(R.id.notification_media_image, image);
                IconResource iconResource = message.media.getIconResource();
                bigView.setImageViewResource(R.id.notification_media_image_icon, iconResource.getDrawableResId());
                bigView.setCharSequence(R.id.notification_media_image_icon, "setContentDescription",
                        context.getString(iconResource.getStringResId()));
            } else {
                bigView.setViewVisibility(R.id.notification_media_image, View.GONE);
                bigView.setImageViewResource(R.id.notification_media_image_icon, R.drawable.ic_error);
                bigView.setCharSequence(R.id.notification_media_image_icon, "setContentDescription",
                        context.getString(R.string.error));
            }
        }
        return bigView;
    }

    private RemoteViews createBaseView(Context context) {
        RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.layout_message_notification);
        view.setImageViewResource(R.id.notification_icon, message.type.getIconResource().getDrawableResId());
        view.setContentDescription(R.id.notification_icon, context.getString(message.type.getIconResource().getStringResId()));
        setText(view, R.id.notification_title, message.title);
        setText(view, R.id.notification_description, message.description);
        if (message.media != null) {
            IconResource iconResource = message.media.getIconResource();
            view.setInt(R.id.notification_media_icon, "setColorFilter", ContextCompat.getColor(context, R.color.cyan));
            view.setImageViewResource(R.id.notification_media_icon,
                    iconResource.getDrawableResId());
            view.setContentDescription(R.id.notification_media_icon,
                    context.getString(iconResource.getStringResId()));
        } else {
            view.setViewVisibility(R.id.notification_media_icon, View.GONE);
        }
        return view;
    }

    private void setText(RemoteViews views, int viewId, CharSequence text) {
        if (text != null) {
            views.setTextViewText(viewId, text);
        } else {
            views.setViewVisibility(viewId, View.GONE);
        }
    }

}
