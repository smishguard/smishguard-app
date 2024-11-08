package com.smishguard;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.smishguard.databinding.ActivityManualAnalysisBinding;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.Executors;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ManualAnalysisActivity extends AppCompatActivity {

    private ActivityManualAnalysisBinding binding;
    private OkHttpClient client;
    private static final String URL = "https://smishguard-api-gateway.onrender.com/consultar-modelo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManualAnalysisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ocultarBarrasDeSistema();

        // Inicializar OkHttpClient
        client = new OkHttpClient();

        // Botón para regresar a la pantalla principal
        binding.btnBackMainManualAnalisis.setOnClickListener(view -> {
            startActivity(new Intent(ManualAnalysisActivity.this, MainActivity.class));
        });

        // Botón para iniciar el análisis
        binding.btnAnalisis.setOnClickListener(view -> {
            String mensaje = binding.setTextAnalisis.getText().toString();

            // Verificar que el campo no esté vacío
            if (TextUtils.isEmpty(mensaje)) {
                Toast.makeText(ManualAnalysisActivity.this, "Por favor ingrese un mensaje para analizar.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ManualAnalysisActivity.this, "Analizando...", Toast.LENGTH_SHORT).show();
                // Enviar la solicitud al backend Flask
                enviarSolicitudAnalisis(mensaje);
            }
        });
    }

    private void enviarSolicitudAnalisis(String mensaje) {
        // Crear ProgressDialog para indicar que la solicitud está en proceso
        ProgressDialog progressDialog = new ProgressDialog(ManualAnalysisActivity.this);
        progressDialog.setMessage("Analizando...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Crear el objeto JSON
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("mensaje", mensaje);
        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog.dismiss(); // Cerrar el ProgressDialog en caso de error
            Toast.makeText(ManualAnalysisActivity.this, "Error al crear el cuerpo de la solicitud", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("ManualAnalysisActivity", "Enviando solicitud con mensaje: " + mensaje);

        // Crear el RequestBody
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

        // Crear la solicitud HTTP
        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .build();

        // Ejecutar la solicitud en un hilo secundario
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Ejecutar la solicitud y esperar la respuesta
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d("ManualAnalysisActivity", "Respuesta exitosa: " + responseData);

                    JSONObject jsonResponse = new JSONObject(responseData);
                    String mensajeAnalizado = jsonResponse.getString("mensaje_analizado");
                    String analisisSmishguard = jsonResponse.getString("analisis_smishguard");
                    String analisisGpt = jsonResponse.getString("analisis_gpt");
                    String enlace = jsonResponse.getString("enlace");
                    int puntaje = jsonResponse.getInt("puntaje");

                    // Actualizar la UI en el hilo principal
                    runOnUiThread(() -> {
                        progressDialog.dismiss(); // Cerrar el ProgressDialog
                        Intent intent = new Intent(ManualAnalysisActivity.this, ResultActivity.class);
                        intent.putExtra("mensajeAnalizado", mensajeAnalizado);
                        intent.putExtra("analisisSmishguard", analisisSmishguard);
                        intent.putExtra("analisisGpt", analisisGpt);
                        intent.putExtra("enlace", enlace);
                        intent.putExtra("puntaje", puntaje);
                        intent.putExtra("numero", "");
                        startActivity(intent);
                    });
                } else {
                    Log.e("ManualAnalysisActivity", "Error en el servidor: " + response.message());
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(ManualAnalysisActivity.this, "Error en el servidor: " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(ManualAnalysisActivity.this, "Error al procesar la solicitud", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ManualAnalysisActivity.this, MainActivity.class);
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