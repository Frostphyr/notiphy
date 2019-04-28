package com.frostphyr.notiphy.io;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class IOUtils {

    public static String[] readPhrases(JSONObject obj) throws JSONException {
        JSONArray phraseArray = obj.getJSONArray("phrases");
        if (phraseArray == null) {
            return null;
        }
        String[] phrases = new String[phraseArray.length()];
        for (int i = 0; i < phrases.length; i++) {
            phrases[i] = phraseArray.getString(i);
        }
        return phrases;
    }

    public static void putPhrases(JSONObject obj, String[] phrases) throws JSONException {
        JSONArray phraseArray = new JSONArray();
        for (String s : phrases) {
            phraseArray.put(s);
        }
        obj.put("phrases", phraseArray);
    }

}
