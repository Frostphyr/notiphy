package com.frostphyr.notiphy;

public enum MediaType implements IconResource {

    OPTIONAL(-1),
    ANY(R.drawable.ic_media),
    NONE(R.drawable.ic_no_media),
    IMAGE(R.drawable.ic_image),
    VIDEO(R.drawable.ic_video);

    private final int iconResId;

    private MediaType(int iconResId) {
        this.iconResId = iconResId;
    }

    @Override
    public int getIconResourceId() {
        return iconResId;
    }

}
