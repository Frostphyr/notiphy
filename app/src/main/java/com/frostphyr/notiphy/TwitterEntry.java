package com.frostphyr.notiphy;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

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
        } else if (phrases.length > AddEntryActivity.MAX_PHRASES) {
            return "Max number of phrases is " + AddEntryActivity.MAX_PHRASES;
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
    public View createView(LayoutInflater inflater, View view, final ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            view = inflater.inflate(R.layout.layout_entry_row_twitter, parent, false);
            holder = new ViewHolder();
            holder.username = view.findViewById(R.id.username);
            holder.phrases = view.findViewById(R.id.phrases);
            holder.active = view.findViewById(R.id.active_switch);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.username.setText(username);
        StringBuilder builder = new StringBuilder(Math.max(phrases.length * 2 - 1, 0));
        for (int i = 0; i < phrases.length; i++) {
            builder.append(phrases[i]);
            if (i != phrases.length - 1) {
                builder.append(", ");
            }
        }
        holder.phrases.setText(builder.toString());
        holder.active.setChecked(active);
        holder.active.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setActive(isChecked);
                ((NotiphyApplication) parent.getContext().getApplicationContext()).saveEntries();
            }

        });
        return view;
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

    static class ViewHolder {

        TextView username;
        TextView phrases;
        Switch active;

    }

}
