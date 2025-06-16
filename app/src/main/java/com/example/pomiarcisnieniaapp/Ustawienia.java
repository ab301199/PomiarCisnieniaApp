package com.example.pomiarcisnieniaapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class Ustawienia extends AppCompatActivity {
    private LineChart chartWeek, chartMonth;
    private ImageView navHome, navHeart, navPills, navSettings;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        PDFBoxResourceLoader.init(getApplicationContext()); // Inicjalizacja PDFBox
        setContentView(R.layout.activity_ustawienia);

        chartWeek = findViewById(R.id.chart_week);
        chartMonth = findViewById(R.id.chart_month);

        navHome     = findViewById(R.id.nav_home);
        navHeart    = findViewById(R.id.nav_heart);
        navPills    = findViewById(R.id.nav_pills);
        navSettings = findViewById(R.id.nav_settings);

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

        findViewById(R.id.wylogujbtn).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(Ustawienia.this, logowanie.class));
            finish();
        });

        findViewById(R.id.button_generate_pdf).setOnClickListener(v -> {
            generatePdfReport();
        });

        loadData();
    }

    private void loadData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Nie zalogowano", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, logowanie.class));
            finish();
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("pomiar")
                .child(user.getUid());

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot snap) {
                Map<Long, Map<String, Long>> dm = new TreeMap<>();
                for (DataSnapshot e : snap.getChildren()) {
                    Long ts = e.child("timestamp").getValue(Long.class);
                    if (ts == null) continue;
                    Map<String, Long> m = new HashMap<>();
                    if (e.hasChild("skurczowe")) m.put("skurczowe", e.child("skurczowe").getValue(Long.class));
                    if (e.hasChild("rozkurczowe")) m.put("rozkurczowe", e.child("rozkurczowe").getValue(Long.class));
                    dm.put(ts, m);
                }
                drawChart(dm);
            }

            public void onCancelled(DatabaseError err) {
                Toast.makeText(Ustawienia.this, "Błąd", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawChart(Map<Long, Map<String, Long>> dataMap) {
        long now = System.currentTimeMillis();
        long dayMillis = 24 * 60 * 60 * 1000;
        long weekAgo = now - 7 * dayMillis;
        long monthAgo = now - 30 * dayMillis;

        TreeMap<Long, Map<String, Long>> sorted = new TreeMap<>(dataMap);

        List<Entry> sysWeek = new ArrayList<>();
        List<Entry> diaWeek = new ArrayList<>();
        List<Entry> sysMonth = new ArrayList<>();
        List<Entry> diaMonth = new ArrayList<>();
        List<Long> weekTimestamps = new ArrayList<>();
        List<Long> monthTimestamps = new ArrayList<>();

        int iWeek = 0, iMonth = 0;
        for (Map.Entry<Long, Map<String, Long>> entry : sorted.entrySet()) {
            long ts = entry.getKey();
            Map<String, Long> vals = entry.getValue();

            if (ts >= monthAgo) {
                if (vals.containsKey("skurczowe")) sysMonth.add(new Entry(iMonth, vals.get("skurczowe")));
                if (vals.containsKey("rozkurczowe")) diaMonth.add(new Entry(iMonth, vals.get("rozkurczowe")));
                monthTimestamps.add(ts);
                iMonth++;
            }

            if (ts >= weekAgo) {
                if (vals.containsKey("skurczowe")) sysWeek.add(new Entry(iWeek, vals.get("skurczowe")));
                if (vals.containsKey("rozkurczowe")) diaWeek.add(new Entry(iWeek, vals.get("rozkurczowe")));
                weekTimestamps.add(ts);
                iWeek++;
            }
        }

        drawLineChart(chartWeek, sysWeek, diaWeek, weekTimestamps);
        drawLineChart(chartMonth, sysMonth, diaMonth, monthTimestamps);
    }

    private void drawLineChart(LineChart chart, List<Entry> sys, List<Entry> dia, List<Long> timestamps) {
        LineDataSet sysSet = new LineDataSet(sys, "Skurczowe");
        sysSet.setColor(Color.RED);
        sysSet.setLineWidth(2f);
        sysSet.setCircleColor(Color.RED);

        LineDataSet diaSet = new LineDataSet(dia, "Rozkurczowe");
        diaSet.setColor(Color.BLUE);
        diaSet.setLineWidth(2f);
        diaSet.setCircleColor(Color.BLUE);

        LineData data = new LineData(sysSet, diaSet);
        chart.setData(data);

        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(getDateFormatter(timestamps));

        chart.getDescription().setEnabled(false);
        chart.getLegend().setForm(Legend.LegendForm.LINE);
        chart.invalidate();
    }

    private ValueFormatter getDateFormatter(List<Long> timestamps) {
        return new ValueFormatter() {
            private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM", Locale.getDefault());

            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < timestamps.size()) {
                    return sdf.format(new Date(timestamps.get(index)));
                } else {
                    return "";
                }
            }
        };
    }

    private void generatePdfReport() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Brak zalogowanego użytkownika", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("pomiar")
                .child(user.getUid());

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    PDDocument document = new PDDocument();
                    PDPage page = new PDPage(PDRectangle.A4);
                    document.addPage(page);

                    PDPageContentStream contentStream = new PDPageContentStream(document, page);
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 770);
                    contentStream.showText(removePolishChars("Raport pomiarów ciśnienia krwi")); // <-- usuń polskie znaki
                    contentStream.endText();

                    contentStream.setFont(PDType1Font.HELVETICA, 10);
                    int y = 750;

                    for (DataSnapshot entry : snapshot.getChildren()) {
                        Long ts = entry.child("timestamp").getValue(Long.class);
                        String dateStr = ts != null ? new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new Date(ts)) : "-";
                        String skurczowe = entry.hasChild("skurczowe") ? String.valueOf(entry.child("skurczowe").getValue()) : "-";
                        String rozkurczowe = entry.hasChild("rozkurczowe") ? String.valueOf(entry.child("rozkurczowe").getValue()) : "-";
                        String tetno = entry.hasChild("tetno") ? String.valueOf(entry.child("tetno").getValue()) : "-";

                        String textLine = dateStr + " | " + skurczowe + "/" + rozkurczowe + " mmHg | Tetno: " + tetno;
                        textLine = removePolishChars(textLine);  // <-- usuń polskie znaki

                        contentStream.beginText();
                        contentStream.newLineAtOffset(50, y);
                        contentStream.showText(textLine);
                        contentStream.endText();

                        y -= 25;

                        if (y < 100) break; // jedna strona
                    }

                    contentStream.close();

                    File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                    if (dir != null && !dir.exists()) dir.mkdirs();

                    File file = new File(dir, "Raport_" + System.currentTimeMillis() + ".pdf");
                    document.save(file);
                    document.close();

                    Toast.makeText(Ustawienia.this, "PDF zapisany: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(Ustawienia.this, "Błąd PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            public void onCancelled(DatabaseError error) {
                Toast.makeText(Ustawienia.this, "Błąd odczytu danych", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String removePolishChars(String input) {
        if (input == null) return "";
        return input
                .replace("ą", "a").replace("ć", "c").replace("ę", "e").replace("ł", "l")
                .replace("ń", "n").replace("ó", "o").replace("ś", "s").replace("ź", "z").replace("ż", "z")
                .replace("Ą", "A").replace("Ć", "C").replace("Ę", "E").replace("Ł", "L")
                .replace("Ń", "N").replace("Ó", "O").replace("Ś", "S").replace("Ź", "Z").replace("Ż", "Z");
    }
}
