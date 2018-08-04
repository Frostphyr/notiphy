package com.frostphyr.notiphy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
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

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EntryListActivity extends AppCompatActivity {

    private static EntryIO[] entryIOs = {
            new TwitterEntryIO()
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_list);
        setSupportActionBar((Toolbar) findViewById(R.id.entry_list_toolbar));

        ListView entryList = findViewById(R.id.entry_list);
        entryList.setAdapter(new EntryRowAdapter(this, 0, readEntries()));
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

    private List<Entry> readEntries() {
        List<Entry> entries = new ArrayList<Entry>();
        try {
            DataInputStream in = new DataInputStream(openFileInput("entries.txt"));
            while (in.available() > 0) {
                byte id = in.readByte();
                if (id >= 0 && id < entryIOs.length) {
                    Entry entry = entryIOs[id].read(in);
                    if (entry != null) {
                        entries.add(entry);
                    }
                }
            }
        } catch (IOException e) {

        }
        return entries;
    }

    @SuppressLint("RestrictedApi")
    private void showAddPopupMenu(MenuItem item) {
        MenuBuilder menuBuilder = new MenuBuilder(this);
        menuBuilder.setCallback(new MenuBuilder.Callback() {
            @Override
            public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_add_twitter:
                        Intent intent = new Intent(EntryListActivity.this, AddTwitterActivity.class);
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
        MenuPopupHelper menuHelper = new MenuPopupHelper(this, menuBuilder, findViewById(item.getItemId()));
        menuHelper.setForceShowIcon(true);
        menuHelper.show();
    }

    private static class EntryRowAdapter extends ArrayAdapter<Entry> {

        private LayoutInflater inflater;

        public EntryRowAdapter(@NonNull Context context, int resource, @NonNull List<Entry> objects) {
            super(context, resource, objects);

            inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            return getItem(position).createView(inflater);
        }

    }

}
