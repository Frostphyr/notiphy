package com.frostphyr.notiphy;

import android.app.Application;
import android.widget.Toast;

import com.frostphyr.notiphy.io.EntryReadTask;
import com.frostphyr.notiphy.io.EntryWriteTask;
import com.frostphyr.notiphy.io.NotiphyWebSocket;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;

public class NotiphyApplication extends Application {

    private Set<Entry> entries = new LinkedHashSet<>();
    private OkHttpClient httpClient = new OkHttpClient();
    private NotiphyWebSocket webSocket;
    private EntryListener entryListener;

    @Override
    public void onCreate() {
        super.onCreate();

        webSocket = new NotiphyWebSocket(this, httpClient, entries);

        new EntryReadTask(this, new AsyncTaskHelper.Callback<List<Entry>>() {
            @Override
            public void onSuccess(List<Entry> readEntries) {
                entries.addAll(readEntries);
                if (entryListener != null) {
                    entryListener.entriesAdded(readEntries);
                }
                webSocket.entriesAdded(readEntries);
            }

            @Override
            public void onException(Exception exception) {
                showErrorMessage(R.string.error_message_read_entries);
            }

        }).execute();
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

    public void addEntry(Entry entry) {
        if (entries.add(entry)) {
            if (entryListener != null) {
                entryListener.entryAdded(entry);
                webSocket.entryAdded(entry);
                saveEntries();
            }
        }
    }

    public void removeEntry(Entry entry) {
        if (entries.remove(entry)) {
            if (entryListener != null) {
                entryListener.entryRemoved(entry);
                webSocket.entryRemoved(entry);
                saveEntries();
            }
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

    public void setEntryListener(EntryListener entryListener) {
        this.entryListener = entryListener;

        if (entries.size() > 0) {
            entryListener.entriesAdded(entries);
        }
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    private void showErrorMessage(int textResId) {
        Toast.makeText(this, textResId, Toast.LENGTH_LONG).show();
    }

}
