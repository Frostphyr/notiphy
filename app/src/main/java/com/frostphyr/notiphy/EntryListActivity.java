package com.frostphyr.notiphy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.frostphyr.notiphy.io.NotiphyWebSocket;
import com.frostphyr.notiphy.twitter.TwitterActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.TooltipCompat;

public class EntryListActivity extends AppCompatActivity {

    private MenuPopupHelper addMenuHelper;
    private NotiphyWebSocket.Listener webSocketListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_list);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        ListView entryList = findViewById(R.id.entry_list);
        entryList.setAdapter(new EntryRowAdapter());

        TooltipCompat.setTooltipText(findViewById(R.id.toolbar_error), getString(R.string.error_message_notiphy_connection,
                new SimpleDateFormat("s", Locale.getDefault()).format(new Date(((NotiphyApplication) getApplication()).getWebSocket().getReconnectDelay()))));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.entry_list_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                showAddPopupMenu(item);
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        if (resultCode == RESULT_OK) {
            Entry entry = resultIntent.getParcelableExtra(EntryActivity.EXTRA_ENTRY);
            NotiphyApplication application = (NotiphyApplication) getApplication();
            ListView entryList = findViewById(R.id.entry_list);
            ArrayAdapter<Entry> adapter = ((ArrayAdapter<Entry>) entryList.getAdapter());

            switch (requestCode) {
                case EntryActivity.REQUEST_CODE_EDIT:
                    Entry oldEntry = resultIntent.getParcelableExtra(EntryActivity.EXTRA_OLD_ENTRY);
                    if (entry == null) {
                        if (application.removeEntry(oldEntry)) {
                            adapter.remove(oldEntry);
                        }
                    } else {
                        replaceEntry(oldEntry, entry);
                    }
                    break;
                case EntryActivity.REQUEST_CODE_NEW:
                    application.addEntry(entry);
                    adapter.add(entry);
                    break;
            }
            adapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onPause() {
        super.onPause();

        if (addMenuHelper != null && addMenuHelper.isShowing()) {
            addMenuHelper.dismiss();
            addMenuHelper = null;
        }

        ((NotiphyApplication) getApplication()).getWebSocket().removeListener(webSocketListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        NotiphyWebSocket webSocket = ((NotiphyApplication) getApplication()).getWebSocket();
        webSocketListener = new ConnectionFailureListener();
        webSocket.addListener(webSocketListener);
        updateError(webSocket.getStatus());
    }

    @SuppressLint("RestrictedApi")
    private void showAddPopupMenu(MenuItem item) {
        MenuBuilder menuBuilder = new MenuBuilder(this);
        menuBuilder.setCallback(new MenuBuilder.Callback() {

            @Override
            public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_add_twitter:
                        Intent intent = new Intent(EntryListActivity.this, TwitterActivity.class);
                        startActivityForResult(intent, EntryActivity.REQUEST_CODE_NEW);
                        break;
                }
                return false;
            }

            @Override
            public void onMenuModeChange(MenuBuilder menu) {

            }

        });

        new MenuInflater(this).inflate(R.menu.entry_list_toolbar_add_popup_menu, menuBuilder);
        addMenuHelper = new MenuPopupHelper(this, menuBuilder, findViewById(item.getItemId()));
        addMenuHelper.setForceShowIcon(true);
        addMenuHelper.show();
    }

    @SuppressWarnings("unchecked")
    private void replaceEntry(Entry oldEntry, Entry newEntry) {
        ArrayAdapter<Entry> adapter = ((ArrayAdapter<Entry>) ((ListView) findViewById(R.id.entry_list)).getAdapter());
        int position = adapter.getPosition(oldEntry);
        if (position != -1) {
            adapter.remove(oldEntry);
            ((NotiphyApplication) getApplication()).replaceEntry(oldEntry, newEntry);
            adapter.insert(newEntry, position);
        }
    }

    private void updateError(NotiphyWebSocket.Status status) {
        findViewById(R.id.toolbar_error).setVisibility(status == NotiphyWebSocket.Status.FAILURE ? View.VISIBLE : View.GONE);
    }

    private class EntryRowAdapter extends ArrayAdapter<Entry> {

        private LayoutInflater inflater;

        public EntryRowAdapter() {
            super(EntryListActivity.this, -1, new ArrayList<>(((NotiphyApplication) getApplication()).getEntries()));

            inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return createView(getItem(position), convertView, parent);
        }

        @Override
        public int getViewTypeCount() {
            return EntryType.values().length;
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).getType().ordinal();
        }

        @SuppressWarnings("unchecked")
        private <T extends Entry> View createView(final T entry, View convertView, ViewGroup parent) {
            ViewGroup view = (ViewGroup) convertView;
            View entryView = null;
            if (convertView == null) {
                view = (ViewGroup) inflater.inflate(R.layout.layout_entry_row, parent, false);
            } else {
                entryView = view.getChildAt(0);
            }
            entryView = ((EntryViewFactory<T>) entry.getType().getViewFactory()).createView(entry, inflater, entryView, view, EntryListActivity.this);
            SwitchCompat activeSwitch = view.findViewById(R.id.active_switch);
            activeSwitch.setChecked(entry.isActive());
            activeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    replaceEntry(entry, entry.withActive(isChecked));
                }

            });
            if (convertView == null) {
                view.addView(entryView, 0);
            }
            return view;
        }

    }

    private class ConnectionFailureListener implements NotiphyWebSocket.Listener {

        @Override
        public void onStatusChange(NotiphyWebSocket socket, NotiphyWebSocket.Status status) {
            updateError(status);
        }

        @Override
        public void onMessage(NotiphyWebSocket socket, Message message) {

        }

    }

}
