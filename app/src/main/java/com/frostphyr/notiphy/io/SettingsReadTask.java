package com.frostphyr.notiphy.io;

import android.content.Context;

import com.frostphyr.notiphy.Setting;

import org.json.JSONObject;

public class SettingsReadTask extends FileReadTask<Void, Object[]> {

    public SettingsReadTask(Context context, Callback callback) {
        super(context, callback);
    }

    @Override
    protected String getFileName() {
        return "settings.json";
    }

    @Override
    protected Object[] run(byte[] data, Void... voids) throws Exception {
        Object[] settings = new Object[Setting.getCount()];
        JSONObject obj = new JSONObject(new String(data));
        for (int i = 0; i < settings.length; i++) {
            Setting setting = Setting.forId(i);
            if (obj.has(setting.getName())) {
                Object o = obj.get(setting.getName());
                if (o != null) {
                    if (setting.getType().isInstance(o)) {
                        settings[i] = o;
                    } else if (setting.getType().isEnum() && o instanceof String) {
                        try {
                            settings[i] = Enum.valueOf(setting.getType(), (String) o);
                        } catch (IllegalArgumentException e) {
                        }
                    }
                }
            }

            if (settings[i] == null) {
                settings[i] = Setting.forId(i).getDefaultValue();
            }
        }
        return settings;
    }

}
