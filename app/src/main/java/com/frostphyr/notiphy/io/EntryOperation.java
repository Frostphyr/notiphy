package com.frostphyr.notiphy.io;

import com.frostphyr.notiphy.Entry;

import java.util.Collection;

public class EntryOperation {

    private Collection<Entry> added;
    private Collection<Entry> removed;

    public EntryOperation(Collection<Entry> added, Collection<Entry> removed) {
        this.added = added;
        this.removed = removed;
    }

    public Collection<Entry> getAdded() {
        return added;
    }

    public Collection<Entry> getRemoved() {
        return removed;
    }

}
