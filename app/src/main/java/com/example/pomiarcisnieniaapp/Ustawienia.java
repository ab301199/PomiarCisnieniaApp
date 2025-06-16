package com.example.pomiarcisnieniaapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Ustawienia extends AppCompatActivity {

    private ImageView navHome, navHeart, navPills, navSettings;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ustawienia);

        navHome     = findViewById(R.id.nav_home);
        navHeart    = findViewById(R.id.nav_heart);
        navPills    = findViewById(R.id.nav_pills);
        navSettings = findViewById(R.id.nav_settings);
        logoutButton = findViewById(R.id.wylogujbtn);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        navHeart.setOnClickListener(v -> {
            startActivity(new Intent(this, Tetno.class));
            finish();
        });

        navPills.setOnClickListener(v -> {
            startActivity(new Intent(this, Leki.class));
            finish();
        });

        navSettings.setOnClickListener(v -> {
            // Jesteśmy już w ustawieniach
        });

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, logowanie.class));
            finish();
        });
    }
}

