package com.frostphyr.notiphy.reddit;

import android.os.Parcel;
import android.os.Parcelable;

import com.frostphyr.notiphy.Entry;
import com.frostphyr.notiphy.EntryType;
import com.frostphyr.notiphy.IconResource;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RedditEntry extends Entry {

    private RedditEntryType entryType;
    private String value;
    private RedditPostType postType;

    public RedditEntry(RedditEntryType type, String value, RedditPostType postType,
                       List<String> phrases, boolean active) {
        super(phrases, active);

        this.entryType = type;
        this.value = value;
        this.postType = postType;
    }

    @SuppressWarnings("unused")
    public RedditEntry() {
        super();
    }

    public RedditEntryType getEntryType() {
        return entryType;
    }

    public String getValue() {
        return value;
    }

    public RedditPostType getPostType() {
        return postType;
    }

    @Override
    public EntryType getType() {
        return EntryType.REDDIT;
    }

    @Exclude
    @Override
    public String getTitle() {
        return entryType.getPrefix() + value;
    }

    @Exclude
    @Override
    public IconResource getDescriptionIconResource() {
        return postType.getIconResource();
    }

    @Override
    public Entry withActive(boolean active) {
        return active == isActive() ? this : new RedditEntry(entryType, value, postType, getPhrases(), active);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(entryType.ordinal());
        dest.writeString(value);
        dest.writeInt(postType.ordinal());
        dest.writeList(getPhrases());
        dest.writeInt(isActive() ? 1 : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RedditEntry) {
            RedditEntry e = (RedditEntry) o;
            return e.entryType == entryType
                    && e.value.equals(value)
                    && e.postType == postType
                    && e.getPhrases().equals(getPhrases())
                    && e.isActive() == isActive();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[] {entryType.ordinal(), value, postType.ordinal(), getPhrases(), isActive()});
    }

    public static final Parcelable.Creator<Entry> CREATOR = new Parcelable.Creator<Entry>() {

        @Override
        public Entry createFromParcel(Parcel in) {
            RedditEntryType type = RedditEntryType.values()[in.readInt()];
            String value = in.readString();
            RedditPostType postType = RedditPostType.values()[in.readInt()];
            List<String> phrases = new ArrayList<>();
            in.readList(phrases, null);
            boolean active = in.readInt() != 0;
            return new RedditEntry(type, value, postType, phrases, active);
        }

        @Override
        public Entry[] newArray(int size) {
            return new Entry[size];
        }

    };

}
