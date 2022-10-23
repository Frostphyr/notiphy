package com.frostphyr.notiphy.io;

import androidx.annotation.NonNull;

import com.frostphyr.notiphy.AndroidUtils;
import com.frostphyr.notiphy.EntryType;
import com.frostphyr.notiphy.R;
import com.frostphyr.notiphy.UserNotSignedInException;
import com.frostphyr.notiphy.notification.NotificationDispatcher;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class NotiphyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        Database.setToken(token, result -> {
            Exception e = result.getException();
            if (e != null && !(e instanceof UserNotSignedInException)) {
                AndroidUtils.handleError(NotiphyFirebaseMessagingService.this,
                        e, R.string.error_message_updating_token);
            }
        });
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        try {
            Map<String, String> data = remoteMessage.getData();
            NotificationDispatcher.dispatch(this,
                    EntryType.valueOf(data.get("type")).getMessageDecoder().decode(data));
        } catch (IllegalArgumentException | NullPointerException e) {
            AndroidUtils.handleError(this, e);
        }
    }

}