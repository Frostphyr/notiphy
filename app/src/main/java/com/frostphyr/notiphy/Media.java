package com.frostphyr.notiphy;

import android.os.Parcel;
import android.os.Parcelable;

public class Media implements Parcelable {

    private final MediaType type;
    private final String thumbnailUrl;
    private final int width;
    private final int height;
    private final int count;

    public Media(MediaType type, String thumbnailUrl, int width, int height, int count) {
        this.type = type;
        this.thumbnailUrl = thumbnailUrl;
        this.width = width;
        this.height = height;
        this.count = count;
    }

    public MediaType getType() {
        return type;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getCount() {
        return count;
    }

    public IconResource getIconResource() {
        return count > 1 ? new IconResource(R.drawable.ic_album, R.string.album) : type.getIconResource();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type.ordinal());
        dest.writeString(thumbnailUrl);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeInt(count);
    }

    public static final Parcelable.Creator<Media> CREATOR = new Parcelable.Creator<Media>() {

        @Override
        public Media createFromParcel(Parcel in) {
            MediaType type = MediaType.values()[in.readInt()];
            String thumbnailUrl = in.readString();
            int width = in.readInt();
            int height = in.readInt();
            int count = in.readInt();
            return new Media(type, thumbnailUrl, width, height, count);
        }

        @Override
        public Media[] newArray(int size) {
            return new Media[size];
        }

    };

}
