package com.example.eventplannerapp;

public class Event {

    private int id;
    private String name;
    private String date;
    private String time;
    private String description;

    public Event(int id, String name, String date, String time, String description) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.description = description;
    }

    // Getter methods
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getDescription() {
        return description;
    }

    // Setter methods
    public void setName(String name) {
        this.name = name;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
