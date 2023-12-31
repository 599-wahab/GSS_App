package com.example.gss;

import static com.example.gss.AppDatabase.TABLE_BOOKINGS;
import static java.text.MessageFormat.format;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity
{
    private ListView bookingList, userList, eventsList, galleryList;
    private ImageView heading;
    private TextView userName;
    private TextView bookingsHeading;
    private TextView usersHeading;
    private TextView gallerypage;
    private TextView eventspage;
    private LinearLayout ll1 , ll2, l1;
    private Button saveButton, addButton,gAddButton,reportButton;
    private EventListAdapter eventsListAdapter;
    private EventListAdapter galleryListAdapter;
    private static final int CAMERA_REQUEST = 1;
    private static final int GALLERY_REQUEST = 2;
    private static final int GALLERY_LIST_REQUEST = 3;
    private static final int EVENT_LIST_REQUEST = 4;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private List<Event> eventList;
    private int req;
    private List<Uri> eventImagesList = new ArrayList<>();
    private List<Uri> galleryImagesList = new ArrayList<>();
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Do you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> logout())
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void logout() {
        Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
        eventImagesList = new ArrayList<>();
        galleryImagesList = new ArrayList<>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_dashboard);
        bookingsHeading = findViewById(R.id.bookingsHeading);
        usersHeading = findViewById(R.id.usersHeading);
        gallerypage = findViewById(R.id.gallerypage);
        eventspage = findViewById(R.id.eventspage);
        userName = findViewById(R.id.userName);
        heading = findViewById(R.id.heading);
        bookingList = findViewById(R.id.bookinglist);
        userList = findViewById(R.id.userlist);
        eventsList = findViewById(R.id.evebtslist);
        galleryList = findViewById(R.id.gallerylist);
        saveButton = findViewById(R.id.saveButton);
        reportButton = findViewById(R.id.reportButton);
        addButton = findViewById(R.id.addButton);
        gAddButton = findViewById(R.id.gAddButton);
        ll1 = findViewById(R.id.ll1);
        ll2 = findViewById(R.id.ll2);
        l1 = findViewById(R.id.l1);
        // Set onClickListeners for the buttons
        findViewById(R.id.bookingdetails).setOnClickListener(v -> {
            // Show booking list, hide others
            userName.setVisibility(View.GONE);
            usersHeading.setVisibility(View.GONE);
            gallerypage.setVisibility(View.GONE);
            eventspage.setVisibility(View.GONE);
            bookingsHeading.setVisibility(View.VISIBLE);
            userList.setVisibility(View.GONE);
            eventsList.setVisibility(View.GONE);
            galleryList.setVisibility(View.GONE);
            l1.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            reportButton.setVisibility(View.VISIBLE);
            ll1.setVisibility(View.GONE);
            ll2.setVisibility(View.GONE);
            heading.setVisibility(View.GONE);
            displayBookingList();
        });
        findViewById(R.id.userdetails).setOnClickListener(v -> {
            // Show user list, hide others
            gallerypage.setVisibility(View.GONE);
            eventspage.setVisibility(View.GONE);
            userName.setVisibility(View.GONE);
            usersHeading.setVisibility(View.VISIBLE);
            bookingsHeading.setVisibility(View.GONE);
            bookingList.setVisibility(View.GONE);
            userList.setVisibility(View.VISIBLE);
            eventsList.setVisibility(View.GONE);
            galleryList.setVisibility(View.GONE);
            l1.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            ll1.setVisibility(View.GONE);
            ll2.setVisibility(View.GONE);
            heading.setVisibility(View.GONE);
            // Display the list of user usernames
            displayUserList();
        });
        findViewById(R.id.addevents).setOnClickListener(v -> {
            // Show events list, hide others
            gallerypage.setVisibility(View.GONE);
            eventspage.setVisibility(View.VISIBLE);
            userName.setVisibility(View.GONE);
            usersHeading.setVisibility(View.GONE);
            bookingsHeading.setVisibility(View.GONE);
            bookingList.setVisibility(View.GONE);
            userList.setVisibility(View.GONE);
            eventsList.setVisibility(View.VISIBLE);
            galleryList.setVisibility(View.GONE);
            l1.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            gAddButton.setVisibility(View.GONE);
            addButton.setVisibility(View.VISIBLE);
            ll1.setVisibility(View.GONE);
            ll2.setVisibility(View.GONE);
            heading.setVisibility(View.GONE);
            // Call getAllEventImages to display the list of saved events images
            displayEventImages();
        });
        findViewById(R.id.gallerdetails).setOnClickListener(v -> {
            // Show gallery list, hide others
            gallerypage.setVisibility(View.VISIBLE);
            eventspage.setVisibility(View.GONE);
            userName.setVisibility(View.GONE);
            usersHeading.setVisibility(View.GONE);
            bookingsHeading.setVisibility(View.GONE);
            bookingList.setVisibility(View.GONE);
            userList.setVisibility(View.GONE);
            eventsList.setVisibility(View.GONE);
            galleryList.setVisibility(View.VISIBLE);
            l1.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            gAddButton.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.GONE);
            ll1.setVisibility(View.GONE);
            ll2.setVisibility(View.GONE);
            heading.setVisibility(View.GONE);
            displayGalleryImages();
        });
        // Set an OnClickListener for the save button
        saveButton.setOnClickListener(v -> {
            // Perform save action here
            // Show gallery list, hide others
            gallerypage.setVisibility(View.GONE);
            eventspage.setVisibility(View.GONE);
            userName.setVisibility(View.VISIBLE);
            usersHeading.setVisibility(View.GONE);
            bookingsHeading.setVisibility(View.GONE);
            bookingList.setVisibility(View.GONE);
            userList.setVisibility(View.GONE);
            eventsList.setVisibility(View.GONE);
            galleryList.setVisibility(View.GONE);
            l1.setVisibility(View.GONE);
            gAddButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            reportButton.setVisibility(View.GONE);
            addButton.setVisibility(View.GONE);
            ll1.setVisibility(View.VISIBLE);
            ll2.setVisibility(View.VISIBLE);
            heading.setVisibility(View.VISIBLE);
        });
        // Set an OnClickListener for the user list items
        userList.setOnItemClickListener((parent, view, position, id) -> {
            Cursor cursor = (Cursor) userList.getItemAtPosition(position);
            if (cursor != null && cursor.getCount() > 0) {
                // Move the cursor to the selected position
                cursor.moveToPosition(position);
                // Check if the column "username" exists
                int columnIndex = cursor.getColumnIndex("username");
                if (columnIndex != -1) {
                    String selectedUsername = cursor.getString(columnIndex);
                    if (selectedUsername != null) {
                        // Query the database to get the user details based on the selected username
                        displayUserDetailsPopup(selectedUsername);
                    } else {
                        // Handle the case where "username" column is null
                        Toast.makeText(AdminDashboardActivity.this, "Invalid username", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle the case where "username" column is not found
                    Toast.makeText(AdminDashboardActivity.this, "Column 'username' not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Handle the case where the Cursor is null or empty
                Toast.makeText(AdminDashboardActivity.this, "No data available", Toast.LENGTH_SHORT).show();
            }
        });
        // Set a long click listener to delete a user
        userList.setOnItemLongClickListener((parent, view, position, id) -> {
            Cursor cursor = (Cursor) userList.getItemAtPosition(position);
            if (cursor != null) {
                @SuppressLint("Range") String selectedUsername = cursor.getString(cursor.getColumnIndex("username"));
                // Show a confirmation dialog for user deletion
                showDeleteUserConfirmation(selectedUsername);
            }
            return true;
        });
        // Set an OnClickListener for the booking list items
        bookingList.setOnItemClickListener((parent, view, position, id) -> {
            Cursor cursor = (Cursor) bookingList.getItemAtPosition(position);
            if (cursor != null && cursor.getCount() > 0) {
                // Move the cursor to the selected position
                cursor.moveToPosition(position);
                // Check if the column "username" exists
                int columnIndex = cursor.getColumnIndex("_id");
                if (columnIndex != -1) {
                    String selectedUsername = cursor.getString(columnIndex);
                    if (selectedUsername != null) {
                        // Query the database to get the user details based on the selected username
                        displayBookingDetailsPopup(selectedUsername);
                    } else {
                        // Handle the case where "username" column is null
                        Toast.makeText(AdminDashboardActivity.this, "Invalid username", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle the case where "username" column is not found
                    Toast.makeText(AdminDashboardActivity.this, "Column 'username' not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Handle the case where the Cursor is null or empty
                Toast.makeText(AdminDashboardActivity.this, "No data available", Toast.LENGTH_SHORT).show();
            }
        });
        // Initialize the list of events
        eventList = new ArrayList<>();
        // Initialize the custom adapter for the events & gallery ListView
        eventsListAdapter = new EventListAdapter(this, eventList);
        galleryListAdapter = new EventListAdapter(this, eventList);
        // Set the adapter for the events ListView
        eventsList.setAdapter(eventsListAdapter);
        galleryList.setAdapter(galleryListAdapter);
//        eventsList.setOnItemLongClickListener((parent, view, position, id) -> {
//            Event selectedEvent = eventsList.get(position);
//            showDeleteConfirmationDialog(selectedEvent.getId(), true);
//            return true;
//        });
//
//        galleryList.setOnItemLongClickListener((parent, view, position, id) -> {
//            Event selectedGalleryItem = galleryList.get(position);
//            showDeleteConfirmationDialog(selectedGalleryItem.getId(), false);
//            return true;
//        });

        addButton.setOnClickListener(v -> {
            // Handle the logic for adding a new image to the events list
            openImagePickerDialog();
            req = EVENT_LIST_REQUEST;
        });
        gAddButton.setOnClickListener(v -> {
            // Handle the logic for adding a new image to the events list
            openImagePickerDialog();
            req = GALLERY_LIST_REQUEST;
        });
        reportButton.setOnClickListener(v -> {
            AppDatabase appDatabase = new AppDatabase(AdminDashboardActivity.this);
            appDatabase.generateMonthlyReport(AdminDashboardActivity.this);
        });

    }
    private void showDeleteConfirmationDialog(int eventId, boolean isEvent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete");
        builder.setMessage("Are you sure you want to delete this " + (isEvent ? "event" : "gallery") + "?");
        builder.setPositiveButton("Yes", (dialog, which) -> deleteSelectedItem(eventId, isEvent));
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void deleteSelectedItem(int itemId, boolean isEvent) {
        // Call the appropriate delete method based on isEvent
        try (AppDatabase appDatabase = new AppDatabase(this)) {
            if (isEvent) {
                appDatabase.deleteEventImage(itemId);
            } else {
                appDatabase.deleteGalleryImage(itemId);
            }
        }
        // You might want to refresh your list or perform other actions here
        // For example, if eventList is an ArrayList<Event>, you can remove the deleted item:
        Event eventToRemove = null;
        for (Event event : eventList) {
            if (event.getId() == itemId) {
                eventToRemove = event;
                break;
            }
        }
        if (eventToRemove != null) {
            eventList.remove(eventToRemove);
            eventsListAdapter.notifyDataSetChanged(); // Notify the adapter about the change
        }

        // If you have a similar list for gallery items, update it accordingly
    }
    private void displayGalleryImages() {
        List<Event> gallery;
        try (AppDatabase appDatabase = new AppDatabase(this)) {
            gallery = appDatabase.getAllGalleryImages();
        }
        galleryListAdapter.clear();
        galleryListAdapter.addAll(gallery);
        galleryListAdapter.notifyDataSetChanged();
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";
        @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
        );
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                galleryImagesList.add(Uri.parse(imagePath));
                // Log each added URI
                Log.d("GalleryImages", "Added URI: " + Uri.parse(imagePath));
            } while (cursor.moveToNext());
            // Log the number of gallery images retrieved
            Log.d("GalleryImages", "Number of Gallery Images: " + galleryImagesList.size());
            // Notify the adapter that the data set has changed
            galleryListAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "No images found", Toast.LENGTH_SHORT).show();
        }
    }
    private void displayEventImages() {
        List<Event> events;
        try (AppDatabase appDatabase = new AppDatabase(this)) {
            events = appDatabase.getAllEventImages();
        }
        eventsListAdapter.clear();
        eventsListAdapter.addAll(events);
        eventsListAdapter.notifyDataSetChanged();
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";
        @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
        );
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                eventImagesList.add(Uri.parse(imagePath));
            } while (cursor.moveToNext());
            //cursor.close();
            eventsListAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "No images found", Toast.LENGTH_SHORT).show();
        }
    }
    private void openCamera() {
        openCameraOrGallery(CAMERA_REQUEST);
    }
    private void openImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source");
        String[] options = {"Gallery", "Camera"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Open gallery
                openGallery();
            } else if (which == 1) {
                // Open camera (you need to implement camera functionality)
                openCamera();
            }
        });
        builder.show();
    }
    // Function to open the gallery for image selection
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
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }
    // Override onActivityResult to handle the result from the gallery Intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (resultCode == RESULT_OK) {
                if (requestCode == GALLERY_REQUEST) {
                    // Handle the selected image from the gallery
                    Uri selectedImageUri = data.getData();
                    if (selectedImageUri != null) {
                        // Add the selected image URI to the events list
                        if (req == EVENT_LIST_REQUEST ) {
                            eventImagesList.add(selectedImageUri);
                        } else if ( req == GALLERY_LIST_REQUEST){
                            galleryImagesList.add(selectedImageUri);
                        }
                        // Notify the adapter that the data set has changed
                        eventsListAdapter.notifyDataSetChanged();

                        galleryListAdapter.notifyDataSetChanged();
                        // Show a dialog to ask for the event name
                        showEventNameDialog(selectedImageUri);
                    } else {
                        Toast.makeText(this, "Failed to get image from gallery", Toast.LENGTH_SHORT).show();
                    }
                } else if (requestCode == CAMERA_REQUEST) {
                    // Handle the captured image from the camera
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            // Convert the Bitmap to Uri
                            Uri imageUri = getImageUri(imageBitmap);
                            // Add the captured image URI to the events list
                            if (req == EVENT_LIST_REQUEST ){
                                eventImagesList.add(imageUri);
                            } else if ( req == GALLERY_LIST_REQUEST){
                                galleryImagesList.add(imageUri);
                            }
                            // Show a dialog to ask for the event name
                            showEventNameDialog(imageUri);
                        } else {
                            Toast.makeText(this, "Failed to capture image from camera", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                // Notify the adapter that the data set has changed (moved outside the if conditions)
                eventsListAdapter.notifyDataSetChanged();
                galleryListAdapter.notifyDataSetChanged();
            }else{
                Toast.makeText(this, "Data is null", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void showEventNameDialog(Uri selectedImageUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Title");
        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String eventName = input.getText().toString().trim();
            if (!eventName.isEmpty()) {
                // Insert the event data into the events table
                try (AppDatabase appDatabase = new AppDatabase(this)) {
                    if (req == GALLERY_LIST_REQUEST) {
                        appDatabase.insertGalleryData(eventName, String.valueOf(selectedImageUri));
                        displayGalleryImages();
                    } else if (req == EVENT_LIST_REQUEST) {
                        appDatabase.insertEventData(eventName, String.valueOf(selectedImageUri));
                        // Notify the adapter that the data set has changed
                        displayEventImages();
                    }
                }

            } else {
                Toast.makeText(this, "Event name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }
    private void showDeleteUserConfirmation(String username) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to delete the user: " + username + "?")
                .setPositiveButton("Yes", (dialog, id) -> {
                    // Delete the user
                    deleteUser(username);
                })
                .setNegativeButton("No", (dialog, id) -> {
                    // Dismiss the dialog, no action needed
                });
        builder.create().show();
    }
    private void deleteUser(String username) {
        // Perform the deletion of the user's data from the database
        SQLiteDatabase db;
        AppDatabase appDatabase = new AppDatabase(this);
        db = appDatabase.getWritableDatabase();
        String whereClause = "username = ?";
        String[] whereArgs = {username};
        int deletedRows = db.delete("users", whereClause, whereArgs);
        if (deletedRows > 0) {
            // User deleted successfully
            Toast.makeText(this, "User deleted successfully!", Toast.LENGTH_SHORT).show();
        } else {
            // User deletion failed
            Toast.makeText(this, "User deletion failed.", Toast.LENGTH_SHORT).show();
        }
    }
    @SuppressLint("SetTextI18n")
    private void displayUserList() {
        SQLiteDatabase db;
        AppDatabase appDatabase = new AppDatabase(this);
        db = appDatabase.getReadableDatabase();
        // Query to get all usernames from the users table
        String[] columns = { "id AS _id", "username" };
        Cursor cursor = db.query("users", columns, null, null, null, null, null);
        // Check if there are users
        if (cursor != null && cursor.getCount() > 0) {
            // Create an adapter to display the usernames
            String[] fromColumns = { "username" };
            int[] toViews = { android.R.id.text1 };
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor, fromColumns, toViews, 0);
            // Set the adapter for the ListView
            userList.setAdapter(adapter);
            // Show the user list
            userList.setVisibility(View.VISIBLE);
        } else {
            // No users exist, show a message
            userList.setVisibility(View.GONE);
            l1.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.GONE);
            Toast.makeText(AdminDashboardActivity.this, "No users exist! :)!", Toast.LENGTH_SHORT).show();
        }
    }
    @SuppressLint("SetTextI18n")
    private void displayBookingList() {
        SQLiteDatabase db;
        AppDatabase appDatabase = new AppDatabase(this);
        db = appDatabase.getReadableDatabase();
        // Query to get distinct usernames from the bookings table
        String[] columns = {"DISTINCT username AS _id"};
        Cursor cursor = db.query(TABLE_BOOKINGS, columns, null, null, null, null, null);
        // Check if there are bookings
        if (cursor != null && cursor.getCount() > 0) {
            // Create an adapter to display the booking details
            String[] fromColumns = {"_id"};
            int[] toViews = {android.R.id.text1};
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor, fromColumns, toViews, 0);
            // Set the adapter for the ListView
            bookingList.setAdapter(adapter);
            bookingList.setVisibility(View.VISIBLE);
            // Set a long click listener to delete a booking record
            bookingList.setOnItemLongClickListener((parent, view, position, id) -> {
                Cursor longClickCursor = (Cursor) bookingList.getItemAtPosition(position);
                if (longClickCursor != null) {
                    @SuppressLint("Range") int bookingId = longClickCursor.getInt(longClickCursor.getColumnIndex("_id"));
                    // Show a confirmation dialog for booking record deletion
                    showDeleteBookingRecordConfirmation(bookingId);
                }
                return true;
            });
        } else {
            bookingList.setVisibility(View.GONE);
            l1.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.GONE);
            reportButton.setVisibility(View.GONE);
            // Display a message when there are no bookings
            Toast.makeText(AdminDashboardActivity.this, "Booking details Available!", Toast.LENGTH_SHORT).show();
        }
    }
    private void displayUserDetailsPopup(String username) {
        // Create an AlertDialog to display the user details
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View userDetailsView = getLayoutInflater().inflate(R.layout.user_details_popup, null);
        builder.setView(userDetailsView);
        TextView userDetailsUsername = userDetailsView.findViewById(R.id.userDetailsUsername);
        TextView userDetailsEmail = userDetailsView.findViewById(R.id.userDetailsEmail);
        TextView userDetailsPhone = userDetailsView.findViewById(R.id.userDetailsPhone);
        // Query the database to get the user details based on the selected username
        AppDatabase appDatabase = new AppDatabase(this);
        SQLiteDatabase db = appDatabase.getReadableDatabase();
        String[] columns = {"username", "email", "country_code","phone"};
        String selection = "username = ?";
        String[] selectionArgs = {username};
        try (Cursor cursor = db.query("users", columns, selection, selectionArgs, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") String userEmail = cursor.getString(cursor.getColumnIndex("email"));
                @SuppressLint("Range") String country_code = cursor.getString(cursor.getColumnIndex("country_code"));
                @SuppressLint("Range") String userPhone = cursor.getString(cursor.getColumnIndex("phone"));
                Toast.makeText(AdminDashboardActivity.this, "User found!", Toast.LENGTH_SHORT).show();
                userDetailsUsername.setText(format("Username: {0}", username));
                userDetailsEmail.setText(MessageFormat.format("Email: {0}", userEmail));
                userDetailsPhone.setText(MessageFormat.format("Phone: {0} {1}", country_code, userPhone));
                // Create and show the details dialog
                AlertDialog userDetailsDialog = builder.create();
                userDetailsDialog.show();
            } else {
                Toast.makeText(AdminDashboardActivity.this, "User not found!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void displayBookingDetailsPopup(String username) {
        // Create an AlertDialog to display the booking details
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View bookingDetailsView = getLayoutInflater().inflate(R.layout.booking_details_popup, null);
        builder.setView(bookingDetailsView);
        // Find views in the booking details layout
        TextView bookingDetailsUsername = bookingDetailsView.findViewById(R.id.bookingDetailsUsername);
        LinearLayout bookingDetailsContainer = bookingDetailsView.findViewById(R.id.bookingDetailsContainer);
        // Query the database to get all booking details for the selected username
        SQLiteDatabase db;
        AppDatabase appDatabase = new AppDatabase(this);
        db = appDatabase.getReadableDatabase();
        String[] columns = {"username", "booking_date"};
        String selection = "username = ?";
        String[] selectionArgs = {username};
        try (Cursor cursor = db.query(TABLE_BOOKINGS, columns, selection, selectionArgs, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                // Display the username
                bookingDetailsUsername.setText(format("Username: {0}", username));
                // Iterate through all booking records for the user
                do {
                    // Retrieve booking details from the cursor
                    @SuppressLint("Range") String bookingDate = cursor.getString(cursor.getColumnIndex("booking_date"));
                    // Create a TextView for each booking date
                    TextView bookingDateTextView = new TextView(this);
                    bookingDateTextView.setText(MessageFormat.format("Booking Date: {0}", bookingDate));
                    // Add the TextView to the container
                    bookingDetailsContainer.addView(bookingDateTextView);
                } while (cursor.moveToNext());
                // Create and show the details dialog
                AlertDialog bookingDetailsDialog = builder.create();
                bookingDetailsDialog.show();
            } else {
                Toast.makeText(AdminDashboardActivity.this, "Booking details not found!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void showDeleteBookingRecordConfirmation(int bookingId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to delete this booking record?")
                .setPositiveButton("Yes", (dialog, id) -> {
                    // Delete the booking record
                    deleteBookingRecord(bookingId);
                })
                .setNegativeButton("No", (dialog, id) -> {
                    // Dismiss the dialog, no action needed
                });
        builder.create().show();
    }
    private void deleteBookingRecord(int bookingId) {
        // Perform the deletion of the booking record from the database
        SQLiteDatabase db;
        AppDatabase appDatabase = new AppDatabase(this);
        db = appDatabase.getWritableDatabase();
        String whereClause = "id = ?";
        String[] whereArgs = {String.valueOf(bookingId)};
        int deletedRows = db.delete(TABLE_BOOKINGS, whereClause, whereArgs);
        if (deletedRows > 0) {
            // Booking record deleted successfully
            Toast.makeText(this, "Booking record deleted successfully!", Toast.LENGTH_SHORT).show();
        } else {
            // Booking record deletion failed
            Toast.makeText(this, "Booking record deletion failed.", Toast.LENGTH_SHORT).show();
        }
    }
}