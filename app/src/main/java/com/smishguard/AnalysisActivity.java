package com.smishguard;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.smishguard.databinding.ActivityAnalysisBinding;
import org.json.JSONArray;
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

public class AnalysisActivity extends AppCompatActivity {

    private ActivityAnalysisBinding binding;
    private OkHttpClient client;
    private static final String REPORT_URL = "https://smishguard-api-gateway.onrender.com/mensajes-para-publicar";
    private static final String SAVE_REPORT_URL = "https://smishguard-api-gateway.onrender.com/guardar-mensaje-para-publicar";
    private static final String BLOCK_URL = "https://smishguard-api-gateway.onrender.com/numeros-bloqueados";
    private static final String TAG = "AnalysisActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnalysisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ocultarBarrasDeSistema();
        client = new OkHttpClient();

        // Obtener los datos del Intent
        String mensajeAnalizado = getIntent().getStringExtra("body");
        String enlace = getIntent().getStringExtra("enlace");
        String analisisGpt = getIntent().getStringExtra("analisisGpt");
        int puntaje = getIntent().getIntExtra("puntaje", 0);
        String analisisSmishguard = getIntent().getStringExtra("analisisSmishguard");
        String numero = getIntent().getStringExtra("address");

        // Mostrar los datos en los TextViews
        binding.textViewMensajeAnalizado.setText(mensajeAnalizado);
        binding.textViewEnlace.setText(enlace);
        binding.textViewAnalisisGpt.setText(analisisGpt);
        binding.textViewPuntaje.setText(String.valueOf(puntaje) + "/10");
        binding.textViewAnalisisSmishguard.setText(analisisSmishguard);


        // Establecer el color de fondo del puntaje
        setPuntajeColor(puntaje);

        // Verificar y ajustar la visibilidad según las condiciones
        verificarReporteYBloqueo(mensajeAnalizado, puntaje, numero);

        binding.btnBlockNumber.setOnClickListener(view -> bloquearNumero(numero));

        binding.btnShareReport.setOnClickListener(view -> enviarReporte(mensajeAnalizado, analisisSmishguard, analisisGpt, enlace, puntaje));

        binding.btnBackAnalysis.setOnClickListener(view -> {
            startActivity(new Intent(AnalysisActivity.this, AnalyzedMessagesActivity.class));
        });
    }

    private void setPuntajeColor(int puntaje) {
        int backgroundColor;
        if (puntaje >= 1 && puntaje <= 3) {
            backgroundColor = Color.parseColor("#00FF00");
        } else if (puntaje >= 4 && puntaje <= 7) {
            backgroundColor = Color.parseColor("#FFFF00");
        } else {
            backgroundColor = Color.parseColor("#FF0000");
        }
        GradientDrawable background = (GradientDrawable) binding.textViewPuntaje.getBackground();
        background.setColor(backgroundColor);
    }

    private void verificarReporteYBloqueo(String mensaje, int puntaje, String numero) {
        if (puntaje > 7) {
            verificarMensajeReportado(mensaje);
        } else {
            // Ocultar los elementos si el puntaje es <= 7
            binding.btnShareReport.setVisibility(View.GONE);
            binding.textViewInfo.setVisibility(View.GONE);
        }

        // Verificar si el número es nulo o vacío antes de llamar a verificarNumeroBloqueado
        if (numero != null && !numero.isEmpty()) {
            verificarNumeroBloqueado(numero);
        } else {
            binding.btnBlockNumber.setVisibility(View.GONE);
        }
    }

    private void verificarMensajeReportado(String mensaje) {
        Request request = new Request.Builder()
                .url(REPORT_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error en la solicitud de verificación de reporte: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray historialArray = jsonObject.getJSONArray("documentos");

                        boolean alreadyReported = false;
                        for (int i = 0; i < historialArray.length(); i++) {
                            JSONObject item = historialArray.getJSONObject(i);
                            if (mensaje.equals(item.optString("contenido", ""))) {
                                alreadyReported = true;
                                break;
                            }
                        }

                        final boolean finalAlreadyReported = alreadyReported;
                        runOnUiThread(() -> {
                            if (finalAlreadyReported) {
                                binding.btnShareReport.setVisibility(View.GONE);
                                binding.textViewInfo.setVisibility(View.GONE);
                            } else {
                                binding.btnShareReport.setVisibility(View.VISIBLE);
                                binding.textViewInfo.setVisibility(View.VISIBLE);
                            }
                        });
                    } catch (JSONException e) {
                        Log.e(TAG, "Error al procesar JSON de verificación de reporte: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "Error en la respuesta de verificación de reporte. Código: " + response.code());
                }
            }
        });
    }

    private void verificarNumeroBloqueado(String numero) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = (user != null) ? user.getEmail() : "usuario@example.com";

        Request request = new Request.Builder()
                .url(BLOCK_URL + "/" + userEmail)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error en la solicitud de verificación de bloqueo: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        JSONArray numerosArray = jsonObject.getJSONArray("numeros");

                        boolean alreadyBlocked = false;
                        for (int i = 0; i < numerosArray.length(); i++) {
                            JSONObject item = numerosArray.getJSONObject(i);
                            if (numero.equals(item.getString("numero"))) {
                                alreadyBlocked = true;
                                break;
                            }
                        }

                        final boolean finalAlreadyBlocked = alreadyBlocked;
                        runOnUiThread(() -> {
                            if (finalAlreadyBlocked) {
                                binding.btnBlockNumber.setVisibility(View.GONE);
                            } else {
                                binding.btnBlockNumber.setVisibility(View.VISIBLE);
                            }
                        });
                    } catch (JSONException e) {
                        Log.e(TAG, "Error procesando la respuesta JSON de bloqueo: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "Error en la respuesta de verificación de bloqueo. Código: " + response.code());
                }
            }
        });
    }

    private void bloquearNumero(String numero) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = (user != null) ? user.getEmail() : "usuario@example.com";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("numero", numero);
            jsonBody.put("correo", userEmail);
            Log.d(TAG, "Cuerpo de bloqueo JSON creado: " + jsonBody.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Error al crear el cuerpo de bloqueo JSON: " + e.getMessage());
            Toast.makeText(AnalysisActivity.this, "Error al crear el cuerpo de la solicitud", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(BLOCK_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error en la solicitud de bloqueo de número: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(AnalysisActivity.this, "Error en la solicitud de bloqueo de número", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(AnalysisActivity.this, "Número bloqueado exitosamente", Toast.LENGTH_SHORT).show();
                        binding.btnBlockNumber.setVisibility(View.GONE);  // Ocultar botón después de bloquear
                    });
                } else {
                    Log.e(TAG, "Error en la respuesta de bloqueo de número. Código: " + response.code());
                    runOnUiThread(() -> Toast.makeText(AnalysisActivity.this, "Error al bloquear el número", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void enviarReporte(String mensaje, String analisisSmishguard, String analisisGpt, String enlace, int puntaje) {

        JSONObject jsonBody = new JSONObject();
        try {
            JSONObject analisisObject = new JSONObject();
            analisisObject.put("calificacion_gpt", JSONObject.NULL);
            analisisObject.put("calificacion_ml", JSONObject.NULL);
            analisisObject.put("calificacion_vt", JSONObject.NULL);
            analisisObject.put("ponderado", puntaje);
            analisisObject.put("nivel_peligro", analisisSmishguard);
            analisisObject.put("justificacion_gpt", analisisGpt);

            jsonBody.put("contenido", mensaje);
            jsonBody.put("url", enlace);
            jsonBody.put("analisis", analisisObject);

            Log.d(TAG, "Cuerpo de reporte JSON creado: " + jsonBody.toString());

        } catch (JSONException e) {
            Log.e(TAG, "Error al crear el reporte JSON: " + e.getMessage());
            Toast.makeText(AnalysisActivity.this, "Error al crear el reporte", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear el cuerpo de la solicitud y enviarla
        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(SAVE_REPORT_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error en la solicitud de reporte: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(AnalysisActivity.this, "Error al enviar el reporte", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(AnalysisActivity.this, "Reporte enviado exitosamente", Toast.LENGTH_SHORT).show();
                        binding.btnShareReport.setVisibility(View.GONE);  // Ocultar botón después de enviar el reporte
                        binding.textViewInfo.setVisibility(View.GONE);
                    });
                } else {
                    Log.e(TAG, "Error en la respuesta del reporte. Código: " + response.code());
                    runOnUiThread(() -> Toast.makeText(AnalysisActivity.this, "Error al enviar el reporte", Toast.LENGTH_SHORT).show());
                }
            }
        });
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