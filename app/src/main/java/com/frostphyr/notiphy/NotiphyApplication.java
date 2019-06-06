package com.frostphyr.notiphy;

import android.app.Application;
import android.widget.Toast;

import com.frostphyr.notiphy.io.EntryWriteTask;
import com.frostphyr.notiphy.io.NotiphyWebSocket;
import com.frostphyr.notiphy.io.SettingsWriteTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;

public class NotiphyApplication extends Application {

    public static final int MAX_ENTRIES = 25;

    private List<Entry> entries = new ArrayList<>();
    private OkHttpClient httpClient = new OkHttpClient();
    private NotificationDispatcher notificationDispatcher;
    private NotiphyWebSocket webSocket;
    private Object[] settings;

    @Override
    public void onCreate() {
        super.onCreate();

        notificationDispatcher = new NotificationDispatcher(this);
        webSocket = new NotiphyWebSocket(this, httpClient);
        webSocket.addListener(new NotiphyWebSocket.Listener() {

            @Override
            public void onStatusChange(NotiphyWebSocket socket, NotiphyWebSocket.Status status) {

            }

            @Override
            public void onMessage(NotiphyWebSocket socket, Message message) {
                notificationDispatcher.dispatch(message);
            }

        });
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

        }).execute(entries.toArray(new Entry[0]));
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void addEntries(List<Entry> newEntries) {
        entries.addAll(newEntries);

        List<Entry> activeEntries = new ArrayList<>(newEntries.size());
        for (Entry e : newEntries) {
            if (e.isActive()) {
                activeEntries.add(e);
            }
        }

        if (activeEntries.size() > 0) {
            webSocket.entriesAdded(activeEntries);
        }
    }

    public void addEntry(Entry entry) {
        if (entries.size() < MAX_ENTRIES) {
            entries.add(entry);
            if (entry.isActive()) {
                webSocket.entryAdded(entry);
            }
            saveEntries();
        }
    }

    public boolean removeEntry(Entry entry) {
        if (entries.remove(entry)) {
            if (entry.isActive()) {
                webSocket.entryRemoved(entry);
            }
            saveEntries();
            return true;
        }
        return false;
    }

    public void replaceEntry(Entry oldEntry, Entry newEntry) {
        int index = entries.indexOf(oldEntry);
        if (index != -1) {
            entries.remove(index);
            entries.add(index, newEntry);
            if (oldEntry.isActive() && newEntry.isActive()) {
                webSocket.entryReplaced(oldEntry, newEntry);
            } else if (oldEntry.isActive()) {
                webSocket.entryRemoved(oldEntry);
            } else if (newEntry.isActive()) {
                webSocket.entryAdded(newEntry);
            }
        } else {
            entries.add(newEntry);
            webSocket.entryAdded(newEntry);
        }
        saveEntries();
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

    public NotificationDispatcher getNotificationDispatcher() {
        return notificationDispatcher;
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
