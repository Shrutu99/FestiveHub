
package com.shruti.festivehub;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            FirebaseUser currentUser = mAuth.getCurrentUser();

            Intent intent;

            if (currentUser != null) {

                // Student is already logged in
                intent = new Intent(
                        MainActivity.this,
                        HomeActivity.class
                );

            } else {

                // Student needs to log in
                intent = new Intent(
                        MainActivity.this,
                        LoginActivity.class
                );
            }

            startActivity(intent);
            finish();

        }, 2000);
    }
}
