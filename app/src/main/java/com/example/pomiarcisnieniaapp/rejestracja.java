package com.example.pomiarcisnieniaapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

public class rejestracja extends AppCompatActivity {
    TextInputEditText editTextEmail, editTextHaslo;
    Button rejestracjabtn;
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
        setContentView(R.layout.activity_rejestracja);
        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextHaslo = findViewById(R.id.haslo);
        rejestracjabtn = findViewById(R.id.rejestracja_btn);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.gotologowanie);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), logowanie.class);
                startActivity(intent);
                finish();
            }
        });


        rejestracjabtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email, haslo;
                email = String.valueOf(editTextEmail.getText());
                haslo = String.valueOf(editTextHaslo.getText());

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(rejestracja.this,"Wprowadź email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(haslo)){
                    Toast.makeText(rejestracja.this,"Wprowadź hasło", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, haslo)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(rejestracja.this, "Zarejestrowano pomyślnie",
                                            Toast.LENGTH_SHORT).show();

                                } else {
                                    // If sign in fails, display a message to the user.

                                    Toast.makeText(rejestracja.this, "Rejestracja nie powiodła się",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });


            }
        });

        }
    }
