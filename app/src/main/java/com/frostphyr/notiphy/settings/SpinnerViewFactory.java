package com.frostphyr.notiphy.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.frostphyr.notiphy.AndroidUtils;
import com.frostphyr.notiphy.R;
import com.frostphyr.notiphy.SpinnerItem;

public class SpinnerViewFactory<T extends SpinnerItem> implements SettingViewFactory {

    private final Setting<T> setting;
    private final T[] values;

    public SpinnerViewFactory(Setting<T> setting, T[] values) {
        this.setting = setting;
        this.values = values;
    }

    @Override
    public View create(Context context, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_settings_spinner, parent, false);
        TextView textView = view.findViewById(R.id.settings_spinner_text);
        textView.setText(setting.getTitleResId());
        view.findViewById(R.id.settings_spinner_description).setOnClickListener(v ->
                AndroidUtils.showDialog(context, setting.getTitleResId(), setting.getDescriptionResId()));
        Spinner spinner = view.findViewById(R.id.settings_spinner);
        ArrayAdapter<T> adapter = AndroidUtils.createSpinnerAdapter(context, values);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getPosition(setting.get(context)));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setting.set(context, adapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });
        return view;
    }

}
