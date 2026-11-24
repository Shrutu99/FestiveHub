
package com.shruti.festivehub;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    EditText etFullName, etSignupEmail;
    EditText etSignupPassword, etConfirmPassword;

    Button btnSignUp;
    TextView tvLogin;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Connect XML views
        etFullName = findViewById(R.id.etFullName);
        etSignupEmail = findViewById(R.id.etSignupEmail);
        etSignupPassword = findViewById(R.id.etSignupPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnSignUp = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);

        // Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Sign Up button
        btnSignUp.setOnClickListener(v -> registerUser());

        // Login navigation
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(
                    SignUpActivity.this,
                    LoginActivity.class
            );
            startActivity(intent);
            finish();
        });
    }

    private void registerUser() {

        String name = etFullName.getText().toString().trim();
        String email = etSignupEmail.getText().toString().trim();
        String password = etSignupPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Empty field validation
        if (TextUtils.isEmpty(name)) {
            etFullName.setError("Enter your full name");
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etSignupEmail.setError("Enter your email");
            etSignupEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etSignupEmail.setError("Enter a valid email address");
            etSignupEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etSignupPassword.setError("Enter your password");
            etSignupPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etSignupPassword.setError(
                    "Password must be at least 6 characters"
            );
            etSignupPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Prevent multiple clicks
        btnSignUp.setEnabled(false);
        btnSignUp.setText("PLEASE WAIT...");

        // Create Firebase Authentication account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        if (mAuth.getCurrentUser() == null) {
                            btnSignUp.setEnabled(true);
                            btnSignUp.setText("SIGN UP");
                            Toast.makeText(
                                    SignUpActivity.this,
                                    "Unable to get user details.",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        String userId = mAuth.getCurrentUser().getUid();

                        // Prepare user details
                        Map<String, Object> user = new HashMap<>();

                        user.put("userId", userId);
                        user.put("fullName", name);
                        user.put("email", email);
                        user.put("role", "student");
                        user.put("createdAt",
                                FieldValue.serverTimestamp());

                        // Save details in Firestore
                        db.collection("users")
                                .document(userId)
                                .set(user)
                                .addOnSuccessListener(unused -> {

                                    Toast.makeText(
                                            SignUpActivity.this,
                                            "Registration successful!",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    // Sign out and navigate to Login
                                    mAuth.signOut();

                                    Intent intent = new Intent(
                                            SignUpActivity.this,
                                            LoginActivity.class
                                    );

                                    intent.setFlags(
                                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    );

                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {

                                    btnSignUp.setEnabled(true);
                                    btnSignUp.setText("SIGN UP");

                                    Toast.makeText(
                                            SignUpActivity.this,
                                            "Account created, but profile saving failed: "
                                                    + e.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show();
                                });

                    } else {

                        btnSignUp.setEnabled(true);
                        btnSignUp.setText("SIGN UP");

                        String error = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration failed";

                        Toast.makeText(
                                SignUpActivity.this,
                                error,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}
