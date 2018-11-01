package com.frostphyr.notiphy.io;

import android.content.Context;

import com.frostphyr.notiphy.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EntryWriteTask extends FileWriteTask<Entry[], Void> {

    public EntryWriteTask(Context context, Callback callback) {
        super(context, callback);
    }

    @Override
    protected String getFileName() {
        return "entries.json";
    }

    @Override
    protected byte[] getBytes(Entry[]... entries) throws Exception {
        JSONArray arr = new JSONArray();
        for (Entry e : entries[0]) {
            arr.put(encodeEntry(e.getType().getEntryEncoder(), e));
        }
        return arr.toString(2).getBytes();
    }

    private <T extends Entry> JSONObject encodeEntry(JSONEncoder<T> encoder, Entry entry) throws JSONException {
        return encoder.encode((T) entry);
    }

}
