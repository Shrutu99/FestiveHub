
package com.shruti.festivehub;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;

    private Button btnUpdatePassword;
    private ImageButton tvBack;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
        tvBack = findViewById(R.id.tvBack);

        // Back Button
        tvBack.setOnClickListener(v -> finish());

        // Update Password Button
        btnUpdatePassword.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {

        String currentPassword =
                etCurrentPassword.getText().toString().trim();

        String newPassword =
                etNewPassword.getText().toString().trim();

        String confirmPassword =
                etConfirmPassword.getText().toString().trim();

        // Check empty fields
        if (currentPassword.isEmpty() ||
                newPassword.isEmpty() ||
                confirmPassword.isEmpty()) {

            Toast.makeText(this,
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Check minimum password length
        if (newPassword.length() < 6) {

            etNewPassword.setError(
                    "Password must be at least 6 characters"
            );
            etNewPassword.requestFocus();
            return;
        }

        // Check new password and confirmation
        if (!newPassword.equals(confirmPassword)) {

            etConfirmPassword.setError(
                    "Passwords do not match"
            );
            etConfirmPassword.requestFocus();
            return;
        }

        // Check whether new password is different
        if (currentPassword.equals(newPassword)) {

            etNewPassword.setError(
                    "New password must be different"
            );
            etNewPassword.requestFocus();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null || user.getEmail() == null) {

            Toast.makeText(this,
                    "Please login again",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Verify current password
        btnUpdatePassword.setEnabled(false);

        String email = user.getEmail();

        user.reauthenticate(
                EmailAuthProvider.getCredential(email, currentPassword)
        ).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                // Update Firebase password
                user.updatePassword(newPassword)
                        .addOnCompleteListener(updateTask -> {

                            btnUpdatePassword.setEnabled(true);

                            if (updateTask.isSuccessful()) {

                                Toast.makeText(this,
                                        "Password updated successfully!",
                                        Toast.LENGTH_LONG).show();

                                finish();

                            } else {

                                Toast.makeText(this,
                                        "Password update failed: "
                                                + updateTask.getException()
                                                .getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });

            } else {

                btnUpdatePassword.setEnabled(true);

                Toast.makeText(this,
                        "Current password is incorrect",
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
