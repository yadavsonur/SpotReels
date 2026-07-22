package com.sonu.spotreels.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sonu.spotreels.R;
import com.sonu.spotreels.ui.main.MainActivity;

public class AuthActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnSignup;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    private static final int MIN_PASSWORD_LENGTH = 6;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);
        progressBar = findViewById(R.id.progressBar);

        // ✅ Always hide progress initially
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        auth = FirebaseAuth.getInstance();

        // ✅ EXTRA SAFETY
        if (auth == null) {
            Toast.makeText(this, "Authentication unavailable", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnSignup.setOnClickListener(v -> signup());
        btnLogin.setOnClickListener(v -> login());
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser current = auth.getCurrentUser();
        if (current != null) {
            // ✅ User already logged in → skip auth screen
            navigateToMain();
        }
    }

    // ✅ SIGN UP – FULLY SAFE
    private void signup() {

        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (!isValidInput(email, pass)) return;

        setUiEnabled(false);
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {

                    setUiEnabled(true);
                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {

                        // ✅ OPTIONAL (SAFE FOR LATER USE)
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // user.sendEmailVerification(); // Enable later if needed
                        }

                        navigateToMain();

                    } else {
                        String msg = (task.getException() == null)
                                ? "Signup failed"
                                : task.getException().getMessage();

                        Toast.makeText(AuthActivity.this,
                                "Signup failed: " + msg,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ✅ LOGIN – FULLY SAFE
    private void login() {

        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (!isValidInput(email, pass)) return;

        setUiEnabled(false);
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {

                    setUiEnabled(true);
                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        navigateToMain();
                    } else {

                        String msg = (task.getException() == null)
                                ? "Login failed"
                                : task.getException().getMessage();

                        Toast.makeText(AuthActivity.this,
                                "Login failed: " + msg,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ✅ INPUT VALIDATION – HARDENED
    private boolean isValidInput(@NonNull String email, @NonNull String pass) {

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (pass.length() < MIN_PASSWORD_LENGTH) {
            Toast.makeText(this,
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // ✅ PREVENT DOUBLE TAPS
    private void setUiEnabled(boolean enabled) {
        if (btnLogin != null) btnLogin.setEnabled(enabled);
        if (btnSignup != null) btnSignup.setEnabled(enabled);
        if (etEmail != null) etEmail.setEnabled(enabled);
        if (etPassword != null) etPassword.setEnabled(enabled);
    }

    // ✅ SAFE NAVIGATION
    private void navigateToMain() {
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        startActivity(intent);
        finish();  // ✅ User cannot return to Auth screen using back
    }
}
