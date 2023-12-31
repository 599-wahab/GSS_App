package com.example.gss;

public class Event {
    private int id;
    private String eventName;
    private String imagePath;

    public Event(int id, String eventName, String imagePath) {
        this.id = id;
        this.eventName = eventName;
        this.imagePath = imagePath;
    }

    public int getId() {
        return id;
    }

    public String getEventName() {
        return eventName;
    }

    public String getImagePath() {
        return imagePath;
    }
}
