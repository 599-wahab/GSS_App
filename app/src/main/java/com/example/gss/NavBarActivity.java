package com.example.gss;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

public class NavBarActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1;
    private static final int GALLERY_REQUEST = 2;
    private ImageView circularUserProfile;
    private GestureDetectorCompat gestureDetector;
    private AppDatabase appDatabase;
    private TextView usernameTextView;
    private static final String PREFS_KEY_PROFILE_PIC_URI = "profile_pic_uri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navbar);
        String username = getIntent().getStringExtra("user_name");
        // Set the username to the username TextView
        usernameTextView = findViewById(R.id.username);
        usernameTextView.setText(username);
        // Initialize gesture detector for swiping
        gestureDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                // Detect swipe to the right
                if (e1.getX() < e2.getX()) {
                    // Swipe to the right, finish the current activity (or start a new activity if needed)
                    finish();
                    return true;
                }
                return false;
            }
        });
        circularUserProfile = findViewById(R.id.circularUserProfile);
        // Initialize the AppDatabase
        appDatabase = new AppDatabase(this);
        String savedProfilePicUri = getSavedProfilePicUri();
        if (savedProfilePicUri != null) {
            setProfilePicture(Uri.parse(savedProfilePicUri));
        }
        // Handle circular user profile image click
        circularUserProfile.setOnClickListener(v -> checkCameraPermission());
        // Handle edit profile button click
        findViewById(R.id.editProfileButton).setOnClickListener(v -> showEditProfileDialog());
        // Handle customer care button click
        findViewById(R.id.customerCareButton).setOnClickListener(v -> showCustomerCareDialog());
        // Handle feedback button click
        findViewById(R.id.feedbackButton).setOnClickListener(v -> showFeedbackDialog());
    }
    private String getSavedProfilePicUri() {
        // Retrieve the saved profile picture URI from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return preferences.getString(PREFS_KEY_PROFILE_PIC_URI, null);
    }
    private void setProfilePicture(Uri profilePicUri) {
        // Retrieve profile picture data from the database based on the logged-in user
        String username = getIntent().getStringExtra("user_name");
        byte[] profilePicData = appDatabase.getUserProfilePicData(username);

        // Convert the byte array to a Bitmap
        Bitmap profilePicBitmap = BitmapFactory.decodeByteArray(profilePicData, 0, profilePicData.length);

        // Set the profile picture to the circularUserProfile ImageView
        circularUserProfile.setImageBitmap(centerCropBitmap(profilePicBitmap, 200));
    }
    private void saveProfilePicDataToDatabase(Bitmap profilePicBitmap) {
        // Convert the Bitmap to a byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        profilePicBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        // Save the byte array to the user table in the database
        String username = getIntent().getStringExtra("user_name");
        appDatabase.updateUserProfilePicData(username, byteArray);
    }
    private void showEditProfileDialog() {
        // Create a dialog layout with EditText fields for username, phone, email, and password
        LayoutInflater inflater = LayoutInflater.from(this);
        View editProfileView = inflater.inflate(R.layout.edit_profile_dialog, null);
        // Get references to EditText fields
        EditText usernameEditText = editProfileView.findViewById(R.id.editUsername);
        EditText phoneEditText = editProfileView.findViewById(R.id.editPhone);
        EditText emailEditText = editProfileView.findViewById(R.id.editEmail);
        EditText passwordEditText = editProfileView.findViewById(R.id.editPassword);
        String username = getIntent().getStringExtra("user_name");
        // Retrieve user data from the database using AppDatabase
        Cursor cursor = appDatabase.queryUserData(username);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String userUsername = cursor.getString(cursor.getColumnIndex("username"));
            @SuppressLint("Range") String userEmail = cursor.getString(cursor.getColumnIndex("email"));
            @SuppressLint("Range") String userPhone = cursor.getString(cursor.getColumnIndex("phone"));
            @SuppressLint("Range") String userPassword = cursor.getString(cursor.getColumnIndex("password"));

            // Set initial values from the user table in AppDatabase
            usernameEditText.setText(userUsername);
            phoneEditText.setText(userPhone);
            emailEditText.setText(userEmail);
            passwordEditText.setText(userPassword);
        }
        // Create and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(editProfileView);
        builder.setTitle("Edit Profile");
        // Add save button to update user details
        builder.setPositiveButton("Save", (dialog, which) -> {
            // Get updated values from EditText fields
            String newUsername = usernameEditText.getText().toString();
            String newPhone = phoneEditText.getText().toString();
            String newEmail = emailEditText.getText().toString();
            String newPassword = passwordEditText.getText().toString();
            // Update user details in the user table of AppDatabase
            appDatabase.updateUserData(username, newUsername, newPhone, newEmail, newPassword);
            // Refresh the UI or perform any other necessary actions
            refreshUI(newUsername);
        });
        // Add cancel button
        builder.setNegativeButton("Cancel", null);
        // Show the dialog
        builder.show();
    }
    // Method to show the customer care dialog
    private void showCustomerCareDialog() {
        // Create a dialog layout with buttons for contacting through phone or email
        LayoutInflater inflater = LayoutInflater.from(this);
        View customerCareView = inflater.inflate(R.layout.custom_report_popup, null);
        // Create and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(customerCareView);
        builder.setTitle("Customer Care");
        // Add buttons for contacting through phone or email
        builder.setPositiveButton("Contact through Phone", (dialog, which) -> {
            // Implement the logic to initiate a phone call
            initiatePhoneCall();
        });
        builder.setNegativeButton("Contact through Email", (dialog, which) -> {
            // Implement the logic to compose an email
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + "rupeeravin@gmail.com"));
            startActivity(intent);
        });

        // Show the dialog
        builder.show();
    }
    // Method to show the feedback dialog
    private void showFeedbackDialog() {
        // Create a dialog layout with a TextInputEditText for feedback
        LayoutInflater inflater = LayoutInflater.from(this);
        View feedbackView = inflater.inflate(R.layout.feedback_dialog, null);
        // Get reference to the TextInputEditText
        TextInputEditText feedbackEditText = feedbackView.findViewById(R.id.feedbackEditText);
        // Create and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(feedbackView);
        builder.setTitle("Feedback");
        // Add submit button to send feedback via email
        builder.setPositiveButton("Submit", (dialog, which) -> {
            // Get the feedback text
            String feedbackText = Objects.requireNonNull(feedbackEditText.getText()).toString();
            // Implement the logic to send feedback via email
            sendFeedbackEmail(getCurrentUserEmail(), feedbackText);
            // Show a success message or perform any other necessary actions
            builder.setTitle("Feedback submitted successfully!");
        });
        // Add cancel button
        builder.setNegativeButton("Cancel", null);
        // Show the dialog
        builder.show();
    }
    // Method to refresh the UI after editing the profile
    private void refreshUI(String newUsername) {
        // Retrieve user data from the database using AppDatabase
        Cursor cursor = appDatabase.queryUserData(newUsername);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String updatedUsername = cursor.getString(cursor.getColumnIndex("username"));
            usernameTextView.setText(updatedUsername);
            Intent intent = new Intent(NavBarActivity.this, UserDashboardActivity.class);
            intent.putExtra("user_name", updatedUsername);
            startActivity(intent);
        }
    }
    private String getCurrentUserEmail() {
        // Implement the logic to get the email of the current user
        return "user_email@example.com";
    }
    private void initiatePhoneCall() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + "+601110358035"));
        startActivity(intent);
    }
    // Implement the logic to send a feedback email from the user's email to the app's email
    private void sendFeedbackEmail(String senderEmail, String body) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + "rupeeravin@gmail.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
        intent.putExtra(Intent.EXTRA_TEXT, "From: " + senderEmail + "\n\n" + body);
        startActivity(intent);
    }
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
        } else {
            openImageSourceDialog();
        }
    }
    private void openImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image Source");
        builder.setItems(new CharSequence[]{"Take from Camera", "Select from Gallery"}, (dialog, which) -> {
            if (which == 0) {
                openCamera();
            } else {
                openGallery();
            }
        });
        builder.show();
    }
    private void openCamera() {
        openCameraOrGallery(CAMERA_REQUEST);
    }
    private void openGallery() {
        openCameraOrGallery(GALLERY_REQUEST);
    }
    @SuppressLint("IntentReset")
    private void openCameraOrGallery(int requestCode) {
        Intent intent = new Intent();
        if (requestCode == CAMERA_REQUEST) {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        } else if (requestCode == GALLERY_REQUEST) {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
        }
        startActivityForResult(intent, requestCode);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {
                Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                setImageToCircularUserProfile(photo); // Set the captured image directly
            } else if (requestCode == GALLERY_REQUEST) {
                Uri selectedImageUri = data.getData();
                try {
                    Bitmap selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    setImageToCircularUserProfile(selectedImageBitmap); // Set the selected image directly
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void setImageToCircularUserProfile(Bitmap imageBitmap) {
        // Center crop the image with a maximum size of 200 (replace with your desired maximum size)
        Bitmap croppedBitmap = centerCropBitmap(imageBitmap, 200);
        // Save the profile picture URI to persistent storage
        saveProfilePicDataToDatabase(croppedBitmap);
        // Load and set the cropped image to the circularUserProfile ImageView
        circularUserProfile.setImageBitmap(croppedBitmap);
    }

    private Bitmap centerCropBitmap(Bitmap sourceBitmap, int maxSize) {
        int sourceWidth = sourceBitmap.getWidth();
        int sourceHeight = sourceBitmap.getHeight();

        int targetSize = Math.min(sourceWidth, sourceHeight);

        // Ensure that the target size does not exceed the specified maximum size
        targetSize = Math.min(targetSize, maxSize);

        int startX = (sourceWidth - targetSize) / 4;
        int startY = (sourceHeight - targetSize) / 4;

        Bitmap croppedBitmap = Bitmap.createBitmap(sourceBitmap, startX, startY, targetSize, targetSize);
        if (croppedBitmap != sourceBitmap) {
            sourceBitmap.recycle(); // Recycle the original bitmap if a new one is created
        }

        return croppedBitmap;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImageSourceDialog();
            } else {
                showPermissionDeniedDialog();
            }
        }
    }
    private void showPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Denied");
        builder.setMessage("You denied the camera permission. This permission is required to take pictures. Would you like to enable it in the app settings?");
        builder.setPositiveButton("Open Settings", (dialog, which) -> {
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }
}
