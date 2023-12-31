package com.example.gss;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class UserDashboardActivity extends AppCompatActivity {
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Do you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> logout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logout() {
        Intent intent = new Intent(UserDashboardActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_dashboard);
        // Initialize UI elements
        ImageView heading = findViewById(R.id.heading);
        ImageView navButton = findViewById(R.id.navButton);
        CardView cardView = findViewById(R.id.cardView);
        // Set the initial visibility
        heading.setVisibility(View.VISIBLE);
        navButton.setVisibility(View.VISIBLE);
        cardView.setVisibility(View.VISIBLE);
        String username = getIntent().getStringExtra("user_name");
        TextView welcomeTextView = findViewById(R.id.userName);
        welcomeTextView.setText("Welcome " + username);
        // Handle navigation button click
        navButton.setOnClickListener(v -> {
            Intent intent = new Intent(UserDashboardActivity.this, NavBarActivity.class);
            intent.putExtra("user_name", username);
            startActivity(intent);
        });
        // Handle button clicks for different activities
        findViewById(R.id.bookingLayout).setOnClickListener(v ->{
            Intent intent = new Intent(UserDashboardActivity.this,BookingActivity.class);
            intent.putExtra("user_name", username);
            startActivity(intent);
        });
        findViewById(R.id.calenderLayout).setOnClickListener(v ->{
            Intent intent = new Intent(UserDashboardActivity.this,CalendarActivity.class);
            intent.putExtra("user_name", username);
            startActivity(intent);
        });
        findViewById(R.id.evntsLayout).setOnClickListener(v ->{
            Intent intent = new Intent(UserDashboardActivity.this,EventsActivity.class);
            intent.putExtra("user_name", username);
            startActivity(intent);
        });
        findViewById(R.id.galleryLayout).setOnClickListener(v ->{
            Intent intent = new Intent(UserDashboardActivity.this,GalleryActivity.class);
            intent.putExtra("user_name", username);
            startActivity(intent);
        });
    }
}
