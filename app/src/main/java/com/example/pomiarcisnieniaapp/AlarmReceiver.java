package com.example.pomiarcisnieniaapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String lek = intent.getStringExtra("lek");
        Toast.makeText(context, "Czas na lek: " + lek, Toast.LENGTH_LONG).show();

        // (Opcjonalnie) Możesz tu wywołać notyfikację zamiast Toasta.
    }
}
