package com.frostphyr.notiphy.twitter;

import android.os.Parcel;
import android.os.Parcelable;

import com.frostphyr.notiphy.Entry;
import com.frostphyr.notiphy.EntryType;
import com.frostphyr.notiphy.IconResource;
import com.frostphyr.notiphy.MediaType;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TwitterEntry extends Entry {

    private String userId;
    private String username;
    private MediaType mediaType;

    public TwitterEntry(String userId, String username, MediaType mediaType,
                        List<String> phrases, boolean active) {
        super(phrases, active);

        this.userId = userId;
        this.username = username;
        this.mediaType = mediaType;
    }

    @SuppressWarnings("unused")
    public TwitterEntry() {
        super();
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    @Override
    public EntryType getType() {
        return EntryType.TWITTER;
    }

    @Exclude
    @Override
    public String getTitle() {
        return username;
    }

    @Exclude
    @Override
    public IconResource getDescriptionIconResource() {
        return mediaType.getIconResource();
    }

    @Override
    public TwitterEntry withActive(boolean active) {
        return active == isActive() ? this : new TwitterEntry(userId, username, mediaType, getPhrases(), active);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(username);
        dest.writeInt(mediaType.ordinal());
        dest.writeList(getPhrases());
        dest.writeInt(isActive() ? 1 : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TwitterEntry) {
            TwitterEntry e = (TwitterEntry) o;
            return e.username.equalsIgnoreCase(username)
                    && e.mediaType.equals(mediaType)
                    && e.getPhrases().equals(getPhrases())
                    && e.isActive() == isActive();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[] {userId, mediaType.ordinal(), getPhrases(), isActive()});
    }

    public static final Parcelable.Creator<Entry> CREATOR = new Parcelable.Creator<Entry>() {

        @Override
        public Entry createFromParcel(Parcel in) {
            String userId = in.readString();
            String username = in.readString();
            MediaType mediaType = MediaType.values()[in.readInt()];
            List<String> phrases = new ArrayList<>();
            in.readList(phrases, null);
            boolean active = in.readInt() != 0;
            return new TwitterEntry(userId, username, mediaType, phrases, active);
        }

        @Override
        public Entry[] newArray(int size) {
            return new Entry[size];
        }

    };

}
