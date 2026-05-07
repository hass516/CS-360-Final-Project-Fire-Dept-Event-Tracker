package com.example.projecttwo;

public class Event {
    private final int id;
    private final String title;
    private final String date;
    private final String location;

    public Event(int id, String title, String date, String location) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }
}