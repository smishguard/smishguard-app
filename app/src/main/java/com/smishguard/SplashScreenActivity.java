package com.smishguard;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar si el usuario está autenticado
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Si el usuario ya ha iniciado sesión, redirigir a MainActivity
            Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Finalizar la actividad de Splash para que no se pueda volver a ella
        } else {
            // Si no hay un usuario autenticado, redirigir a LoginActivity
            Intent intent = new Intent(SplashScreenActivity.this, InitialActivity.class);
            startActivity(intent);
            finish(); // Finalizar la actividad de Splash para que no se pueda volver a ella
        }
    }
}
