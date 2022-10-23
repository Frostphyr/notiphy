package com.frostphyr.notiphy.settings;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public interface SettingViewFactory {

    View create(Context context, ViewGroup parent);

}
