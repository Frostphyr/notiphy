package com.frostphyr.notiphy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.frostphyr.notiphy.twitter.TwitterActivity;

import java.util.ArrayList;
import java.util.Collection;

public class EntryListActivity extends AppCompatActivity {

    private MenuPopupHelper addMenuHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_list);
        setSupportActionBar((Toolbar) findViewById(R.id.entry_list_toolbar));

        ListView entryList = findViewById(R.id.entry_list);
        entryList.setAdapter(new EntryRowAdapter());

        ((NotiphyApplication) getApplication()).setEntryListener(new EntryListenerImpl());
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
            default:
                return super.onOptionsItemSelected(item);
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
                        startActivity(intent);
                        break;
                }
                return false;
            }

            @Override
            public void onMenuModeChange(MenuBuilder menu) {

            }
        });

        new MenuInflater(this).inflate(R.menu.entry_list_toolbar_add_menu, menuBuilder);
        addMenuHelper = new MenuPopupHelper(this, menuBuilder, findViewById(item.getItemId()));
        addMenuHelper.setForceShowIcon(true);
        addMenuHelper.show();
    }

    private class EntryRowAdapter extends ArrayAdapter<Entry> {

        private LayoutInflater inflater;

        public EntryRowAdapter() {
            super(EntryListActivity.this, -1, new ArrayList<Entry>());

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
        private <T extends Entry> View createView(T entry, View convertView, ViewGroup parent) {
            return ((EntryViewFactory<T>) entry.getType().getViewFactory()).createView(entry, inflater, convertView, parent, EntryListActivity.this);
        }

    }

    @SuppressWarnings("unchecked")
    private class EntryListenerImpl implements EntryListener {

        @Override
        public void entriesAdded(Collection<Entry> collection) {
            ListView entryList = findViewById(R.id.entry_list);
            ArrayAdapter<Entry> adapter = ((ArrayAdapter<Entry>) entryList.getAdapter());
            adapter.addAll(collection);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void entryAdded(Entry entry) {
            ListView entryList = findViewById(R.id.entry_list);
            ArrayAdapter<Entry> adapter = ((ArrayAdapter<Entry>) entryList.getAdapter());
            adapter.add(entry);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void entryRemoved(Entry entry) {
            ListView entryList = findViewById(R.id.entry_list);
            ArrayAdapter<Entry> adapter = ((ArrayAdapter<Entry>) entryList.getAdapter());
            adapter.remove(entry);
            adapter.notifyDataSetChanged();
        }

    }

}
