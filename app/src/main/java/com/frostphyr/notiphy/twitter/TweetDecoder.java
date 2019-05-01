package com.frostphyr.notiphy.twitter;

import com.frostphyr.notiphy.EntryType;
import com.frostphyr.notiphy.Media;
import com.frostphyr.notiphy.MediaType;
import com.frostphyr.notiphy.Message;
import com.frostphyr.notiphy.io.JSONDecoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class TweetDecoder implements JSONDecoder<Message> {

    @Override
    public Message decode(JSONObject obj) throws JSONException {
        String username = obj.getString("username");
        Message.Builder builder = new Message.Builder()
                .setType(EntryType.TWITTER)
                .setTitle(username)
                .setText(obj.getString("text"))
                .setCreatedAt(new Date(Long.parseLong(obj.getString("createdAt"))))
                .setNsfw(obj.getBoolean("nsfw"))
                .setUrl("https://twitter.com/" + username + "/status/" + obj.getString("id"));

        if (obj.has("media")) {
            JSONArray mediaArray = obj.getJSONArray("media");
            Media[] media = new Media[mediaArray.length()];
            for (int i = 0; i < media.length; i++) {
                JSONObject mediaObj = mediaArray.getJSONObject(i);
                MediaType type = MediaType.valueOf(mediaObj.getString("type"));
                if (type == MediaType.IMAGE) {
                    media[i] = new Media(type, mediaObj.getString("url"));
                } else {
                    media[i] = new Media(type, mediaObj.getString("url"), mediaObj.getString("thumbnailUrl"));
                }
            }
            builder.setMedia(media);
        }
        return builder.build();
    }

}
