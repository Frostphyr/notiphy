package com.frostphyr.notiphy.twitter;

import android.os.Parcel;
import android.os.Parcelable;

import com.frostphyr.notiphy.Entry;
import com.frostphyr.notiphy.EntryType;
import com.frostphyr.notiphy.IllegalInputException;
import com.frostphyr.notiphy.MediaType;
import com.frostphyr.notiphy.R;
import com.frostphyr.notiphy.TextUtils;

import java.util.Arrays;

public class TwitterEntry extends Entry {

    public static final char[][] USERNAME_CHAR_RANGES = {
            {'a', 'z'},
            {'A', 'Z'},
            {'0', '9'},
            {'_'}
    };

    private String id;
    private String username;
    private MediaType mediaType;

    public TwitterEntry(String id, String username, MediaType mediaType, String[] phrases, boolean active) {
        super(phrases, active);

        this.id = validateId(id);
        this.username = validateUsername(username);
        this.mediaType = validateMediaType(mediaType);
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

    @Override
    public EntryType getType() {
        return EntryType.TWITTER;
    }

    @Override
    public String getTitle() {
        return username;
    }

    @Override
    public String getDescription() {
        return TextUtils.concat(getPhrases(), ", ");
    }

    @Override
    public int getDescriptionIconResId() {
        return mediaType.getIconResourceId();
    }
    @Override
    public TwitterEntry withActive(boolean active) {
        return active == isActive() ? this : new TwitterEntry(id, username, mediaType, getPhrases(), active);
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
        dest.writeStringArray(getPhrases());
        dest.writeInt(isActive() ? 1 : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TwitterEntry) {
            TwitterEntry e = (TwitterEntry) o;
            return e.id.equals(id)
                    && e.username.equalsIgnoreCase(username)
                    && e.mediaType.equals(mediaType)
                    && Arrays.equals(e.getPhrases(), getPhrases())
                    && e.isActive() == isActive();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[] {id, username, mediaType, Arrays.hashCode(getPhrases()), isActive()});
    }

    private static String validateId(String id) {
        if (id == null) {
            throw new IllegalInputException(R.string.error_message_twitter_id);
        }
        return id;
    }

    private static String validateUsername(String username) {
        if (username.length() <= 0 || username.length() > 15 || !TextUtils.inRanges(USERNAME_CHAR_RANGES, username)) {
            throw new IllegalInputException(R.string.error_message_twitter_username);
        }
        return username;
    }

    private static MediaType validateMediaType(MediaType mediaType) {
        if (mediaType == null) {
            throw new IllegalInputException(R.string.error_message_twitter_media_type);
        }
        return mediaType;
    }

    public static final Parcelable.Creator<Entry> CREATOR = new Parcelable.Creator<Entry>() {

        @Override
        public Entry createFromParcel(Parcel in) {
            String id = in.readString();
            String username = in.readString();
            MediaType mediaType = MediaType.values()[in.readInt()];
            String[] phrases = in.createStringArray();
            boolean active = in.readInt() != 0;
            return new TwitterEntry(id, username, mediaType, phrases, active);
        }

        @Override
        public Entry[] newArray(int size) {
            return new Entry[size];
        }

    };

}
