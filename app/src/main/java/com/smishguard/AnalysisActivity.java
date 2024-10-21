package com.smishguard;

import android.content.Intent;
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