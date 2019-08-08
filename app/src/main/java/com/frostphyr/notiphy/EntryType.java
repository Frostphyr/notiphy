package com.frostphyr.notiphy;

import android.app.Activity;

import com.frostphyr.notiphy.io.JSONDecoder;
import com.frostphyr.notiphy.io.JSONEncoder;
import com.frostphyr.notiphy.reddit.RedditActivity;
import com.frostphyr.notiphy.reddit.RedditEntryDecoder;
import com.frostphyr.notiphy.reddit.RedditEntryEncoder;
import com.frostphyr.notiphy.reddit.RedditPostDecoder;
import com.frostphyr.notiphy.twitter.TweetDecoder;
import com.frostphyr.notiphy.twitter.TwitterActivity;
import com.frostphyr.notiphy.twitter.TwitterEntryDecoder;
import com.frostphyr.notiphy.twitter.TwitterEntryEncoder;

public enum EntryType {

    TWITTER(new TwitterEntryEncoder(false), new TwitterEntryDecoder(),
            new TwitterEntryEncoder(true), new TweetDecoder(),
            TwitterActivity.class, R.drawable.ic_twitter_logo, R.drawable.ic_twitter_logo_image),

    REDDIT(new RedditEntryEncoder(false), new RedditEntryDecoder(),
            new RedditEntryEncoder(true), new RedditPostDecoder(),
            RedditActivity.class, R.drawable.ic_reddit_logo, R.drawable.ic_reddit_logo_image);

    private final JSONEncoder<? extends Entry> entryEncoder;
    private final JSONDecoder<? extends Entry> entryDecoder;
    private final JSONEncoder<? extends Entry> entryTransportEncoder;
    private final JSONDecoder<? extends Message> messageDecoder;
    private final Class<? extends Activity>  activityClass;
    private final int iconResId;
    private final int iconImageResId;

    private EntryType(JSONEncoder<? extends Entry> entryEncoder, JSONDecoder<? extends Entry> entryDecoder,
                      JSONEncoder<? extends Entry> entryTransportEncoder, JSONDecoder<? extends Message> messageDecoder,
                      Class<? extends Activity> activityClass, int iconResId, int iconImageResId) {
        this.entryEncoder = entryEncoder;
        this.entryDecoder = entryDecoder;
        this.entryTransportEncoder = entryTransportEncoder;
        this.messageDecoder = messageDecoder;
        this.activityClass = activityClass;
        this.iconResId = iconResId;
        this.iconImageResId = iconImageResId;
    }

    public JSONEncoder<? extends Entry> getEntryEncoder() {
         return entryEncoder;
    }

    public JSONDecoder<? extends Entry> getEntryDecoder() {
        return entryDecoder;
    }

    public JSONEncoder<? extends Entry> getEntryTransportEncoder() {
        return entryTransportEncoder;
    }

    public JSONDecoder<? extends Message> getMessageDecoder() {
        return messageDecoder;
    }

    public Class<? extends Activity>  getActivityClass() {
        return activityClass;
    }

    public int getIconResourceId() {
        return iconResId;
    }

    public int getIconImageResourceId() {
        return iconImageResId;
    }

    public String getName() {
        String s = toString();
        return s.charAt(0) + s.substring(1, s.length()).toLowerCase();
    }

}
