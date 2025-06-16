package com.example.pomiarcisnieniaapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class Tetno extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference dbRef;
    private EditText tetnoInput;
    private TextView dateTV;
    private Button saveBtn;
    private String selDate = "";

    // NAWIGACJA
    private ImageView navHome, navHeart, navPills, navSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tetno);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, logowanie.class));
            finish();
            return;
        }
        dbRef = FirebaseDatabase.getInstance()
                .getReference("pomiar")
                .child(user.getUid());

        tetnoInput = findViewById(R.id.tetno_input);
        dateTV = findViewById(R.id.selected_date_tetno);
        saveBtn = findViewById(R.id.dodaj_pomiar_btn_tetno);

        dateTV.setOnClickListener(v -> showDatePicker());
        saveBtn.setOnClickListener(v -> saveTetno());

        // DOLNA NAWIGACJA
        navHome     = findViewById(R.id.nav_home);
        navHeart    = findViewById(R.id.nav_heart);
        navPills    = findViewById(R.id.nav_pills);
        navSettings = findViewById(R.id.nav_settings);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        navHeart.setOnClickListener(v -> {
            // Jesteś już w Tetno – nie rób nic lub odśwież
        });
        navPills.setOnClickListener(v -> {
            startActivity(new Intent(this, Leki.class));
            finish();
        });
        navSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, Ustawienia.class));
            finish();
        });
    }

    private void saveTetno() {
        if (selDate.isEmpty()) {
            Toast.makeText(this, "Wybierz datę", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tetnoInput.getText().toString().isEmpty()) {
            Toast.makeText(this, "Podaj tętno", Toast.LENGTH_SHORT).show();
            return;
        }

        long ts = getTimestamp(selDate);
        int t = Integer.parseInt(tetnoInput.getText().toString());
        PomiarTetno pt = new PomiarTetno(t, ts);
        dbRef.push().setValue(pt)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Tętno zapisane", Toast.LENGTH_SHORT).show();
                        tetnoInput.setText("");
                        dateTV.setText("Wybierz datę");
                        selDate = "";
                    } else {
                        Log.e("Err", task.getException().toString());
                    }
                });
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selDate = (selectedMonth + 1) + "/" + selectedDay + "/" + selectedYear;
                    dateTV.setText(selDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private long getTimestamp(String date) {
        String[] p = date.split("/");
        Calendar c = Calendar.getInstance();
        c.set(Integer.parseInt(p[2]), Integer.parseInt(p[0]) - 1, Integer.parseInt(p[1]), 0, 0);
        return c.getTimeInMillis();
    }

    public static class PomiarTetno {
        public int tetno;
        public long timestamp;

        public PomiarTetno() {}

        public PomiarTetno(int t, long ts) {
            tetno = t;
            timestamp = ts;
        }
    }
}
