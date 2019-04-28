package com.frostphyr.notiphy.reddit;

import com.frostphyr.notiphy.io.IOUtils;
import com.frostphyr.notiphy.io.JSONDecoder;

import org.json.JSONException;
import org.json.JSONObject;

public class RedditEntryDecoder implements JSONDecoder<RedditEntry> {

    @Override
    public RedditEntry decode(JSONObject obj) throws JSONException {
        try {
            RedditEntryType type = null;
            String value = null;
            if (obj.has("user")) {
                type = RedditEntryType.USER;
                value = obj.getString("user");
            } else if (obj.has("subreddit")) {
                type = RedditEntryType.SUBREDDIT;
                value = obj.getString("subreddit");
            }

            RedditPostType postType = RedditPostType.valueOf(obj.getString("postType"));
            String[] phrases = IOUtils.readPhrases(obj);
            boolean active = obj.getBoolean("active");
            return new RedditEntry(type, value, postType, phrases, active);
        } catch (IllegalArgumentException | NullPointerException e) {
        }
        return null;
    }

}
