package com.frostphyr.notiphy.reddit;

import android.os.Parcel;
import android.os.Parcelable;

import com.frostphyr.notiphy.Entry;
import com.frostphyr.notiphy.EntryType;
import com.frostphyr.notiphy.IllegalInputException;
import com.frostphyr.notiphy.R;
import com.frostphyr.notiphy.TextUtils;

import java.util.Arrays;

public class RedditEntry extends Entry {

    public static final char[][] USER_CHAR_RANGES = {
            {'a', 'z'},
            {'A', 'Z'},
            {'0', '9'},
            {'-'},
            {'_'}
    };

    public static final char[][] SUBREDDIT_CHAR_RANGES = {
            {'a', 'z'},
            {'A', 'Z'},
            {'0', '9'},
            {'_'}
    };

    public static final char[][] VALUE_CHAR_RANGES = {
            {'a', 'z'},
            {'A', 'Z'},
            {'0', '9'},
            {'-'},
            {'_'}
    };

    private RedditEntryType type;
    private String value;

    private RedditPostType postType;

    public RedditEntry(RedditEntryType type, String value, RedditPostType postType,
                          String[] phrases, boolean active) {
        super(phrases, active);

        this.type = validateType(type);
        this.value = validateValue(value);
        this.postType = validatePostType(postType);
    }

    public RedditEntryType getEntryType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public RedditPostType getPostType() {
        return postType;
    }

    @Override
    public EntryType getType() {
        return EntryType.REDDIT;
    }

    @Override
    public String getTitle() {
        return type.getPrefix() + value;
    }

    @Override
    public String getDescription() {
        return TextUtils.concat(getPhrases(), ", ");
    }

    @Override
    public int getDescriptionIconResId() {
        return postType.getIconResourceId();
    }

    @Override
    public Entry withActive(boolean active) {
        return active == isActive() ? this : new RedditEntry(type, value, postType, getPhrases(), active);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type.ordinal());
        dest.writeString(value);
        dest.writeInt(postType.ordinal());
        dest.writeStringArray(getPhrases());
        dest.writeInt(isActive() ? 1 : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RedditEntry) {
            RedditEntry e = (RedditEntry) o;
            return e.type == type
                    && e.value.equals(value)
                    && e.postType == postType
                    && Arrays.equals(e.getPhrases(), getPhrases())
                    && e.isActive() == isActive();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new Object[] {type, value, postType, Arrays.hashCode(getPhrases()), isActive()});
    }

    private RedditEntryType validateType(RedditEntryType type) {
        if (type == null) {
            throw new IllegalInputException(R.string.error_message_reddit_type);
        }
        return type;
    }

    private String validateValue(String value) {
        if (value == null) {
            throw new IllegalInputException(R.string.error_message_reddit_value);
        } if (type == RedditEntryType.USER && value.length() < 3 || value.length() > 20 || !TextUtils.inRanges(USER_CHAR_RANGES, value)) {
            throw new IllegalInputException(R.string.error_message_reddit_user);
        } else if (type == RedditEntryType.SUBREDDIT && value.length() < 3 || value.length() > 21 || !TextUtils.inRanges(SUBREDDIT_CHAR_RANGES, value)) {
            throw new IllegalInputException(R.string.error_message_reddit_subreddit);
        }
        return value;
    }

    private RedditPostType validatePostType(RedditPostType postType) {
        if (postType == null) {
            throw new IllegalInputException(R.string.error_message_reddit_post_type);
        }
        return postType;
    }

    public static final Parcelable.Creator<Entry> CREATOR = new Parcelable.Creator<Entry>() {

        @Override
        public Entry createFromParcel(Parcel in) {
            RedditEntryType type = RedditEntryType.values()[in.readInt()];
            String value = in.readString();
            RedditPostType postType = RedditPostType.values()[in.readInt()];
            String[] phrases = in.createStringArray();
            boolean active = in.readInt() != 0;
            return new RedditEntry(type, value, postType, phrases, active);
        }

        @Override
        public Entry[] newArray(int size) {
            return new Entry[size];
        }

    };

}
