package com.frostphyr.notiphy;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import com.frostphyr.notiphy.io.EntryIO;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;

public class NotiphyApplication extends Application {

    private Set<Entry> entries = new LinkedHashSet<Entry>();
    private OkHttpClient httpClient = new OkHttpClient();
    private EntryListener entryListener;

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter readFilter = new IntentFilter(EntryIO.ACTION_READ_RESPONSE);
        readFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(new ReadResponseReceiver(), readFilter);

        IntentFilter exceptionFilter = new IntentFilter(EntryIO.ACTION_EXCEPTION);
        exceptionFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(new ExceptionReceiver(), exceptionFilter);

        Intent intent =  new Intent(this, EntryIO.class);
        intent.setAction(EntryIO.ACTION_READ);
        startService(intent);
    }

    public void saveEntries() {
        Intent intent = new Intent(this, EntryIO.class);
        intent.setAction(EntryIO.ACTION_WRITE);
        intent.putExtra(EntryIO.EXTRA_ENTRIES, entries.toArray(new Entry[entries.size()]));
        startService(intent);
    }

    public void addEntry(Entry entry) {
        if (entries.add(entry)) {
            if (entryListener != null) {
                entryListener.entryAdded(entry);
                saveEntries();
            }
        }
    }

    public void removeEntry(Entry entry) {
        if (entries.remove(entry)) {
            if (entryListener != null) {
                entryListener.entryRemoved(entry);
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
            saveEntries();
        }
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public void setEntryListener(EntryListener entryListener) {
        this.entryListener = entryListener;

        if (entries.size() > 0) {
            entryListener.entriesAdded(entries);
        }
    }

    private class ReadResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            List<Entry> readEntries = intent.getParcelableArrayListExtra(EntryIO.EXTRA_ENTRIES);
            entries.addAll(readEntries);
            if (entryListener != null) {
                entryListener.entriesAdded(readEntries);
            }
        }

    }

    private class ExceptionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, intent.getIntExtra(EntryIO.EXTRA_ERROR_MESSAGE_ID, 0), Toast.LENGTH_LONG).show();
        }

    }

}
