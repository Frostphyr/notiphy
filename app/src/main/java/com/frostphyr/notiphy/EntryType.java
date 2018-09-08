package com.frostphyr.notiphy;

import com.frostphyr.notiphy.io.EntryJSONSerializer;
import com.frostphyr.notiphy.twitter.TwitterEntryJSONSerializer;
import com.frostphyr.notiphy.twitter.TwitterEntryViewFactory;

public enum EntryType {

    TWITTER(new TwitterEntryJSONSerializer(), new TwitterEntryViewFactory());

    private final EntryJSONSerializer jsonSerializer;
    private final EntryViewFactory<?> viewFactory;

    private EntryType(EntryJSONSerializer jsonSerializer, EntryViewFactory<?> viewFactory) {
        this.jsonSerializer = jsonSerializer;
        this.viewFactory = viewFactory;
    }

    public EntryJSONSerializer getJSONSerializer() {
         return jsonSerializer;
    }

    public EntryViewFactory<?> getViewFactory() {
        return viewFactory;
    }

}
