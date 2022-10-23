package com.frostphyr.notiphy.reddit;

import com.frostphyr.notiphy.IconResource;
import com.frostphyr.notiphy.R;
import com.frostphyr.notiphy.SpinnerItem;

public enum RedditPostType implements SpinnerItem {

    ANY(new IconResource(0, R.string.any)),
    TEXT(new IconResource(R.drawable.ic_text, R.string.text)),
    LINK(new IconResource(R.drawable.ic_link, R.string.link));

    private final IconResource iconResource;

    RedditPostType(IconResource iconResource) {
        this.iconResource = iconResource;
    }

    @Override
    public IconResource getIconResource() {
        return iconResource;
    }

}
