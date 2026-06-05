package com.example.projecttwo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "event_tracker.db";

    /*
        Database version number.

        Version 2 added the event time column during the Software Design
        and Engineering enhancement. Increasing this number forces Android
        to run onUpgrade() when the database structure changes.

        Version 3 adds an event status column for the Database enhancement.
        Event status allows records to be managed as Upcoming, Completed,
        or Archived instead of only being permanently deleted.
     */
    private static final int DB_VERSION = 3;

    // -------- USER TABLE CONSTANTS --------
    // These constants define the users table and its column names.
    // Constants help prevent typing errors when creating queries.
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";

    // -------- EVENT TABLE CONSTANTS --------
    // These constants define the events table and its column names.
    // The events table stores the department event records shown in MainActivity.
    public static final String TABLE_EVENTS = "events";
    public static final String COL_EVENT_ID = "id";
    public static final String COL_EVENT_TITLE = "title";
    public static final String COL_EVENT_DATE = "date";
    public static final String COL_EVENT_TIME = "time";
    public static final String COL_EVENT_LOCATION = "location";
    public static final String COL_EVENT_STATUS = "status";

    // -------- EVENT STATUS VALUES --------
    // These values support the Database enhancement by allowing events to be
    // managed through status instead of only being deleted from the database.
    public static final String STATUS_UPCOMING = "Upcoming";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_ARCHIVED = "Archived";

    /*
        Constructor passes the database name and version to SQLiteOpenHelper.
        Android uses this information to create or upgrade the database.
     */
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // -------- EVENTS --------

    /*
        Inserts a new event into the events table.

        ContentValues stores column/value pairs before they are inserted into SQLite.
        This keeps the insert operation organized and avoids manually building SQL strings.

        Event time was added in a previous enhancement to make event scheduling
        more complete and realistic.

        New events are automatically marked as Upcoming. This supports the
        Database enhancement by allowing event records to be tracked by status.
     */
    public long insertEvent(String title, String date, String time, String location) {
        // Open the database in write mode because this method adds a new record.
        SQLiteDatabase db = getWritableDatabase();

        // Store event data using the correct column names before inserting the row.
        ContentValues values = new ContentValues();
        values.put(COL_EVENT_TITLE, title);
        values.put(COL_EVENT_DATE, date);
        values.put(COL_EVENT_TIME, time);
        values.put(COL_EVENT_LOCATION, location);
        values.put(COL_EVENT_STATUS, STATUS_UPCOMING);

        // Insert the event and return the new row ID, or -1 if the insert fails.
        return db.insert(TABLE_EVENTS, null, values);
    }

    /*
        Retrieves all saved events from the events table.

        The Cursor reads rows returned by the database query.
        Each row is converted into an Event object and added to an ArrayList.

        The list is returned to MainActivity, where the application can filter,
        sort, and display the events in the RecyclerView.
     */
    public List<Event> getAllEvents() {
        // Open the database in read mode because this method only retrieves records.
        SQLiteDatabase db = getReadableDatabase();
        // ArrayList stores Event objects after they are read from the database.
        List<Event> events = new ArrayList<>();

        // Query the events table and request the columns needed to build Event objects.
        Cursor cursor = db.query(
                TABLE_EVENTS,
                new String[]{
                        COL_EVENT_ID,
                        COL_EVENT_TITLE,
                        COL_EVENT_DATE,
                        COL_EVENT_TIME,
                        COL_EVENT_LOCATION,
                        COL_EVENT_STATUS
                },
                null,
                null,
                null,
                null,
                COL_EVENT_ID + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                // Read each column value from the current database row.
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_EVENT_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_TITLE));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_DATE));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_TIME));
                String location = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_LOCATION));

                // Status is read to confirm the database column exists for the enhancement.
                // The current Event model will be updated separately to display this value.
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_STATUS));

                // Convert the database row into an Event object, including status,
                // and add it to the ArrayList returned to the application.
                events.add(new Event(
                        id,
                        title,
                        date,
                        time,
                        location,
                        status
                ));

            } while (cursor.moveToNext());
        }

        // Close the Cursor to release database resources.
        cursor.close();
        // Return the completed list of Event objects to the calling activity.
        return events;
    }

    /*
        Updates the status of an existing event.

        This method supports the Database enhancement by allowing the app to
        change an event's status without deleting the record. Status-based
        updates make it possible to preserve event history for future reporting
        or archiving features.
     */
    public boolean updateEventStatus(int eventId, String status) {
        // Open the database in write mode because this method changes an existing record.
        SQLiteDatabase db = getWritableDatabase();

        // Store the new status value before updating the selected event row.
        ContentValues values = new ContentValues();
        values.put(COL_EVENT_STATUS, status);

        // Update only the row that matches the selected event ID.
        int rows = db.update(
                TABLE_EVENTS,
                values,
                COL_EVENT_ID + "=?",
                new String[]{String.valueOf(eventId)}
        );

        // Return true when at least one row was updated.
        return rows > 0;
    }
    /*
        Deletes an event using its unique database ID.

        The event ID is used in the WHERE clause so only the selected event
        is removed from the events table.
     */
    public boolean deleteEvent(int eventId) {
        // Open the database in write mode because this method removes a record.
        SQLiteDatabase db = getWritableDatabase();

        // Delete only the event row that matches the selected event ID.
        int rows = db.delete(
                TABLE_EVENTS,
                COL_EVENT_ID + "=?",
                new String[]{String.valueOf(eventId)}
        );

        // Return true when at least one row was deleted.
        return rows > 0;
    }

    /*
        Updates an existing event record.

        The selected event ID determines which row is updated.
        Time is included so edited events preserve complete scheduling information.
     */
    public boolean updateEvent(int eventId, String title, String date, String time, String location) {
        // Open the database in write mode because this method changes an existing record.
        SQLiteDatabase db = getWritableDatabase();

        // Store the updated event values before applying them to the selected row.
        ContentValues values = new ContentValues();
        values.put(COL_EVENT_TITLE, title);
        values.put(COL_EVENT_DATE, date);
        values.put(COL_EVENT_TIME, time);
        values.put(COL_EVENT_LOCATION, location);
        values.put(COL_EVENT_STATUS, STATUS_UPCOMING);

        // Update only the row that matches the selected event ID.
        int rows = db.update(
                TABLE_EVENTS,
                values,
                COL_EVENT_ID + "=?",
                new String[]{String.valueOf(eventId)}
        );

        // Return true when at least one row was updated.
        return rows > 0;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /*
            onCreate runs the first time Android creates the database.
            This method defines the original table structures for users and events.
         */

        // SQL statement used to create the users table.
        String createUsers =
                "CREATE TABLE " + TABLE_USERS + " (" +
                        COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_USERNAME + " TEXT NOT NULL UNIQUE, " +
                        COL_PASSWORD + " TEXT NOT NULL" +
                        ");";

        // SQL statement used to create the events table.
        String createEvents =
                "CREATE TABLE " + TABLE_EVENTS + " (" +
                        COL_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_EVENT_TITLE + " TEXT NOT NULL, " +
                        COL_EVENT_DATE + " TEXT NOT NULL, " +
                        COL_EVENT_TIME + " TEXT NOT NULL, " +
                        COL_EVENT_LOCATION + " TEXT NOT NULL, " +
                        COL_EVENT_STATUS + " TEXT NOT NULL DEFAULT '" + STATUS_UPCOMING + "'" +
                        ");";

        // Execute the SQL statements to create both database tables.
        db.execSQL(createUsers);
        db.execSQL(createEvents);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
            onUpgrade runs when DB_VERSION increases.

            For this course project, the simplest upgrade strategy is to drop
            and recreate the tables. In a production app, a migration would
            preserve existing user data instead of deleting records.
         */
        // Remove old table versions before rebuilding the database structure.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        // Recreate the database tables using the latest schema.
        onCreate(db);
    }

    // -------- USERS --------

    /*
        Creates a new user account in the users table.

        The username column is marked UNIQUE in the table schema,
        which helps prevent duplicate usernames.
     */
    public boolean createUser(String username, String password) {
        // Open the database in write mode because this method inserts a new user.
        SQLiteDatabase db = getWritableDatabase();

        // Store username and password values before inserting the user row.
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);

        // Insert the new user and check whether the insert succeeded.
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    /*
        Checks whether a username already exists in the users table.
        This prevents users from creating duplicate accounts with the same username.
     */
    public boolean userExists(String username) {
        // Open the database in read mode because this method only checks existing data.
        SQLiteDatabase db = getReadableDatabase();

        // Search for a row where the username matches the entered username.
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USERNAME + "=?",
                new String[]{username},
                null,
                null,
                null
        );

        // If the Cursor has a first row, the username already exists.
        boolean exists = cursor.moveToFirst();
        // Close the Cursor to release database resources.
        cursor.close();

        return exists;
    }

    /*
        Validates login credentials against the users table.

        The query checks whether a row exists with both the entered username
        and password. If a matching row is found, login is considered valid.
     */
    public boolean validateLogin(String username, String password) {
        // Open the database in read mode because this method only checks credentials.
        SQLiteDatabase db = getReadableDatabase();

        // Search for a user row where both username and password match.
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USERNAME + "=? AND " + COL_PASSWORD + "=?",
                new String[]{username, password},
                null,
                null,
                null
        );

        // If the Cursor has a first row, the entered credentials are valid.
        boolean valid = cursor.moveToFirst();
        // Close the Cursor to release database resources.
        cursor.close();

        return valid;
    }
}