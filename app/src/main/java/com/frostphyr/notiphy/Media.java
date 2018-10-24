package com.frostphyr.notiphy;

import android.os.Parcel;
import android.os.Parcelable;

public class Media implements Parcelable {

    private MediaType type;
    private String url;
    private String thumbnailUrl;

    public Media(MediaType type, String url, String thumbnailUrl) {
        this.type = type;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
    }

    public Media(MediaType type, String url) {
        this(type, url, url);
    }

    public MediaType getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type.ordinal());
        dest.writeString(url);
        dest.writeString(thumbnailUrl);
    }

    public static final Parcelable.Creator<Media> CREATOR = new Parcelable.Creator<Media>() {

        @Override
        public Media createFromParcel(Parcel in) {
            MediaType type = MediaType.values()[in.readInt()];
            String url = in.readString();
            String thumbnailUrl = in.readString();
            return new Media(type, url, thumbnailUrl);
        }

        @Override
        public Media[] newArray(int size) {
            return new Media[size];
        }

    };

}
