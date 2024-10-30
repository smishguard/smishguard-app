package com.smishguard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.smishguard.databinding.ActivitySupportBinding;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupportActivity extends AppCompatActivity {

    private ActivitySupportBinding binding;
    private OkHttpClient client;
    private static final String URL = "https://smishguard-api-gateway.onrender.com/comentario-soporte";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySupportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ocultarBarrasDeSistema();

        // Inicializar OkHttpClient
        client = new OkHttpClient();

        // Configurar el botón para enviar los comentarios
        binding.btnSendComments.setOnClickListener(view -> {
            String comentarios = binding.setTextComments.getText().toString().trim();

            // Verificar que el campo no esté vacío
            if (TextUtils.isEmpty(comentarios)) {
                Toast.makeText(SupportActivity.this, "Por favor ingrese sus comentarios.", Toast.LENGTH_SHORT).show();
            } else {
                // Enviar los comentarios
                enviarComentarios(comentarios);
            }
        });

        binding.btnGoX.setOnClickListener(view -> openTwitterProfile());
        binding.btnGoInstagram.setOnClickListener(view -> openInstagramProfile());

        // Botón para regresar a la pantalla principal
        binding.btnBackSupport.setOnClickListener(view -> {
            startActivity(new Intent(SupportActivity.this, ProfileActivity.class));
        });
    }

    // Método para enviar los comentarios al servidor
    private void enviarComentarios(String comentarios) {
        // Obtener el correo del usuario de Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = (user != null) ? user.getEmail() : "Usuario desconocido";

        // Crear el objeto JSON con solo los campos requeridos
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("comentario", comentarios);
            jsonBody.put("correo", userEmail);
        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(SupportActivity.this, "Error al crear el cuerpo de la solicitud", Toast.LENGTH_SHORT).show());
            return;
        }

        // Convertir el objeto JSON a un RequestBody
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

        // Crear la solicitud HTTP
        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .build();

        // Enviar la solicitud de manera asíncrona
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Manejar error en la solicitud
                Log.e("SupportActivity", "Error en la solicitud: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(SupportActivity.this, "Error al enviar los comentarios", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // Manejar respuesta del servidor
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(SupportActivity.this, "Comentarios enviados exitosamente", Toast.LENGTH_SHORT).show();
                        binding.setTextComments.setText(""); // Limpiar el campo de comentarios
                    });
                } else {
                    Log.e("SupportActivity", "Error en la respuesta del servidor: " + response.message());
                    runOnUiThread(() -> Toast.makeText(SupportActivity.this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show());
                }
            }
        });
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
        Intent intent = new Intent(SupportActivity.this, ProfileActivity.class);
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
}