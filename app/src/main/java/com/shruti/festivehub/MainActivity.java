
package com.shruti.festivehub;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (currentUser == null) {

                openLoginScreen();

            } else {

                checkUserRole(currentUser.getUid());
            }

        }, 2000);
    }

    // Check the logged-in user's role from Firestore
    private void checkUserRole(String userId) {

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    if (!document.exists()) {

                        mAuth.signOut();
                        openLoginScreen();
                        return;
                    }

                    String role = document.getString("role");

                    if (role == null) {

                        mAuth.signOut();
                        openLoginScreen();
                        return;
                    }

                    Intent intent;

                    if (role.equalsIgnoreCase("admin")) {

                        // Admin Dashboard
                        intent = new Intent(
                                MainActivity.this,
                                AdminDashboardActivity.class
                        );

                    } else if (role.equalsIgnoreCase("student")) {

                        // Student Home
                        intent = new Intent(
                                MainActivity.this,
                                HomeActivity.class
                        );

                    } else {

                        // Volunteer dashboard not ready yet
                        mAuth.signOut();

                        Toast.makeText(
                                MainActivity.this,
                                "Volunteer dashboard is not ready yet.",
                                Toast.LENGTH_LONG
                        ).show();

                        openLoginScreen();
                        return;
                    }

                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );

                    startActivity(intent);
                    finish();

                })
                .addOnFailureListener(e -> {

                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    Toast.makeText(
                            MainActivity.this,
                            "Unable to verify user role: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                    // Keep the signed-in user authenticated.
                    // Do not send them to the wrong dashboard.
                    openLoginScreen();
                });
    }

    // Open Login screen
    private void openLoginScreen() {

        Intent intent = new Intent(
                MainActivity.this,
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
