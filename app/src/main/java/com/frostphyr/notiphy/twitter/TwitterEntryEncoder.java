package com.frostphyr.notiphy.twitter;

import com.frostphyr.notiphy.io.JSONEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TwitterEntryEncoder implements JSONEncoder<TwitterEntry> {

    private boolean transport;

    public TwitterEntryEncoder(boolean transport) {
        this.transport = transport;
    }

    @Override
    public JSONObject encode(TwitterEntry entry) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("type", entry.getType().toString());
        obj.put("userId", entry.getId());
        obj.put("mediaType", entry.getMediaType().toString());
        JSONArray phrases = new JSONArray();
        for (String s : entry.getPhrases()) {
            phrases.put(s);
        }
        obj.put("phrases", phrases);

        if (!transport) {
            obj.put("username", entry.getUsername());
            obj.put("active", entry.isActive());
        }
        return obj;
    }

}
