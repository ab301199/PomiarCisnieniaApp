package com.example.pomiarcisnieniaapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TimePicker;
import android.app.AlarmManager;
import android.content.Context;

import com.example.pomiarcisnieniaapp.Ustawienia;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;

public class Leki extends AppCompatActivity {
    private LinearLayout bottomNav;
    private ImageView navHome, navHeart, navPills, navSettings;

    private EditText editTextLek;
    private TimePicker timePicker;
    private Button buttonDodaj;
    private ListView listViewLeki;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> listaLekow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leki);


        timePicker = findViewById(R.id.timePicker);
        if (Build.VERSION.SDK_INT >= 23) {
            timePicker.setHour(12);
            timePicker.setMinute(0);
        } else {
            timePicker.setCurrentHour(12);
            timePicker.setCurrentMinute(0);
        }


        bottomNav = findViewById(R.id.custom_bottom_nav);
        navHome = findViewById(R.id.nav_home);
        navHeart = findViewById(R.id.nav_heart);
        navPills = findViewById(R.id.nav_pills);
        navSettings = findViewById(R.id.nav_settings);

        navHome.setOnClickListener(view -> {
            Intent intent = new Intent(Leki.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        navHeart.setOnClickListener(view -> {
            Intent intent = new Intent(Leki.this, Tetno.class);
            startActivity(intent);
            finish();
        });

        navPills.setOnClickListener(view -> {
            Intent intent = new Intent(Leki.this, Leki.class);
            startActivity(intent);
            finish();
        });

        navSettings.setOnClickListener(view -> {
            Intent intent = new Intent(Leki.this,
                    Ustawienia.class);
            startActivity(intent);
            finish();
        });


        editTextLek = findViewById(R.id.editTextLek);
        buttonDodaj = findViewById(R.id.buttonDodaj);
        listViewLeki = findViewById(R.id.listViewLeki);


        listaLekow = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaLekow);
        listViewLeki.setAdapter(adapter);


        buttonDodaj.setOnClickListener(view -> {
            String nazwaLeku = editTextLek.getText().toString().trim();
            int godzina = timePicker.getHour();
            int minuta = timePicker.getMinute();

            if (!nazwaLeku.isEmpty()) {
                String wpis = nazwaLeku + " - " + String.format("%02d:%02d", godzina, minuta);
                listaLekow.add(wpis);
                adapter.notifyDataSetChanged();
                editTextLek.setText("");

                ustawAlarm(godzina, minuta, nazwaLeku);
            }
        });
    }

    private void ustawAlarm(int godzina, int minuta, String lek) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, godzina);
        calendar.set(Calendar.MINUTE, minuta);
        calendar.set(Calendar.SECOND, 0);

        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        intent.putExtra("lek", lek);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                lek.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (alarmManager == null) {
            return;
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {

                Intent settingsIntent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(settingsIntent);
                return;
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        } catch (SecurityException e) {
            e.printStackTrace();

        }
    }
}
