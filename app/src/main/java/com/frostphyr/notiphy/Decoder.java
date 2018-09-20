package com.frostphyr.notiphy;

import org.json.JSONException;
import org.json.JSONObject;

public interface Decoder<T> {

    T decode(JSONObject obj) throws JSONException;

}
