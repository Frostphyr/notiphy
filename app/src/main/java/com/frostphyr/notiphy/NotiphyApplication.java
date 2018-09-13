package com.frostphyr.notiphy;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import com.frostphyr.notiphy.io.EntryIO;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

public class NotiphyApplication extends Application {

    private List<Entry> entries = new ArrayList<Entry>();
    private OkHttpClient httpClient = new OkHttpClient();
    private Runnable readListener;
    private boolean finishedReadingEntries;

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

    public void replaceEntry(Entry oldEntry, Entry newEntry) {
        int index = entries.indexOf(oldEntry);
        if (index != -1) {
            entries.set(index, newEntry);
        }
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public void setReadListener(Runnable readListener) {
        this.readListener = readListener;
    }

    public boolean finishedReadingEntries() {
        return finishedReadingEntries;
    }

    private class ReadResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            List<Entry> readEntries = intent.getParcelableArrayListExtra(EntryIO.EXTRA_ENTRIES);
            entries.addAll(readEntries);
            finishedReadingEntries = true;
            if (readListener != null) {
                readListener.run();
                readListener = null;
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
