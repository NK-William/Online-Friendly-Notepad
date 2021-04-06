package com.willsprogrammer.onlinefriendlynotepad;

public class Favourites {

    public String title;
    public String notes;
    public String date_time;

    public Favourites() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Favourites(String title, String notes, String date_time) {
        this.title = title;
        this.notes = notes;
        this.date_time = date_time;
    }
}
