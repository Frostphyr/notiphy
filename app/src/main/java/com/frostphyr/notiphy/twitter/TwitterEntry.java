package com.frostphyr.notiphy.twitter;

import android.os.Parcel;
import android.os.Parcelable;

import com.frostphyr.notiphy.Entry;
import com.frostphyr.notiphy.EntryActivity;
import com.frostphyr.notiphy.EntryType;
import com.frostphyr.notiphy.MediaType;

import java.util.Arrays;

public class TwitterEntry implements Entry {

    private String username;
    private MediaType mediaType;
    private String[] phrases;
    private volatile boolean active;

    public static String validateUsername(String username) {
        if (username.length() <= 0) {
            return "Username required";
        } else if (username.length() > 15) {
            return "Username cannot be longer than 15 characters";
        } else if (!username.matches("^[a-zA-Z0-9_]*$")) {
            return "Username can only contain alphanumeric characters and underscores";
        }
        return null;
    }

    public static String validateMediaType(MediaType mediaType) {
        if (mediaType == null) {
            return "Media Type cannot be null";
        }
        return null;
    }

    public static String validatePhrases(String[] phrases) {
        if (phrases == null) {
            return "Phrases cannot be null";
        } else if (phrases.length > EntryActivity.MAX_PHRASES) {
            return "Max number of phrases is " + EntryActivity.MAX_PHRASES;
        }
        return null;
    }

    public TwitterEntry(String username, MediaType mediaType, String[] phrases, boolean active) {
        this.username = username;
        this.mediaType = mediaType;
        this.phrases = phrases;
        this.active = active;
    }

    public String getUsername() {
        return username;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public String[] getPhrases() {
        return phrases;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public EntryType getType() {
        return EntryType.TWITTER;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeInt(mediaType.ordinal());
        dest.writeInt(phrases.length);
        dest.writeStringArray(phrases);
        dest.writeInt(active ? 1 : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TwitterEntry) {
            TwitterEntry e = (TwitterEntry) o;
            return e.username.equals(username)
                    && e.mediaType.equals(mediaType)
                    && Arrays.equals(e.phrases, phrases)
                    && active == active;
        }
        return false;
    }

    public static final Parcelable.Creator<Entry> CREATOR = new Parcelable.Creator<Entry>() {

        @Override
        public Entry createFromParcel(Parcel in) {
            String username = in.readString();
            MediaType mediaType = MediaType.values()[in.readInt()];
            String[] phrases = new String[in.readInt()];
            in.readStringArray(phrases);
            boolean active = in.readInt() != 0;
            return new TwitterEntry(username, mediaType, phrases, active);
        }

        @Override
        public Entry[] newArray(int size) {
            return new Entry[size];
        }

    };

}
