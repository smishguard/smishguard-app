package com.smishguard.utils;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.smishguard.activities.AdminMainActivity;
import com.smishguard.activities.InitialActivity;
import com.smishguard.activities.MainActivity;

public class SplashScreenUtil extends AppCompatActivity {

    private static final String TAG = "SplashScreenUtil";

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
            Intent intent = new Intent(SplashScreenUtil.this, InitialActivity.class);
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
                            // Usuario es administrador, redirigir a AdminMainActivity
                            Log.d(TAG, "Usuario es administrador");
                            Intent intent = new Intent(SplashScreenUtil.this, AdminMainActivity.class);
                            startActivity(intent);
                        } else {
                            // Usuario no es administrador, redirigir a MainActivity y pasar showGuide extra si está presente
                            Log.d(TAG, "Usuario no es administrador");
                            Intent intent = new Intent(SplashScreenUtil.this, MainActivity.class);

                            // Obtener el valor del extra "showGuide" del intent que inició SplashScreenActivity
                            boolean showGuide = getIntent().getBooleanExtra("showGuide", false);
                            intent.putExtra("showGuide", showGuide);

                            startActivity(intent);
                        }
                    } else {
                        Log.w(TAG, "Error al verificar administrador", task.getException());
                        // Redirigir a MainActivity en caso de error
                        Intent intent = new Intent(SplashScreenUtil.this, MainActivity.class);
                        intent.putExtra("showGuide", getIntent().getBooleanExtra("showGuide", false));
                        startActivity(intent);
                    }

                    // Finalizar SplashScreen para que no se pueda volver a ella
                    finish();
                });
    }
}