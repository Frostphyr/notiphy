package com.frostphyr.notiphy;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
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

    private View createSwitchSetting(LayoutInflater inflater, ViewGroup parent, final Setting setting, boolean value) {
        View view = inflater.inflate(R.layout.layout_settings_switch, parent, false);
        SwitchCompat s = view.findViewById(R.id.settings_switch);
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

    private View createSpinnerSetting(LayoutInflater inflater, ViewGroup parent, final Setting setting, Object value) {
        View view = inflater.inflate(R.layout.layout_settings_spinner, parent, false);
        TitledSpinner spinner = view.findViewById(R.id.settings_spinner);
        spinner.setAdapter(new BasicSpinnerAdapter<>(this, NsfwContent.values()));
        spinner.setSelectedItem(((NsfwContent) value).ordinal());
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((NotiphyApplication) getApplication()).setSetting(setting, parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });
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
                return createSwitchSetting(inflater, parent, setting, (boolean) getItem(position));
            } else if (setting.getType().isEnum()) {
                return createSpinnerSetting(inflater, parent, setting, getItem(position));
            }
            return null;
        }

    }

}
