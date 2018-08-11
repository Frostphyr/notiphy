package com.frostphyr.notiphy.io;

import android.content.Context;

import com.frostphyr.notiphy.Entry;
import com.frostphyr.notiphy.EntryType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EntryIO {

    public static final String ENTRIES_FILE_NAME = "entries.json";

    public static List<Entry> read(Context context) throws IOException, JSONException {
        byte[] data = FileIO.read(context, ENTRIES_FILE_NAME);
        if (data != null) {
            List<Entry> entries = new ArrayList<Entry>();
            JSONArray arr = new JSONArray(new String(data));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                entries.add(EntryType.valueOf(obj.getString("type")).getJSONSerializer().deserialize(obj));
            }
            return entries;
        }
        return null;
    }

    public static void write(Context context, List<Entry> entries) throws IOException, JSONException {
        JSONArray arr = new JSONArray();
        for (Entry e : entries) {
            arr.put(e.getType().getJSONSerializer().serialize(e));
        }
        byte[] data = arr.toString(2).getBytes();
        FileIO.write(context, ENTRIES_FILE_NAME, data);
    }

}
