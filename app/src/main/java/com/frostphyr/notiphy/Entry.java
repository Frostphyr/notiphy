package com.frostphyr.notiphy;

import android.os.Parcelable;

public interface Entry extends Parcelable {

    boolean isActive();

    Entry withActive(boolean active);

    EntryType getType();

}
