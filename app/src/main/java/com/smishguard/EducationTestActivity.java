package com.smishguard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.smishguard.databinding.ActivityEducationTestBinding;

public class EducationTestActivity extends AppCompatActivity {

    private ActivityEducationTestBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEducationTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ocultarBarrasDeSistema();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(EducationTestActivity.this, EducationActivity.class);
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
