package com.frostphyr.notiphy;

import android.os.Parcelable;

public interface Entry extends Parcelable {

    boolean isActive();

    void setActive(boolean active);

    EntryType getType();

}
