package com.frostphyr.notiphy;

import android.os.Parcel;
import android.os.Parcelable;

public class DatabaseEntry implements Parcelable {

    private final Entry entry;
    private final String id;

    public DatabaseEntry(Entry entry, String id) {
        this.entry = entry;
        this.id = id;
    }

    @SuppressWarnings("unchecked")
    public <T extends Entry> T getEntry() {
        return (T) entry;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DatabaseEntry) {
            return ((DatabaseEntry) o).id.equals(id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(entry, flags);
        dest.writeString(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DatabaseEntry> CREATOR = new Creator<DatabaseEntry>() {
        @Override
        public DatabaseEntry createFromParcel(Parcel in) {
            return new DatabaseEntry(in.readParcelable(getClass().getClassLoader()), in.readString());
        }

        @Override
        public DatabaseEntry[] newArray(int size) {
            return new DatabaseEntry[size];
        }
    };

}
