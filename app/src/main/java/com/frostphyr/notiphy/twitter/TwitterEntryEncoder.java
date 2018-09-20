package com.frostphyr.notiphy.twitter;

import com.frostphyr.notiphy.Encoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TwitterEntryEncoder implements Encoder<TwitterEntry> {

    @Override
    public JSONObject encode(TwitterEntry entry) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("type", entry.getType().toString());
        obj.put("active", entry.isActive());
        obj.put("id", entry.getId());
        obj.put("username", entry.getUsername());
        obj.put("mediaType", entry.getMediaType().toString());
        JSONArray phrases = new JSONArray();
        for (String s : entry.getPhrases()) {
            phrases.put(s);
        }
        obj.put("phrases", phrases);
        return obj;
    }

}
