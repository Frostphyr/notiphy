package com.frostphyr.notiphy;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;

public class TwitterEntry implements Entry {

    private String username;
    private MediaType mediaType;
    private String[] phrases;

    public TwitterEntry(String username, MediaType mediaType, String[] phrases) {
        this.username = username;
        this.mediaType = mediaType;
        this.phrases = phrases;
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
    public EntryType getType() {
        return EntryType.TWITTER;
    }

    @Override
    public View createView(LayoutInflater inflater) {
        return null;
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
    }

    public static final Parcelable.Creator<TwitterEntry> CREATOR = new Parcelable.Creator<TwitterEntry>() {

        @Override
        public TwitterEntry createFromParcel(Parcel in) {
            String username = in.readString();
            MediaType mediaType = MediaType.values()[in.readInt()];
            String[] phrases = new String[in.readInt()];
            in.readStringArray(phrases);
            return new TwitterEntry(username, mediaType, phrases);
        }

        @Override
        public TwitterEntry[] newArray(int size) {
            return new TwitterEntry[size];
        }

    };

}
