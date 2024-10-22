package com.smishguard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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

        // Obtener el usuario actualmente autenticado
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Mostrar la información del usuario en los TextView correspondientes
            binding.textViewProfileID.setText(currentUser.getUid());
            binding.textViewProfileEmail.setText(currentUser.getEmail());
            binding.textViewProfileDate.setText(currentUser.getMetadata() != null
                    ? android.text.format.DateFormat.format("dd/MM/yyyy", currentUser.getMetadata().getCreationTimestamp()).toString()
                    : "N/A");
        } else {
            // Manejar el caso en el que no hay un usuario autenticado
            binding.textViewProfileID.setText("N/A");
            binding.textViewProfileEmail.setText("N/A");
            binding.textViewProfileDate.setText("N/A");
        }

        // Botón para regresar a la pantalla principal
        binding.btnBackProfile.setOnClickListener(view -> {
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
        });

        // Botón para ir a la pantalla de soporte
        binding.btnSupport.setOnClickListener(view -> {
            startActivity(new Intent(ProfileActivity.this, SupportActivity.class));
        });

        // Botón para cerrar sesión
        binding.btnCloseSession.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProfileActivity.this, InitialActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        binding.btnGoX.setOnClickListener(view -> openTwitterProfile());
        binding.btnGoInstagram.setOnClickListener(view -> openInstagramProfile());

    }

    private void openTwitterProfile() {
        String twitterUsername = "SmishGuard";

        // Intent para abrir la aplicación de Twitter
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("twitter://user?screen_name=" + twitterUsername));
            intent.setPackage("com.twitter.android");
            startActivity(intent);
        } catch (Exception e) {
            // Si no está instalada la aplicación, abrir en el navegador
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://twitter.com/" + twitterUsername));
            startActivity(intent);
        }
    }

    private void openInstagramProfile() {
        String instagramUsername = "SmishGuard";

        // Intent para abrir la aplicación de Instagram
        Uri uri = Uri.parse("http://instagram.com/_u/" + instagramUsername);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.instagram.android");

        try {
            startActivity(intent);
        } catch (Exception e) {
            // Si no está instalada la aplicación, abrir en el navegador
            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://instagram.com/" + instagramUsername));
            startActivity(webIntent);
        }
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
