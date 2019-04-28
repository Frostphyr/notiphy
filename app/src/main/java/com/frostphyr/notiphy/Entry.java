package com.frostphyr.notiphy;

import android.os.Parcelable;

public abstract class Entry implements Parcelable {

    private String[] phrases;
    private boolean active;

    protected Entry(String[] phrases, boolean active) {
        this.phrases = validatePhrases(phrases);
        this.active = active;
    }

    public abstract EntryType getType();

    public abstract String getTitle();

    public abstract String getDescription();

    public abstract int getDescriptionIconResId();

    public abstract Entry withActive(boolean active);

    public String[] getPhrases() {
        return phrases;
    }

    public boolean isActive() {
        return active;
    }

    private static String[] validatePhrases(String[] phrases) {
        if (phrases == null || phrases.length > EntryActivity.MAX_PHRASES) {
            throw new IllegalArgumentException();
        }
        return phrases;
    }

}
