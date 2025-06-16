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

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference dbRef;
    private Button dodajPomiarButton;
    private EditText skurczoweInput, rozkurczoweInput;
    private TextView selectedDateTextView;
    private String selectedDate = "";

    // Nawigacja
    private ImageView navHome, navHeart, navPills, navSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        skurczoweInput = findViewById(R.id.skurczowe_input);
        rozkurczoweInput = findViewById(R.id.rozkurczowe_input);
        selectedDateTextView = findViewById(R.id.selected_date);
        dodajPomiarButton = findViewById(R.id.dodaj_pomiar_btn);

        selectedDateTextView.setOnClickListener(v -> showDatePicker());
        dodajPomiarButton.setOnClickListener(v -> savePomiar());

        // Nawigacja dolna
        navHome     = findViewById(R.id.nav_home);
        navHeart    = findViewById(R.id.nav_heart);
        navPills    = findViewById(R.id.nav_pills);
        navSettings = findViewById(R.id.nav_settings);

        navHome.setOnClickListener(v -> {
            // już jesteś na Home
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
            startActivity(new Intent(this, Ustawienia.class));
            finish();
        });
    }

    private void savePomiar() {
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Wybierz datę", Toast.LENGTH_SHORT).show();
            return;
        }
        long ts = getTimestampFromDate(selectedDate);
        if (ts > System.currentTimeMillis()) {
            Toast.makeText(this, "Data nie może być z przyszłości", Toast.LENGTH_SHORT).show();
            return;
        }
        String sStr = skurczoweInput.getText().toString();
        String rStr = rozkurczoweInput.getText().toString();
        if (sStr.isEmpty() || rStr.isEmpty()) {
            Toast.makeText(this, "Wypełnij oba pola", Toast.LENGTH_SHORT).show();
            return;
        }
        int s = Integer.parseInt(sStr);
        int r = Integer.parseInt(rStr);
        Pomiar pm = new Pomiar(s, r, ts);
        dbRef.push().setValue(pm)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Pomiar zapisany", Toast.LENGTH_SHORT).show();
                        skurczoweInput.setText("");
                        rozkurczoweInput.setText("");
                        selectedDateTextView.setText("Wybierz datę");
                        selectedDate = "";
                    } else {
                        Log.e("FirebaseError", task.getException().toString());
                        Toast.makeText(this, "Błąd zapisu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate = (selectedMonth + 1) + "/" + selectedDay + "/" + selectedYear;
                    selectedDateTextView.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private long getTimestampFromDate(String date) {
        String[] p = date.split("/");
        Calendar c = Calendar.getInstance();
        c.set(Integer.parseInt(p[2]), Integer.parseInt(p[0]) - 1, Integer.parseInt(p[1]), 0, 0);
        return c.getTimeInMillis();
    }

    public static class Pomiar {
        public int skurczowe;
        public int rozkurczowe;
        public long timestamp;

        public Pomiar() {}

        public Pomiar(int s, int r, long t) {
            skurczowe = s;
            rozkurczowe = r;
            timestamp = t;
        }
    }
}
