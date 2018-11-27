package com.frostphyr.notiphy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.frostphyr.notiphy.io.EntryReadTask;
import com.frostphyr.notiphy.io.SettingsReadTask;

import java.io.FileNotFoundException;
import java.util.List;

public class StartupActivity extends AppCompatActivity {

    private StartupTask<?>[] tasks = new StartupTask<?>[] {
            new SettingsStartupTask(),
            new EntriesStartupTask()
    };

    private int nextTaskIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        ProgressBar progress = findViewById(R.id.startup_progress);
        progress.setMax(tasks.length);
        startNextTask();
    }

    private void startNextTask() {
        TextView textView = findViewById(R.id.startup_text);
        if (nextTaskIndex >= tasks.length) {
            textView.setText(R.string.finished);
            Intent intent = new Intent(StartupActivity.this, EntryListActivity.class);
            startActivity(intent);
        } else {
            StartupTask<?> task = tasks[nextTaskIndex++];
            textView.setText(getString(R.string.loading, getString(task.getNameResourceId())));
            task.createAsyncTask(createCallback(task)).execute();
        }
    }

    private <T> AsyncTaskHelper.Callback<T> createCallback(final StartupTask<T> task) {
        return new AsyncTaskHelper.Callback<T>() {

            @Override
            public void onSuccess(T t) {
                increment();
                task.onFinish(t);
                startNextTask();
            }

            @Override
            public void onException(Exception exception) {
                increment();
                if (!(exception instanceof FileNotFoundException)) {
                    Toast.makeText(StartupActivity.this, task.getErrorResourceId(), Toast.LENGTH_LONG).show();
                }

                T def = task.getDefault();
                if (def != null) {
                    task.onFinish(def);
                }
                startNextTask();
            }

            private void increment() {
                ProgressBar progress = findViewById(R.id.startup_progress);
                progress.incrementProgressBy(1);
            }

        };
    }

    private interface StartupTask<T> {

        AsyncTaskHelper<Void, Void, T> createAsyncTask(AsyncTaskHelper.Callback callback);

        void onFinish(T result);

        T getDefault();

        int getNameResourceId();

        int getErrorResourceId();

    }

    private class SettingsStartupTask implements StartupTask<Object[]> {

        @Override
        public AsyncTaskHelper<Void, Void, Object[]> createAsyncTask(AsyncTaskHelper.Callback callback) {
            return new SettingsReadTask(StartupActivity.this, callback);
        }

        @Override
        public void onFinish(Object[] result) {
            ((NotiphyApplication) getApplication()).setSettings(result);
        }

        @Override
        public Object[] getDefault() {
            Object[] settings = new Object[Setting.getCount()];
            for (int i = 0; i < settings.length; i++) {
                settings[i] = Setting.forId(i).getDefaultValue();
            }
            return settings;
        }

        @Override
        public int getNameResourceId() {
            return R.string.settings;
        }

        @Override
        public int getErrorResourceId() {
            return R.string.error_message_read_settings;
        }

    }

    private class EntriesStartupTask implements StartupTask<List<Entry>> {

        @Override
        public AsyncTaskHelper<Void, Void, List<Entry>> createAsyncTask(AsyncTaskHelper.Callback callback) {
            return new EntryReadTask(StartupActivity.this, callback);
        }

        @Override
        public void onFinish(List<Entry> result) {
            ((NotiphyApplication) getApplication()).setEntries(result);
        }

        @Override
        public List<Entry> getDefault() {
            return null;
        }

        @Override
        public int getNameResourceId() {
            return R.string.entries;
        }

        @Override
        public int getErrorResourceId() {
            return R.string.error_message_read_entries;
        }

    }

}
