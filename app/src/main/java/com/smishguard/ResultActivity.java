package com.smishguard;

import android.app.AlertDialog;
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
import com.smishguard.databinding.ActivityResultBinding;
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

public class ResultActivity extends AppCompatActivity {

    private ActivityResultBinding binding;
    private OkHttpClient client;
    private static final String REPORT_URL = "https://smishguard-api-gateway.onrender.com/mensajes-para-publicar";
    private static final String SAVE_REPORT_URL = "https://smishguard-api-gateway.onrender.com/guardar-mensaje-para-publicar";
    private static final String BLOCK_URL = "https://smishguard-api-gateway.onrender.com/numeros-bloqueados";
    private static final String HISTORIAL_URL = "https://smishguard-api-gateway.onrender.com/historial-analisis-usuarios";
    private static final String TAG = "ResultActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ocultarBarrasDeSistema();

        client = new OkHttpClient();

        String mensajeAnalizado = getIntent().getStringExtra("mensajeAnalizado");
        String analisisSmishguard = getIntent().getStringExtra("analisisSmishguard");
        String analisisGpt = getIntent().getStringExtra("analisisGpt");
        String enlace = getIntent().getStringExtra("enlace");
        int puntaje = getIntent().getIntExtra("puntaje", 0);
        String numero = getIntent().getStringExtra("numero");

        // Mostrar u ocultar el botón de reporte según el puntaje
        if (puntaje > 7) {
            binding.btnShareReport.setVisibility(View.VISIBLE);
        } else {
            binding.btnShareReport.setVisibility(View.GONE);
        }

        if (numero.isEmpty()) {
            numero = JSONObject.NULL.toString();
            binding.btnBlockNumber.setVisibility(View.GONE);
        } else {
            String finalNumero = numero;
            binding.btnBlockNumber.setOnClickListener(view -> {
                Log.d(TAG, "Botón de bloqueo presionado.");
                verificarNumeroBloqueado(finalNumero);
            });
        }


        binding.textViewMensajeAnalizado.setText(mensajeAnalizado);
        binding.textViewAnalisisSmishguard.setText(analisisSmishguard);
        binding.textViewAnalisisGpt.setText(analisisGpt);
        binding.textViewEnlace.setText(enlace);
        binding.textViewPuntaje.setText(String.valueOf(puntaje) + "/10");

        setPuntajeColor(puntaje);

        // Verificar y guardar automáticamente en el historial
        verificarYGuardarHistorial(mensajeAnalizado, analisisSmishguard, analisisGpt, enlace, puntaje, numero);

        binding.btnShareReport.setOnClickListener(view -> {
            Log.d(TAG, "Botón de reporte presionado.");
            verificarMensajeReportado(mensajeAnalizado, analisisSmishguard, analisisGpt, enlace, puntaje);
        });

        binding.btnBackResult.setOnClickListener(view -> {
            startActivity(new Intent(ResultActivity.this, MainActivity.class));
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

    private void verificarYGuardarHistorial(String mensaje, String analisisSmishguard, String analisisGpt, String enlace, int puntaje, String numero) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = (user != null) ? user.getEmail() : "usuario@example.com";

        String urlWithParams = HISTORIAL_URL + "/" + userEmail;

        // Realizar la solicitud GET para verificar si el mensaje ya está en el historial
        Request request = new Request.Builder()
                .url(urlWithParams)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error en la solicitud de verificación de historial: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONArray historialArray = jsonObject.getJSONArray("historial");

                        boolean mensajeYaGuardado = false;
                        for (int i = 0; i < historialArray.length(); i++) {
                            JSONObject item = historialArray.getJSONObject(i);
                            if (mensaje.equals(item.getString("mensaje"))) {
                                mensajeYaGuardado = true;
                                break;
                            }
                        }

                        // Si el mensaje no está guardado, guardarlo en el historial
                        if (!mensajeYaGuardado) {
                            runOnUiThread(() -> guardarHistorialAutomatico(mensaje, analisisSmishguard, analisisGpt, enlace, puntaje, numero));
                        } else {
                            Log.d(TAG, "El mensaje ya está en el historial, no se guardará nuevamente.");
                        }

                    } catch (JSONException e) {
                        Log.e(TAG, "Error al procesar el JSON de historial: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "Error en la respuesta de verificación de historial. Código: " + response.code());
                }
            }
        });
    }

    private void guardarHistorialAutomatico(String mensaje, String analisisSmishguard, String analisisGpt, String enlace, int puntaje, String numero) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = (user != null) ? user.getEmail() : "usuario@example.com";

        JSONObject jsonBody = new JSONObject();
        try {
            JSONObject analisisObject = new JSONObject();
            analisisObject.put("calificacion_gpt", JSONObject.NULL);
            analisisObject.put("calificacion_ml", JSONObject.NULL);
            analisisObject.put("calificacion_vt", JSONObject.NULL);
            analisisObject.put("ponderado", puntaje);
            analisisObject.put("nivel_peligro", analisisSmishguard);
            analisisObject.put("justificacion_gpt", analisisGpt);

            jsonBody.put("mensaje", mensaje);
            jsonBody.put("url", enlace);
            jsonBody.put("analisis", analisisObject);
            jsonBody.put("numero_celular", numero);
            jsonBody.put("correo", userEmail);

            Log.d(TAG, "Cuerpo de historial automático JSON creado: " + jsonBody.toString());

        } catch (JSONException e) {
            Log.e(TAG, "Error al crear el historial JSON: " + e.getMessage());
            return;
        }

        enviarPost(HISTORIAL_URL, jsonBody);
    }

    private void verificarMensajeReportado(String mensaje, String analisisSmishguard, String analisisGpt, String enlace, int puntaje) {
        // Asegúrate de que REPORT_URL esté apuntando al endpoint correcto
        Request request = new Request.Builder()
                .url(REPORT_URL)
                .build();

        Log.d(TAG, "Iniciando verificación de mensaje reportado en URL: " + REPORT_URL);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error en la solicitud de verificación de reporte: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(ResultActivity.this, "Error en la verificación del reporte", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d(TAG, "Respuesta de verificación de reporte recibida: " + responseData);
                    try {
                        // Verifica si el JSON tiene el array esperado
                        JSONObject jsonObject = new JSONObject(responseData);
                        if (!jsonObject.has("documentos")) {
                            Log.e(TAG, "El JSON no contiene el array 'documentos'. Verifica la estructura del JSON.");
                            runOnUiThread(() -> Toast.makeText(ResultActivity.this, "Error en el formato de la respuesta", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        JSONArray historialArray = jsonObject.getJSONArray("documentos");

                        boolean alreadyReported = false;
                        for (int i = 0; i < historialArray.length(); i++) {
                            JSONObject item = historialArray.getJSONObject(i);
                            // Cambiar a "mensaje" si esa es la clave correcta
                            if (mensaje.equals(item.optString("contenido", ""))) {
                                alreadyReported = true;
                                break;
                            }
                        }

                        if (alreadyReported) {
                            runOnUiThread(() -> Toast.makeText(ResultActivity.this, "Este mensaje ya fue reportado", Toast.LENGTH_SHORT).show());
                        } else {
                            runOnUiThread(() -> enviarReporte(mensaje, analisisSmishguard, analisisGpt, enlace, puntaje));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error procesando la respuesta JSON de reporte: " + e.getMessage());
                        runOnUiThread(() -> Toast.makeText(ResultActivity.this, "Error en la verificación del reporte", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Log.e(TAG, "Error en la respuesta de verificación de reporte. Código: " + response.code());
                    runOnUiThread(() -> Toast.makeText(ResultActivity.this, "Error en la respuesta de verificación de reporte", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void verificarNumeroBloqueado(String numero) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = user != null ? user.getEmail() : "usuario@example.com";

        Request request = new Request.Builder()
                .url(BLOCK_URL + "/" + userEmail)
                .build();

        Log.d(TAG, "Iniciando verificación de número bloqueado...");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error en la solicitud de verificación de bloqueo: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(ResultActivity.this, "Error en la verificación del bloqueo", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d(TAG, "Respuesta de verificación de bloqueo recibida.");
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONArray numerosArray = jsonObject.getJSONArray("numeros");

                        boolean alreadyBlocked = false;
                        for (int i = 0; i < numerosArray.length(); i++) {
                            JSONObject item = numerosArray.getJSONObject(i);
                            if (numero.equals(item.getString("numero"))) {
                                alreadyBlocked = true;
                                break;
                            }
                        }

                        if (alreadyBlocked) {
                            runOnUiThread(() -> Toast.makeText(ResultActivity.this, "Este número ya está bloqueado", Toast.LENGTH_SHORT).show());
                        } else {
                            runOnUiThread(() -> mostrarConfirmacionBloqueo(numero));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error procesando la respuesta JSON de bloqueo: " + e.getMessage());
                        runOnUiThread(() -> Toast.makeText(ResultActivity.this, "Error en la verificación del bloqueo", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Log.e(TAG, "Error en la respuesta de verificación de bloqueo. Código: " + response.code());
                    runOnUiThread(() -> Toast.makeText(ResultActivity.this, "Error en la respuesta de verificación de bloqueo", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void enviarPost(String url, JSONObject jsonBody) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Log.d(TAG, "Enviando solicitud POST a " + url);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error en la solicitud POST: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(ResultActivity.this, "Error en la solicitud: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d(TAG, "Respuesta recibida para solicitud POST. Código: " + response.code());
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(ResultActivity.this, "Operación completada con éxito", Toast.LENGTH_SHORT).show());
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Sin mensaje de error";
                    Log.e(TAG, "Error del servidor: Código " + response.code() + ", respuesta: " + errorBody);
                    runOnUiThread(() -> Toast.makeText(ResultActivity.this, "Error en el servidor: Código " + response.code(), Toast.LENGTH_SHORT).show());
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
            Toast.makeText(ResultActivity.this, "Error al crear el reporte", Toast.LENGTH_SHORT).show();
            return;
        }

        enviarPost(SAVE_REPORT_URL, jsonBody);
    }

    private void mostrarConfirmacionBloqueo(String numero) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar bloqueo")
                .setMessage("¿Estás seguro de que deseas bloquear este número?")
                .setPositiveButton("Sí", (dialog, which) -> bloquearNumero(numero))
                .setNegativeButton("No", null)
                .show();
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
            Toast.makeText(ResultActivity.this, "Error al crear el cuerpo de la solicitud", Toast.LENGTH_SHORT).show();
            return;
        }

        enviarPost(BLOCK_URL, jsonBody);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ResultActivity.this, MainActivity.class);
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