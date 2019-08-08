package com.frostphyr.notiphy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.frostphyr.notiphy.io.EntryReadTask;
import com.frostphyr.notiphy.io.SettingsReadTask;
import com.google.android.gms.ads.MobileAds;

import java.io.FileNotFoundException;
import java.util.List;

public class StartupActivity extends AppCompatActivity {

    public static final String EXTRA_FINISH_ACTIVITY_INTENT = "com.frostphyr.notiphy.extra.FINISH_ACTIVITY_INTENT";
    public static final String EXTRA_IS_APP_LAUNCH = "com.frostphyr.notiphy.extra.IS_APP_LAUNCH";

    private static boolean init;

    private StartupTask<?>[] tasks = new StartupTask<?>[] {
            new SettingsStartupTask(),
            new EntriesStartupTask(),
            new AdsStartupTask()
    };

    private int nextTaskIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        if (!init) {
            init = true;
            ProgressBar progress = findViewById(R.id.startup_progress);
            progress.setMax(tasks.length);
            startNextTask();
        } else {
            complete(false);
        }
    }

    private void complete(boolean appLaunch) {
        Intent intent = getIntent().getParcelableExtra(EXTRA_FINISH_ACTIVITY_INTENT);
        if (intent == null) {
            intent = new Intent(this, EntryListActivity.class);
        }
        intent.setExtrasClassLoader(getClassLoader());
        startActivity(intent.putExtra(EXTRA_IS_APP_LAUNCH, appLaunch));
    }

    private void startNextTask() {
        TextView textView = findViewById(R.id.startup_text);
        if (nextTaskIndex >= tasks.length) {
            textView.setText(R.string.finished);
            complete(true);
        } else {
            StartupTask<?> task = tasks[nextTaskIndex++];
            textView.setText(getString(R.string.loading, getString(task.getNameResourceId())));
            execute(task);
        }
    }

    private <T> void execute(final StartupTask<T> task) {
        task.execute(new AsyncTaskHelper.Callback<T>() {

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

        });
    }

    private interface StartupTask<T> {

        void execute(AsyncTaskHelper.Callback<T> callback);

        void onFinish(T result);

        T getDefault();

        int getNameResourceId();

        int getErrorResourceId();

    }

    private class SettingsStartupTask implements StartupTask<Object[]> {

        @Override
        public void execute(AsyncTaskHelper.Callback callback) {
            new SettingsReadTask(StartupActivity.this, callback).execute();
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
        public void execute(AsyncTaskHelper.Callback callback) {
            new EntryReadTask(StartupActivity.this, callback).execute();
        }

        @Override
        public void onFinish(List<Entry> result) {
            ((NotiphyApplication) getApplication()).addEntries(result);
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

    private class AdsStartupTask implements StartupTask<Void> {

        @Override
        public void execute(AsyncTaskHelper.Callback callback) {
            MobileAds.initialize(StartupActivity.this, "ca-app-pub-5141874150695762~5117322160");
            callback.onSuccess(null);
        }

        @Override
        public void onFinish(Void result) {
        }

        @Override
        public Void getDefault() {
            return null;
        }

        @Override
        public int getNameResourceId() {
            return R.string.ads;
        }

        @Override
        public int getErrorResourceId() {
            return R.string.error_message_initializing_ads;
        }

    }

}
