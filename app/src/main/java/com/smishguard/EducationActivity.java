package com.smishguard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.smishguard.databinding.ActivityEducationBinding;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import java.util.Collections;
import java.util.Comparator;

public class EducationActivity extends AppCompatActivity {

    private ActivityEducationBinding binding;
    private List<String> imageUrls;
    private ImagesCarouselEducationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEducationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ocultarBarrasDeSistema();

        binding.btnBackEducation.setOnClickListener(view -> {
            startActivity(new Intent(EducationActivity.this, MainActivity.class));
        });

        binding.btnGoEducationTest.setOnClickListener(view -> {
            startActivity(new Intent(EducationActivity.this, EducationTestActivity.class));
        });

        // Configurar el carrusel de fotos
        imageUrls = new ArrayList<>();
        adapter = new ImagesCarouselEducationAdapter(this, imageUrls);
        binding.viewPager.setAdapter(adapter);

        // Vincular el TabLayout con el ViewPager para el indicador de imágenes
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            // El TabLayout no necesita configuración en cada posición
        }).attach();

        // Cargar imágenes desde Firebase Storage
        loadImagesFromFirebase();
    }

    private void loadImagesFromFirebase() {
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://smishguard-6cf37.firebasestorage.app");
        StorageReference storageRef = storage.getReference().child("education_images");

        // Crear una lista temporal para almacenar las referencias de archivo
        List<StorageReference> fileReferences = new ArrayList<>();

        storageRef.listAll().addOnSuccessListener(listResult -> {
            // Agregar todos los archivos encontrados a la lista temporal
            fileReferences.addAll(listResult.getItems());

            // Ordenar los archivos alfabéticamente por nombre
            Collections.sort(fileReferences, Comparator.comparing(StorageReference::getName));

            // Obtener las URLs en el orden deseado
            for (StorageReference fileRef : fileReferences) {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrls.add(uri.toString());

                    // Actualizar el adaptador después de agregar todas las URLs en orden
                    if (imageUrls.size() == fileReferences.size()) {
                        adapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(EducationActivity.this, "Error al cargar imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(EducationActivity.this, "Error al cargar imágenes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("FirebaseStorage", "Error al listar archivos: ", e);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(EducationActivity.this, MainActivity.class);
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
