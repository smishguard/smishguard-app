package com.smishguard.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.smishguard.databinding.ActivityConfigurationBinding;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;
    private ActivityConfigurationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflar el layout usando View Binding
        binding = ActivityConfigurationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ocultarBarrasDeSistema();

        // Listener para el botón "Confirmar" usando binding
        binding.btnConfirmSettings.setOnClickListener(v -> requestPermissionsBasedOnCheckboxes());

        binding.btnBackMainConfiguration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ConfigurationActivity.this, MainActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ConfigurationActivity.this, MainActivity.class);
        // Estas banderas crean una nueva tarea con MainActivity y eliminan todas las actividades anteriores
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ocultarBarrasDeSistema();
    }

    private void ocultarBarrasDeSistema() {
        // Método para ocultar las barras del sistema
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    // Método para solicitar los permisos seleccionados en los checkbox
    private void requestPermissionsBasedOnCheckboxes() {
        ArrayList<String> permissionsList = new ArrayList<>();

        // Verifica si el checkbox de SMS está marcado
        if (binding.checkBoxSms.isChecked()) {
            permissionsList.add(Manifest.permission.RECEIVE_SMS);
            permissionsList.add(Manifest.permission.READ_SMS);
        }

        // Verifica si el checkbox de Contactos está marcado
        if (binding.checkBoxContacts.isChecked()) {
            permissionsList.add(Manifest.permission.READ_CONTACTS);
        }

        // Si no hay permisos seleccionados, mostrar mensaje
        if (permissionsList.isEmpty()) {
            Toast.makeText(this, "Por favor selecciona al menos un permiso.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convierte la lista de permisos en un array de String
        String[] permissionsArray = permissionsList.toArray(new String[0]);

        // Verificar si los permisos ya están concedidos
        if (!hasPermissions(permissionsArray)) {
            // Solicitar permisos
            ActivityCompat.requestPermissions(this, permissionsArray, PERMISSION_REQUEST_CODE);
        } else {
            // Los permisos ya están concedidos
            Toast.makeText(this, "Todos los permisos ya están concedidos", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para verificar si los permisos ya están concedidos
    private boolean hasPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // Método para manejar el resultado de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            Map<String, Integer> permissionResults = new HashMap<>();
            for (int i = 0; i < permissions.length; i++) {
                permissionResults.put(permissions[i], grantResults[i]);
            }

            // Verifica los permisos concedidos
            if ((permissionResults.get(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
                    permissionResults.get(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) ||
                    permissionResults.get(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                // Algunos o todos los permisos concedidos
                Toast.makeText(this, "Permisos concedidos.", Toast.LENGTH_SHORT).show();
            } else {
                // Algún permiso fue denegado
                Toast.makeText(this, "Algunos permisos fueron denegados", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Asegúrate de limpiar el objeto binding cuando se destruya la actividad
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}