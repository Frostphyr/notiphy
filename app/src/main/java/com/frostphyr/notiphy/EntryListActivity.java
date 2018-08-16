package com.frostphyr.notiphy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.util.List;

public class EntryListActivity extends AppCompatActivity {

    private NotiphyApplication application;
    private MenuPopupHelper addMenuHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_list);
        setSupportActionBar((Toolbar) findViewById(R.id.entry_list_toolbar));

        application = (NotiphyApplication) getApplication();

        ListView entryList = findViewById(R.id.entry_list);
        entryList.setAdapter(new EntryRowAdapter(application.getEntries()));

        if (!application.finishedReadingEntries()) {
            application.setReadListener(new Runnable() {

                @Override
                public void run() {
                    updateEntryList();
                }

            });
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EntryActivity.REQUEST_CODE && resultCode == RESULT_OK) {
            application.saveEntries();
            updateEntryList();
        }
    }

    private void updateEntryList() {
        ListView entryList = findViewById(R.id.entry_list);
        ((ArrayAdapter<?>) entryList.getAdapter()).notifyDataSetChanged();
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
                        startActivityForResult(intent, EntryActivity.REQUEST_CODE);
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

        public EntryRowAdapter(List<Entry> objects) {
            super(EntryListActivity.this, -1, objects);

            inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getItem(position).createView(inflater, convertView, parent, EntryListActivity.this);
        }

        @Override
        public int getViewTypeCount() {
            return EntryType.values().length;
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).getType().ordinal();
        }

    }

}
