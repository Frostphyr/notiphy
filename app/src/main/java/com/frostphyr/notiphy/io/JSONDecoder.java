package com.frostphyr.notiphy.io;

import org.json.JSONException;
import org.json.JSONObject;

public interface JSONDecoder<T> {

    T decode(JSONObject obj) throws JSONException;

}
