package com.frostphyr.notiphy.reddit;

public enum RedditEntryType {

    USER("u/"),
    SUBREDDIT("r/");

    private final String prefix;

    private RedditEntryType(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

}
