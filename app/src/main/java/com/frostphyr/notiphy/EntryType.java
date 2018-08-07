package com.frostphyr.notiphy;

import com.frostphyr.notiphy.io.EntryJSONSerializer;
import com.frostphyr.notiphy.io.TwitterEntryJSONSerializer;

public enum EntryType {

    TWITTER(new TwitterEntryJSONSerializer());

    private final EntryJSONSerializer jsonSerializer;

    private EntryType(EntryJSONSerializer jsonSerializer) {
        this.jsonSerializer = jsonSerializer;
    }

    public EntryJSONSerializer getJSONSerializer() {
         return jsonSerializer;
    }

}
