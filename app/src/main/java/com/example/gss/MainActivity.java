package com.example.gss;

import static android.app.appsearch.SetSchemaRequest.READ_EXTERNAL_STORAGE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText;
    private AppDatabase appDatabase;
    private static final int CAMERA_PERMISSION_REQUEST = 1;
    private static final int GALLERY_PERMISSION_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestCameraAndGalleryPermissions();
        }
        setContentView(R.layout.login_page);
        appDatabase = new AppDatabase(this);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.saveButton);
        TextView signupText = findViewById(R.id.signupText);
        // Find the passwordVisibilityToggle ImageView by its ID
        final ImageView passwordVisibilityToggle = findViewById(R.id.passwordVisibilityToggle);
        @SuppressLint("UseCompatLoadingForDrawables") final Drawable visibilityOnDrawable = getResources().getDrawable(R.drawable.ic_baseline_visibility_24);
        @SuppressLint("UseCompatLoadingForDrawables") final Drawable visibilityOffDrawable = getResources().getDrawable(R.drawable.ic_baseline_visibility_off_24);

        passwordVisibilityToggle.setOnClickListener(v -> {
            // Toggle the password visibility
            if (passwordEditText.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
                // Password is currently hidden, show it
                passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                // Replace the icon with ic_baseline_visibility_on
                passwordVisibilityToggle.setImageDrawable(visibilityOnDrawable);
                int position = passwordEditText.length();
                passwordEditText.setSelection(position);
            } else {
                // Password is currently visible, hide it
                passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                // Replace the icon with ic_baseline_visibility_24
                passwordVisibilityToggle.setImageDrawable(visibilityOffDrawable);
                int position = passwordEditText.length();
                passwordEditText.setSelection(position);
            }
        });
        // Set an OnClickListener for the loginButton
        loginButton.setOnClickListener(v -> {
            Log.d("MainActivity", "Login button clicked");
            if (!checkPermissions()) {
                // Permissions are not granted, request permissions
                requestPermissions();
            }
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                // Check if username or password is empty
                Toast.makeText(MainActivity.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            } else if (isValidAdminUser(username, password)) {
                // Admin login
                openAdminDashboard();
                Toast.makeText(MainActivity.this, "Admin Logged In Successfully :)", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
            } else if (validateUser(username, password)) {
                // User login
                Toast.makeText(MainActivity.this, "User Logged In Successfully :)", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, UserDashboardActivity.class);
                intent.putExtra("user_name", username);
                startActivity(intent);
            } else if (isValidUser(username, password)) {
                // Regular user login
                openUserDashboard(username);
                Toast.makeText(MainActivity.this, "User Logged In Successfully :)", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, UserDashboardActivity.class);
                intent.putExtra("user_name", username);
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });
        // Set an OnClickListener for the signupText TextView
        signupText.setOnClickListener(v -> {
            // Open the registration activity when the signup text is clicked
            String username = usernameEditText.getText().toString().trim();
            Intent intent = new Intent(MainActivity.this, UserRegistrationActivity.class);
            intent.putExtra("user_name", username);
            startActivity(intent);
        });
    }
    // Validate the username and password against the database
    private boolean validateUser(String username, String password) {
        SQLiteDatabase db = appDatabase.getReadableDatabase();
        String[] columns = {"username"};
        String selection = "username = ?" + " AND " + "password = ?";
        String[] selectionArgs = {username, password};
        @SuppressLint("Recycle") Cursor cursor = db.query("users", columns, selection, selectionArgs, null, null, null);
        boolean isValid = cursor.moveToFirst();
        // cursor.close();
         db.close();
        return isValid;
    }
    // Define the methods to check if a user is an admin or a regular user
    private boolean isValidAdminUser(String username, String password) {
        Cursor cursor = appDatabase.queryAdminCredentials(username, password);
        boolean isValid = cursor.moveToFirst();
         cursor.close();
        return isValid;
    }
    private boolean isValidUser(String username, String password) {
        Cursor cursor = appDatabase.queryUserCredentials(username, password);
        boolean isValid = cursor.moveToFirst();
         cursor.close();
        return isValid;
    }
    private void openAdminDashboard() {
        Toast.makeText(MainActivity.this, "Admin Logged In Successfully :)", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
        startActivity(intent);
    }
    private void openUserDashboard(String username) {
        Toast.makeText(MainActivity.this, "User Logged In Successfully :)", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, UserDashboardActivity.class);
        intent.putExtra("user_name", username);
        startActivity(intent);
    }
    private boolean checkPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int galleryPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        return cameraPermission == PackageManager.PERMISSION_GRANTED && galleryPermission == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermissions() {
        // Request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        }
        // Request gallery permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION_REQUEST);
        }
    }
    // Request both camera and gallery permissions
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requestCameraAndGalleryPermissions() {
        // Check if the camera permission is granted
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        // Check if the gallery permission is granted
        int galleryPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        List<String> permissionsToRequest = new ArrayList<>();
        // If camera permission is not granted, add it to the list of permissions to request
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA);
            // User selected "Never ask again", show a dialog to open app settings
            showSettingsAlertDialog();
        }
        // If gallery permission is not granted, add it to the list of permissions to request
        if (galleryPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            // Check if the user selected "Never ask again"
        }
        // If there are permissions to request, proceed with the request
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), CAMERA_PERMISSION_REQUEST);
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), READ_EXTERNAL_STORAGE);
        }
    }
    // Method to show an alert dialog to open app settings
    private void showSettingsAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Required")
                .setMessage("Please grant permission in the app settings to use this feature.")
                .setPositiveButton("Open Settings", (dialog, which) -> openAppSettings())
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Method to open the app settings
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }
}
