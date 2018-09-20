package com.frostphyr.notiphy;

import com.frostphyr.notiphy.twitter.TwitterEntryDecoder;
import com.frostphyr.notiphy.twitter.TwitterEntryEncoder;
import com.frostphyr.notiphy.twitter.TwitterEntryViewFactory;

public enum EntryType {

    TWITTER(new TwitterEntryEncoder(), new TwitterEntryDecoder(), new TwitterEntryViewFactory());

    private final Encoder<? extends Entry> entryEncoder;
    private final Decoder<? extends Entry> entryDecoder;
    private final EntryViewFactory<?> viewFactory;

    private EntryType(Encoder<? extends Entry> entryEncoder, Decoder<? extends Entry> entryDecoder, EntryViewFactory<?> viewFactory) {
        this.entryEncoder = entryEncoder;
        this.entryDecoder = entryDecoder;
        this.viewFactory = viewFactory;
    }

    public Encoder<? extends Entry> getEntryEncoder() {
         return entryEncoder;
    }

    public Decoder<? extends Entry> getEntryDecoder() {
        return entryDecoder;
    }

    public EntryViewFactory<?> getViewFactory() {
        return viewFactory;
    }

}
