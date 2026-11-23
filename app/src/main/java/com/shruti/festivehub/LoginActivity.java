
package com.shruti.festivehub;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;

public class LoginActivity extends AppCompatActivity {

    TextView tvSignUp, tvForgotPassword;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnLogin = findViewById(R.id.btnLogin);

        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    SignUpActivity.class
            );
            startActivity(intent);
        });

    }
}
