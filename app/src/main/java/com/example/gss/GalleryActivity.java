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

public class GalleryActivity extends AppCompatActivity {
    private ImageView[] galleryImages;
    private int currentImageIndex = 0;
    private List<Event> galleryList;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery);
        ImageView navButton = findViewById(R.id.navButton);
        navButton.setVisibility(View.VISIBLE);
        LinearLayout gallery1Layout = findViewById(R.id.gallery1Layout);
        LinearLayout gallery2Layout = findViewById(R.id.gallery2Layout);
        LinearLayout gallery3Layout = findViewById(R.id.gallery3Layout);
        LinearLayout gallery4Layout = findViewById(R.id.gallery4Layout);

        galleryImages = new ImageView[]{
                (ImageView) gallery1Layout.getChildAt(0),
                (ImageView) gallery2Layout.getChildAt(0),
                (ImageView) gallery3Layout.getChildAt(0),
                (ImageView) gallery4Layout.getChildAt(0)
        };

        for (ImageView image : galleryImages) {
            image.setVisibility(View.VISIBLE);
            image.setOnClickListener(v -> showFullResolutionImage(image));
        }
        // Get the user's name (you can replace this with your actual user name retrieval logic)
        String username = getIntent().getStringExtra("user_name");
        // Handle navigation button click
        loadEventData();
        navButton.setOnClickListener(v -> {
            Intent intent = new Intent(GalleryActivity.this, NavBarActivity.class);
            intent.putExtra("user_name", username);
            startActivity(intent);
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void showFullResolutionImage(final ImageView galleryImage) {
        // Create a Dialog
        final Dialog dialog = new Dialog(this);

        // Create an ImageView and set its content to the gallery image
        final ImageView dialogImageView = new ImageView(this);
        dialogImageView.setImageDrawable(galleryImage.getDrawable());

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
                                currentImageIndex = (currentImageIndex + 1) % galleryImages.length;
                            } else {
                                // Swipe to the right
                                currentImageIndex = (currentImageIndex - 1 + galleryImages.length) % galleryImages.length;
                            }

                            // Update the dialog image
                            dialogImageView.setImageDrawable(galleryImages[currentImageIndex].getDrawable());
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
            galleryList = appDatabase.getAllGalleryImages();
        }
        // Check if there are events in the database
        if (galleryList.isEmpty()) {
            Toast.makeText(context, "No events found in the database", Toast.LENGTH_SHORT).show();
        } else {
            // Set images dynamically based on the retrieved data
            setEventImages();
        }
    }
    private void setEventImages() {
        // Set images dynamically based on the number of events
        for (int i = 0; i < galleryImages.length && i < galleryList.size(); i++) {
            Event event = galleryList.get(i);
            // Use Picasso to load and display images
            loadImageWithPicasso(event.getImagePath(), galleryImages[i]);
            galleryImages[i].setVisibility(View.VISIBLE);
            int finalI = i;
            galleryImages[i].setOnClickListener(v -> showFullResolutionImage(galleryImages[finalI]));
        }
    }
    private void loadImageWithPicasso(String imagePath, ImageView imageView) {
        // Use Picasso to load and display images
        Picasso.get().load(imagePath).into(imageView);
    }
}
