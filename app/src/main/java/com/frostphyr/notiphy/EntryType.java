package com.frostphyr.notiphy;

import android.app.Activity;

import com.frostphyr.notiphy.reddit.RedditActivity;
import com.frostphyr.notiphy.reddit.RedditEntry;
import com.frostphyr.notiphy.reddit.RedditPostDecoder;
import com.frostphyr.notiphy.twitter.TweetDecoder;
import com.frostphyr.notiphy.twitter.TwitterActivity;
import com.frostphyr.notiphy.twitter.TwitterEntry;

public enum EntryType {

    TWITTER(TwitterEntry.class, TwitterActivity.class, new TweetDecoder(),
            new IconResource(R.drawable.ic_twitter_logo, R.string.twitter)),

    REDDIT(RedditEntry.class, RedditActivity.class, new RedditPostDecoder(),
            new IconResource(R.drawable.ic_reddit_logo, R.string.reddit));

    private final Class<? extends Entry> entryClass;
    private final Class<? extends Activity>  activityClass;
    private final MessageDecoder messageDecoder;
    private final IconResource iconResource;

    EntryType(Class<? extends Entry> entryClass, Class<? extends Activity> activityClass,
              MessageDecoder messageDecoder, IconResource iconResource) {
        this.entryClass = entryClass;
        this.activityClass = activityClass;
        this.messageDecoder = messageDecoder;
        this.iconResource = iconResource;
    }

    public Class<? extends Entry> getEntryClass() {
        return entryClass;
    }

    public Class<? extends Activity>  getActivityClass() {
        return activityClass;
    }

    public MessageDecoder getMessageDecoder() {
        return messageDecoder;
    }

    public IconResource getIconResource() {
        return iconResource;
    }

}
