package com.willsprogrammer.onlinefriendlynotepad;

public class Notes {

    public String title;
    public String notes;
    public String date_time;
    public String favourite;

    public Notes() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Notes(String title, String notes, String date_time, String favourite) {
        this.title = title;
        this.notes = notes;
        this.date_time = date_time;
        this.favourite = favourite;
    }
}
