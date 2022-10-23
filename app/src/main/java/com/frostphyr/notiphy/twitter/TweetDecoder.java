package com.frostphyr.notiphy.twitter;

import android.text.SpannableString;

import com.frostphyr.notiphy.EntryType;
import com.frostphyr.notiphy.Media;
import com.frostphyr.notiphy.MediaType;
import com.frostphyr.notiphy.MessageDecoder;
import com.frostphyr.notiphy.notification.Message;

import java.util.Date;
import java.util.Map;

public class TweetDecoder implements MessageDecoder {

    @Override
    public Message decode(Map<String, String> data) {
        Message message = new Message();
        message.type = EntryType.TWITTER;
        message.timestamp = new Date(Long.parseLong(data.get("timestamp")));
        message.title = data.get("username");
        message.text = new SpannableString(data.get("text"));
        message.url = "https://twitter.com/" + message.title + "/status/" + data.get("id");
        message.mature = Boolean.parseBoolean(data.get("mature"));
        if (data.containsKey("media_type")) {
            message.media = new Media(MediaType.valueOf(data.get("media_type")),
                    data.get("thumbnail_url"),
                    Integer.parseInt(data.get("thumbnail_width")),
                    Integer.parseInt(data.get("thumbnail_height")),
                    Integer.parseInt(data.get("media_count")));
        }
        return message;
    }

}
