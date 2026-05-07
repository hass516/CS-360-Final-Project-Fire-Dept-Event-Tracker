package com.example.projecttwo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnCreateAccount;

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        db = new DatabaseHelper(this);

        btnCreateAccount.setOnClickListener(v -> handleCreateAccount());
        btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleCreateAccount() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter a username and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (db.userExists(username)) {
            Toast.makeText(this, "That username already exists. Try logging in.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean created = db.createUser(username, password);
        if (created) {
            Toast.makeText(this, "Account created. You can log in now.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Account creation failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter a username and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean valid = db.validateLogin(username, password);
        if (valid) {
            Toast.makeText(this, "Login successful.", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

        } else {
            Toast.makeText(this, "Invalid username or password.", Toast.LENGTH_SHORT).show();
        }
    }
}