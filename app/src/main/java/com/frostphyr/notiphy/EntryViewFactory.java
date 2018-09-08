package com.frostphyr.notiphy;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public interface EntryViewFactory<T extends Entry> {

    View createView(T entry, LayoutInflater inflater, View view, ViewGroup parent, Activity activity);

}
