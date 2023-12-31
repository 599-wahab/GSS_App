package com.example.gss;

import static com.example.gss.AppDatabase.TABLE_BOOKINGS;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class BookingActivity extends AppCompatActivity {
    @SuppressLint("StaticFieldLeak")
    static EditText timeEditText;
    @SuppressLint("StaticFieldLeak")
    static EditText dateEditText;
 @Override
 protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.booking);
     ImageView heading = findViewById(R.id.heading);
     ImageView navButton = findViewById(R.id.navButton);
     navButton.setVisibility(View.VISIBLE);
     timeEditText = findViewById(R.id.timeEditText);
     dateEditText = findViewById(R.id.dateEditText);
     Button saveButton = findViewById(R.id.saveButton);
     // Get the user's name (you can replace this with your actual user name retrieval logic)
     String username = getIntent().getStringExtra("user_name");
     if (username == null) {
         // Handle the case where "user_name" is not found in the intent
         Toast.makeText(BookingActivity.this, "User name not found.", Toast.LENGTH_SHORT).show();
         finish(); // Finish the activity or handle it as per your app's requirements
     }
     // Handle navigation button click
     navButton.setOnClickListener(v -> {
         Intent intent = new Intent(BookingActivity.this, NavBarActivity.class);
         intent.putExtra("user_name", username);
         startActivity(intent);
     });

     // Handle the "Save" button click
     saveButton.setOnClickListener(v -> {
         String selectedTime = timeEditText.getText().toString();
         String selectedDate = dateEditText.getText().toString();

         if (selectedTime.isEmpty() || selectedDate.isEmpty()) {
             Toast.makeText(BookingActivity.this, "Please enter both date and time", Toast.LENGTH_SHORT).show();
             return;
         }

         int userId = getUserId(username);

         if (userId != -1) {
             String bookingDateTime = selectedDate + " " + selectedTime;
             long result = new AppDatabase(this).insertBookingData(username, userId,  bookingDateTime);

             if (result != -1) {
                 // Data saved successfully
                 // You can display a success message or perform other actions here
                 Toast.makeText(BookingActivity.this, "Data saved successfully :)", Toast.LENGTH_SHORT).show();
             } else {
                 // Data save failed
                 // You can display an error message or handle the failure here
                 Toast.makeText(BookingActivity.this, "Data save failed :(", Toast.LENGTH_SHORT).show();
             }
         } else {
             // Handle the case where the user ID is not found
             Toast.makeText(BookingActivity.this, "user ID is not found :(", Toast.LENGTH_SHORT).show();
         }
         Intent intent = new Intent(BookingActivity.this, UserDashboardActivity.class);
         intent.putExtra("user_name", username);
         startActivity(intent);
     });

 }
    public void showTimePickerDialog(View view) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void showDatePickerDialog(View view) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }
    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), this, hour, minute, true);
        }
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String selectedTime = hourOfDay + ":" + minute;

            // Check if the selected time is available in the bookings table
            if (isTimeAvailable(selectedTime)) {
                timeEditText.setText(selectedTime); // Update the timeEditText
            } else {
                // Time is not available, show a message
                Toast.makeText(getActivity(), "Selected time is not available. Please choose another time.", Toast.LENGTH_SHORT).show();
            }
        }
        private boolean isTimeAvailable(String selectedTime) {
            SQLiteDatabase db = new AppDatabase(getActivity()).getReadableDatabase();
            String[] columns = {"id"};
            String selection = "booking_date LIKE ?";
            String[] selectionArgs = {selectedTime + "%"};
            Cursor cursor = db.query(TABLE_BOOKINGS, columns, selection, selectionArgs, null, null, null);

            boolean isAvailable = cursor.getCount() == 0;
//            cursor.close();
            return isAvailable;
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            // Handle the date selection here
            String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth; // Adjust month (+1)
            dateEditText.setText(selectedDate); // Update the dateEditText
        }
    }
    private int getUserId(String username) {
        return new AppDatabase(this).getUserId(username);
    }
}
