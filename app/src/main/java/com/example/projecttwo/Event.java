package com.example.projecttwo;

/*
    Event object represents a single event record stored in SQLite.

    This class acts as a model between the database layer and the user interface.
    The Database enhancement adds event status tracking so events can be marked
    as Upcoming, Completed, or Archived instead of only being deleted.
 */
public class Event {

    private final int id;
    private final String title;
    private final String date;
    private final String time;
    private final String location;
    private final String status;

    public Event(int id,
                 String title,
                 String date,
                 String time,
                 String location,
                 String status) {

        this.id = id;
        this.title = title;
        this.date = date;
        this.time = time;
        this.location = location;
        this.status = status;
    }

    /*
        Returns the unique database ID for the event.
     */
    public int getId() {
        return id;
    }

    /*
        Returns the event title.
     */
    public String getTitle() {
        return title;
    }

    /*
        Returns the event date.
     */
    public String getDate() {
        return date;
    }

    /*
        Returns the event time.
     */
    public String getTime() {
        return time;
    }

    /*
        Returns the event location or notes.
     */
    public String getLocation() {
        return location;
    }

    /*
        Returns the current event status.

        Possible values:
        - Upcoming
        - Completed
        - Archived
     */
    public String getStatus() {
        return status;
    }
}