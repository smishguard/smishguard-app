package com.smishguard.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.smishguard.databinding.ActivityChangeCredentialsBinding;
import java.util.regex.Pattern;

public class ChangeCredentialsActivity extends AppCompatActivity {

    private ActivityChangeCredentialsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangeCredentialsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ocultarBarrasDeSistema();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Botón para regresar a la pantalla de perfil
        binding.btnBackChangeCredentials.setOnClickListener(view -> {
            startActivity(new Intent(ChangeCredentialsActivity.this, ProfileActivity.class));
        });

        // Lógica para cambiar el correo
        binding.btnChangeMail.setOnClickListener(view -> {
            String newEmail = binding.setTextNewMail.getText().toString().trim();
            if (newEmail.isEmpty()) {
                Toast.makeText(ChangeCredentialsActivity.this, "Por favor ingresa un nuevo correo", Toast.LENGTH_SHORT).show();
                return;
            }

            // Mostrar un AlertDialog para solicitar la contraseña actual
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirmar Cambio de Correo");

            // Agregar un campo de texto para la contraseña
            final EditText inputPassword = new EditText(this);
            inputPassword.setHint("Ingrese su contraseña actual");
            inputPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            builder.setView(inputPassword);

            // Configurar el botón de cambio de correo
            builder.setPositiveButton("Cambiar", (dialog, which) -> {
                String currentPassword = inputPassword.getText().toString().trim();
                if (currentPassword.isEmpty()) {
                    Toast.makeText(ChangeCredentialsActivity.this, "Por favor ingresa tu contraseña para reautenticación", Toast.LENGTH_SHORT).show();
                    return;
                }

                reauthenticateAndSendVerification(currentUser, newEmail, currentPassword);
            });

            builder.setNegativeButton("Cancelar", null);
            builder.show();
        });

        // Lógica para cambiar la contraseña
        binding.btnChangePassword.setOnClickListener(view -> {
            String newPassword = binding.setTextNewPassword.getText().toString().trim();
            String confirmNewPassword = binding.setTextConfirmNewPassword.getText().toString().trim();

            if (newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa y confirma tu nueva contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmNewPassword)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verificar que la nueva contraseña cumpla con los requisitos mínimos
            if (!esPasswordValido(newPassword)) {
                return;
            }

            // Solicitar la contraseña actual para reautenticación antes de cambiar la contraseña
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirmar Cambio de Contraseña");

            final EditText inputCurrentPassword = new EditText(this);
            inputCurrentPassword.setHint("Ingrese su contraseña actual");
            inputCurrentPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            builder.setView(inputCurrentPassword);

            builder.setPositiveButton("Cambiar", (dialog, which) -> {
                String currentPassword = inputCurrentPassword.getText().toString().trim();
                if (currentPassword.isEmpty()) {
                    Toast.makeText(this, "Por favor ingresa tu contraseña actual para reautenticación", Toast.LENGTH_SHORT).show();
                    return;
                }

                reauthenticateAndChangePassword(currentUser, newPassword, currentPassword);
            });

            builder.setNegativeButton("Cancelar", null);
            builder.show();
        });
    }

    private void reauthenticateAndSendVerification(FirebaseUser currentUser, String newEmail, String password) {
        if (currentUser != null && currentUser.getProviderData().get(1).getProviderId().equals("password")) {
            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);

            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            sendVerificationEmailAndSignOut(currentUser, newEmail);
                        } else {
                            Toast.makeText(this, "Reautenticación fallida: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Solo los usuarios registrados con correo y contraseña pueden cambiar su correo.", Toast.LENGTH_LONG).show();
        }
    }

    private void sendVerificationEmailAndSignOut(FirebaseUser user, String newEmail) {
        user.verifyBeforeUpdateEmail(newEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Correo de verificación enviado a " + newEmail + ". Por favor verifica tu correo.", Toast.LENGTH_LONG).show();
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(ChangeCredentialsActivity.this, InitialActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Error al enviar correo de verificación: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void reauthenticateAndChangePassword(FirebaseUser currentUser, String newPassword, String currentPassword) {
        if (currentUser != null && currentUser.getProviderData().get(1).getProviderId().equals("password")) {
            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);

            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            currentUser.updatePassword(newPassword)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            Toast.makeText(this, "Contraseña actualizada exitosamente, vuelve a iniciar sesión", Toast.LENGTH_SHORT).show();
                                            // Cerrar sesión y redirigir al usuario a InitialActivity
                                            FirebaseAuth.getInstance().signOut();
                                            Intent intent = new Intent(ChangeCredentialsActivity.this, InitialActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(this, "Error al actualizar la contraseña: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(this, "Reautenticación fallida: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Solo los usuarios registrados con correo y contraseña pueden cambiar su contraseña.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean esPasswordValido(String password) {
        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }

        Pattern mayusculaPattern = Pattern.compile("[A-Z]");
        if (!mayusculaPattern.matcher(password).find()) {
            Toast.makeText(this, "La contraseña debe tener al menos una letra mayúscula", Toast.LENGTH_SHORT).show();
            return false;
        }

        Pattern minusculaPattern = Pattern.compile("[a-z]");
        if (!minusculaPattern.matcher(password).find()) {
            Toast.makeText(this, "La contraseña debe tener al menos una letra minúscula", Toast.LENGTH_SHORT).show();
            return false;
        }

        Pattern numeroPattern = Pattern.compile("[0-9]");
        if (!numeroPattern.matcher(password).find()) {
            Toast.makeText(this, "La contraseña debe tener al menos un número", Toast.LENGTH_SHORT).show();
            return false;
        }

        Pattern especialPattern = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");
        if (!especialPattern.matcher(password).find()) {
            Toast.makeText(this, "La contraseña debe tener al menos un carácter especial", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ChangeCredentialsActivity.this, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ocultarBarrasDeSistema();
    }

    private void ocultarBarrasDeSistema() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
}