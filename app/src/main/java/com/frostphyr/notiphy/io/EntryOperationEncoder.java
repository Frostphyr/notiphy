package com.frostphyr.notiphy.io;

import com.frostphyr.notiphy.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;

public class EntryOperationEncoder {

    private static final int OPERATION_ADD = 0;
    private static final int OPERATION_REMOVE = 1;

    public static JSONArray encode(EntryOperation operation) {
        JSONArray array = new JSONArray();
        encodeEntries(array, operation.getAdded(), OPERATION_ADD);
        encodeEntries(array, operation.getRemoved(), OPERATION_REMOVE);
        return array;
    }

    private static void encodeEntries(JSONArray array, Collection<Entry> entries, int operation) {
        if (entries != null) {
            JSONArray entriesArray = new JSONArray();
            for (Entry e : entries) {
                try {
                    entriesArray.put(encodeEntry(e.getType().getEntryTransportEncoder(), e));
                } catch (JSONException ex) {
                }
            }
            array.put(encodeEntries(entriesArray, operation));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entry> JSONObject encodeEntry(JSONEncoder<T> encoder, Entry entry) throws JSONException {
        return encoder.encode((T) entry);
    }

    private static JSONObject encodeEntries(JSONArray entries, int operation) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("op", operation);
            obj.put("entries", entries);
            return obj;
        } catch (JSONException e) {
            return null;
        }
    }

}
