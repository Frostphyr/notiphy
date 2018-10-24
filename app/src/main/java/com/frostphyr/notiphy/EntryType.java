package com.frostphyr.notiphy;

import com.frostphyr.notiphy.io.JSONDecoder;
import com.frostphyr.notiphy.io.JSONEncoder;
import com.frostphyr.notiphy.twitter.TweetDecoder;
import com.frostphyr.notiphy.twitter.TwitterEntryDecoder;
import com.frostphyr.notiphy.twitter.TwitterEntryEncoder;
import com.frostphyr.notiphy.twitter.TwitterEntryViewFactory;

public enum EntryType {

    TWITTER(new TwitterEntryEncoder(false), new TwitterEntryDecoder(),
            new TwitterEntryEncoder(true), new TweetDecoder(),
            new TwitterEntryViewFactory(), R.drawable.ic_twitter_logo);

    private final JSONEncoder<? extends Entry> entryEncoder;
    private final JSONDecoder<? extends Entry> entryDecoder;
    private final JSONEncoder<? extends Entry> entryTransportEncoder;
    private final JSONDecoder<? extends Message> messageDecoder;
    private final EntryViewFactory<?> viewFactory;
    private final int iconResId;

    private EntryType(JSONEncoder<? extends Entry> entryEncoder, JSONDecoder<? extends Entry> entryDecoder,
                      JSONEncoder<? extends Entry> entryTransportEncoder, JSONDecoder<? extends Message> messageDecoder,
                      EntryViewFactory<?> viewFactory, int iconResId) {
        this.entryEncoder = entryEncoder;
        this.entryDecoder = entryDecoder;
        this.entryTransportEncoder = entryTransportEncoder;
        this.messageDecoder = messageDecoder;
        this.viewFactory = viewFactory;
        this.iconResId = iconResId;
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

    public EntryViewFactory<?> getViewFactory() {
        return viewFactory;
    }

    public int getIconResourceId() {
        return iconResId;
    }

    public String getName() {
        String s = toString();
        return s.charAt(0) + s.substring(1, s.length()).toLowerCase();
    }

}
