package com.frostphyr.notiphy.twitter;

import com.frostphyr.notiphy.EntryType;
import com.frostphyr.notiphy.Media;
import com.frostphyr.notiphy.MediaType;
import com.frostphyr.notiphy.Message;
import com.frostphyr.notiphy.io.JSONDecoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class TweetDecoder implements JSONDecoder<Message> {

    @Override
    public Message decode(JSONObject obj) throws JSONException {
        Date createdAt = null;
        try {
            createdAt = DateFormat.getInstance().parse(obj.getString("createdAt"));
        } catch (ParseException e) {
        }
        String id = obj.getString("id");
        String username = obj.getString("username");
        String text = obj.getString("text");
        Media[] media = null;
        if (obj.has("media")) {
            JSONArray mediaArray = obj.getJSONArray("media");
            media = new Media[mediaArray.length()];
            for (int i = 0; i < media.length; i++) {
                JSONObject mediaObj = mediaArray.getJSONObject(i);
                MediaType type = MediaType.valueOf(mediaObj.getString("type"));
                if (type == MediaType.IMAGE) {
                    media[i] = new Media(type, mediaObj.getString("url"));
                } else {
                    media[i] = new Media(type, mediaObj.getString("url"), mediaObj.getString("thumbnailUrl"));
                }
            }
        }
        return new Message(EntryType.TWITTER, createdAt, username, null, text, "https://twitter.com/" + username + "/status/" + id, media);
    }

}
