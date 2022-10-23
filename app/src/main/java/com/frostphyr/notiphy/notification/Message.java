package com.frostphyr.notiphy.notification;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableString;
import android.text.Spanned;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.frostphyr.notiphy.EntryType;
import com.frostphyr.notiphy.Media;

import java.util.Date;

@Entity
public class Message implements Parcelable {

    public EntryType type;
    public Date timestamp;
    public String title;
    public String description;
    @NonNull
    @PrimaryKey
    public String url;
    public Spanned text;
    public Media media;
    public boolean mature;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type.ordinal());
        dest.writeLong(timestamp.getTime());
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(url);
        dest.writeString(text == null ? null : text.toString());
        dest.writeParcelable(media, flags);
        dest.writeInt(mature ? 1 : 0);
    }

    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {

        @Override
        public Message createFromParcel(Parcel in) {
            Message message = new Message();
            message.type = EntryType.values()[in.readInt()];
            message.timestamp = new Date(in.readLong());
            message.title = in.readString();
            message.description = in.readString();
            message.url = in.readString();
            String text = in.readString();
            message.text = text == null ? null : new SpannableString(text);
            message.media = in.readParcelable(Media.class.getClassLoader());
            message.mature = in.readInt() != 0;
            return message;
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }

    };

}
