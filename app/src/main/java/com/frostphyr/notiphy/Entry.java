package com.frostphyr.notiphy;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public interface Entry extends Parcelable {

    EntryType getType();

    View createView(LayoutInflater inflater, View view, ViewGroup parent);

}
