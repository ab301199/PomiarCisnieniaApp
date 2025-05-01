package com.example.pomiarcisnieniaapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class logowanie extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextHaslo;
    Button logowaniebtn;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logowanie);
        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextHaslo = findViewById(R.id.haslo);
        logowaniebtn = findViewById(R.id.logowanie_btn);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.gotorejestracja);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), rejestracja.class);
                startActivity(intent);
                finish();
            }

        });
        logowaniebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email, haslo;
                email = String.valueOf(editTextEmail.getText());
                haslo = String.valueOf(editTextHaslo.getText());

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(logowanie.this,"Wprowadź email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(haslo)){
                    Toast.makeText(logowanie.this,"Wprowadź hasło", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, haslo)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(),"Logowanie powiodło się", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                    startActivity(intent);
                                    finish();

                                } else {

                                    Toast.makeText(logowanie.this, "Logowanie nie powiodło sie",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });

            }
        });



        };
    }
