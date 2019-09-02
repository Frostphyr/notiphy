package com.frostphyr.notiphy;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.frostphyr.notiphy.io.EntryWriteTask;
import com.frostphyr.notiphy.io.NotiphyWebSocket;
import com.frostphyr.notiphy.io.SettingsWriteTask;
import com.frostphyr.notiphy.notification.NotificationDispatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;

public class NotiphyApplication extends Application {

    public static final int MAX_ENTRIES = 25;

    private List<Entry> entries = new ArrayList<>();
    private OkHttpClient httpClient = new OkHttpClient();
    private NotificationDispatcher notificationDispatcher;
    private Object[] settings;

    @Override
    public void onCreate() {
        super.onCreate();

        registerReceiver(messageReceivedBroadcastReceiver, new IntentFilter(NotiphyWebSocket.ACTION_MESSAGE_RECEIVED));
        ContextCompat.startForegroundService(this, new Intent(this, NotiphyWebSocket.class));
        notificationDispatcher = new NotificationDispatcher(this);
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

        ArrayList<Entry> activeEntries = new ArrayList<>(newEntries.size());
        for (Entry e : newEntries) {
            if (e.isActive()) {
                activeEntries.add(e);
            }
        }

        if (activeEntries.size() > 0) {
            updateWebSocket(activeEntries, NotiphyWebSocket.ACTION_ENTRY_ADD);
        }
    }

    public void addEntry(Entry entry) {
        if (entries.size() < MAX_ENTRIES) {
            entries.add(entry);
            if (entry.isActive()) {
                updateWebSocket(entry, NotiphyWebSocket.ACTION_ENTRY_ADD);
            }
            saveEntries();
        }
    }

    public boolean removeEntry(Entry entry) {
        if (entries.remove(entry)) {
            if (entry.isActive()) {
                updateWebSocket(entry, NotiphyWebSocket.ACTION_ENTRY_REMOVE);
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
                ContextCompat.startForegroundService(this, new Intent(this, NotiphyWebSocket.class)
                        .setAction(NotiphyWebSocket.ACTION_ENTRY_REPLACE)
                        .putExtra(NotiphyWebSocket.EXTRA_OLD_ENTRY, oldEntry)
                        .putExtra(NotiphyWebSocket.EXTRA_NEW_ENTRY, newEntry));
            } else if (oldEntry.isActive()) {
                updateWebSocket(oldEntry, NotiphyWebSocket.ACTION_ENTRY_REMOVE);
            } else if (newEntry.isActive()) {
                updateWebSocket(newEntry, NotiphyWebSocket.ACTION_ENTRY_ADD);
            }
        } else {
            entries.add(newEntry);
            updateWebSocket(newEntry, NotiphyWebSocket.ACTION_ENTRY_ADD);
        }
        saveEntries();
    }

    private void updateWebSocket(ArrayList<Entry> entries, String action) {
        ContextCompat.startForegroundService(this, new Intent(this, NotiphyWebSocket.class)
                .setAction(action)
                .putExtra(NotiphyWebSocket.EXTRA_ENTRIES, entries));
    }

    private void updateWebSocket(Entry entry, String action) {
        ArrayList<Entry> entries = new ArrayList<>(1);
        entries.add(entry);
        updateWebSocket(entries, action);
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

    private void showErrorMessage(int textResId) {
        Toast.makeText(this, textResId, Toast.LENGTH_LONG).show();
    }

    private BroadcastReceiver messageReceivedBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            notificationDispatcher.dispatch((Message) intent.getParcelableExtra(NotiphyWebSocket.EXTRA_MESSAGE));
        }

    };

}
