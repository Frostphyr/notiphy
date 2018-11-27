package com.frostphyr.notiphy;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        setSupportActionBar((Toolbar) findViewById(R.id.settings_toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        actionBar.setDisplayHomeAsUpEnabled(true);

        ((ListView) findViewById(R.id.settings_list)).setAdapter(new SettingsAdapter());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private View createSwitchSetting(LayoutInflater inflater, final Setting setting, boolean value) {
        View view = inflater.inflate(R.layout.layout_settings_switch, null, false);
        Switch s = view.findViewById(R.id.settings_switch);
        s.setChecked(value);
        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((NotiphyApplication) getApplication()).setSetting(setting, isChecked);
            }

        });
        TextView textView = view.findViewById(R.id.settings_switch_text);
        textView.setText(setting.getName());
        return view;
    }

    private class SettingsAdapter extends ArrayAdapter<Object> {

        private LayoutInflater inflater;

        public SettingsAdapter() {
            super(SettingsActivity.this, -1, ((NotiphyApplication) getApplication()).getSettings());

            inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Setting setting = Setting.forId(position);
            if (setting.getType().equals(Boolean.class)) {
                return createSwitchSetting(inflater, setting, (boolean) getItem(position));
            }
            return null;
        }

    }

}
