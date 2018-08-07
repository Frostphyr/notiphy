package com.frostphyr.notiphy.io;

import com.frostphyr.notiphy.Entry;

import org.json.JSONException;
import org.json.JSONObject;

public interface EntryJSONSerializer {

    JSONObject serialize(Entry entry) throws JSONException;

    Entry deserialize(JSONObject obj) throws JSONException;

}
