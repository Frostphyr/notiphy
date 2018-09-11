package com.frostphyr.notiphy.twitter;

import com.frostphyr.notiphy.Entry;
import com.frostphyr.notiphy.MediaType;
import com.frostphyr.notiphy.io.EntryJSONSerializer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TwitterEntryJSONSerializer implements EntryJSONSerializer {

    @Override
    public JSONObject serialize(Entry entry) throws JSONException {
        TwitterEntry twitterEntry = (TwitterEntry) entry;
        JSONObject obj = new JSONObject();
        obj.put("type", twitterEntry.getType().toString());
        obj.put("active", twitterEntry.isActive());
        obj.put("id", twitterEntry.getId());
        obj.put("username", twitterEntry.getUsername());
        obj.put("mediaType", twitterEntry.getMediaType().toString());
        JSONArray phrases = new JSONArray();
        for (String s : twitterEntry.getPhrases()) {
            phrases.put(s);
        }
        obj.put("phrases", phrases);
        return obj;
    }

    @Override
    public Entry deserialize(JSONObject obj) throws JSONException {
        try {
            boolean active = obj.getBoolean("active");
            long id = obj.getLong("id");
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
