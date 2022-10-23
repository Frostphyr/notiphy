package com.frostphyr.notiphy;

public enum MatureContent implements SpinnerItem {

    SHOW(new IconResource(R.drawable.ic_visibility, R.string.show)),
    HIDE(new IconResource(R.drawable.ic_hide, R.string.hide)),
    BLOCK(new IconResource(R.drawable.ic_block, R.string.block));

    private final IconResource iconResource;

    MatureContent(IconResource iconResource) {
        this.iconResource = iconResource;
    }

    @Override
    public IconResource getIconResource() {
        return iconResource;
    }

}
