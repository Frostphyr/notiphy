package com.frostphyr.notiphy;

import java.util.Date;

public class Message {

    private EntryType type;
    private Date createdAt;
    private String username;
    private String title;
    private String text;
    private Media[] media;

    public Message(EntryType type, Date createdAt, String username, String title, String text, Media[] media) {
        this.type = type;
        this.createdAt = createdAt;
        this.username = username;
        this.text = text;
        this.media = media;
    }

    public EntryType getType() {
        return type;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getUsername() {
        return username;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public Media[] getMedia() {
        return media;
    }

}
