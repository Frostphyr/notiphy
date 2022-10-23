package com.frostphyr.notiphy;

public class IconResource {

    private final int drawableResId;
    private final int stringResId;

    public IconResource(int drawableResId, int stringResId) {
        this.drawableResId = drawableResId;
        this.stringResId = stringResId;
    }

    public int getDrawableResId() {
        return drawableResId;
    }

    public int getStringResId() {
        return stringResId;
    }

}
