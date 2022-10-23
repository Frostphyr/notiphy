package com.frostphyr.notiphy.reddit;

import androidx.core.text.HtmlCompat;

import com.frostphyr.notiphy.EntryType;
import com.frostphyr.notiphy.Media;
import com.frostphyr.notiphy.MediaType;
import com.frostphyr.notiphy.MessageDecoder;
import com.frostphyr.notiphy.notification.Message;

import java.util.Date;
import java.util.Map;

public class RedditPostDecoder implements MessageDecoder {

    @Override
    public Message decode(Map<String, String> data) {
        Message message = new Message();
        message.type = EntryType.REDDIT;
        message.timestamp = new Date(Long.parseLong(data.get("timestamp")));
        message.title = data.get("title");
        message.description = "r/" + data.get("subreddit") + " \u00B7 u/" + data.get("user");
        message.url = data.get("url");
        message.mature = Boolean.parseBoolean(data.get("mature"));
        if (data.containsKey("text")) {
            message.text = HtmlCompat.fromHtml(data.get("text"), HtmlCompat.FROM_HTML_MODE_COMPACT);
        } else if (data.containsKey("media_type")) {
            message.media = new Media(MediaType.valueOf(data.get("media_type")),
                    data.get("thumbnail_url"),
                    Integer.parseInt(data.get("thumbnail_width")),
                    Integer.parseInt(data.get("thumbnail_height")),
                    1);
        }
        return message;
    }

}
