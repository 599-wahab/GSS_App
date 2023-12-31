package com.example.gss;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "GSS.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_BOOKINGS = "bookings";
    public static final String TABLE_USERS = "users";
    private static final String TABLE_ADMIN = "admin";
    public static final String TABLE_EVENTS = "events";
    public static final String TABLE_GALLERY = "gallery";
    // Define your table creation SQL statements here
    private static final String CREATE_BOOKINGS_TABLE = "CREATE TABLE " + TABLE_BOOKINGS + " (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT,  user_id INTEGER, booking_date TEXT);";
    private static final String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, email TEXT, phone TEXT, country_code TEXT, password TEXT, registration_date DATETIME, profile_pic_data BLOB);";
    private static final String CREATE_ADMIN_TABLE = "CREATE TABLE " + TABLE_ADMIN + " (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT, image_path TEXT);";
    private static final String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + " (id INTEGER PRIMARY KEY AUTOINCREMENT, event_name TEXT, image_path TEXT);";
    private static final String CREATE_GALLERY_TABLE = "CREATE TABLE " + TABLE_GALLERY + " (id INTEGER PRIMARY KEY AUTOINCREMENT, img_name TEXT, image_path TEXT);";
    private final Context context;
    public AppDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the tables
        db.execSQL(CREATE_BOOKINGS_TABLE);
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_ADMIN_TABLE);
        db.execSQL(CREATE_EVENTS_TABLE);
        db.execSQL(CREATE_GALLERY_TABLE);
        // Insert the default admin user

        insertDefaultAdminUser(db);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades if needed
    }
    // Method to insert the default admin user
    private void insertDefaultAdminUser(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put("username", "admin");
        values.put("password", "admin");
        // You can also add other details of the admin user here

        long result = db.insert(TABLE_ADMIN, null, values);

        if (result != -1) {
            // Data saved successfully
            showToast("Default admin user data saved successfully!");
        } else {
            // Data save failed
            showToast("Default admin user data save failed.");
        }
    }
    public Cursor queryAdminCredentials(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = { "username" };
        String selection = "username = ? AND password = ?";
        String[] selectionArgs = { username, password };
        return db.query(TABLE_ADMIN, columns, selection, selectionArgs, null, null, null);
    }
    public Cursor queryUserCredentials(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {"username"};
        String selection = "username = ? AND password = ?";
        String[] selectionArgs = {username, password};
        return db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
    }
    public long insertUserData(String email, String phone, String username, String password, String countryCode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("phone", phone);
        values.put("username", username);
        values.put("password", password);
        values.put("country_code", countryCode);
        values.put("registration_date", getCurrentDateTime());
        // Set profile_pic_data initially as null
        values.put("profile_pic_data", (byte[]) null);
        long result = db.insert(TABLE_USERS, null, values);
//        if (result != -1) {
//            // Data saved successfully
////            showToast("User data saved successfully!");
//        } else {
//            // Data save failed
////            showToast("User data save failed.");
//        }
        return result;
    }
    public void updateUserProfilePicData(String username, byte[] profilePicData) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("profile_pic_data", profilePicData);

        // Update the user table with the new profile picture data
        int rowsAffected = db.update(TABLE_USERS, values, "username=?", new String[]{username});
        db.close();

        if (rowsAffected > 0) {
            // Data updated successfully
            showToast("Profile picture updated successfully!");
        } else {
            // Data update failed
            showToast("Profile picture update failed.");
        }
    }

    private String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public long insertBookingData(String username, int userId, String bookingDate) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if the booking time already exists in the bookings table
        if (isBookingTimeAvailable(db, bookingDate)) {
            ContentValues values = new ContentValues();
            values.put("username", username);
            values.put("user_id", userId);
            values.put("booking_date", bookingDate);
            long result = db.insert(TABLE_BOOKINGS, null, values);

//            if (result != -1) {
//                // Data saved successfully
////                showToast("Booking data saved successfully!");
//            } else {
//                // Data save failed
////                showToast("Booking data save failed.");
//            }
            return result;
        } else {
            // Booking time already exists
            showToast("Sorry, the selected booking time is not available.");
            return -1;
        }
    }
    private boolean isBookingTimeAvailable(SQLiteDatabase db, String bookingDate) {
        String[] columns = {"id"};
        String selection = "booking_date = ?";
        String[] selectionArgs = {bookingDate};
        @SuppressLint("Recycle") Cursor cursor = db.query(TABLE_BOOKINGS, columns, selection, selectionArgs, null, null, null);

        boolean isAvailable = cursor.getCount() == 0; // If count is 0, the time is available; otherwise, it's already booked
//        cursor.close();
        
        return isAvailable;
    }
    private void showToast(String message) {
        // Display a toast message to confirm data save status
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    public Cursor checkUsernameAvailability(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {"username"};
        String selection = "username = ?";
        String[] selectionArgs = {username};
        return db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
    }
    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {"id"};
        String selection = "username = ?";
        String[] selectionArgs = {username};
        @SuppressLint("Recycle") Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int userId = -1;  // Default to -1 if not found
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex("id");
                if (idIndex != -1) {
                    userId = cursor.getInt(idIndex);
                }
//                else {
//                    // Handle the case where 'id' column is not found in the cursor
////                    showToast("'id' column is not found in the cursor");
//                }
            } else {
                // if there are no rows in the cursor
                showToast("No user found for username: " + username);
            }
//            cursor.close();
        } else {
            // Handle the case where the cursor is null
            showToast("Cursor is null");
        }
        return userId;
    }
    public void insertEventData(String eventName, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("event_name", eventName);
        values.put("image_path", imagePath);
        db.insert(TABLE_EVENTS, null, values);
    }
    // Method to insert gallery data
    public void insertGalleryData(String img_name, String imageUri) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("img_name", img_name);
        values.put("image_path", imageUri);

        // Insert the data into the gallery table
        db.insert(TABLE_GALLERY, null, values);
    }
    public List<Event> getAllEventImages() {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {"id", "event_name", "image_path"};
        Cursor cursor = db.query(TABLE_EVENTS, columns, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int eventIdIndex = cursor.getColumnIndex("id");
                int eventNameIndex = cursor.getColumnIndex("event_name");
                int imagePathIndex = cursor.getColumnIndex("image_path");
                if (eventIdIndex != -1 && eventNameIndex != -1 && imagePathIndex != -1) {
                    int eventId = cursor.getInt(eventIdIndex);
                    String eventName = cursor.getString(eventNameIndex);
                    String imagePath = cursor.getString(imagePathIndex);
                    // Create Event object and add it to the list
                    Event event = new Event(eventId, eventName, imagePath);
                    eventList.add(event);
                } else {
                    // Handle the case where some columns are not found in the cursor
                    showToast("NO Event Images are Available");
                }
            } while (cursor.moveToNext());
            // Close the cursor after retrieving data
            cursor.close();
        }
//        else {
//            // Handle the case where the cursor is null or has no rows
////            showToast("No event images found in the database");
//        }
        return eventList;
    }
    // Method to get all gallery images
    public List<Event> getAllGalleryImages() {
        List<Event> galleryEvents = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        // Query to get all gallery events from the gallery table
        String[] columns = {"id", "img_name", "image_path"};
        Cursor cursor = db.query(TABLE_GALLERY, columns, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int eventIdIndex = cursor.getColumnIndex("id");
                int eventNameIndex = cursor.getColumnIndex("img_name");
                int imagePathIndex = cursor.getColumnIndex("image_path");
                if (eventIdIndex != -1 && eventNameIndex != -1 && imagePathIndex != -1) {
                    int eventId = cursor.getInt(eventIdIndex);
                    String eventName = cursor.getString(eventNameIndex);
                    String imagePath = cursor.getString(imagePathIndex);
                    // Create Event object and add it to the list
                    Event event = new Event(eventId, eventName, imagePath);
                    galleryEvents.add(event);
                } else {
                    // Handle the case where some columns are not found in the cursor
                    showToast("NO Event Images are Available");
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
//        else {
//            // Handle the case where the cursor is null or has no rows
////            showToast("No event images found in the database");
//        }
        return galleryEvents;
    }
    public void deleteEventImage(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = "id = ?";
        String[] whereArgs = {String.valueOf(eventId)};
        db.delete("events", whereClause, whereArgs);
        showToast("Event image deleted successfully!");
    }
    public void deleteGalleryImage(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = "id = ?";
        String[] whereArgs = {String.valueOf(eventId)};
        db.delete("gallery", whereClause, whereArgs);
        showToast("Gallery image deleted successfully!");
    }
    public Cursor queryUserData(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {"username", "email", "phone", "country_code", "password"};
        String selection = "username = ?";
        String[] selectionArgs = {username};
        return db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
    }
    // update user data
    public void updateUserData(String oldUsername, String newUsername, String newPhone, String newEmail, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", newUsername);
        values.put("phone", newPhone);
        values.put("email", newEmail);
        values.put("password", newPassword);
        String whereClause = "username = ?";
        String[] whereArgs = {oldUsername};
        int userRowsAffected  = db.update(TABLE_USERS, values, whereClause, whereArgs);
        // Update user data in TABLE_BOOKINGS
        ContentValues bookingValues = new ContentValues();
        bookingValues.put("username", newUsername);
        String bookingWhereClause = "username = ?";
        String[] bookingWhereArgs = {oldUsername};
        int bookingRowsAffected = db.update(TABLE_BOOKINGS, bookingValues, bookingWhereClause, bookingWhereArgs);
        if (userRowsAffected > 0 && bookingRowsAffected > 0) {
            // Data updated successfully
            showToast("User data updated successfully!");
        } else {
            // Data update failed
            showToast("User data update failed.");
        }
    }
    public void generateMonthlyReport(Context context) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Get current month and year
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1;  // Calendar.MONTH is zero-based
        int currentYear = calendar.get(Calendar.YEAR);

        // Query to get bookings and registrations for the current month
        String bookingsQuery = "SELECT * FROM " + TABLE_BOOKINGS +
                " WHERE strftime('%m', booking_date) = ? AND strftime('%Y', booking_date) = ?";
        String usersQuery = "SELECT * FROM " + TABLE_USERS +
                " WHERE strftime('%m', registration_date) = ? AND strftime('%Y', registration_date) = ?";

        // You can customize the columns based on your needs
//        String[] columns = {"id", "username", "booking_date"};
        String[] selectionArgs = {String.format("%02d", currentMonth), String.valueOf(currentYear)};

        // Execute the queries
        Cursor bookingsCursor = db.rawQuery(bookingsQuery, selectionArgs);
        @SuppressLint("Recycle") Cursor usersCursor = db.rawQuery(usersQuery, selectionArgs);

        // Create a StringBuilder to build the report
        StringBuilder reportBuilder = new StringBuilder();

        // Append booking data to the report
        reportBuilder.append("Bookings:\n");
        appendCursorData(reportBuilder, bookingsCursor, "Booking");

        // Append a separator between bookings and users
        reportBuilder.append("\n<---------------------------------------------->\n\n");

//        // Append user data to the report
//        reportBuilder.append("Users:\n");
//        appendCursorData(reportBuilder, usersCursor, "User");

        // Close the Cursors
        bookingsCursor.close();
//        usersCursor.close();

        // Show the report in a popup
        showReportPopup(context, reportBuilder.toString());
    }

    // Helper method to append cursor data to the report
    private void appendCursorData(StringBuilder reportBuilder, Cursor cursor, String type) {
        if (cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex("id");
                int usernameIndex = cursor.getColumnIndex("username");
                int dateIndex = cursor.getColumnIndex(type.equals("Booking") ? "booking_date" : "registration_date");

                int id = cursor.getInt(idIndex);
                String username = cursor.getString(usernameIndex);
                String date = cursor.getString(dateIndex);

                reportBuilder.append(type)
                        .append(" - ID: ")
                        .append(id)
                        .append("\nUsername: ")
                        .append(username)
                        .append("\nDate: ")
                        .append(date)
                        .append("\n------------>\n");
            } while (cursor.moveToNext());
        } else {
            reportBuilder.append("No ")
                    .append(TextUtils.isEmpty(type) ? "data" : type + "s")
                    .append(" found for the current month and year.\n");
        }
    }

    // Helper method to show the report in a popup
    private void showReportPopup(Context context, String report) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Report")
                .setMessage(report)
                .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    @SuppressLint("Range")
    public byte[] getUserProfilePicData(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        byte[] profilePicData = null;

        Cursor cursor = db.query("user_table", new String[]{"profile_pic_data"}, "username=?",
                new String[]{username}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            profilePicData = cursor.getBlob(cursor.getColumnIndex("profile_pic_data"));
            cursor.close();
        }

        db.close();
        return profilePicData;
    }
}
