package com.smishguard;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.smishguard.databinding.ActivityResultBinding;

public class ResultActivity extends AppCompatActivity {

    private ActivityResultBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ocultarBarrasDeSistema();

        // Obtener los datos del Intent
        String mensajeAnalizado = getIntent().getStringExtra("mensajeAnalizado");
        String analisisSmishguard = getIntent().getStringExtra("analisisSmishguard");
        String analisisGpt = getIntent().getStringExtra("analisisGpt");
        String enlace = getIntent().getStringExtra("enlace");
        int puntaje = getIntent().getIntExtra("puntaje", -1);  // -1 por defecto si no se encuentra

        // Mostrar los datos en los TextView correspondientes
        binding.textViewMensajeAnalizado.setText(mensajeAnalizado);
        binding.textViewAnalisisSmishguard.setText(analisisSmishguard);
        binding.textViewAnalisisGpt.setText(analisisGpt);
        binding.textViewEnlace.setText(enlace);
        binding.textViewPuntaje.setText(String.valueOf(puntaje)+"/10");

        // Establecer el color del fondo del puntaje
        setPuntajeColor(puntaje);

        // Botón para regresar a la pantalla principal
        binding.btnBackResult.setOnClickListener(view -> {
            startActivity(new Intent(ResultActivity.this, MainActivity.class));
        });
    }

    private void setPuntajeColor(int puntaje) {
        int backgroundColor;
        if (puntaje >= 1 && puntaje <= 3) {
            // Verde claro
            backgroundColor = Color.parseColor("#00FF00");
        } else if (puntaje >= 4 && puntaje <= 7) {
            // Amarillo claro
            backgroundColor = Color.parseColor("#FFFF00");
        } else if (puntaje >= 8 && puntaje <= 10) {
            // Rojo
            backgroundColor = Color.parseColor("#FF0000");
        } else {
            // Color por defecto (gris claro) si el puntaje no está en el rango esperado
            backgroundColor = Color.parseColor("#E0E0E0");
        }

        // Cambiar el fondo del TextView para que tenga el color correspondiente
        GradientDrawable background = (GradientDrawable) binding.textViewPuntaje.getBackground();
        background.setColor(backgroundColor);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ResultActivity.this, MainActivity.class);
        // Estas banderas crean una nueva tarea con MainActivity y eliminan todas las actividades anteriores
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ocultarBarrasDeSistema();
    }

    private void ocultarBarrasDeSistema() {
        // Método para ocultar las barras del sistema
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
}