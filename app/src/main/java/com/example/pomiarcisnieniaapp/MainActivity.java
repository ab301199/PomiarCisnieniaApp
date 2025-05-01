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
import android.widget.NumberPicker;
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

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    Button dodajPomiarButton;
    FirebaseUser user;
    EditText skurczoweInput, rozkurczoweInput;
    DatabaseReference database;

    private LinearLayout bottomNav;
    private ImageView navHome, navHeart, navPills, navSettings;
    private TextView selectedDateTextView;
    private String selectedDate = ""; // Zmienna do przechowywania wybranej daty

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        dodajPomiarButton = findViewById(R.id.dodaj_pomiar_btn);
        skurczoweInput = findViewById(R.id.skurczowe_input);
        rozkurczoweInput = findViewById(R.id.rozkurczowe_input);
        database = FirebaseDatabase.getInstance().getReference("users");

        bottomNav = findViewById(R.id.custom_bottom_nav);
        navHome = findViewById(R.id.nav_home);
        navHeart = findViewById(R.id.nav_heart);
        navPills = findViewById(R.id.nav_pills);
        navSettings = findViewById(R.id.nav_settings);
        selectedDateTextView = findViewById(R.id.selected_date); // Widok tekstu dla daty

        user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), logowanie.class);
            startActivity(intent);
            finish();
        }

        // Obsługa kliknięcia na TextView do wyboru daty
        selectedDateTextView.setOnClickListener(view -> showDatePicker());

        // Obsługa przycisku dodania pomiaru
        dodajPomiarButton.setOnClickListener(view -> {
            String skurczoweStr = skurczoweInput.getText().toString();
            String rozkurczoweStr = rozkurczoweInput.getText().toString();

            if (selectedDate.isEmpty()) {
                Toast.makeText(MainActivity.this, "Wybierz datę przed zapisaniem pomiaru.", Toast.LENGTH_SHORT).show();
                return;
            }

            long selectedDateTimestamp = getTimestampFromDate(selectedDate);
            if (selectedDateTimestamp > System.currentTimeMillis()) {
                Toast.makeText(MainActivity.this, "Data nie może być z przyszłości.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!skurczoweStr.isEmpty() && !rozkurczoweStr.isEmpty()) {
                try {
                    int skurczowe = Integer.parseInt(skurczoweStr);
                    int rozkurczowe = Integer.parseInt(rozkurczoweStr);

                    if (skurczowe >= 50 && skurczowe <= 250 && rozkurczowe >= 30 && rozkurczowe <= 150) {

                        // Tworzymy obiekt pomiaru
                        Pomiar nowyPomiar = new Pomiar(skurczowe, rozkurczowe, selectedDateTimestamp);

                        // Zapisujemy pomiar do bazy danych Firebase na głównym wątku
                        database.child(user.getUid()).child("pomiar").child(formatDateForDatabase(selectedDate)).setValue(nowyPomiar)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Wrócimy do głównego wątku i pokazujemy komunikat
                                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Pomiar zapisany", Toast.LENGTH_SHORT).show());
                                    } else {
                                        // Wrócimy do głównego wątku i pokażemy błąd
                                        runOnUiThread(() -> {
                                            Log.e("FirebaseError", "Błąd zapisu do bazy: " + task.getException());
                                            Toast.makeText(MainActivity.this, "Błąd zapisu do bazy danych", Toast.LENGTH_SHORT).show();
                                        });
                                    }
                                });
                    } else {
                        Toast.makeText(MainActivity.this, "Podaj wartości ciśnienia w poprawnym zakresie", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Wprowadź poprawne liczby", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Wypełnij oba pola", Toast.LENGTH_SHORT).show();
            }
        });

        navHome.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
        });

        navHeart.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, Tetno.class);
            startActivity(intent);
        });

        navPills.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, Leki.class);
            startActivity(intent);
        });

        navSettings.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, Ustawienia.class);
            startActivity(intent);
        });
    }

    // Funkcja do konwertowania daty na timestamp
    private long getTimestampFromDate(String date) {
        String[] dateParts = date.split("/");
        int month = Integer.parseInt(dateParts[0]) - 1; // Miesiące w Calendar zaczynają się od 0
        int day = Integer.parseInt(dateParts[1]);
        int year = Integer.parseInt(dateParts[2]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);
        return calendar.getTimeInMillis();
    }

    // Funkcja do formatowania daty na format yyyy/MM/dd do zapisu w bazie danych
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
                MainActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Formatowanie daty na: dd/MM/yyyy
                    selectedDate = selectedMonth + 1 + "/" + selectedDay + "/" + selectedYear;
                    selectedDateTextView.setText(selectedDate); // Wyświetlenie wybranej daty w TextView
                },
                year, month, day
        );

        // Blokuje możliwość wyboru daty w przyszłości
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    public static class Pomiar {
        public int skurczowe;
        public int rozkurczowe;
        public long timestamp;

        public Pomiar() {}

        public Pomiar(int skurczowe, int rozkurczowe, long timestamp) {
            this.skurczowe = skurczowe;
            this.rozkurczowe = rozkurczowe;
            this.timestamp = timestamp;
        }
    }
}










