package com.frostphyr.notiphy;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public interface Entry extends Parcelable {

    boolean isActive();

    void setActive(boolean active);

    EntryType getType();

    View createView(LayoutInflater inflater, View view, ViewGroup parent);

}
