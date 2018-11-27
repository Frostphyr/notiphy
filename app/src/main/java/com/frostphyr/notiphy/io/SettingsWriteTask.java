package com.frostphyr.notiphy.io;

import android.content.Context;

import com.frostphyr.notiphy.Setting;

import org.json.JSONObject;

public class SettingsWriteTask extends FileWriteTask<Object, Void> {

    public SettingsWriteTask(Context context, Callback callback) {
        super(context, callback);
    }

    @Override
    protected String getFileName() {
        return "settings.json";
    }

    @Override
    protected byte[] getBytes(Object... objects) throws Exception {
        JSONObject obj = new JSONObject();
        for (int i = 0; i < objects.length; i++) {
            obj.put(Setting.forId(i).getName(), objects[i]);
        }
        return obj.toString(2).getBytes();
    }

}
