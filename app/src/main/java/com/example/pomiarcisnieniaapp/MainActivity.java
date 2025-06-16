// importy zostają te same
package com.example.pomiarcisnieniaapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference dbRef;

    private static final int CREATE_PDF_REQUEST_CODE = 101;

    private Button dodajPomiarButton;
    private EditText skurczoweInput, rozkurczoweInput;
    private TextView selectedDateTextView;
    private String selectedDate = "";

    private ImageView navHome, navHeart, navPills, navSettings;

    private LineChart chartWeek, chartMonth;
    private CheckBox checkboxWeek, checkboxMonth;

    private PdfDocument pendingPdfDocument;

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

        dbRef = FirebaseDatabase.getInstance().getReference("pomiar").child(user.getUid());

        skurczoweInput = findViewById(R.id.skurczowe_input);
        rozkurczoweInput = findViewById(R.id.rozkurczowe_input);
        selectedDateTextView = findViewById(R.id.selected_date);
        dodajPomiarButton = findViewById(R.id.dodaj_pomiar_btn);
        chartWeek = findViewById(R.id.chart_week);
        chartMonth = findViewById(R.id.chart_month);
        checkboxWeek = findViewById(R.id.checkbox_week);
        checkboxMonth = findViewById(R.id.checkbox_month);

        navHome = findViewById(R.id.nav_home);
        navHeart = findViewById(R.id.nav_heart);
        navPills = findViewById(R.id.nav_pills);
        navSettings = findViewById(R.id.nav_settings);

        selectedDateTextView.setOnClickListener(v -> showDatePicker());
        dodajPomiarButton.setOnClickListener(v -> savePomiar());

        navHome.setOnClickListener(v -> {});
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

        checkboxWeek.setOnCheckedChangeListener((b, isChecked) -> updateChartVisibility());
        checkboxMonth.setOnCheckedChangeListener((b, isChecked) -> updateChartVisibility());

        findViewById(R.id.button_generate_pdf).setOnClickListener(v -> createPdfReport());

        loadData();
    }

    private void updateChartVisibility() {
        chartWeek.setVisibility(checkboxWeek.isChecked() ? View.VISIBLE : View.GONE);
        chartMonth.setVisibility(checkboxMonth.isChecked() ? View.VISIBLE : View.GONE);
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

        showPressureWarning(s, r);

        Pomiar pm = new Pomiar(s, r, ts);
        dbRef.push().setValue(pm)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Pomiar zapisany", Toast.LENGTH_SHORT).show();
                        skurczoweInput.setText("");
                        rozkurczoweInput.setText("");
                        selectedDateTextView.setText("Wybierz datę");
                        selectedDate = "";
                        loadData();
                    } else {
                        Toast.makeText(this, "Błąd zapisu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showPressureWarning(int s, int r) {
        if (s < 90 && r < 60)
            Toast.makeText(this, "Uwaga: Zbyt niskie ciśnienie!", Toast.LENGTH_LONG).show();
        else if (s > 140 && r > 90)
            Toast.makeText(this, "Uwaga: Zbyt wysokie ciśnienie!", Toast.LENGTH_LONG).show();
    }

    private void loadData() {
        if (user == null) return;

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                Toast.makeText(MainActivity.this, "Błąd odczytu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawChart(Map<Long, Map<String, Long>> dataMap) {
        long now = System.currentTimeMillis();
        long day = 24 * 60 * 60 * 1000L;
        long weekAgo = now - 7 * day;
        long monthAgo = now - 30 * day;

        List<Entry> sw = new ArrayList<>(), rw = new ArrayList<>(), sm = new ArrayList<>(), rm = new ArrayList<>();
        List<Long> wt = new ArrayList<>(), mt = new ArrayList<>();

        int iw = 0, im = 0;
        for (Map.Entry<Long, Map<String, Long>> e : new TreeMap<>(dataMap).entrySet()) {
            long ts = e.getKey();
            Map<String, Long> v = e.getValue();
            if (ts >= monthAgo) {
                if (v.containsKey("skurczowe") && v.containsKey("rozkurczowe")) {
                    if (ts >= weekAgo) {
                        sw.add(new Entry(iw, v.get("skurczowe")));
                        rw.add(new Entry(iw, v.get("rozkurczowe")));
                        wt.add(ts);
                        iw++;
                    }
                    sm.add(new Entry(im, v.get("skurczowe")));
                    rm.add(new Entry(im, v.get("rozkurczowe")));
                    mt.add(ts);
                    im++;
                }
            }
        }

        chartWeek.setData(new LineData(new LineDataSet(sw, "Skurczowe") {{ setColor(Color.RED); }},
                new LineDataSet(rw, "Rozkurczowe") {{ setColor(Color.BLUE); }}));
        chartWeek.getDescription().setEnabled(false);
        chartWeek.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chartWeek.getXAxis().setValueFormatter(new TimestampFormatter(wt));
        chartWeek.getAxisRight().setEnabled(false);
        chartWeek.invalidate();

        chartMonth.setData(new LineData(new LineDataSet(sm, "Skurczowe") {{ setColor(Color.RED); }},
                new LineDataSet(rm, "Rozkurczowe") {{ setColor(Color.BLUE); }}));
        chartMonth.getDescription().setEnabled(false);
        chartMonth.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chartMonth.getXAxis().setValueFormatter(new TimestampFormatter(mt));
        chartMonth.getAxisRight().setEnabled(false);
        chartMonth.invalidate();

        updateChartVisibility();
    }

    private static class TimestampFormatter extends ValueFormatter {
        private final List<Long> timestamps;
        private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM", Locale.getDefault());

        TimestampFormatter(List<Long> ts) { this.timestamps = ts; }

        @Override
        public String getFormattedValue(float value) {
            int i = (int) value;
            return (i >= 0 && i < timestamps.size()) ? sdf.format(new Date(timestamps.get(i))) : "";
        }
    }

    private long getTimestampFromDate(String d) {
        try {
            Date dt = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(d);
            return dt != null ? dt.getTime() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (v, y, m, d) -> {
            selectedDate = String.format(Locale.getDefault(), "%02d.%02d.%d", d, m + 1, y);
            selectedDateTextView.setText(selectedDate);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void createPdfReport() {
        if (user == null) return;

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot snap) {
                if (!snap.exists()) {
                    Toast.makeText(MainActivity.this, "Brak danych do eksportu", Toast.LENGTH_SHORT).show();
                    return;
                }

                PdfDocument pdf = new PdfDocument();
                Paint paint = new Paint();
                Paint titlePaint = new Paint();
                titlePaint.setTextSize(18);
                titlePaint.setColor(Color.BLACK);

                int y = 50, pageNum = 1;
                PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(595, 842, pageNum).create();
                PdfDocument.Page page = pdf.startPage(info);
                Canvas canvas = page.getCanvas();
                canvas.drawText("Raport pomiarów ciśnienia", 40, y, titlePaint);
                y += 30;

                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

                for (DataSnapshot e : snap.getChildren()) {
                    Long ts = e.child("timestamp").getValue(Long.class);
                    Long s = e.child("skurczowe").getValue(Long.class);
                    Long r = e.child("rozkurczowe").getValue(Long.class);
                    if (ts == null || s == null || r == null) continue;

                    canvas.drawText(sdf.format(new Date(ts)) + ": " + s + " / " + r + " mmHg", 40, y, paint);
                    y += 20;
                    if (y > 800) {
                        pdf.finishPage(page);
                        pageNum++;
                        info = new PdfDocument.PageInfo.Builder(595, 842, pageNum).create();
                        page = pdf.startPage(info);
                        canvas = page.getCanvas();
                        y = 50;
                    }
                }
                pdf.finishPage(page);

                pendingPdfDocument = pdf;

                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.setType("application/pdf");
                intent.putExtra(Intent.EXTRA_TITLE, "Raport_Pomiarow.pdf");
                startActivityForResult(intent, CREATE_PDF_REQUEST_CODE);
            }

            public void onCancelled(DatabaseError err) {
                Toast.makeText(MainActivity.this, "Błąd odczytu danych", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_PDF_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null && pendingPdfDocument != null) {
                try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                    pendingPdfDocument.writeTo(os);
                    Toast.makeText(this, "PDF zapisany", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Błąd zapisu PDF", Toast.LENGTH_SHORT).show();
                } finally {
                    pendingPdfDocument.close();
                    pendingPdfDocument = null;
                }
            }
        }
    }

    public static class Pomiar {
        public int skurczowe;
        public int rozkurczowe;
        public long timestamp;

        public Pomiar() {}
        public Pomiar(int s, int r, long ts) {
            this.skurczowe = s;
            this.rozkurczowe = r;
            this.timestamp = ts;
        }
    }
}





