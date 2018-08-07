package com.frostphyr.notiphy.io;

import com.frostphyr.notiphy.Entry;
import com.frostphyr.notiphy.MediaType;
import com.frostphyr.notiphy.TwitterEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TwitterEntryJSONSerializer implements EntryJSONSerializer {

    @Override
    public JSONObject serialize(Entry entry) throws JSONException {
        TwitterEntry twitterEntry = (TwitterEntry) entry;
        JSONObject obj = new JSONObject();
        obj.put("type", twitterEntry.getType().toString());
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
        String username = obj.getString("username");
        MediaType mediaType = MediaType.valueOf(obj.getString("mediaType"));
        JSONArray phraseArray = obj.getJSONArray("phrases");
        String[] phrases = new String[phraseArray.length()];
        for (int i = 0; i < phrases.length; i++) {
            phrases[i] = phraseArray.getString(i);
        }
        return new TwitterEntry(username, mediaType, phrases);
    }

}
