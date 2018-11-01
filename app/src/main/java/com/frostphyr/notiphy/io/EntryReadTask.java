package com.frostphyr.notiphy.io;

import android.content.Context;

import com.frostphyr.notiphy.Entry;
import com.frostphyr.notiphy.EntryType;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EntryReadTask extends FileReadTask<Void, List<Entry>> {

    public EntryReadTask(Context context, Callback callback) {
        super(context, callback);
    }

    @Override
    protected String getFileName() {
        return "entries.json";
    }

    @Override
    protected List<Entry> run(byte[] data, Void... params) throws Exception {
        List<Entry> entries = new ArrayList<>();
        JSONArray arr = new JSONArray(new String(data));
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            Entry e = EntryType.valueOf(obj.getString("type")).getEntryDecoder().decode(obj);
            if (e != null) {
                entries.add(e);
            }
        }
        return entries;
    }

}
