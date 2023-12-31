package com.example.gss;

import static com.example.gss.AppDatabase.TABLE_BOOKINGS;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {
    private ImageView navButton;
    private CalendarView calendarView;
    private ListView bookingListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calender);

        navButton = findViewById(R.id.navButton);
        calendarView = findViewById(R.id.calendarView);
        bookingListView = findViewById(R.id.bookingList);

        String username = getIntent().getStringExtra("user_name");

        // Handle navigation button click
        navButton.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, NavBarActivity.class);
            intent.putExtra("user_name", username);
            startActivity(intent);
        });

        // Get booked dates for the logged-in user
        String[] bookedDates = getBookedDates(username);

        // Mark booked dates on the CalendarView
        markBookedDates(bookedDates);

        // Display bookings in the ListView
        displayBookings(username);
    }

    private String[] getBookedDates(String username) {
        AppDatabase appDatabase = new AppDatabase(this);
        SQLiteDatabase db = appDatabase.getReadableDatabase();
        String[] columns = {"booking_date"};
        String selection = "username = ?";
        String[] selectionArgs = {username};

        // Query to get booked dates for the specified user
        Cursor cursor = db.query(TABLE_BOOKINGS, columns, selection, selectionArgs, null, null, null);

        List<String> bookedDatesList = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int bookingDateIndex = cursor.getColumnIndex("booking_date");
                if (bookingDateIndex != -1) {
                    String bookingDate = cursor.getString(bookingDateIndex);
                    bookedDatesList.add(bookingDate);
                } else {
                    // Handle the case where 'booking_date' column is not found in the cursor
//                    showToast("'booking_date' column is not found in the cursor");
                }
            } while (cursor.moveToNext());
            // Close the cursor after retrieving data
            cursor.close();
        } else {
            // Handle the case where the cursor is null or has no rows
//            showToast("No booking dates found in the database");
        }

        // Convert the list to an array
        return bookedDatesList.toArray(new String[0]);
    }
    private void markBookedDates(String[] bookedDates) {
        if (bookedDates != null) {
            for (String dateTime : bookedDates) {
                try {
                    // Extract date part from date-time string
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    Date parsedDateTime = inputFormat.parse(dateTime);
                    // Convert to date only format
                    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String dateOnly = outputFormat.format(parsedDateTime);
                    // Convert date string to milliseconds
                    Date parsedDate = outputFormat.parse(dateOnly);
                    long milliseconds = parsedDate.getTime();
                    // Mark the date on the CalendarView
                    calendarView.setDate(milliseconds, true, true);
                } catch (ParseException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error parsing date", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void displayBookings(String username) {
        AppDatabase appDatabase = new AppDatabase(this);
        SQLiteDatabase db = appDatabase.getReadableDatabase();

        // Get the current date and time in yyyy-MM-dd HH:mm:ss format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDateTime = dateFormat.format(new Date());

        // Query to get upcoming bookings for the specified user
        String[] columns = {"booking_date"};
        String selection = "username = ? AND booking_date >= ?";
        String[] selectionArgs = {username, currentDateTime};

        Cursor cursor = db.query(TABLE_BOOKINGS, columns, selection, selectionArgs, null, null, "booking_date");

        List<String> bookingList = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int bookingDateIndex = cursor.getColumnIndex("booking_date");
                if (bookingDateIndex != -1) {
                    String bookingDate = cursor.getString(bookingDateIndex);
                    bookingList.add(bookingDate);
                } else {
                    // Handle the case where 'booking_date' column is not found in the cursor
//                    showToast("'booking_date' column is not found in the cursor");
                }
            } while (cursor.moveToNext());

            cursor.close();
        } else {
            // Handle the case where the cursor is null or has no rows
            showToast("No upcoming booking dates");
        }

        // Create an adapter and set it to the ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                bookingList
        );

        bookingListView.setAdapter(adapter);
    }

}
