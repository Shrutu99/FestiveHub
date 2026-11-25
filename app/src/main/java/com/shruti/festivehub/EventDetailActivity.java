
package com.shruti.festivehub;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class EventDetailActivity extends AppCompatActivity {

    private ImageButton tvBack;
    private TextView tvEventCategory;
    private TextView tvDetailEventTitle;
    private TextView tvEventDescription;
    private TextView tvEventDate;
    private TextView tvEventTime;
    private TextView tvEventVenue;

    private Button btnRegisterEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Initialize Views
        tvBack = findViewById(R.id.tvBack);
        tvEventCategory = findViewById(R.id.tvEventCategory);
        tvDetailEventTitle = findViewById(R.id.tvDetailEventTitle);
        tvEventDescription = findViewById(R.id.tvEventDescription);
        tvEventDate = findViewById(R.id.tvEventDate);
        tvEventTime = findViewById(R.id.tvEventTime);
        tvEventVenue = findViewById(R.id.tvEventVenue);

        btnRegisterEvent = findViewById(R.id.btnRegisterEvent);

        // Back Button
        tvBack.setOnClickListener(v -> finish());

        // Receive Event Details from HomeActivity
        String title = getIntent().getStringExtra("eventTitle");
        String category = getIntent().getStringExtra("eventCategory");
        String description = getIntent().getStringExtra("eventDescription");
        String date = getIntent().getStringExtra("eventDate");
        String time = getIntent().getStringExtra("eventTime");
        String venue = getIntent().getStringExtra("eventVenue");

        // Display Event Details
        tvDetailEventTitle.setText(title);
        tvEventCategory.setText(category);
        tvEventDescription.setText(description);
        tvEventDate.setText(date);
        tvEventTime.setText(time);
        tvEventVenue.setText(venue);
    }
}
