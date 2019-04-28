package com.frostphyr.notiphy.reddit;

import com.frostphyr.notiphy.io.IOUtils;
import com.frostphyr.notiphy.io.JSONEncoder;

import org.json.JSONException;
import org.json.JSONObject;

public class RedditEntryEncoder implements JSONEncoder<RedditEntry> {

    private boolean transport;

    public RedditEntryEncoder(boolean transport) {
        this.transport = transport;
    }

    @Override
    public JSONObject encode(RedditEntry entry) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("type", entry.getType().toString());
        obj.put(entry.getEntryType() == RedditEntryType.USER ? "user" : "subreddit", entry.getValue());
        obj.put("postType", entry.getPostType().toString());
        IOUtils.putPhrases(obj, entry.getPhrases());
        if (!transport) {
            obj.put("active", entry.isActive());
        }
        return obj;
    }

}
