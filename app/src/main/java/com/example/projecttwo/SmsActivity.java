package com.example.projecttwo;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SmsActivity extends AppCompatActivity {

    private static final int REQ_SEND_SMS = 2001;
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_SMS_ENABLED = "sms_enabled";

    private TextView tvSmsStatus;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        tvSmsStatus = findViewById(R.id.tvSmsStatus);
        Button btnAllowSms = findViewById(R.id.btnAllowSms);
        Button btnDenySms = findViewById(R.id.btnDenySms);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        updateStatusText();

        btnAllowSms.setOnClickListener(v -> requestSmsPermission());
        btnDenySms.setOnClickListener(v -> {
            setSmsEnabled(false);
            Toast.makeText(this, "SMS notifications turned off.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @SuppressWarnings("IfCanBeSwitch")
    private void requestSmsPermission() {
        // If already granted, just enable SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            setSmsEnabled(true);
            Toast.makeText(this, "SMS permission already granted.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Request permission
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.SEND_SMS},
                REQ_SEND_SMS
        );
    }

    private void setSmsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SMS_ENABLED, enabled).apply();
        updateStatusText();
    }

    private void updateStatusText() {
        boolean enabled = prefs.getBoolean(KEY_SMS_ENABLED, false);
        if (enabled) {
            tvSmsStatus.setText(getString(R.string.sms_status_on));
        } else {
            tvSmsStatus.setText(getString(R.string.sms_status_off));
        }
    }

    @Override
    @SuppressWarnings("IfCanBeSwitch")
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setSmsEnabled(true);
                Toast.makeText(this, "SMS permission granted.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                setSmsEnabled(false);
                Toast.makeText(this, "SMS permission denied. App will continue without SMS.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}