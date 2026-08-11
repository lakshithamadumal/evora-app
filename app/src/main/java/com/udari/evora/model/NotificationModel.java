package com.udari.evora.model;

public class NotificationModel {
    private String title, body, timestamp, userId;
    private boolean read;

    public NotificationModel() {
    }

    public NotificationModel(String title, String body, String timestamp, String userId, boolean read) {
        this.title = title;
        this.body = body;
        this.timestamp = timestamp;
        this.userId = userId;
        this.read = read;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
