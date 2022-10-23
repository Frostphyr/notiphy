package com.frostphyr.notiphy;

public enum MediaType implements SpinnerItem {

    OPTIONAL(new IconResource(0, R.string.optional)),
    ANY(new IconResource(R.drawable.ic_media, R.string.any)),
    NONE(new IconResource(R.drawable.ic_no_media, R.string.none)),
    IMAGE(new IconResource(R.drawable.ic_image, R.string.image)),
    VIDEO(new IconResource(R.drawable.ic_video, R.string.video));

    private final IconResource iconResource;

    MediaType(IconResource iconResource) {
        this.iconResource = iconResource;
    }

    public IconResource getIconResource() {
        return iconResource;
    }

}
