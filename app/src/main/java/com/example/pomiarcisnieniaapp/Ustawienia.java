package com.example.pomiarcisnieniaapp;

import com.example.pomiarcisnieniaapp.MainActivity;
import com.example.pomiarcisnieniaapp.Tetno;
import com.example.pomiarcisnieniaapp.Leki;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.pomiarcisnieniaapp.R;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.components.Legend;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

public class Ustawienia extends AppCompatActivity {

    private LineChart chartWeek, chartMonth;
    private Button buttonGeneratePDF;
    private Button wylogujBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ustawienia);

        // Inicjalizacja komponentów
        chartWeek = findViewById(R.id.chart_week);
        chartMonth = findViewById(R.id.chart_month);
        buttonGeneratePDF = findViewById(R.id.button_generate_pdf);
        wylogujBtn = findViewById(R.id.wylogujbtn);

        showChart(chartWeek, 7);
        showChart(chartMonth, 30);

        buttonGeneratePDF.setOnClickListener(v -> generatePDF());

        wylogujBtn.setOnClickListener(v -> {
            Toast.makeText(Ustawienia.this, "Wylogowano", Toast.LENGTH_SHORT).show();
            // UWAGA: Upewnij się, że klasa logowania nazywa się "logowanie" lub zmień ją na "Logowanie"
            startActivity(new Intent(getApplicationContext(), com.example.pomiarcisnieniaapp.logowanie.class));
            finish();
        });

        setupNavigation();
    }

    private void showChart(LineChart chart, int days) {
        List<Entry> pressureEntries = new ArrayList<>();
        List<Entry> heartEntries = new ArrayList<>();

        Random rand = new Random();
        for (int i = 0; i < days; i++) {
            pressureEntries.add(new Entry(i, 110 + rand.nextInt(30))); // 110–140
            heartEntries.add(new Entry(i, 60 + rand.nextInt(40)));     // 60–100
        }

        LineDataSet pressureSet = new LineDataSet(pressureEntries, "Ciśnienie");
        pressureSet.setColor(Color.RED);
        pressureSet.setValueTextColor(Color.BLACK);
        pressureSet.setLineWidth(2f);
        pressureSet.setCircleRadius(3f);

        LineDataSet heartSet = new LineDataSet(heartEntries, "Tętno");
        heartSet.setColor(Color.BLUE);
        heartSet.setValueTextColor(Color.BLACK);
        heartSet.setLineWidth(2f);
        heartSet.setCircleRadius(3f);

        LineData data = new LineData(pressureSet, heartSet);
        chart.setData(data);
        chart.getDescription().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setTextSize(12f);
        legend.setForm(Legend.LegendForm.LINE);

        chart.invalidate();
    }

    private void generatePDF() {
        try {
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
            File file = new File(path, "raport_cisnienie_tetno.pdf");

            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Raport pomiarów ciśnienia i tętna"));
            document.add(new Paragraph("Data generacji: " + new Date().toString()));
            document.add(new Paragraph("\nOstatnie 7 dni:"));

            Random rand = new Random();
            for (int i = 1; i <= 7; i++) {
                int pressure = 110 + rand.nextInt(30);
                int pulse = 60 + rand.nextInt(40);
                document.add(new Paragraph("Dzień " + i + ": Ciśnienie = " + pressure + " mmHg, Tętno = " + pulse + " BPM"));
            }

            document.close();
            Toast.makeText(this, "PDF zapisany w: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd generowania PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupNavigation() {
        ImageView home = findViewById(R.id.nav_home);
        ImageView heart = findViewById(R.id.nav_heart);
        ImageView pills = findViewById(R.id.nav_pills);
        ImageView settings = findViewById(R.id.nav_settings);

        home.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        heart.setOnClickListener(v -> startActivity(new Intent(this, Tetno.class)));
        pills.setOnClickListener(v -> startActivity(new Intent(this, Leki.class)));
        settings.setOnClickListener(v -> {
            // już tu jesteśmy
        });
    }
}
