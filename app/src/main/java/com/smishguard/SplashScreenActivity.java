package com.smishguard;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar si el usuario está autenticado
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Verificar si el UID del usuario está en la lista de administradores
            if (isAdmin(userId)) {
                // Si es administrador, redirigir a AdminMainActivity
                Intent intent = new Intent(SplashScreenActivity.this, AdminMainActivity.class);
                startActivity(intent);
            } else {
                // Si no es administrador, redirigir a MainActivity
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(intent);
            }
        } else {
            // Si no hay un usuario autenticado, redirigir a LoginActivity
            Intent intent = new Intent(SplashScreenActivity.this, InitialActivity.class);
            startActivity(intent);
        }

        // Finalizar la actividad de Splash para que no se pueda volver a ella
        finish();
    }

    private boolean isAdmin(String userId) {
        Set<String> adminIds = new HashSet<>();

        try (InputStream inputStream = getAssets().open("admins_ids.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                adminIds.add(line.trim()); // Agregar cada UID a la lista
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Manejar el error aquí si es necesario (puedes mostrar un mensaje de error)
        }

        // Verificar si el UID del usuario actual está en la lista de administradores
        return adminIds.contains(userId);
    }
}
