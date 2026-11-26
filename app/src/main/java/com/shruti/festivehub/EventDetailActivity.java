
package com.shruti.festivehub;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EventDetailActivity extends AppCompatActivity {

    // UI Variables
    private ImageButton tvBack;
    private TextView tvEventCategory;
    private TextView tvDetailEventTitle;
    private TextView tvEventDescription;
    private TextView tvEventDate;
    private TextView tvEventTime;
    private TextView tvEventVenue;

    private Button btnRegisterEvent;

    // Firebase Variables
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Event Details
    private String eventTitle;
    private String eventCategory;
    private String eventDescription;
    private String eventDate;
    private String eventTime;
    private String eventVenue;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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
        eventTitle = getIntent().getStringExtra("eventTitle");
        eventCategory = getIntent().getStringExtra("eventCategory");
        eventDescription = getIntent().getStringExtra("eventDescription");
        eventDate = getIntent().getStringExtra("eventDate");
        eventTime = getIntent().getStringExtra("eventTime");
        eventVenue = getIntent().getStringExtra("eventVenue");

        // Generate a stable ID for the current sample event
        eventId = eventTitle == null ? "" :
                eventTitle.toLowerCase()
                        .replaceAll("[^a-z0-9]+", "_")
                        .replaceAll("^_|_$", "");

        // Display Event Details
        tvDetailEventTitle.setText(eventTitle);
        tvEventCategory.setText(eventCategory);
        tvEventDescription.setText(eventDescription);
        tvEventDate.setText(eventDate);
        tvEventTime.setText(eventTime);
        tvEventVenue.setText(eventVenue);

        // Check Existing Registration
        checkExistingRegistration();

        // Register Button
        btnRegisterEvent.setOnClickListener(v -> registerForEvent());
    }

    // Check Whether Student Already Registered
    private void checkExistingRegistration() {

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null || eventId.isEmpty()) {
            return;
        }

        String userId = user.getUid();

        String registrationId = userId + "_" + eventId;

        db.collection("registrations")
                .document(registrationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        btnRegisterEvent.setText("Registered");
                        btnRegisterEvent.setEnabled(false);
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this,
                            "Unable to check registration status",
                            Toast.LENGTH_SHORT).show();
                });
    }

    // Register Student for Event
    private void registerForEvent() {

        FirebaseUser user = mAuth.getCurrentUser();

        // Check Login
        if (user == null) {

            Toast.makeText(this,
                    "Please login to register for an event",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        if (eventId.isEmpty()) {

            Toast.makeText(this,
                    "Event information is missing",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        String userId = user.getUid();

        // Unique Registration ID
        String registrationId = userId + "_" + eventId;

        // Disable Button While Saving
        btnRegisterEvent.setEnabled(false);
        btnRegisterEvent.setText("Registering...");

        // Check Duplicate Registration
        db.collection("registrations")
                .document(registrationId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        btnRegisterEvent.setText("Registered");

                        Toast.makeText(this,
                                "You are already registered for this event",
                                Toast.LENGTH_LONG).show();

                    } else {

                        // Prepare Registration Data
                        Map<String, Object> registration = new HashMap<>();

                        registration.put("registrationId", registrationId);
                        registration.put("userId", userId);
                        registration.put("eventId", eventId);
                        registration.put("eventTitle", eventTitle);
                        registration.put("eventCategory", eventCategory);
                        registration.put("eventDescription", eventDescription);
                        registration.put("eventDate", eventDate);
                        registration.put("eventTime", eventTime);
                        registration.put("eventVenue", eventVenue);
                        registration.put("registeredAt",
                                FieldValue.serverTimestamp());
                        registration.put("status", "Registered");

                        // Save Registration in Firestore
                        db.collection("registrations")
                                .document(registrationId)
                                .set(registration)
                                .addOnSuccessListener(aVoid -> {

                                    btnRegisterEvent.setText("Registered");

                                    Toast.makeText(this,
                                            "Successfully registered for "
                                                    + eventTitle,
                                            Toast.LENGTH_LONG).show();
                                })
                                .addOnFailureListener(e -> {

                                    btnRegisterEvent.setText("Register Now");
                                    btnRegisterEvent.setEnabled(true);

                                    Toast.makeText(this,
                                            "Registration failed: "
                                                    + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {

                    btnRegisterEvent.setText("Register Now");
                    btnRegisterEvent.setEnabled(true);

                    Toast.makeText(this,
                            "Something went wrong: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
