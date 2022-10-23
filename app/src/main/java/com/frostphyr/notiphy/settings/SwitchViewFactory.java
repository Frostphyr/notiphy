package com.frostphyr.notiphy.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import com.frostphyr.notiphy.AndroidUtils;
import com.frostphyr.notiphy.R;

public class SwitchViewFactory implements SettingViewFactory {

    private final Setting<Boolean> setting;

    public SwitchViewFactory(Setting<Boolean> setting) {
        this.setting = setting;
    }

    @Override
    public View create(Context context, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_settings_switch, parent, false);
        TextView textView = view.findViewById(R.id.settings_switch_text);
        textView.setText(setting.getTitleResId());
        view.findViewById(R.id.settings_switch_description).setOnClickListener(v ->
                AndroidUtils.showDialog(context, setting.getTitleResId(), setting.getDescriptionResId()));
        SwitchCompat s = view.findViewById(R.id.settings_switch);
        s.setChecked(setting.get(context));
        s.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setting.set(context, isChecked);
        });
        return view;
    }

}
