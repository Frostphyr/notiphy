package com.frostphyr.notiphy;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.Date;

public class Message implements Parcelable {

    private EntryType type;
    private Date createdAt;
    private String title;
    private String description;
    private String url;
    private CharSequence text;
    private Media[] media;
    private boolean nsfw;

    private Message() {
    }

    public EntryType getType() {
        return type;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public CharSequence getText() {
        return text;
    }

    public Media[] getMedia() {
        return media;
    }

    public boolean isNsfw() {
        return nsfw;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type.ordinal());
        dest.writeLong(createdAt.getTime());
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(url);
        TextUtils.writeToParcel(text, dest, flags);
        dest.writeTypedArray(media, flags);
        dest.writeInt(nsfw ? 1 : 0);
    }

    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {

        @Override
        public Message createFromParcel(Parcel in) {
            return new Builder()
                    .setType(EntryType.values()[in.readInt()])
                    .setCreatedAt(new Date(in.readLong()))
                    .setTitle(in.readString())
                    .setDescription(in.readString())
                    .setUrl(in.readString())
                    .setText(TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in))
                    .setMedia(in.createTypedArray(Media.CREATOR))
                    .setNsfw(in.readInt() != 0)
                    .build();
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }

    };

    public static class Builder {

        private Message message = new Message();

        public Builder setType(EntryType type) {
            message.type = type;
            return this;
        }

        public Builder setCreatedAt(Date createdAt) {
            message.createdAt = createdAt;
            return this;
        }

        public Builder setTitle(String title) {
            message.title = title;
            return this;
        }

        public Builder setDescription(String description) {
            message.description = description;
            return this;
        }

        public Builder setUrl(String url) {
            message.url = url;
            return this;
        }

        public Builder setText(CharSequence text) {
            message.text = text;
            return this;
        }

        public Builder setMedia(Media[] media) {
            message.media = media;
            return this;
        }

        public Builder setNsfw(boolean nsfw) {
            message.nsfw = nsfw;
            return this;
        }

        public Message build() {
            Message m = message;
            message = null;
            return m;
        }

    }

}
