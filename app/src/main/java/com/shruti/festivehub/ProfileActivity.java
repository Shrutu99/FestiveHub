
package com.shruti.festivehub;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ProfileActivity extends AppCompatActivity {

    TextView tvProfileName, tvProfileEmail, tvProfileRole;
    TextView tvInfoName, tvInfoEmail, tvNoRegisteredEvents;

    LinearLayout layoutRegisteredEvents;
    Button btnLogout;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Connect XML views
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileRole = findViewById(R.id.tvProfileRole);

        tvInfoName = findViewById(R.id.tvInfoName);
        tvInfoEmail = findViewById(R.id.tvInfoEmail);

        tvNoRegisteredEvents =
                findViewById(R.id.tvNoRegisteredEvents);

        layoutRegisteredEvents =
                findViewById(R.id.layoutRegisteredEvents);

        btnLogout = findViewById(R.id.btnLogout);

        // Check logged-in user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        String userId = currentUser.getUid();

        // Load profile
        loadProfile(userId, currentUser.getEmail());

        // Load registered events
        loadRegisteredEvents(userId);

        // Logout
        btnLogout.setOnClickListener(v -> logoutUser());
    }

    // Load student profile
    private void loadProfile(String userId, String authEmail) {

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {

                        String name = document.getString("fullName");
                        String email = document.getString("email");
                        String role = document.getString("role");

                        if (name == null || name.isEmpty()) {
                            name = "Student";
                        }

                        if (email == null || email.isEmpty()) {
                            email = authEmail != null
                                    ? authEmail
                                    : "Email unavailable";
                        }

                        if (role == null || role.isEmpty()) {
                            role = "student";
                        }

                        tvProfileName.setText(name);
                        tvInfoName.setText(name);

                        tvProfileEmail.setText(email);
                        tvInfoEmail.setText(email);

                        tvProfileRole.setText(
                                role.toUpperCase()
                        );

                    } else {
                        Toast.makeText(
                                this,
                                "Profile information not found.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed to load profile: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    // Load registered events
    private void loadRegisteredEvents(String userId) {

        layoutRegisteredEvents.removeAllViews();

        db.collection("registrations")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {

                        tvNoRegisteredEvents.setVisibility(
                                TextView.VISIBLE
                        );

                        return;
                    }

                    tvNoRegisteredEvents.setVisibility(
                            TextView.GONE
                    );

                    for (QueryDocumentSnapshot document :
                            querySnapshot) {

                        String title =
                                document.getString("eventTitle");

                        String category =
                                document.getString("eventCategory");

                        String date =
                                document.getString("eventDate");

                        String status =
                                document.getString("status");

                        addRegisteredEventCard(
                                title,
                                category,
                                date,
                                status
                        );
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed to load registered events: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    // Create registered event card
    private void addRegisteredEventCard(
            String title,
            String category,
            String date,
            String status) {

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(18, 16, 18, 16);
        card.setBackgroundColor(
                android.graphics.Color.WHITE
        );
        card.setElevation(3);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(0, 0, 0, 14);
        card.setLayoutParams(params);

        TextView tvTitle = new TextView(this);

        tvTitle.setText(safe(title));
        tvTitle.setTextSize(17);
        tvTitle.setTextColor(
                android.graphics.Color.rgb(18, 52, 90)
        );
        tvTitle.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        TextView tvDetails = new TextView(this);

        tvDetails.setText(
                "Category: " + safe(category) +
                        "\nDate: " + safe(date) +
                        "\nStatus: " + safe(status)
        );

        tvDetails.setTextSize(14);
        tvDetails.setTextColor(
                android.graphics.Color.DKGRAY
        );
        tvDetails.setPadding(0, 8, 0, 0);

        card.addView(tvTitle);
        card.addView(tvDetails);

        layoutRegisteredEvents.addView(card);
    }

    private String safe(String value) {
        return value == null || value.isEmpty()
                ? "Not available"
                : value;
    }

    // Logout
    private void logoutUser() {

        mAuth.signOut();

        Toast.makeText(
                this,
                "Logged out successfully!",
                Toast.LENGTH_SHORT
        ).show();

        redirectToLogin();
    }

    // Clear previous screens
    private void redirectToLogin() {

        Intent intent = new Intent(
                ProfileActivity.this,
                LoginActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}
