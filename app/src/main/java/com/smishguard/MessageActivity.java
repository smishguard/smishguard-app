package com.smishguard;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.smishguard.databinding.ActivityMessageBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MessageActivity extends AppCompatActivity {

    private ActivityMessageBinding binding;
    private OkHttpClient client;
    private static final String URL = "https://smishguard-api-gateway.onrender.com/consultar-modelo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ocultarBarrasDeSistema();

        // Inicializar OkHttpClient
        client = new OkHttpClient();

        // Obtener los datos del Intent
        final String address = getIntent().getStringExtra("address");
        String body = getIntent().getStringExtra("body");
        long date = getIntent().getLongExtra("date", -1);

        // Formatear la fecha
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String formattedDate = sdf.format(new Date(date));

        // Asignar los datos a los TextViews
        binding.textViewAddress.setText(address);
        binding.textViewBody.setText(body);
        binding.textViewDate.setText(formattedDate);

        // Botón para iniciar el análisis
        binding.btnAnalyze.setOnClickListener(view -> {
            String mensaje = binding.textViewBody.getText().toString();

            if (TextUtils.isEmpty(address)) {
                Toast.makeText(MessageActivity.this, "Número de remitente no disponible", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(MessageActivity.this, "Analizando...", Toast.LENGTH_SHORT).show();
            // Enviar la solicitud al backend Flask
            enviarSolicitudAnalisis(mensaje, address);
        });

        binding.btnBackMessage.setOnClickListener(view -> {
            startActivity(new Intent(MessageActivity.this, InboxActivity.class));
        });
    }

    // Método para enviar la solicitud al backend Flask
    private void enviarSolicitudAnalisis(String mensaje, String address) {
        // Crear el objeto JSON
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("mensaje", mensaje);
            jsonBody.put("numero_celular", address);
        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(MessageActivity.this, "Error al crear el cuerpo de la solicitud", Toast.LENGTH_SHORT).show());
            return;
        }

        // Mostrar un mensaje de log para saber que la solicitud está por enviarse
        Log.d("MessageActivity", "Enviando solicitud con mensaje: " + mensaje);

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
            public void onFailure(Call call, IOException e) {
                Log.e("MessageActivity", "Error en la solicitud: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(MessageActivity.this, "Error al conectarse al servidor", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d("MessageActivity", "Respuesta exitosa: " + responseData);

                    // Parsear la respuesta como JSON
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        String mensajeAnalizado = jsonResponse.getString("mensaje_analizado");
                        String analisisSmishguard = jsonResponse.getString("analisis_smishguard");
                        String analisisGpt = jsonResponse.getString("analisis_gpt");
                        String enlace = jsonResponse.getString("enlace");
                        String numero = jsonResponse.getString("numero_celular");
                        int puntaje = jsonResponse.getInt("puntaje");

                        // Crear un Intent para ir a ResultActivity y pasar los datos
                        Intent intent = new Intent(MessageActivity.this, ResultActivity.class);
                        intent.putExtra("mensajeAnalizado", mensajeAnalizado);
                        intent.putExtra("analisisSmishguard", analisisSmishguard);
                        intent.putExtra("analisisGpt", analisisGpt);
                        intent.putExtra("enlace", enlace);
                        intent.putExtra("puntaje", puntaje);
                        intent.putExtra("numero", numero);
                        startActivity(intent);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(MessageActivity.this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Log.e("MessageActivity", "Error en el servidor: " + response.message());
                    runOnUiThread(() -> Toast.makeText(MessageActivity.this, "Error en el servidor: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(MessageActivity.this, InboxActivity.class);
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