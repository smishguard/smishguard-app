package com.smishguard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.smishguard.databinding.ActivityAdminUpdateEducationBinding;
import com.bumptech.glide.Glide;

public class AdminUpdateEducationActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ActivityAdminUpdateEducationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminUpdateEducationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ocultarBarrasDeSistema();

        binding.btnBackUpdateEducation.setOnClickListener(view -> {
            startActivity(new Intent(AdminUpdateEducationActivity.this, AdminMainActivity.class));
        });

        // Seleccionar imagen
        binding.btnSelectImage.setOnClickListener(view -> openImageChooser());

        // Subir imagen
        binding.btnUploadImage.setOnClickListener(view -> uploadImageToFirebase());
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            // Mostrar la imagen seleccionada en el ImageView
            Glide.with(this).load(imageUri).into(binding.imageViewPreview);
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            // Muestra un Toast de carga
            Toast.makeText(this, "Cargando la imagen, por favor espera", Toast.LENGTH_SHORT).show();

            // Define el bucket de almacenamiento y la carpeta
            FirebaseStorage storage = FirebaseStorage.getInstance("gs://smishguard-6cf37.firebasestorage.app");
            StorageReference storageRef = storage.getReference().child("education_images/" + System.currentTimeMillis() + ".jpg");

            // Subir la imagen
            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> Toast.makeText(this, "Imagen cargada exitosamente", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Por favor selecciona una imagen primero", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(AdminUpdateEducationActivity.this, AdminMainActivity.class);
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