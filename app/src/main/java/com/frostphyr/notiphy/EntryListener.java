package com.frostphyr.notiphy;

import java.util.Collection;

public interface EntryListener {

    void entriesAdded(Collection<Entry> collection);

    void entryAdded(Entry entry);

    void entryRemoved(Entry entry);

}
