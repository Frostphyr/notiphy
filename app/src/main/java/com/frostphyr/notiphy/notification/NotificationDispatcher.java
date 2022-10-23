package com.frostphyr.notiphy.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.frostphyr.notiphy.AndroidUtils;
import com.frostphyr.notiphy.MatureContent;
import com.frostphyr.notiphy.R;
import com.frostphyr.notiphy.settings.Setting;

import java.util.ArrayList;
import java.util.List;

public class NotificationDispatcher {

    public static final int MAX_MESSAGES = 999;

    private static final String CHANNEL_ID = "all";
    private static final int ID = 0;

    private static int flags;
    private static boolean init;

    private static void init(Context context) {
        init = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(
                    new NotificationChannel(CHANNEL_ID,
                            context.getString(R.string.all),
                            NotificationManager.IMPORTANCE_DEFAULT));
        }

        flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
    }

    public static void dispatch(Context context, Message message) {
        if (!init) {
            init(context);
        }

        MatureContent matureContent = Setting.MATURE_CONTENT.get(context);
        if (matureContent == MatureContent.BLOCK && message.mature) {
            return;
        }

        MessageDao messageDao = MessageDao.getInstance(context);
        List<Message> messages = messageDao.getAll();
        if (messages.size() < MAX_MESSAGES) {
            try {
                messageDao.add(message);
            } catch (SQLiteConstraintException e) {
                AndroidUtils.handleError(context, e);
                return;
            }
        }

        NotificationHelper helper;
        if (messages.size() > 1) {
            helper = new ListNotificationHelper((ArrayList<Message>) messages, matureContent);
        } else if (matureContent == MatureContent.HIDE && message.mature) {
            helper = new HiddenNotificationHelper(message);
        } else {
            helper = new MessageNotificationHelper(message);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        dispatch(context, notificationManager, helper);
    }

    private static void dispatch(Context context, NotificationManagerCompat notificationManager,
                                 NotificationHelper helper) {
        notificationManager.notify(ID, new NotificationCompat.Builder(context, CHANNEL_ID)
                .setOnlyAlertOnce(false)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notification_logo)
                .setColor(ContextCompat.getColor(context, R.color.black))
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setWhen(helper.getTimestamp())
                .setShowWhen(true)
                .setCustomContentView(helper.createSmallView(context))
                .setCustomBigContentView(helper.createBigView(context))
                .setContentIntent(helper.createContentIntent(context, flags))
                .setDeleteIntent(helper.createDeleteIntent(context, flags))
                .build());
    }

}
