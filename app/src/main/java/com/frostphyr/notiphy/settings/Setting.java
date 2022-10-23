package com.frostphyr.notiphy.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.frostphyr.notiphy.MatureContent;
import com.frostphyr.notiphy.R;
import com.frostphyr.notiphy.SpinnerItem;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Setting<T> {

    private static final List<Setting<?>> SETTINGS = new ArrayList<>(3);

    public static final Setting<Boolean> CRASH_REPORTING = new BooleanSetting("CRASH_REPORTING", R.string.crash_reporting,
            R.string.description_crash_reporting, false, value -> {
                FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
                if (value) {
                    crashlytics.deleteUnsentReports();
                }
                crashlytics.setCrashlyticsCollectionEnabled(value);
            });

    public static final Setting<Boolean> SHOW_MEDIA = new BooleanSetting("SHOW_MEDIA", R.string.show_media,
            R.string.description_show_media, true);

    public static final Setting<MatureContent> MATURE_CONTENT = new EnumSetting<>("MATURE_CONTENT", R.string.mature_content,
            R.string.description_mature_content, MatureContent.HIDE);

    private static final String PREFERENCE_FILE_KEY = "com.frostphyr.notiphy.SETTINGS";

    static {
        CRASH_REPORTING.viewFactory = new SwitchViewFactory(CRASH_REPORTING);
        SHOW_MEDIA.viewFactory = new SwitchViewFactory(SHOW_MEDIA);
        MATURE_CONTENT.viewFactory = new SpinnerViewFactory<>(MATURE_CONTENT, MatureContent.values());
    }

    public static void init(Context context) {
        for (Setting<?> s : SETTINGS) {
            if (s.onChangeListener != null) {
                onChange(context, s);
            }
        }
    }

    private static <T> void onChange(Context context, Setting<T> setting) {
        setting.onChangeListener.onChange(setting.get(context));
    }

    public static List<Setting<?>> getSettings() {
        return Collections.unmodifiableList(SETTINGS);
    }

    private final String name;
    private final int titleResId;
    private final int descriptionResId;
    private final OnChangeListener<T> onChangeListener;
    private SettingViewFactory viewFactory;

    protected Setting(String name, int titleResId, int descriptionResId, OnChangeListener<T> onChangeListener) {
        this.name = name;
        this.titleResId = titleResId;
        this.descriptionResId = descriptionResId;
        this.onChangeListener = onChangeListener;

        SETTINGS.add(this);
    }

    protected Setting(String name, int titleResId, int descriptionResId) {
        this(name,titleResId, descriptionResId, null);
    }

    protected abstract T get(SharedPreferences sharedPref);

    protected abstract void set(SharedPreferences sharedPref, T value);

    protected String getName() {
        return name;
    }

    public int getTitleResId() {
        return titleResId;
    }

    public int getDescriptionResId() {
        return descriptionResId;
    }

    public boolean isSet(Context context) {
        return getSharedPreferences(context).contains(name);
    }

    public T get(Context context) {
        return get(getSharedPreferences(context));
    }

    public void set(Context context, T value) {
        set(getSharedPreferences(context), value);
        if (onChangeListener != null) {
            onChangeListener.onChange(value);
        }
    }

    private SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(Setting.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
    }

    public SettingViewFactory getViewFactory() {
        return viewFactory;
    }

    private interface OnChangeListener<T> {

        void onChange(T value);

    }

    private static class BooleanSetting extends Setting<Boolean> {

        private final boolean defaultValue;

        private BooleanSetting(String name, int titleResId, int descriptionResId, boolean defaultValue, OnChangeListener<Boolean> onChangeListener) {
            super(name, titleResId, descriptionResId, onChangeListener);

            this.defaultValue = defaultValue;
        }

        private BooleanSetting(String name, int titleResId, int descriptionResId, boolean defaultValue) {
            this(name, titleResId, descriptionResId, defaultValue, null);
        }

        @Override
        public Boolean get(SharedPreferences sharedPref) {
            return sharedPref.getBoolean(getName(), defaultValue);
        }

        @Override
        public void set(SharedPreferences sharedPref, Boolean value) {
            sharedPref.edit()
                    .putBoolean(getName(), value)
                    .apply();
        }

    }

    private static class EnumSetting<T extends Enum<? extends SpinnerItem>> extends Setting<T> {

        private final T defaultValue;

        private EnumSetting(String name, int titleResId, int descriptionResId, T defaultValue) {
            super(name, titleResId, descriptionResId);

            this.defaultValue = defaultValue;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T get(SharedPreferences sharedPref) {
            String value = sharedPref.getString(getName(), null);
            return value != null ? (T) Enum.valueOf(defaultValue.getClass(), value) : defaultValue;
        }

        @Override
        public void set(SharedPreferences sharedPref, T value) {
            sharedPref.edit()
                    .putString(getName(), value.toString())
                    .apply();
        }

    }

}
