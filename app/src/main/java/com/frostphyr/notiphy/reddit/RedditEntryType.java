package com.frostphyr.notiphy.reddit;

import com.frostphyr.notiphy.IconResource;
import com.frostphyr.notiphy.R;
import com.frostphyr.notiphy.SpinnerItem;

public enum RedditEntryType implements SpinnerItem {

    USER(new IconResource(0, R.string.user), "u/", 20, new char[][] {
            {'a', 'z'},
            {'A', 'Z'},
            {'0', '9'},
            {'-'},
            {'_'}
    }),

    SUBREDDIT(new IconResource(0, R.string.subreddit), "r/", 21, new char[][] {
            {'a', 'z'},
            {'A', 'Z'},
            {'0', '9'},
            {'_'}
    });

    private final IconResource iconResource;
    private final String prefix;
    private final int charLimit;
    private final char[][] charRanges;

    RedditEntryType(IconResource iconResource, String prefix, int charLimit, char[][] charRanges) {
        this.iconResource = iconResource;
        this.prefix = prefix;
        this.charLimit = charLimit;
        this.charRanges = charRanges;
    }

    @Override
    public IconResource getIconResource() {
        return iconResource;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getCharLimit() {
        return charLimit;
    }

    public char[][] getCharRanges() {
        return charRanges;
    }

}
