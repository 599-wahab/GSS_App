package com.example.gss;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.util.List;

public class EventsActivity extends AppCompatActivity {
    private ImageView[] eventImages;
    private int currentImageIndex = 0;
    private List<Event> eventList;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.events);
        ImageView navButton = findViewById(R.id.navButton);
        navButton.setVisibility(View.VISIBLE);
        LinearLayout event1Layout = findViewById(R.id.event1Layout);
        LinearLayout event2Layout = findViewById(R.id.event2Layout);
        LinearLayout event3Layout = findViewById(R.id.event3Layout);
        LinearLayout event4Layout = findViewById(R.id.event4Layout);
        eventImages = new ImageView[]{
                (ImageView) event1Layout.getChildAt(0),
                (ImageView) event2Layout.getChildAt(0),
                (ImageView) event3Layout.getChildAt(0),
                (ImageView) event4Layout.getChildAt(0)
        };

        for (ImageView image : eventImages) {
            image.setVisibility(View.VISIBLE);
            image.setOnClickListener(v -> showFullResolutionImage(image));
        }
        // Get the user's name (you can replace this with your actual user name retrieval logic)
        String username = getIntent().getStringExtra("user_name");
        // Load event data from the database
        loadEventData();
        // Handle navigation button click
        navButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventsActivity.this, NavBarActivity.class);
            intent.putExtra("user_name", username);
            startActivity(intent);
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void showFullResolutionImage(final ImageView eventImage) {
        // Create a Dialog
        final Dialog dialog = new Dialog(this);

        // Create an ImageView and set its content to the event image
        final ImageView dialogImageView = new ImageView(this);
        dialogImageView.setImageDrawable(eventImage.getDrawable());

        // Add the ImageView to the dialog
        dialog.setContentView(dialogImageView);

        // Set touch event listeners to detect swipe gestures
        dialogImageView.setOnTouchListener(new View.OnTouchListener() {
            private float x1;
            private static final float MIN_SWIPE_DISTANCE = 150;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        break;

                    case MotionEvent.ACTION_UP:
                        float x2 = event.getX();
                        float deltaX = x2 - x1;

                        // Detect swipe to the right
                        if (Math.abs(deltaX) > MIN_SWIPE_DISTANCE) {
                            if (deltaX < 0) {
                                // Swipe to the left
                                currentImageIndex = (currentImageIndex + 1) % eventImages.length;
                            } else {
                                // Swipe to the right
                                currentImageIndex = (currentImageIndex - 1 + eventImages.length) % eventImages.length;
                            }

                            // Update the dialog image
                            dialogImageView.setImageDrawable(eventImages[currentImageIndex].getDrawable());
                        }
                        break;
                }
                return true;
            }
        });

        // Set a click listener to close the dialog when the full-resolution image is clicked
        dialogImageView.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    private void loadEventData() {
        // Retrieve event data from the database
        try (AppDatabase appDatabase = new AppDatabase(this)) {
            eventList = appDatabase.getAllEventImages();
        }
        // Check if there are events in the database
        if (eventList.isEmpty()) {
            Toast.makeText(context, "No events found in the database", Toast.LENGTH_SHORT).show();
        } else {
            // Set images dynamically based on the retrieved data
            setEventImages();
        }
    }
    private void setEventImages() {
        // Set images dynamically based on the number of events
        for (int i = 0; i < eventImages.length && i < eventList.size(); i++) {
            Event event = eventList.get(i);
            // Use Picasso to load and display images
            loadImageWithPicasso(event.getImagePath(), eventImages[i]);
            eventImages[i].setVisibility(View.VISIBLE);
            int finalI = i;
            eventImages[i].setOnClickListener(v -> showFullResolutionImage(eventImages[finalI]));
        }
    }
    private void loadImageWithPicasso(String imagePath, ImageView imageView) {
        // Use Picasso to load and display images
        Picasso.get().load(imagePath).into(imageView);
    }
}
