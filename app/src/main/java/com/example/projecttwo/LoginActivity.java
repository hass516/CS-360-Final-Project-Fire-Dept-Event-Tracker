package com.example.projecttwo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    /*
        Minimum password length requirement.
        Using a constant improves maintainability because
        the value only needs to be changed in one place.
     */
    private static final int MIN_PASSWORD_LENGTH = 6;

    // Input fields for username and password
    private EditText etUsername;
    private EditText etPassword;

    // Database helper object used for login and account creation
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Connects this activity to the XML layout file
        setContentView(R.layout.activity_login);

        // Connect Java variables to UI components in activity_login.xml
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);

        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);

        // Initialize database helper
        db = new DatabaseHelper(this);

        /*
            Button listeners trigger methods when the user taps a button.
            Keeps onCreate() cleaner and improves readability.
         */
        btnCreateAccount.setOnClickListener(v -> handleCreateAccount());
        btnLogin.setOnClickListener(v -> handleLogin());
    }

    /*
        Handles new account creation.
        Includes validation, duplicate username checking,
        database insertion, and user feedback.
     */
    private void handleCreateAccount() {

        // Retrieve cleaned user input
        String username = getUsernameInput();
        String password = getPasswordInput();

        /*
            Validate username and password.
            "true" means we are creating an account,
            so password length rules should apply.
         */
        if (!validateCredentials(username, password, true)) {
            return;
        }

        // Prevent duplicate usernames
        if (db.userExists(username)) {

            // Display field-level error
            etUsername.setError("Username already exists.");

            Toast.makeText(
                    this,
                    "That username already exists. Try logging in.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Attempt to create account in database
        boolean created = db.createUser(username, password);

        if (created) {

            Toast.makeText(
                    this,
                    "Account created. You can log in now.",
                    Toast.LENGTH_SHORT
            ).show();

            // Clears text fields after successful account creation
            clearInputFields();

        } else {

            Toast.makeText(
                    this,
                    "Account creation failed.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /*
        Handles login attempts.
        Validates user input and checks credentials against database.
     */
    private void handleLogin() {

        // Retrieve cleaned user input
        String username = getUsernameInput();
        String password = getPasswordInput();

        /*
            "false" means this is NOT account creation,
            so minimum password rules are skipped.
         */
        if (!validateCredentials(username, password, false)) {
            return;
        }

        // Check if login credentials are valid
        boolean valid = db.validateLogin(username, password);

        if (valid) {

            Toast.makeText(
                    this,
                    "Login successful.",
                    Toast.LENGTH_SHORT
            ).show();

            /*
                Navigate to MainActivity after successful login.
                finish() prevents returning to login screen with back button.
             */
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

        } else {

            // Field-specific error message
            etPassword.setError("Invalid username or password.");

            Toast.makeText(
                    this,
                    "Invalid username or password.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /*
        Retrieves username input and removes unnecessary spaces.
        Centralized helper method improves code reuse.
     */
    private String getUsernameInput() {
        return etUsername.getText().toString().trim();
    }

    /*
        Retrieves password input and removes unnecessary spaces.
     */
    private String getPasswordInput() {
        return etPassword.getText().toString().trim();
    }

    /*
        Shared validation method used for both:
        - account creation
        - login attempts

        This avoids duplicate validation logic and improves maintainability.
     */
    private boolean validateCredentials(
            String username,
            String password,
            boolean creatingAccount
    ) {

        // Username cannot be blank
        if (username.isEmpty()) {
            etUsername.setError("Username is required.");
            etUsername.requestFocus();

            return false;
        }

        // Password cannot be blank
        if (password.isEmpty()) {
            etPassword.setError("Password is required.");
            etPassword.requestFocus();

            return false;
        }

        /*
            Password length validation only applies
            during account creation.
         */
        if (creatingAccount && password.length() < MIN_PASSWORD_LENGTH) {
            etPassword.setError(
                    "Password must be at least "
                            + MIN_PASSWORD_LENGTH
                            + " characters."
            );
            etPassword.requestFocus();
            return false;
        }

        // Validation passed
        return true;
    }

    /*
        Clears both text fields after successful account creation.
        Improves user experience and prepares form for next use.
     */
    private void clearInputFields() {

        etUsername.setText("");
        etPassword.setText("");

        // Places cursor back into username field
        etUsername.requestFocus();
    }
}