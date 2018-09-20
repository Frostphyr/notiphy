package com.frostphyr.notiphy;

import org.json.JSONException;
import org.json.JSONObject;

public interface Encoder<T> {

    JSONObject encode(T t) throws JSONException;

}
