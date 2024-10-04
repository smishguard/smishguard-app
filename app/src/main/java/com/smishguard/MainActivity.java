package com.smishguard;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.airbnb.lottie.LottieAnimationView;

public class MainActivity extends AppCompatActivity {

    private LottieAnimationView lottieButton;
    private ImageView imageOff;
    private boolean isOn = false; // Estado inicial

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        lottieButton = findViewById(R.id.lottie_button);
        imageOff = findViewById(R.id.image_off);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lottieButton.setOnClickListener(v -> toggleButton());
        imageOff.setOnClickListener(v -> toggleButton());
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