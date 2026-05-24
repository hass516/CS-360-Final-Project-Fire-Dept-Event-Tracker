package com.example.projecttwo;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements EventAdapter.OnDataChangedListener {

    /*
        Regular expression used to validate event dates.
        This checks that dates are entered in MM/DD/YYYY format.
     */
    private static final Pattern DATE_PATTERN =
            Pattern.compile("^(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])/\\d{4}$");

    /*
        Regular expression used to validate event times.
        This checks that times are entered in HH:MM AM/PM format.
        Examples: 07:30 PM, 09:00 AM, 12:15 PM
     */
    private static final Pattern TIME_PATTERN =
            Pattern.compile("^(1[0-2]|[1-9]):[0-5][0-9]\\s?(AM|PM|am|pm)$");

    // Input fields used to collect event details from the user
    private EditText etEventName;
    private EditText etEventDate;
    private EditText etEventTime;
    private EditText etEventLocation;

    private TextView tvEmptyEvents;

    // RecyclerView displays saved events
    private RecyclerView rvEvents;

    // Database helper manages SQLite event and user data
    private DatabaseHelper db;

    // SharedPreferences keys used for SMS settings
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_SMS_ENABLED = "sms_enabled";

    /*
        Demo phone number used for the course project SMS feature.
        In a production app, this would be replaced with a user-selected or verified phone number.
     */
    private static final String DEMO_PHONE_NUMBER = "5551234567";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Connects this activity to the main screen layout
        setContentView(R.layout.activity_main);

        tvEmptyEvents = findViewById(R.id.tvEmptyEvents);

        // Initialize SMS settings button and navigation
        Button btnSmsSettings = findViewById(R.id.btnSmsSettings);
        btnSmsSettings.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SmsActivity.class))
        );

        // Connect Java variables to UI fields in activity_main.xml
        etEventName = findViewById(R.id.etEventName);
        etEventDate = findViewById(R.id.etEventDate);
        etEventTime = findViewById(R.id.etEventTime);
        etEventLocation = findViewById(R.id.etEventLocation);

        Button btnAddEvent = findViewById(R.id.btnAddEvent);

        // Initialize RecyclerView
        rvEvents = findViewById(R.id.rvEvents);

        // Initialize database helper
        db = new DatabaseHelper(this);

        /*
            GridLayoutManager with one column behaves like a vertical list.
            This keeps event cards organized and easy to read.
         */
        rvEvents.setLayoutManager(new GridLayoutManager(this, 1));

        // Load saved events when the screen opens
        loadEvents();

        // Add event when the user taps the Add Event button
        btnAddEvent.setOnClickListener(v -> addEvent());

        /*
            Long-press anywhere on the title to open SMS settings.
            This was kept as a simple demo navigation feature.
         */
        findViewById(R.id.tvEventTitle).setOnLongClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SmsActivity.class));
            return true;
        });
    }

    /*
        Handles adding a new event.
        This method uses helper methods for cleaner validation and maintainability.
     */
    private void addEvent() {

        // Retrieve cleaned input from the form
        String title = getEventTitleInput();
        String date = getEventDateInput();
        String time = getEventTimeInput();
        time = formatEventTime(time);
        String location = getEventLocationInput();

        // Stop the insert process if validation fails
        if (!validateEventInput(title, date, time, location)) {
            return;
        }

        // Insert validated event data into the SQLite database
        long id = db.insertEvent(title, date, time, location);

        if (id != -1) {

            Toast.makeText(this, "Event added.", Toast.LENGTH_SHORT).show();

            /*
                Demo SMS trigger for the course project.
                Sends an alert only if SMS is enabled and permission is granted.
             */
            sendSmsIfAllowed(
                    DEMO_PHONE_NUMBER,
                    "Event added: " + title + " on " + date + " at " + time
            );

            // Reset form fields after successful insert
            clearEventInputFields();

            // Refresh RecyclerView so the new event appears
            loadEvents();

        } else {

            Toast.makeText(this, "Insert failed.", Toast.LENGTH_SHORT).show();
        }
    }

    /*
        Retrieves and trims event title input.
     */
    private String getEventTitleInput() {
        return etEventName.getText().toString().trim();
    }

    /*
        Retrieves and trims event date input.
     */
    private String getEventDateInput() {
        return etEventDate.getText().toString().trim();
    }

    /*
        Retrieves and trims event time input.
     */
    private String getEventTimeInput() {
        return etEventTime.getText().toString().trim();
    }

    /*
        Retrieves and trims event location or notes input.
     */
    private String getEventLocationInput() {
        return etEventLocation.getText().toString().trim();
    }

    /*
        Validates event form input before database insertion.
        This prevents incomplete or incorrectly formatted event records.
     */
    private boolean validateEventInput(String title, String date, String time, String location) {

        if (title.isEmpty()) {
            etEventName.setError("Event name is required.");
            etEventName.requestFocus();
            return false;
        }

        if (date.isEmpty()) {
            etEventDate.setError("Event date is required.");
            etEventDate.requestFocus();
            return false;
        }

        // Verify date follows MM/DD/YYYY format
        if (!DATE_PATTERN.matcher(date).matches()) {
            etEventDate.setError("Use MM/DD/YYYY format.");
            etEventDate.requestFocus();
            return false;
        }

        /*
            Convert the entered string into a real Date object.
            setLenient(false) prevents invalid dates like 13/99/2026.
         */
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            sdf.setLenient(false);

            Date enteredDate = sdf.parse(date);

            Calendar enteredCalendar = Calendar.getInstance();
            enteredCalendar.setTime(enteredDate);
            resetTimeToStartOfDay(enteredCalendar);

            Calendar todayCalendar = Calendar.getInstance();
            resetTimeToStartOfDay(todayCalendar);

            /*
                Prevent users from creating events before today's date.
                Today's date is allowed, but yesterday and older dates are rejected.
             */
            if (enteredCalendar.before(todayCalendar)) {
                etEventDate.setError("Event date cannot be in the past.");
                etEventDate.requestFocus();
                return false;
            }

        } catch (ParseException e) {
            etEventDate.setError("Invalid date.");
            etEventDate.requestFocus();
            return false;
        }

        if (time.isEmpty()) {
            etEventTime.setError("Event time is required.");
            etEventTime.requestFocus();
            return false;
        }

        // Verify time follows HH:MM AM/PM format
        if (!TIME_PATTERN.matcher(time).matches()) {
            etEventTime.setError("Use HH:MM AM/PM format.");
            etEventTime.requestFocus();
            return false;
        }

        if (location.isEmpty()) {
            etEventLocation.setError("Location or notes are required.");
            etEventLocation.requestFocus();
            return false;
        }

        return true;
    }

    /*
        Clears event form fields after a successful event insert.
     */
    private void clearEventInputFields() {
        etEventName.setText("");
        etEventDate.setText("");
        etEventTime.setText("");
        etEventLocation.setText("");
        etEventName.requestFocus();
    }

    /*
        Loads all events from the database and displays them in the RecyclerView.
     */
    private void loadEvents() {
        List<Event> events = db.getAllEvents();
        if (events.isEmpty()) {
            tvEmptyEvents.setVisibility(View.VISIBLE);
        } else {
            tvEmptyEvents.setVisibility(View.GONE);
        }
        EventAdapter adapter = new EventAdapter(this, events, db, this);
        rvEvents.setAdapter(adapter);
    }

    /*
        Checks whether the user enabled SMS notifications in SharedPreferences.
     */
    private boolean isSmsEnabled() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_SMS_ENABLED, false);
    }

    /*
        Sends an SMS message only when SMS is enabled and permission has been granted.
        This method protects the app from attempting unauthorized SMS actions.
     */
    @SuppressWarnings("SameParameterValue")
    private void sendSmsIfAllowed(String phoneNumber, String message) {

        if (!isSmsEnabled()) {
            Toast.makeText(this, "SMS is off. Enable it in SMS settings.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "SMS permission not granted.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {

            android.telephony.SmsManager smsManager =
                    getSystemService(android.telephony.SmsManager.class);

            smsManager.sendTextMessage(phoneNumber, null, message, null, null);

            Toast.makeText(this, "SMS sent.", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {

            Toast.makeText(this, "SMS failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /*
        Called by EventAdapter when event data changes.
        Reloads the list so the UI stays up to date.
     */
    @Override
    public void onDataChanged() {
        loadEvents();
    }

    /*
        Resets a Calendar object to midnight so date comparisons ignore time of day.
        This allows today's date to pass validation while blocking past dates.
     */
    private void resetTimeToStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
    private String formatEventTime(String time) {
        String cleanedTime = time.trim().toUpperCase(Locale.US);
        cleanedTime = cleanedTime.replaceAll("\\s*AM", " AM");
        cleanedTime = cleanedTime.replaceAll("\\s*PM", " PM");
        cleanedTime = cleanedTime.replaceAll("\\s+", " ");

        if (cleanedTime.startsWith("0")) {
            cleanedTime = cleanedTime.substring(1);
        }
        return cleanedTime;
    }
}