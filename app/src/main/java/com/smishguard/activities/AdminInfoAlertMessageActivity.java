package com.smishguard.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.smishguard.databinding.ActivityAdminInfoAlertMessageBinding;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;

public class AdminInfoAlertMessageActivity extends AppCompatActivity {

    private ActivityAdminInfoAlertMessageBinding binding;
    private String alertMessageId;
    private static final String TAG = "AdminInfoAlertMessage";
    private static final String URL_ALERT_DETAILS = "https://smishguard-api-gateway.onrender.com/mensajes-para-publicar/";
    private static final String URL_POST_TWEET = "https://smishguard-api-gateway.onrender.com/publicar-tweet";
    private static final String URL_DELETE_ALERT = "https://smishguard-api-gateway.onrender.com/eliminar-mensaje-para-publicar/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminInfoAlertMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ocultarBarrasDeSistema();

        alertMessageId = getIntent().getStringExtra("alertMessageId");
        String contenido = getIntent().getStringExtra("contenido");
        String url = getIntent().getStringExtra("url");
        String nivelPeligro = getIntent().getStringExtra("nivel_peligro");
        String justificacionGpt = getIntent().getStringExtra("justificacion_gpt");
        int ponderado = getIntent().getIntExtra("ponderado", -1);

        // Muestra los datos recibidos directamente
        binding.textViewMensajeAnalizado.setText(contenido != null ? contenido : "No disponible");
        binding.textViewEnlace.setText(url != null ? url : "No disponible");
        binding.textViewAnalisisGpt.setText(justificacionGpt != null ? justificacionGpt : "No disponible");
        binding.textViewAnalisisSmishguard.setText(nivelPeligro != null ? nivelPeligro : "No disponible");

        // Configuración del puntaje con color
        if (ponderado >= 0) {
            binding.textViewPuntaje.setText(ponderado + "/10");
            setPuntajeColor(ponderado);
        } else {
            binding.textViewPuntaje.setText("No disponible");
        }

        binding.btnBackAlertMessages.setOnClickListener(view -> {
            startActivity(new Intent(AdminInfoAlertMessageActivity.this, AdminAlertMessagesActivity.class));
        });

        // Cargar los detalles del mensaje de alerta
        loadAlertMessageDetails();

        // Publicar tweet
        binding.btnPostTweet.setOnClickListener(view -> publishTweet());

        // Eliminar alerta
        binding.btnDeleteAlert.setOnClickListener(view -> deleteAlert());
    }

    private void loadAlertMessageDetails() {
        OkHttpClient client = new OkHttpClient();
        String url = URL_ALERT_DETAILS + alertMessageId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error al cargar detalles de la alerta: " + e.getMessage(), e);
                runOnUiThread(() ->
                        Toast.makeText(AdminInfoAlertMessageActivity.this, "Error al cargar detalles de la alerta", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject alertJson = new JSONObject(responseBody);
                        JSONObject analysis = alertJson.getJSONObject("analisis");

                        runOnUiThread(() -> {
                            try {
                                // Mostrar contenido y enlace
                                binding.textViewMensajeAnalizado.setText(alertJson.getString("contenido"));
                                binding.textViewEnlace.setText(alertJson.getString("url"));

                                // Mostrar análisis detallado y nivel de peligro
                                binding.textViewAnalisisGpt.setText(analysis.getString("justificacion_gpt"));
                                binding.textViewAnalisisSmishguard.setText(analysis.getString("nivel_peligro"));

                                // Obtener el puntaje ponderado y aplicarle el color de fondo adecuado
                                int puntaje = analysis.optInt("ponderado", 0); // Si no existe, asigna 0
                                if (puntaje >= 0) { // Evitar mostrar "null"
                                    binding.textViewPuntaje.setText(puntaje + "/10");
                                    setPuntajeColor(puntaje);
                                } else {
                                    binding.textViewPuntaje.setText("No disponible");
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(AdminInfoAlertMessageActivity.this, "Error al interpretar detalles de la alerta", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } catch (JSONException e) {
                        Log.e(TAG, "Error al interpretar detalles de la alerta", e);
                    }
                } else {
                    Log.e(TAG, "Error en la respuesta: " + response.message());
                }
            }
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

    private void publishTweet() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Publicación")
                .setMessage("¿Estás seguro de que quieres publicar este tweet?")
                .setPositiveButton("Publicar", (dialog, which) -> {
                    OkHttpClient client = new OkHttpClient();
                    JSONObject jsonBody = new JSONObject();
                    try {
                        jsonBody.put("mensaje", binding.textViewMensajeAnalizado.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al preparar contenido para tweet", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
                    Request request = new Request.Builder()
                            .url(URL_POST_TWEET)
                            .post(body)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.e(TAG, "Error al publicar tweet: " + e.getMessage(), e);
                            runOnUiThread(() ->
                                    Toast.makeText(AdminInfoAlertMessageActivity.this, "Error al publicar tweet", Toast.LENGTH_SHORT).show()
                            );
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if (response.isSuccessful()) {
                                runOnUiThread(() ->
                                        Toast.makeText(AdminInfoAlertMessageActivity.this, "Tweet publicado con éxito", Toast.LENGTH_SHORT).show()
                                );
                                updateMessageStatusToPublished(); // Llamada para actualizar el estado a "publicado"
                            } else {
                                Log.e(TAG, "Error en respuesta de publicación de tweet: " + response.message());
                                runOnUiThread(() ->
                                        Toast.makeText(AdminInfoAlertMessageActivity.this, "Error al publicar tweet", Toast.LENGTH_SHORT).show()
                                );
                            }
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void updateMessageStatusToPublished() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://smishguard-api-gateway.onrender.com/actualizar-publicado/" + alertMessageId;

        Request request = new Request.Builder()
                .url(url)
                .put(RequestBody.create("", null)) // Cuerpo vacío para una solicitud PUT
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error al actualizar el estado de publicación: " + e.getMessage(), e);
                runOnUiThread(() ->
                        Toast.makeText(AdminInfoAlertMessageActivity.this, "Error al actualizar el estado del mensaje", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(AdminInfoAlertMessageActivity.this, "Estado de publicación actualizado con éxito", Toast.LENGTH_SHORT).show();
                        // Redirige de regreso a la actividad anterior
                        Intent intent = new Intent(AdminInfoAlertMessageActivity.this, AdminAlertMessagesActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    Log.e(TAG, "Error en respuesta de actualización: " + response.message());
                    runOnUiThread(() ->
                            Toast.makeText(AdminInfoAlertMessageActivity.this, "Error al actualizar el estado del mensaje", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void deleteAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás seguro de que quieres eliminar esta alerta?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    OkHttpClient client = new OkHttpClient();
                    String url = URL_DELETE_ALERT + alertMessageId;

                    Request request = new Request.Builder()
                            .url(url)
                            .delete()
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.e(TAG, "Error al eliminar alerta: " + e.getMessage(), e);
                            runOnUiThread(() ->
                                    Toast.makeText(AdminInfoAlertMessageActivity.this, "Error al eliminar alerta", Toast.LENGTH_SHORT).show()
                            );
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if (response.isSuccessful()) {
                                runOnUiThread(() -> {
                                    Toast.makeText(AdminInfoAlertMessageActivity.this, "Alerta eliminada con éxito", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(AdminInfoAlertMessageActivity.this, AdminAlertMessagesActivity.class));
                                });
                            } else {
                                Log.e(TAG, "Error en la respuesta de eliminación: " + response.message());
                                runOnUiThread(() ->
                                        Toast.makeText(AdminInfoAlertMessageActivity.this, "Error al eliminar alerta", Toast.LENGTH_SHORT).show()
                                );
                            }
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(AdminInfoAlertMessageActivity.this, AdminAlertMessagesActivity.class);
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