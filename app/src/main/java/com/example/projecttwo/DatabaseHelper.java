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
    private static final int DB_VERSION = 1;

    // Users table
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";

    // Events table (we will use this in MainActivity later)
    public static final String TABLE_EVENTS = "events";
    public static final String COL_EVENT_ID = "id";
    public static final String COL_EVENT_TITLE = "title";
    public static final String COL_EVENT_DATE = "date";
    public static final String COL_EVENT_LOCATION = "location";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    // -------- EVENTS --------

    public long insertEvent(String title, String date, String location) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_EVENT_TITLE, title);
        values.put(COL_EVENT_DATE, date);
        values.put(COL_EVENT_LOCATION, location);

        return db.insert(TABLE_EVENTS, null, values);
    }

    public List<Event> getAllEvents() {
        SQLiteDatabase db = getReadableDatabase();
        List<Event> events = new ArrayList<>();

        Cursor cursor = db.query(
                TABLE_EVENTS,
                new String[]{COL_EVENT_ID, COL_EVENT_TITLE, COL_EVENT_DATE, COL_EVENT_LOCATION},
                null,
                null,
                null,
                null,
                COL_EVENT_ID + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_EVENT_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_TITLE));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_DATE));
                String location = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENT_LOCATION));

                events.add(new Event(id, title, date, location));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return events;
    }

    public boolean deleteEvent(int eventId) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_EVENTS, COL_EVENT_ID + "=?", new String[]{eventId + ""});
        return rows > 0;
    }

    public boolean updateEvent(int eventId, String title, String date, String location) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_EVENT_TITLE, title);
        values.put(COL_EVENT_DATE, date);
        values.put(COL_EVENT_LOCATION, location);

        int rows = db.update(TABLE_EVENTS, values, COL_EVENT_ID + "=?", new String[]{eventId + ""});
        return rows > 0;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

        String createUsers =
                "CREATE TABLE " + TABLE_USERS + " (" +
                        COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_USERNAME + " TEXT NOT NULL UNIQUE, " +
                        COL_PASSWORD + " TEXT NOT NULL" +
                        ");";

        String createEvents =
                "CREATE TABLE " + TABLE_EVENTS + " (" +
                        COL_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_EVENT_TITLE + " TEXT NOT NULL, " +
                        COL_EVENT_DATE + " TEXT NOT NULL, " +
                        COL_EVENT_LOCATION + " TEXT" +
                        ");";

        db.execSQL(createUsers);
        db.execSQL(createEvents);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For this course project, simplest upgrade is drop and recreate
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // -------- USERS --------

    public boolean createUser(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean userExists(String username) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USERNAME + "=?",
                new String[]{username},
                null, null, null
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean validateLogin(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USERNAME + "=? AND " + COL_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null
        );

        boolean valid = cursor.moveToFirst();
        cursor.close();
        return valid;
    }
}