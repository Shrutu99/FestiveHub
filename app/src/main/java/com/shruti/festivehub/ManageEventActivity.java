
package com.shruti.festivehub;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ManageEventActivity extends AppCompatActivity {

    Button btnAddEvent;
    LinearLayout layoutEvents;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_event);

        btnAddEvent = findViewById(R.id.btnAddEvent);
        layoutEvents = findViewById(R.id.layoutEvents);

        db = FirebaseFirestore.getInstance();

        btnAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(
                    ManageEventActivity.this,
                    AddEventActivity.class
            );
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }

    // Read events from Firestore
    private void loadEvents() {

        layoutEvents.removeAllViews();

        db.collection("events")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {
                        showEmptyMessage();
                        return;
                    }

                    for (QueryDocumentSnapshot document :
                            querySnapshot) {

                        String eventId = document.getId();

                        String title = document.getString("eventTitle");
                        String category = document.getString("eventCategory");
                        String date = document.getString("eventDate");
                        String time = document.getString("eventTime");
                        String venue = document.getString("eventVenue");

                        addEventCard(
                                eventId,
                                title,
                                category,
                                date,
                                time,
                                venue
                        );
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Failed to load events: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // Empty message
    private void showEmptyMessage() {

        TextView emptyText = new TextView(this);

        emptyText.setText("No events available yet.");
        emptyText.setTextSize(15);
        emptyText.setGravity(Gravity.CENTER);
        emptyText.setPadding(20, 40, 20, 40);

        layoutEvents.addView(emptyText);
    }

    // Create event card
    private void addEventCard(
            String eventId,
            String title,
            String category,
            String date,
            String time,
            String venue) {

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20, 18, 20, 18);
        card.setBackgroundColor(
                android.graphics.Color.WHITE
        );
        card.setElevation(4);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, 18);
        card.setLayoutParams(cardParams);

        // Event title
        TextView tvTitle = new TextView(this);

        tvTitle.setText(
                title != null ? title : "Untitled Event"
        );

        tvTitle.setTextSize(18);
        tvTitle.setTextColor(
                android.graphics.Color.rgb(18, 52, 90)
        );
        tvTitle.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        // Event details
        TextView tvDetails = new TextView(this);

        tvDetails.setText(
                "Category: " + safe(category) +
                        "\nDate: " + safe(date) +
                        "\nTime: " + safe(time) +
                        "\nVenue: " + safe(venue)
        );

        tvDetails.setTextSize(14);
        tvDetails.setTextColor(
                android.graphics.Color.DKGRAY
        );
        tvDetails.setPadding(0, 10, 0, 12);

        // Edit and Delete buttons
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

        Button btnEdit = new Button(this);
        btnEdit.setText("EDIT");
        btnEdit.setTextColor(android.graphics.Color.WHITE);
        btnEdit.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.rgb(18, 52, 90)
                )
        );

        Button btnDelete = new Button(this);
        btnDelete.setText("DELETE");
        btnDelete.setTextColor(android.graphics.Color.WHITE);
        btnDelete.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.rgb(220, 53, 69)
                )
        );

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        buttonParams.setMargins(4, 0, 4, 0);

        buttonLayout.addView(btnEdit, buttonParams);
        buttonLayout.addView(btnDelete, buttonParams);

        card.addView(tvTitle);
        card.addView(tvDetails);
        card.addView(buttonLayout);

        layoutEvents.addView(card);

        // Edit event
        btnEdit.setOnClickListener(v -> {

            Intent intent = new Intent(
                    ManageEventActivity.this,
                    AddEventActivity.class
            );

            intent.putExtra("eventId", eventId);

            startActivity(intent);
        });

        // Delete event
        btnDelete.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Delete Event")
                    .setMessage(
                            "Are you sure you want to delete " +
                                    safe(title) + "?"
                    )
                    .setPositiveButton("Delete", (dialog, which) -> {

                        db.collection("events")
                                .document(eventId)
                                .delete()
                                .addOnSuccessListener(unused -> {

                                    Toast.makeText(
                                            this,
                                            "Event deleted successfully!",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    loadEvents();
                                })
                                .addOnFailureListener(e -> {

                                    Toast.makeText(
                                            this,
                                            "Delete failed: " + e.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show();
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private String safe(String value) {
        return value == null || value.isEmpty()
                ? "Not available"
                : value;
    }
}
