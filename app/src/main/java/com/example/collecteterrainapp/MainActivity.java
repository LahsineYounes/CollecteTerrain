package com.example.collecteterrainapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.collecteterrainapp.auth.LoginFragment;
import com.example.collecteterrainapp.auth.RegisterFragment;
import com.example.collecteterrainapp.database.DatabaseHelper;
import com.example.collecteterrainapp.manager.ManagerDashboardActivity;
import com.example.collecteterrainapp.representative.RepresentantDashboardActivity;

public class MainActivity extends AppCompatActivity {

    private Button loginToggle, signupToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Vérifier si l'utilisateur est déjà connecté
        SharedPreferences prefs = getSharedPreferences("session", Context.MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        String savedRole = prefs.getString("role", null);

        if (isLoggedIn && savedRole != null) {
            // Utilisateur déjà connecté, rediriger selon rôle
            Intent intent;
            if ("manager".equals(savedRole)) {
                intent = new Intent(this, ManagerDashboardActivity.class);
            } else {
                intent = new Intent(this, RepresentantDashboardActivity.class);
            }
            startActivity(intent);
            finish(); // Ne pas rester sur MainActivity
            return;
        }

        setContentView(R.layout.activity_main);

        loginToggle = findViewById(R.id.login_toggle);
        signupToggle = findViewById(R.id.signup_toggle);

        if (savedInstanceState == null) {
            showLogin();
        }

        loginToggle.setOnClickListener(v -> {
            if (!loginToggle.isSelected()) {
                showLogin();
            }
        });

        signupToggle.setOnClickListener(v -> {
            if (!signupToggle.isSelected()) {
                showSignup();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void showLogin() {
        loginToggle.setSelected(true);
        signupToggle.setSelected(false);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.fragment_container, new LoginFragment())
                .commit();
    }

    private void showSignup() {
        loginToggle.setSelected(false);
        signupToggle.setSelected(true);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.fragment_container, new RegisterFragment())
                .commit();
    }
}
