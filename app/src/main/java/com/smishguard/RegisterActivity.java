package com.smishguard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.smishguard.databinding.ActivityRegisterBinding;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        FirebaseApp.initializeApp(this);

        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.setTextMailR.getText().toString().trim();
                String password = binding.setTextPasswordR.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Por favor ingresa un correo válido", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!esPasswordValido(password)) {
                    return;
                }

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Error en el registro: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        binding.btnGoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
    }

    // Método para validar la contraseña
    private boolean esPasswordValido(String password) {
        // Verifica si tiene al menos 6 caracteres
        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Verifica si tiene al menos una letra mayúscula
        Pattern mayusculaPattern = Pattern.compile("[A-Z]");
        Matcher matcherMayuscula = mayusculaPattern.matcher(password);
        if (!matcherMayuscula.find()) {
            Toast.makeText(this, "La contraseña debe tener al menos una letra mayúscula", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Verifica si tiene al menos una letra minúscula
        Pattern minusculaPattern = Pattern.compile("[a-z]");
        Matcher matcherMinuscula = minusculaPattern.matcher(password);
        if (!matcherMinuscula.find()) {
            Toast.makeText(this, "La contraseña debe tener al menos una letra minúscula", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Verifica si tiene al menos un número
        Pattern numeroPattern = Pattern.compile("[0-9]");
        Matcher matcherNumero = numeroPattern.matcher(password);
        if (!matcherNumero.find()) {
            Toast.makeText(this, "La contraseña debe tener al menos un número", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Verifica si tiene al menos un carácter especial
        Pattern especialPattern = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");
        Matcher matcherEspecial = especialPattern.matcher(password);
        if (!matcherEspecial.find()) {
            Toast.makeText(this, "La contraseña debe tener al menos un carácter especial", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // Limpiar el objeto binding cuando se destruya la actividad
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}