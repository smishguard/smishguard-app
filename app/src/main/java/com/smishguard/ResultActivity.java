package com.smishguard;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.smishguard.databinding.ActivityResultBinding;

public class ResultActivity extends AppCompatActivity {

    private ActivityResultBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtener los datos del Intent
        String mensajeAnalizado = getIntent().getStringExtra("mensajeAnalizado");
        String analisisSmishguard = getIntent().getStringExtra("analisisSmishguard");
        String analisisGpt = getIntent().getStringExtra("analisisGpt");
        String enlace = getIntent().getStringExtra("enlace");
        int puntaje = getIntent().getIntExtra("puntaje", -1);  // -1 por defecto si no se encuentra

        // Mostrar los datos en los TextView correspondientes
        binding.textViewMensajeAnalizado.setText("Mensaje Analizado: " + mensajeAnalizado);
        binding.textViewAnalisisSmishguard.setText("Análisis SmishGuard: " + analisisSmishguard);
        binding.textViewAnalisisGpt.setText("Análisis GPT: " + analisisGpt);
        binding.textViewEnlace.setText("Enlace: " + enlace);
        binding.textViewPuntaje.setText("Puntaje: " + puntaje);
    }
}