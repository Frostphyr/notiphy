package com.frostphyr.notiphy;

import android.app.Application;
import android.widget.Toast;

import com.frostphyr.notiphy.io.EntryWriteTask;
import com.frostphyr.notiphy.io.NotiphyWebSocket;
import com.frostphyr.notiphy.io.SettingsWriteTask;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;

public class NotiphyApplication extends Application {

    private Set<Entry> entries = new LinkedHashSet<>();
    private OkHttpClient httpClient = new OkHttpClient();
    private NotiphyWebSocket webSocket;
    private Object[] settings;

    @Override
    public void onCreate() {
        super.onCreate();

        webSocket = new NotiphyWebSocket(this, httpClient, entries);
    }

    public void saveEntries() {
        new EntryWriteTask(this, new AsyncTaskHelper.Callback<Void>() {

            @Override
            public void onSuccess(Void v) {
            }

            @Override
            public void onException(Exception exception) {
                showErrorMessage(R.string.error_message_write_entries);
            }

        }).execute(entries.toArray(new Entry[entries.size()]));
    }

    public Set<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> newEntries) {
        entries.addAll(newEntries);
        webSocket.entriesAdded(newEntries);
    }

    public void addEntry(Entry entry) {
        if (entries.add(entry)) {
            webSocket.entryAdded(entry);
            saveEntries();
        }
    }

    public void removeEntry(Entry entry) {
        if (entries.remove(entry)) {
            webSocket.entryRemoved(entry);
            saveEntries();
        }
    }

    public void replaceEntry(Entry oldEntry, Entry newEntry) {
        boolean modified = false;
        if (entries.remove(oldEntry)) {
            modified = true;
        }
        if (entries.add(newEntry)) {
            modified = true;
        }

        if (modified) {
            webSocket.entryReplaced(oldEntry, newEntry);
            saveEntries();
        }
    }

    public Object[] getSettings() {
        return settings;
    }

    public void setSettings(Object[] settings) {
        this.settings = settings;

        for (int i = 0; i < settings.length; i++) {
            onSettingChange(Setting.forId(i).getOnChangeHandler(), settings[i]);
        }
    }

    public void setSetting(Setting setting, Object value) {
        settings[setting.getId()] = value;
        onSettingChange(setting.getOnChangeHandler(), value);
        saveSettings();
    }

    @SuppressWarnings("unchecked")
    private <T> void onSettingChange(Setting.OnChangeHandler<T> handler, Object value) {
        handler.onChange(this, (T) value);
    }

    private void saveSettings() {
        new SettingsWriteTask(this, new AsyncTaskHelper.Callback<Void>() {

            @Override
            public void onSuccess(Void v) {
            }

            @Override
            public void onException(Exception exception) {
                showErrorMessage(R.string.error_message_write_settings);
            }

        }).execute(Arrays.copyOf(settings, settings.length));
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public NotiphyWebSocket getWebSocket() {
        return webSocket;
    }

    private void showErrorMessage(int textResId) {
        Toast.makeText(this, textResId, Toast.LENGTH_LONG).show();
    }

}
