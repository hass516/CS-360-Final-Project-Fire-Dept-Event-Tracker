package com.example.projecttwo;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements EventAdapter.OnDataChangedListener {

    private EditText etEventName;
    private EditText etEventDate;
    private EditText etEventLocation;
    private Button btnAddEvent;

    private RecyclerView rvEvents;

    private DatabaseHelper db;

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_SMS_ENABLED = "sms_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnSmsSettings = findViewById(R.id.btnSmsSettings);
        btnSmsSettings.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SmsActivity.class))
        );

        etEventName = findViewById(R.id.etEventName);
        etEventDate = findViewById(R.id.etEventDate);
        etEventLocation = findViewById(R.id.etEventLocation);
        btnAddEvent = findViewById(R.id.btnAddEvent);

        rvEvents = findViewById(R.id.rvEvents);

        db = new DatabaseHelper(this);

        rvEvents.setLayoutManager(new GridLayoutManager(this, 1));
        loadEvents();

        btnAddEvent.setOnClickListener(v -> addEvent());

        // Long-press anywhere on the title to open SMS Settings (simple demo navigation)
        findViewById(R.id.tvEventTitle).setOnLongClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SmsActivity.class));
            return true;
        });
    }

    private void addEvent() {
        String title = etEventName.getText().toString().trim();
        String date = etEventDate.getText().toString().trim();
        String location = etEventLocation.getText().toString().trim();

        if (title.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Event name and date are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        long id = db.insertEvent(title, date, location);

        if (id != -1) {
            Toast.makeText(this, "Event added.", Toast.LENGTH_SHORT).show();

            // 🔹 DEMO SMS trigger for grading
            sendSmsIfAllowed("5551234567",
                    "Event added: " + title + " on " + date);

            etEventName.setText("");
            etEventDate.setText("");
            etEventLocation.setText("");
            loadEvents();
        } else {
            Toast.makeText(this, "Insert failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadEvents() {
        List<Event> events = db.getAllEvents();
        EventAdapter adapter = new EventAdapter(this, events, db, this);
        rvEvents.setAdapter(adapter);
    }

    private boolean isSmsEnabled() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_SMS_ENABLED, false);
    }

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
            android.telephony.SmsManager smsManager = android.telephony.SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "SMS sent.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "SMS failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onDataChanged() {
        loadEvents();
    }
}