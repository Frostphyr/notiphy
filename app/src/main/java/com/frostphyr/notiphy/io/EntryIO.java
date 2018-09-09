package com.frostphyr.notiphy.io;

import android.content.Intent;
import android.os.Parcelable;

import com.frostphyr.notiphy.Entry;
import com.frostphyr.notiphy.EntryType;
import com.frostphyr.notiphy.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class EntryIO extends IOService {

    public static final String ACTION_READ = "read";
    public static final String ACTION_WRITE = "write";
    public static final String ACTION_READ_RESPONSE = "readResponse";
    public static final String ACTION_WRITE_RESPONSE = "writeResponse";

    public static final String EXTRA_ENTRIES = "entries";

    public static final String ENTRIES_FILE_NAME = "entries.json";

    public EntryIO() {
        super("EntryIO");
    }

    @Override
    protected void onHandleIntentThrowable(Intent intent) throws Exception {
        switch (intent.getAction()) {
            case ACTION_READ:
                read();
                break;
            case ACTION_WRITE:
                write(intent.getParcelableArrayExtra(EXTRA_ENTRIES));
                break;
        }
    }

    @Override
    protected int getErrorMessageId(Intent intent) {
        return intent.getAction().equals(ACTION_READ) ? R.string.error_message_read_entries : R.string.error_message_write_entries;
    }

    private void read() throws IOException, JSONException {
        byte[] data = FileIO.read(this, ENTRIES_FILE_NAME);
        if (data != null) {
            ArrayList<Entry> entries = new ArrayList<Entry>();
            JSONArray arr = new JSONArray(new String(data));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                Entry e = EntryType.valueOf(obj.getString("type")).getJSONSerializer().deserialize(obj);
                if (e != null) {
                    entries.add(e);
                }
            }

            Intent intent = new Intent();
            intent.setAction(ACTION_READ_RESPONSE);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putParcelableArrayListExtra(EXTRA_ENTRIES, entries);
            sendBroadcast(intent);
        }
    }

    private void write(Parcelable[] entries) throws IOException, JSONException {
        JSONArray arr = new JSONArray();
        for (Parcelable p : entries) {
            Entry e = (Entry) p;
            arr.put(e.getType().getJSONSerializer().serialize(e));
        }
        byte[] data = arr.toString(2).getBytes();
        FileIO.write(this, ENTRIES_FILE_NAME, data);

        Intent intent = new Intent();
        intent.setAction(ACTION_WRITE_RESPONSE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(intent);
    }

}
