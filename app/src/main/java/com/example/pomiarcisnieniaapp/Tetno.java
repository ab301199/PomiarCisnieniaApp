package com.example.pomiarcisnieniaapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class Tetno extends AppCompatActivity {
    FirebaseAuth auth;

    FirebaseUser user;


    Button dodajPomiarButtonTetno;
    EditText tetnoInput;

    DatabaseReference database;
    private LinearLayout bottomNav;
    private ImageView navHome, navHeart, navPills, navSettings;
    private TextView selectedDateTextViewTetno;

    private String selectedDateTetno = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tetno);

        dodajPomiarButtonTetno = findViewById(R.id.dodaj_pomiar_btn_tetno);
        tetnoInput = findViewById(R.id.tetno_input);
        database = FirebaseDatabase.getInstance().getReference("users");
        auth = FirebaseAuth.getInstance();




        bottomNav = findViewById(R.id.custom_bottom_nav);
        navHome = findViewById(R.id.nav_home);
        navHeart = findViewById(R.id.nav_heart);
        navPills = findViewById(R.id.nav_pills);
        navSettings = findViewById(R.id.nav_settings);
        selectedDateTextViewTetno = findViewById(R.id.selected_date_tetno);

        user = auth.getCurrentUser();

        selectedDateTextViewTetno.setOnClickListener(view -> showDatePicker());

        dodajPomiarButtonTetno.setOnClickListener(view -> {
            String tetnoStr = tetnoInput.getText().toString();


            if (selectedDateTetno.isEmpty()) {
                Toast.makeText(Tetno.this, "Wybierz datę przed zapisaniem pomiaru.", Toast.LENGTH_SHORT).show();
                return;
            }

            long selectedDateTimestamp = getTimestampFromDate(selectedDateTetno);
            if (selectedDateTimestamp > System.currentTimeMillis()) {
                Toast.makeText(Tetno.this, "Data nie może być z przyszłości.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!tetnoStr.isEmpty()) {
                try {
                    int tetno = Integer.parseInt(tetnoStr);


                    if (tetno >= 30 && tetno <= 220) {


                        PomiarTetno nowyPomiarTetno = new PomiarTetno(tetno, selectedDateTimestamp);


                        database.child(user.getUid()).child("pomiar").child(formatDateForDatabase(selectedDateTetno)).setValue(nowyPomiarTetno)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {

                                        runOnUiThread(() -> Toast.makeText(Tetno.this, "Pomiar zapisany", Toast.LENGTH_SHORT).show());
                                    } else {

                                        runOnUiThread(() -> {
                                            Log.e("FirebaseError", "Błąd zapisu do bazy: " + task.getException());
                                            Toast.makeText(Tetno.this, "Błąd zapisu do bazy danych", Toast.LENGTH_SHORT).show();
                                        });
                                    }
                                });
                    } else {
                        Toast.makeText(Tetno.this, "Podaj wartości tętna w poprawnym zakresie", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(Tetno.this, "Wprowadź poprawną liczbę", Toast.LENGTH_SHORT).show();
                }
            }
        });


        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Przejście do MainActivity
                Intent intent = new Intent(Tetno.this, MainActivity.class);
                startActivity(intent);
                finish();
            }


        });



        navHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Przejście do aktywności Tętno
                Intent intent = new Intent(Tetno.this, Tetno.class);
                startActivity(intent);
                finish();
            }
        });

        navPills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Przejście do aktywności Leki
                Intent intent = new Intent(Tetno.this, Leki.class);
                startActivity(intent);
                finish();
            }
        });

        navSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Tetno.this, Ustawienia.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private long getTimestampFromDate(String date) {
        String[] dateParts = date.split("/");
        int month = Integer.parseInt(dateParts[0]) - 1;
        int day = Integer.parseInt(dateParts[1]);
        int year = Integer.parseInt(dateParts[2]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);
        return calendar.getTimeInMillis();
    }

    private String formatDateForDatabase(String date) {
        String[] dateParts = date.split("/");
        return dateParts[2] + "/" + dateParts[0] + "/" + dateParts[1]; // yyyy/MM/dd
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                Tetno.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Formatowanie daty na: dd/MM/yyyy
                    selectedDateTetno = selectedMonth + 1 + "/" + selectedDay + "/" + selectedYear;
                    selectedDateTextViewTetno.setText(selectedDateTetno); // Wyświetlenie wybranej daty w TextView
                },
                year, month, day
        );


        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    public static class PomiarTetno {
        public int tetno;

        public long timestamp;

        public PomiarTetno() {}

        public PomiarTetno(int tetno, long timestamp) {
            this.tetno = tetno;

            this.timestamp = timestamp;
        }
    }




}


