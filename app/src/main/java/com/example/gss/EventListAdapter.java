package com.example.gss;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.util.List;

// EventListAdapter.java
public class EventListAdapter extends ArrayAdapter<Event> {

    public EventListAdapter(Context context, List<Event> events) {
        super(context, 0, events);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_layout, parent, false);
        }

        // Get the current Event
        Event currentEvent = getItem(position);

        // Find the ImageView and TextView in the layout
        ImageView eventImageView = listItemView.findViewById(R.id.eventImageView);
        TextView eventNameTextView = listItemView.findViewById(R.id.eventNameTextView);

        // Set the image URI for the ImageView
        if (currentEvent != null) {
            String imagePath = currentEvent.getImagePath();
            Picasso.get().load(imagePath).into(eventImageView);
            eventNameTextView.setText(currentEvent.getEventName());
        }

        return listItemView;
    }

}

