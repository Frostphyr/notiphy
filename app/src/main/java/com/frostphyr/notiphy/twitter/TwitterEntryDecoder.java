package com.frostphyr.notiphy.twitter;

import com.frostphyr.notiphy.io.JSONDecoder;
import com.frostphyr.notiphy.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TwitterEntryDecoder implements JSONDecoder<TwitterEntry> {

    @Override
    public TwitterEntry decode(JSONObject obj) throws JSONException {
        try {
            boolean active = obj.getBoolean("active");
            String id = obj.getString("userId");
            String username = obj.getString("username");
            MediaType mediaType = MediaType.valueOf(obj.getString("mediaType"));

            JSONArray phraseArray = obj.getJSONArray("phrases");
            if (phraseArray == null) {
                return null;
            }
            String[] phrases = new String[phraseArray.length()];
            for (int i = 0; i < phrases.length; i++) {
                phrases[i] = phraseArray.getString(i);
            }

            return new TwitterEntry(id, username, mediaType, phrases, active);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

}
