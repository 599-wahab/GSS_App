package com.example.gss;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UserRegistrationActivity extends AppCompatActivity {
    private EditText emailEditText;
    private EditText phoneEditText;
    private EditText usernameEditText;
    private EditText confirmPasswordEditText;
    private AppDatabase appDatabase;
    private final String[] countries = {"(+60)", "(+92)", "(+91)", "(+212)", "(+977)", "(+7)", "(+880)", "(+973)", "(+32)", "(+975)", "(+1)", "(+236)", "(+86)", "(+57)", "(+357)", "(+45)","(+33)","(+49)","(+995)","(+964)","(+62)"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_registration);

        appDatabase = new AppDatabase(this);
        final EditText passwordEditText = findViewById(R.id.password);

        ImageView passwordVisibilityToggle = findViewById(R.id.passwordVisibilityToggle);
        ImageView confirmPasswordVisibilityToggle = findViewById(R.id.confirmPasswordVisibilityToggle);

        emailEditText = findViewById(R.id.email);
        phoneEditText = findViewById(R.id.phoneNumber);
        usernameEditText = findViewById(R.id.username);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        Button registerButton = findViewById(R.id.registerButton);

        passwordVisibilityToggle.setOnClickListener(v -> {
            if (passwordEditText.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
                passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                confirmPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordVisibilityToggle.setImageResource(R.drawable.ic_baseline_visibility_off_24);
                confirmPasswordVisibilityToggle.setImageResource(R.drawable.ic_baseline_visibility_off_24);
                int position = passwordEditText.length();
                passwordEditText.setSelection(position);
            } else {
                passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                confirmPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordVisibilityToggle.setImageResource(R.drawable.ic_baseline_visibility_24);
                confirmPasswordVisibilityToggle.setImageResource(R.drawable.ic_baseline_visibility_24);
                int position = passwordEditText.length();
                passwordEditText.setSelection(position);
            }
        });

        confirmPasswordVisibilityToggle.setOnClickListener(v -> {
            if (confirmPasswordEditText.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
                confirmPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                confirmPasswordVisibilityToggle.setImageResource(R.drawable.ic_baseline_visibility_off_24);
                int position = confirmPasswordEditText.length();
                confirmPasswordEditText.setSelection(position);
            } else {
                confirmPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                confirmPasswordVisibilityToggle.setImageResource(R.drawable.ic_baseline_visibility_24);
                int position = confirmPasswordEditText.length();
                confirmPasswordEditText.setSelection(position);
            }
        });

        Spinner countryCodeSpinner = findViewById(R.id.countryCodeSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countryCodeSpinner.setAdapter(adapter);

        registerButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String phone = phoneEditText.getText().toString();
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();
            String countryCode = countryCodeSpinner.getSelectedItem().toString();

            if (email.isEmpty() || phone.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                // Check if any of the input fields is empty
                Toast.makeText(UserRegistrationActivity.this, "Enter all input fields", Toast.LENGTH_SHORT).show();
            } else if (!isValidEmail(email)) {
                // Check if the email format is not valid
                Toast.makeText(UserRegistrationActivity.this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
            } else if (!isValidPhoneNumber(phone)) {
                // Check if the phone number format is not valid
                Toast.makeText(UserRegistrationActivity.this, "Enter a valid phone number (10 digits)", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                // Check if the passwords do not match
                Toast.makeText(UserRegistrationActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else if (!isUsernameAvailable(username)) {
                // Check if the username already exists in the database
                Toast.makeText(UserRegistrationActivity.this, "Username already exists. Please choose another username.", Toast.LENGTH_SHORT).show();
            } else {
                // All checks passed, proceed with registration
                long result = appDatabase.insertUserData(email, phone, username, password, countryCode);
                if (result != -1) {
                    Toast.makeText(UserRegistrationActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UserRegistrationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(UserRegistrationActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    // Helper method to check if the email format is valid
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    // Helper method to check if the phone number format is valid
    private boolean isValidPhoneNumber(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches() && phone.length() == 10;
    }
    public boolean isUsernameAvailable(String username) {
        Cursor cursor = appDatabase.checkUsernameAvailability(username);
        if (cursor != null) {
            try {
                return cursor.getCount() == 0;
            } finally {
                cursor.close(); // Close the cursor when you're done with it
            }
        }
        // Handle the case where the cursor is null
        return false; // or return an appropriate default value
    }

}
