
package com.shruti.festivehub;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Calendar;

public class AddEventActivity extends AppCompatActivity {

    EditText etEventTitle, etEventCategory;
    EditText etEventDescription, etEventDate;
    EditText etEventTime, etEventVenue;

    Button btnSaveEvent;

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    // Used when editing an existing event
    String eventId;
    boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Connect XML views
        etEventTitle = findViewById(R.id.etEventTitle);
        etEventCategory = findViewById(R.id.etEventCategory);
        etEventDescription = findViewById(R.id.etEventDescription);
        etEventDate = findViewById(R.id.etEventDate);
        etEventTime = findViewById(R.id.etEventTime);
        etEventVenue = findViewById(R.id.etEventVenue);

        btnSaveEvent = findViewById(R.id.btnSaveEvent);

        // Check whether an existing event is being edited
        eventId = getIntent().getStringExtra("eventId");

        if (eventId != null && !eventId.isEmpty()) {
            isEditMode = true;
            btnSaveEvent.setText("UPDATE EVENT");
            loadEventDetails();
        }

        // Date picker
        etEventDate.setOnClickListener(v -> showDatePicker());

        // Time picker
        etEventTime.setOnClickListener(v -> showTimePicker());

        // Save or update
        btnSaveEvent.setOnClickListener(v -> saveEvent());
    }

    // Load existing event details
    private void loadEventDetails() {

        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {

                        etEventTitle.setText(
                                document.getString("eventTitle"));

                        etEventCategory.setText(
                                document.getString("eventCategory"));

                        etEventDescription.setText(
                                document.getString("eventDescription"));

                        etEventDate.setText(
                                document.getString("eventDate"));

                        etEventTime.setText(
                                document.getString("eventTime"));

                        etEventVenue.setText(
                                document.getString("eventVenue"));

                    } else {
                        Toast.makeText(
                                this,
                                "Event not found.",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed to load event: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    // Date picker
    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {

                    String date = String.format(
                            Locale.getDefault(),
                            "%02d/%02d/%04d",
                            dayOfMonth,
                            month + 1,
                            year
                    );

                    etEventDate.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.getDatePicker().setMinDate(
                System.currentTimeMillis() - 1000
        );

        dialog.show();
    }

    // Time picker
    private void showTimePicker() {

        Calendar calendar = Calendar.getInstance();

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {

                    String time = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            hourOfDay,
                            minute
                    );

                    etEventTime.setText(time);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );

        dialog.show();
    }

    // Save or update event
    private void saveEvent() {

        String title = etEventTitle.getText().toString().trim();
        String category = etEventCategory.getText().toString().trim();
        String description = etEventDescription.getText().toString().trim();
        String date = etEventDate.getText().toString().trim();
        String time = etEventTime.getText().toString().trim();
        String venue = etEventVenue.getText().toString().trim();

        // Validate fields
        if (title.isEmpty()) {
            etEventTitle.setError("Enter event title");
            etEventTitle.requestFocus();
            return;
        }

        if (category.isEmpty()) {
            etEventCategory.setError("Enter event category");
            etEventCategory.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            etEventDescription.setError("Enter event description");
            etEventDescription.requestFocus();
            return;
        }

        if (date.isEmpty()) {
            etEventDate.setError("Select event date");
            return;
        }

        if (time.isEmpty()) {
            etEventTime.setError("Select event time");
            return;
        }

        if (venue.isEmpty()) {
            etEventVenue.setError("Enter event venue");
            etEventVenue.requestFocus();
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(
                    this,
                    "Please login first.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Prepare event data
        Map<String, Object> event = new HashMap<>();

        event.put("eventTitle", title);
        event.put("eventCategory", category);
        event.put("eventDescription", description);
        event.put("eventDate", date);
        event.put("eventTime", time);
        event.put("eventVenue", venue);

        btnSaveEvent.setEnabled(false);

        if (isEditMode) {

            btnSaveEvent.setText("UPDATING...");

            db.collection("events")
                    .document(eventId)
                    .update(event)
                    .addOnSuccessListener(unused -> {

                        Toast.makeText(
                                this,
                                "Event updated successfully!",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();
                    })
                    .addOnFailureListener(e -> {

                        btnSaveEvent.setEnabled(true);
                        btnSaveEvent.setText("UPDATE EVENT");

                        Toast.makeText(
                                this,
                                "Update failed: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    });

        } else {

            event.put("createdAt", FieldValue.serverTimestamp());
            event.put("createdBy", mAuth.getCurrentUser().getUid());

            btnSaveEvent.setText("SAVING...");

            db.collection("events")
                    .add(event)
                    .addOnSuccessListener(documentReference -> {

                        Toast.makeText(
                                this,
                                "Event added successfully!",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();
                    })
                    .addOnFailureListener(e -> {

                        btnSaveEvent.setEnabled(true);
                        btnSaveEvent.setText("SAVE EVENT");

                        Toast.makeText(
                                this,
                                "Failed to add event: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    });
        }
    }
}
