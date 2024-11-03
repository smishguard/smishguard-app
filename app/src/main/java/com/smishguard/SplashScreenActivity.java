package com.smishguard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashScreenActivity extends AppCompatActivity {

    private static final String TAG = "SplashScreenActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar si el usuario está autenticado
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Verificar si el UID del usuario está en la colección de administradores
            checkIfAdmin(userId);
        } else {
            // Si no hay un usuario autenticado, redirigir a LoginActivity
            Intent intent = new Intent(SplashScreenActivity.this, InitialActivity.class);
            startActivity(intent);
            finish(); // Finalizar la actividad de Splash para que no se pueda volver a ella
        }
    }

    private void checkIfAdmin(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Buscar el documento en la colección "admins" con el UID del usuario actual
        db.collection("admins").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Si el documento existe, es un administrador
                            Log.d(TAG, "Usuario es administrador");
                            Intent intent = new Intent(SplashScreenActivity.this, AdminMainActivity.class);
                            startActivity(intent);
                        } else {
                            // Si no existe, redirigir a MainActivity
                            Log.d(TAG, "Usuario no es administrador");
                            Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        Log.w(TAG, "Error al verificar administrador", task.getException());
                        // Redirigir a MainActivity en caso de error
                        Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                        startActivity(intent);
                    }

                    // Finalizar SplashScreen para que no se pueda volver a ella
                    finish();
                });
    }
}