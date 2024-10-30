package com.smishguard;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.smishguard.databinding.ActivityAnalysisBinding;

public class AnalysisActivity extends AppCompatActivity {

    private ActivityAnalysisBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnalysisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ocultarBarrasDeSistema();

        // Obtener los datos del Intent
        String mensajeAnalizado = getIntent().getStringExtra("body");
        String enlace = getIntent().getStringExtra("enlace");
        String analisisGpt = getIntent().getStringExtra("analisisGpt");
        int puntaje = getIntent().getIntExtra("puntaje", 0);
        String analisisSmishguard = getIntent().getStringExtra("analisisSmishguard");

        // Mostrar los datos en los TextViews
        binding.textViewMensajeAnalizado.setText(mensajeAnalizado);
        binding.textViewEnlace.setText(enlace);
        binding.textViewAnalisisGpt.setText(analisisGpt);
        binding.textViewPuntaje.setText(String.valueOf(puntaje) + "/10");
        binding.textViewAnalisisSmishguard.setText(analisisSmishguard);

        // Establecer el color de fondo del puntaje
        setPuntajeColor(puntaje);

        binding.btnBackAnalysis.setOnClickListener(view -> {
            startActivity(new Intent(AnalysisActivity.this, AnalyzedMessagesActivity.class));
        });
    }

    private void setPuntajeColor(int puntaje) {
        int backgroundColor;
        if (puntaje >= 1 && puntaje <= 3) {
            backgroundColor = Color.parseColor("#00FF00"); // Verde claro
        } else if (puntaje >= 4 && puntaje <= 7) {
            backgroundColor = Color.parseColor("#FFFF00"); // Amarillo claro
        } else {
            backgroundColor = Color.parseColor("#FF0000"); // Rojo
        }

        // Establecer el color en el fondo del puntaje
        GradientDrawable background = (GradientDrawable) binding.textViewPuntaje.getBackground();
        background.setColor(backgroundColor);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ocultarBarrasDeSistema();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(AnalysisActivity.this, AnalyzedMessagesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void ocultarBarrasDeSistema() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
}