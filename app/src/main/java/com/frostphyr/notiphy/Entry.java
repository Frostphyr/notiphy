package com.frostphyr.notiphy;

import android.os.Parcelable;
import android.text.TextUtils;

import com.google.firebase.firestore.Exclude;

import java.util.List;

public abstract class Entry implements Parcelable {

    private List<String> phrases;
    private boolean active;
    private long timestamp;

    public Entry(List<String> phrases, boolean active) {
        this.phrases = phrases;
        this.active = active;
        timestamp = System.currentTimeMillis();
    }

    public Entry() {
    }

    @Exclude
    public String getDescription() {
        return TextUtils.join(", ", getPhrases());
    }

    public abstract EntryType getType();

    public abstract String getTitle();

    public abstract IconResource getDescriptionIconResource();

    public abstract Entry withActive(boolean active);

    public List<String> getPhrases() {
        return phrases;
    }

    public boolean isActive() {
        return active;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
