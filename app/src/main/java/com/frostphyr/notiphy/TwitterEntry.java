package com.frostphyr.notiphy;

import android.view.LayoutInflater;
import android.view.View;

public class TwitterEntry implements Entry {

    private String username;
    private String phrase;

    public TwitterEntry(String username, String phrase) {
        this.username = username;
        this.phrase = phrase;
    }

    public String getUsername() {
        return username;
    }

    public String getPhrase() {
        return phrase;
    }

    @Override
    public View createView(LayoutInflater inflater) {
        return null;
    }

}
