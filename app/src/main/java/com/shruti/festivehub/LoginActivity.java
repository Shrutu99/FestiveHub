
package com.shruti.festivehub;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etLoginPassword;
    TextView tvSignUp, tvForgotPassword;
    Button btnLogin;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Connect XML views
        etEmail = findViewById(R.id.etEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);

        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Login button
        btnLogin.setOnClickListener(v -> loginUser());

        // Signup navigation
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    SignUpActivity.class
            );
            startActivity(intent);
        });

        // Forgot password
        tvForgotPassword.setOnClickListener(v -> resetPassword());
    }

    private void loginUser() {

        String email = etEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString();

        // Validate email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter your email");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            etLoginPassword.setError("Enter your password");
            etLoginPassword.requestFocus();
            return;
        }

        // Disable button during login
        btnLogin.setEnabled(false);
        btnLogin.setText("PLEASE WAIT...");

        // Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        if (mAuth.getCurrentUser() == null) {
                            showLoginError("Unable to get user details.");
                            return;
                        }

                        String userId =
                                mAuth.getCurrentUser().getUid();

                        // Check user role in Firestore
                        db.collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(document -> {

                                    if (!document.exists()) {
                                        mAuth.signOut();
                                        showLoginError("User profile not found.");
                                        return;
                                    }

                                    String role = document.getString("role");

                                    if (role == null) {
                                        mAuth.signOut();
                                        showLoginError("User role not assigned.");
                                        return;
                                    }

                                    Intent intent;

                                    if (role.equalsIgnoreCase("admin")) {

                                        // Admin Dashboard
                                        intent = new Intent(
                                                LoginActivity.this,
                                                AdminDashboardActivity.class
                                        );

                                    } else if (role.equalsIgnoreCase("student")) {

                                        // Student Dashboard
                                        intent = new Intent(
                                                LoginActivity.this,
                                                HomeActivity.class
                                        );

                                    } else if (role.equalsIgnoreCase("volunteer")) {

                                        // Volunteer Dashboard is not created yet
                                        mAuth.signOut();
                                        showLoginError(
                                                "Volunteer dashboard is not ready yet."
                                        );
                                        return;

                                    } else {

                                        mAuth.signOut();
                                        showLoginError("Invalid user role.");
                                        return;
                                    }

                                  // Clear previous screens
                                    intent.setFlags(
                                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    );

                                    startActivity(intent);
                                    finish();

                                    btnLogin.setEnabled(true);
                                    btnLogin.setText("LOGIN");

                                    // Student Home Page navigation
                                    // will be added when HomeActivity is created.
                                })
                                .addOnFailureListener(e -> {

                                    mAuth.signOut();

                                    showLoginError(
                                            "Unable to verify user role: "
                                                    + e.getMessage()
                                    );
                                });

                    } else {

                        String error = task.getException() != null
                                ? task.getException().getMessage()
                                : "Login failed.";

                        showLoginError(error);
                    }
                });
    }

    private void resetPassword() {

        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter your email first");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            LoginActivity.this,
                            "Password reset email sent. Check your inbox.",
                            Toast.LENGTH_LONG
                    ).show();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            LoginActivity.this,
                            e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void showLoginError(String message) {

        btnLogin.setEnabled(true);
        btnLogin.setText("LOGIN");

        Toast.makeText(
                LoginActivity.this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}