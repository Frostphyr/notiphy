package com.frostphyr.notiphy.io;

import android.content.Context;
import android.os.Environment;

import com.frostphyr.notiphy.Entry;
import com.frostphyr.notiphy.EntryType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EntryIO {

    public static final String ENTRIES_FILE_NAME = "entries.json";

    public static List<Entry> read(Context context) throws IOException, JSONException {
        File file = getEntriesFile(context);
        if (file.exists()) {
            FileInputStream in = new FileInputStream(file);
            byte[] buffer = new byte[in.available()];
            in.read(buffer);
            in.close();

            List<Entry> entries = new ArrayList<Entry>();
            JSONArray arr = new JSONArray(new String(buffer));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                entries.add(EntryType.valueOf(obj.getString("type")).getJSONSerializer().deserialize(obj));
            }
            return entries;
        }
        return null;
    }

    public static void write(Context context, List<Entry> entries) throws IOException, JSONException {
        File file = new File(getEntriesFile(context), ENTRIES_FILE_NAME);
        FileOutputStream out = new FileOutputStream(file);
        JSONArray arr = new JSONArray();
        for (Entry e : entries) {
            arr.put(e.getType().getJSONSerializer().serialize(e));
        }

        out.write(arr.toString(2).getBytes());
        out.close();
    }

    private static File getEntriesFile(Context context) {
        File dir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            dir = context.getExternalFilesDir(null);
        } else {
            dir =  context.getFilesDir();
        }
        return new File(dir, ENTRIES_FILE_NAME);
    }

}
