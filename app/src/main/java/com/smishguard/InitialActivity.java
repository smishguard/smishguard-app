package com.smishguard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.smishguard.databinding.ActivityInitialBinding;

public class InitialActivity extends AppCompatActivity {

    private ActivityInitialBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInitialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        FirebaseApp.initializeApp(this);

        binding.btnGoLoginIni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(InitialActivity.this, LoginActivity.class));
            }
        });

        binding.btnGoRegisterIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(InitialActivity.this, RegisterActivity.class));
            }
        });
    }

    // Limpiar el objeto binding cuando se destruya la actividad
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}