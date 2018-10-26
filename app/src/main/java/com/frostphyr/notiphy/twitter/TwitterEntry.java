package com.frostphyr.notiphy.twitter;

import android.os.Parcel;
import android.os.Parcelable;

import com.frostphyr.notiphy.CharUtils;
import com.frostphyr.notiphy.Entry;
import com.frostphyr.notiphy.EntryActivity;
import com.frostphyr.notiphy.EntryType;
import com.frostphyr.notiphy.MediaType;

import java.util.Arrays;

public class TwitterEntry implements Entry {

    public static final char[][] USERNAME_CHAR_RANGES = {
            {'a', 'z'},
            {'A', 'Z'},
            {'0', '9'},
            {'_'}
    };

    private String id;
    private String username;
    private MediaType mediaType;
    private String[] phrases;
    private volatile boolean active;

    public TwitterEntry(String id, String username, MediaType mediaType, String[] phrases, boolean active) {
        this.id = id;
        this.username = validateUsername(username);
        this.mediaType = validateMediaType(mediaType);
        this.phrases = validatePhrases(phrases);
        this.active = active;
    }

    public String getId() {
        return id;
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
        dest.writeString(id);
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
            return e.id == id
                    && e.username.equals(username)
                    && e.mediaType.equals(mediaType)
                    && Arrays.equals(e.phrases, phrases)
                    && active == active;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[] {id, username, mediaType, active, Arrays.hashCode(phrases)});
    }

    private static String validateUsername(String username) {
        if (username.length() <= 0 || username.length() > 15 || !CharUtils.inRanges(USERNAME_CHAR_RANGES, username)) {
            throw new IllegalArgumentException();
        }
        return username;
    }

    private static MediaType validateMediaType(MediaType mediaType) {
        if (mediaType == null) {
            throw new IllegalArgumentException();
        }
        return mediaType;
    }

    private static String[] validatePhrases(String[] phrases) {
        if (phrases == null || phrases.length > EntryActivity.MAX_PHRASES) {
            throw new IllegalArgumentException();
        }
        return phrases;
    }

    public static final Parcelable.Creator<Entry> CREATOR = new Parcelable.Creator<Entry>() {

        @Override
        public Entry createFromParcel(Parcel in) {
            String id = in.readString();
            String username = in.readString();
            MediaType mediaType = MediaType.values()[in.readInt()];
            String[] phrases = new String[in.readInt()];
            in.readStringArray(phrases);
            boolean active = in.readInt() != 0;
            return new TwitterEntry(id, username, mediaType, phrases, active);
        }

        @Override
        public Entry[] newArray(int size) {
            return new Entry[size];
        }

    };

}
