package com.smishguard.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.smishguard.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ocultarBarrasDeSistema();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            binding.textViewProfileID.setText(currentUser.getUid());
            binding.textViewProfileEmail.setText(currentUser.getEmail());
            binding.textViewProfileDate.setText(currentUser.getMetadata() != null
                    ? android.text.format.DateFormat.format("dd/MM/yyyy", currentUser.getMetadata().getCreationTimestamp()).toString()
                    : "N/A");
        } else {
            binding.textViewProfileID.setText("N/A");
            binding.textViewProfileEmail.setText("N/A");
            binding.textViewProfileDate.setText("N/A");
        }

        binding.btnBackProfile.setOnClickListener(view -> {
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
        });

        binding.btnSupport.setOnClickListener(view -> {
            startActivity(new Intent(ProfileActivity.this, SupportActivity.class));
        });

        binding.btnChangeCredentials.setOnClickListener(view -> {
            startActivity(new Intent(ProfileActivity.this, ChangeCredentialsActivity.class));
        });

        binding.btnCloseSession.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProfileActivity.this, InitialActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        binding.btnDeleteAccount.setOnClickListener(view -> {
            if (currentUser != null) {
                new AlertDialog.Builder(this)
                        .setTitle("Eliminar Cuenta")
                        .setMessage("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.")
                        .setPositiveButton("Eliminar", (dialog, which) -> {
                            currentUser.delete()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(ProfileActivity.this, "Cuenta eliminada exitosamente", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(ProfileActivity.this, InitialActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(ProfileActivity.this, "Error al eliminar la cuenta: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
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