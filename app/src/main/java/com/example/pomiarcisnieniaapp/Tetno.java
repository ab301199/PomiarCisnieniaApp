package com.example.pomiarcisnieniaapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class Tetno extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference dbRef;

    private EditText tetnoInput;
    private TextView dateTV;
    private Button saveBtn;

    private String selDate = "";

    private LineChart chartWeek, chartMonth;
    private CheckBox checkboxWeek, checkboxMonth;

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

        dbRef = FirebaseDatabase.getInstance().getReference("pomiar").child(user.getUid());

        tetnoInput = findViewById(R.id.tetno_input);
        dateTV = findViewById(R.id.selected_date_tetno);
        saveBtn = findViewById(R.id.dodaj_pomiar_btn_tetno);

        chartWeek = findViewById(R.id.chart_tetno_week);
        chartMonth = findViewById(R.id.chart_tetno_month);

        checkboxWeek = findViewById(R.id.checkbox_week);
        checkboxMonth = findViewById(R.id.checkbox_month);

        dateTV.setOnClickListener(v -> showDatePicker());
        saveBtn.setOnClickListener(v -> saveTetno());

        checkboxWeek.setOnCheckedChangeListener((buttonView, isChecked) -> {
            chartWeek.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        checkboxMonth.setOnCheckedChangeListener((buttonView, isChecked) -> {
            chartMonth.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });


        navHome = findViewById(R.id.nav_home);
        navHeart = findViewById(R.id.nav_heart);
        navPills = findViewById(R.id.nav_pills);
        navSettings = findViewById(R.id.nav_settings);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        navHeart.setOnClickListener(v -> {
            
        });
        navPills.setOnClickListener(v -> {
            startActivity(new Intent(this, Leki.class));
            finish();
        });
        navSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, Ustawienia.class));
            finish();
        });

        loadChartsData();


        checkboxWeek.setChecked(true);
        checkboxMonth.setChecked(true);
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


                        if (t < 50 || t > 100) {
                            Toast.makeText(this, "Uwaga: Tętno poza normą!", Toast.LENGTH_LONG).show();
                        }

                        tetnoInput.setText("");
                        dateTV.setText("Wybierz datę");
                        selDate = "";
                        loadChartsData();
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

    private void loadChartsData() {
        dbRef.get().addOnSuccessListener(dataSnapshot -> {
            List<PomiarTetno> allMeasurements = new ArrayList<>();
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                PomiarTetno p = ds.getValue(PomiarTetno.class);
                if (p != null) allMeasurements.add(p);
            }
            drawWeekChart(allMeasurements);
            drawMonthChart(allMeasurements);
        });
    }

    private void drawWeekChart(List<PomiarTetno> data) {
        Calendar now = Calendar.getInstance();
        long oneDayMs = 24 * 60 * 60 * 1000;
        long startWeek = now.getTimeInMillis() - 6 * oneDayMs;

        Map<String, List<Integer>> dayToValues = new TreeMap<>();

        for (int i = 0; i < 7; i++) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(startWeek + i * oneDayMs);
            String dayKey = String.format(Locale.getDefault(), "%1$tm/%1$td", c);
            dayToValues.put(dayKey, new ArrayList<>());
        }

        for (PomiarTetno p : data) {
            if (p.timestamp >= startWeek && p.timestamp <= now.getTimeInMillis()) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(p.timestamp);
                String dayKey = String.format(Locale.getDefault(), "%1$tm/%1$td", c);
                if (dayToValues.containsKey(dayKey)) {
                    dayToValues.get(dayKey).add(p.tetno);
                }
            }
        }

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;
        for (String day : dayToValues.keySet()) {
            List<Integer> values = dayToValues.get(day);
            if (!values.isEmpty()) {
                int sum = 0;
                for (int v : values) sum += v;
                float avg = sum / (float) values.size();
                entries.add(new Entry(index, avg));
                labels.add(day);
                index++;
            }
        }

        if (entries.isEmpty()) {
            chartWeek.clear();
            chartWeek.invalidate();
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Średnie tętno (ostatni tydzień)");
        dataSet.setColor(Color.RED);
        dataSet.setCircleColor(Color.RED);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(5f);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);
        chartWeek.setData(lineData);

        XAxis xAxis = chartWeek.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = (int) value;
                if (i >= 0 && i < labels.size()) {
                    return labels.get(i);
                } else {
                    return "";
                }
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        chartWeek.getAxisRight().setEnabled(false);
        chartWeek.getDescription().setEnabled(false);
        chartWeek.invalidate();
    }

    private void drawMonthChart(List<PomiarTetno> data) {
        Calendar now = Calendar.getInstance();
        long oneDayMs = 24 * 60 * 60 * 1000;
        long startMonth = now.getTimeInMillis() - 29 * oneDayMs;

        Map<String, List<Integer>> dayToValues = new TreeMap<>();

        for (int i = 0; i < 30; i++) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(startMonth + i * oneDayMs);
            String dayKey = String.format(Locale.getDefault(), "%1$tm/%1$td", c);
            dayToValues.put(dayKey, new ArrayList<>());
        }

        for (PomiarTetno p : data) {
            if (p.timestamp >= startMonth && p.timestamp <= now.getTimeInMillis()) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(p.timestamp);
                String dayKey = String.format(Locale.getDefault(), "%1$tm/%1$td", c);
                if (dayToValues.containsKey(dayKey)) {
                    dayToValues.get(dayKey).add(p.tetno);
                }
            }
        }

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;
        for (String day : dayToValues.keySet()) {
            List<Integer> values = dayToValues.get(day);
            if (!values.isEmpty()) {
                int sum = 0;
                for (int v : values) sum += v;
                float avg = sum / (float) values.size();
                entries.add(new Entry(index, avg));
                if (index % 5 == 0) {
                    labels.add(day);
                } else {
                    labels.add("");
                }
                index++;
            }
        }

        if (entries.isEmpty()) {
            chartMonth.clear();
            chartMonth.invalidate();
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Średnie tętno (ostatni miesiąc)");
        dataSet.setColor(Color.MAGENTA);
        dataSet.setCircleColor(Color.MAGENTA);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(5f);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);
        chartMonth.setData(lineData);

        XAxis xAxis = chartMonth.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int i = (int) value;
                if (i >= 0 && i < labels.size()) {
                    return labels.get(i);
                } else {
                    return "";
                }
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        chartMonth.getAxisRight().setEnabled(false);
        chartMonth.getDescription().setEnabled(false);
        chartMonth.invalidate();
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






