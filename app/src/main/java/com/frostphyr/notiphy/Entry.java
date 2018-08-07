package com.frostphyr.notiphy;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;

public interface Entry extends Parcelable {

    EntryType getType();

    View createView(LayoutInflater inflater);

}
