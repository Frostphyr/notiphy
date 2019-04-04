package com.frostphyr.notiphy;

import java.util.Date;

public class Message {

    private EntryType type;
    private Date createdAt;
    private String title;
    private String description;
    private String text;
    private String url;
    private Media[] media;

    private Message() {
    }

    public EntryType getType() {
        return type;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getText() {
        return text;
    }

    public String getUrl() {
        return url;
    }

    public Media[] getMedia() {
        return media;
    }

    public static class Builder {

        private Message message = new Message();

        public Builder setType(EntryType type) {
            message.type = type;
            return this;
        }

        public Builder setCreatedAt(Date createdAt) {
            message.createdAt = createdAt;
            return this;
        }

        public Builder setTitle(String title) {
            message.title = title;
            return this;
        }

        public Builder setDescription(String description) {
            message.description = description;
            return this;
        }

        public Builder setText(String text) {
            message.text = text;
            return this;
        }

        public Builder setUrl(String url) {
            message.url = url;
            return this;
        }

        public Builder setMedia(Media[] media) {
            message.media = media;
            return this;
        }

        public Message build() {
            Message m = message;
            message = null;
            return m;
        }

    }

}
