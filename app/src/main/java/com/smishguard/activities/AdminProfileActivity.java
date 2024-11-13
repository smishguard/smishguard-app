package com.smishguard.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.smishguard.databinding.ActivityAdminProfileBinding;

public class AdminProfileActivity extends AppCompatActivity {

    private ActivityAdminProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminProfileBinding.inflate(getLayoutInflater());
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

        binding.btnBackAdminProfile.setOnClickListener(view -> {
            startActivity(new Intent(AdminProfileActivity.this, AdminMainActivity.class));
        });

        binding.btnCloseSessionAdmin.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(AdminProfileActivity.this, InitialActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(AdminProfileActivity.this, AdminMainActivity.class);
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