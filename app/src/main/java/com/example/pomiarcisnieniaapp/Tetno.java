package com.example.pomiarcisnieniaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Tetno extends AppCompatActivity {
    private LinearLayout bottomNav;
    private ImageView navHome, navHeart, navPills, navSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tetno);

        // Inicjalizacja bottom nav
        bottomNav = findViewById(R.id.custom_bottom_nav);
        navHome = findViewById(R.id.nav_home);
        navHeart = findViewById(R.id.nav_heart);
        navPills = findViewById(R.id.nav_pills);
        navSettings = findViewById(R.id.nav_settings);

        // Ustawienie akcji kliknięcia dla każdej ikony
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Przejście do MainActivity
                Intent intent = new Intent(Tetno.this, MainActivity.class);
                startActivity(intent);
                finish(); // Zakończenie bieżącej aktywności
            }
        });

        navHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Przejście do aktywności Tętno
                Intent intent = new Intent(Tetno.this, Tetno.class);
                startActivity(intent);
                finish(); // Zakończenie bieżącej aktywności
            }
        });

        navPills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Przejście do aktywności Leki
                Intent intent = new Intent(Tetno.this, Leki.class);
                startActivity(intent);
                finish(); // Zakończenie bieżącej aktywności
            }
        });

        navSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Przejście do aktywności Ustawienia
                Intent intent = new Intent(Tetno.this, Ustawienia.class);
                startActivity(intent);
                finish(); // Zakończenie bieżącej aktywności
            }
        });
    }
}


