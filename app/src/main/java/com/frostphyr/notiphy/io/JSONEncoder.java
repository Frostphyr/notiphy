package com.frostphyr.notiphy.io;

import org.json.JSONException;
import org.json.JSONObject;

public interface JSONEncoder<T> {

    JSONObject encode(T t) throws JSONException;

}
