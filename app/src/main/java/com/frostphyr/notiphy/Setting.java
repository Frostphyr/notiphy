package com.frostphyr.notiphy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Setting<T> {

    public static final Setting WIFI_ONLY;
    public static final Setting SHOW_MEDIA;

    private static Map<String, Setting> settingNames;
    private static List<Setting> settingIds;
    private static int nextId;

    private String name;
    private Class<T> type;
    private T defaultValue;
    private OnChangeHandler<T> onChangeHandler;
    private int id;

    static {
        settingNames = new HashMap<>();
        settingIds = new ArrayList<>();

        WIFI_ONLY = new Setting<>("WiFi Only", Boolean.class, false, new OnChangeHandler<Boolean>() {

            @Override
            public void onChange(NotiphyApplication application, Boolean value) {
                application.getWebSocket().setWifiOnly(value);
            }

        });

        SHOW_MEDIA = new Setting<>("Show Media", Boolean.class, true, new OnChangeHandler<Boolean>() {

            @Override
            public void onChange(NotiphyApplication application, Boolean value) {
                application.getNotificationDispatcher().setShowMedia(value);
            }

        });
    }

    public Setting(String name, Class<T> type, T defaultValue, OnChangeHandler<T> onChangeHandler) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.onChangeHandler = onChangeHandler;

        id = nextId++;
        settingIds.add(this);
        settingNames.put(name, this);
    }

    public static Setting<?> forName(String name) {
        return settingNames.get(name);
    }

    public static Setting forId(int id) {
        return id >= 0 && id < settingIds.size() ? settingIds.get(id) : null;
    }

    public static int getCount() {
        return nextId;
    }

    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public OnChangeHandler<T> getOnChangeHandler() {
        return onChangeHandler;
    }

    public int getId() {
        return id;
    }

    public interface OnChangeHandler<T> {

        void onChange(NotiphyApplication application, T value);

    }

}
