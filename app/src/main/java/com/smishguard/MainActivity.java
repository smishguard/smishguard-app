package com.smishguard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.airbnb.lottie.LottieAnimationView;
import com.smishguard.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private LottieAnimationView lottieButton;
    private ImageView imageOff;
    private boolean isOn = false; // Estado inicial

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lottieButton = binding.lottieButton;
        imageOff = binding.imageOff;
        lottieButton.setOnClickListener(v -> toggleButton());
        imageOff.setOnClickListener(v -> toggleButton());

        binding.imgViewManualAnalisis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ManualAnalysisActivity.class));
            }
        });

        binding.btnConfiguration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ConfigurationActivity.class));
            }
        });
    }

    private void toggleButton() {
        if (isOn) {
            lottieButton.setVisibility(View.GONE); // Oculta la animación Lottie
            imageOff.setVisibility(View.VISIBLE); // Muestra la imagen de apagado
        } else {
            lottieButton.setVisibility(View.VISIBLE); // Muestra la animación Lottie
            imageOff.setVisibility(View.GONE); // Oculta la imagen de apagado
        }
        isOn = !isOn; // Alterna el estado
    }
}