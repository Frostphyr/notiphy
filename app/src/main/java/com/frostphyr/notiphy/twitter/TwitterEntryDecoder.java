package com.frostphyr.notiphy.twitter;

import com.frostphyr.notiphy.IllegalInputException;
import com.frostphyr.notiphy.MediaType;
import com.frostphyr.notiphy.io.IOUtils;
import com.frostphyr.notiphy.io.JSONDecoder;

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
            String[] phrases = IOUtils.readPhrases(obj);
            return new TwitterEntry(id, username, mediaType, phrases, active);
        } catch (IllegalInputException | NullPointerException e) {
            return null;
        }
    }

}
