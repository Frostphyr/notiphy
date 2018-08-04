package com.frostphyr.notiphy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TwitterEntryIO implements EntryIO {

    @Override
    public Entry read(DataInputStream in) throws IOException {
        String username = IOUtils.readString(in);
        String phrase = IOUtils.readString(in);
        return new TwitterEntry(username, phrase);
    }

    @Override
    public void write(DataOutputStream out, Entry entry) throws IOException {
        TwitterEntry twitterEntry = (TwitterEntry) entry;
        IOUtils.writeString(out, twitterEntry.getUsername());
        IOUtils.writeString(out, twitterEntry.getPhrase());
    }

}
