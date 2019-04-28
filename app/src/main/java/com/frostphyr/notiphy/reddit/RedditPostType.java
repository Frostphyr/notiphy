package com.frostphyr.notiphy.reddit;

import com.frostphyr.notiphy.IconResource;
import com.frostphyr.notiphy.R;

public enum RedditPostType implements IconResource {

    ANY(-1),
    TEXT(R.drawable.ic_text),
    LINK(R.drawable.ic_link);

    private int iconResId;

    private RedditPostType(int iconResId) {
        this.iconResId = iconResId;
    }

    @Override
    public int getIconResourceId() {
        return iconResId;
    }

}
