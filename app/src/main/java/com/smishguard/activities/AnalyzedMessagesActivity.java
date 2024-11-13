package com.smishguard.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.smishguard.adapters.AnalyzedMessagesAdapter;
import com.smishguard.databinding.ActivityAnalyzedMessagesBinding;
import com.smishguard.models.SmsModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AnalyzedMessagesActivity extends AppCompatActivity {

    private ActivityAnalyzedMessagesBinding binding;
    private List<SmsModel> reportedMessagesList = new ArrayList<>();
    private AnalyzedMessagesAdapter analyzedMessagesAdapter;
    private OkHttpClient client;
    private static final String BASE_URL = "https://smishguard-api-gateway.onrender.com/historial-analisis-usuarios/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnalyzedMessagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ocultarBarrasDeSistema();

        client = new OkHttpClient();

        analyzedMessagesAdapter = new AnalyzedMessagesAdapter(reportedMessagesList,
                this::viewAnalysis);

        binding.recyclerViewReportedMessages.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewReportedMessages.setAdapter(analyzedMessagesAdapter);

        cargarMensajesAnaliazados();

        binding.btnBackReportedMessages.setOnClickListener(view -> {
            startActivity(new Intent(AnalyzedMessagesActivity.this, MainActivity.class));
        });
    }

    private void cargarMensajesAnaliazados() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            if (userEmail != null) {
                try {
                    String encodedEmail = URLEncoder.encode(userEmail, "UTF-8");
                    String urlWithParams = BASE_URL + encodedEmail;
                    Log.d("AnalyzedMessagesActivity", "URL de solicitud: " + urlWithParams);

                    Request request = new Request.Builder().url(urlWithParams).build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.e("AnalyzedMessagesActivity", "Error en la solicitud: " + e.getMessage());
                            runOnUiThread(() -> Toast.makeText(AnalyzedMessagesActivity.this, "Error al cargar los mensajes reportados", Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if (response.isSuccessful()) {
                                String responseData = response.body().string();
                                Log.d("AnalyzedMessagesActivity", "Datos recibidos: " + responseData);

                                try {
                                    JSONObject jsonResponse = new JSONObject(responseData);
                                    JSONArray jsonArray = jsonResponse.getJSONArray("historial");
                                    reportedMessagesList.clear();

                                    // Formatos de fecha y hora
                                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                                        // Usamos `has` y `isNull` para verificar si el campo existe y no es nulo
                                        String address = jsonObject.has("numero_celular") && !jsonObject.isNull("numero_celular")
                                                ? jsonObject.getString("numero_celular")
                                                : "Mensaje Manual";

                                        String body = jsonObject.optString("mensaje", "Mensaje no disponible");
                                        String enlace = jsonObject.optString("url", "Enlace no disponible");
                                        JSONObject analisisObj = jsonObject.optJSONObject("analisis");
                                        String analisisSmishguard = analisisObj.optString("nivel_peligro", "Desconocido");
                                        String analisisGpt = analisisObj.optString("justificacion_gpt", "Sin justificación");
                                        int puntaje = analisisObj.optInt("ponderado", 0);

                                        // Procesar fecha dentro de `analisis`
                                        String fechaAnalisisRaw = analisisObj.optString("fecha_analisis", "");
                                        String fechaAnalisisFormatted = "";
                                        long dateMillis = System.currentTimeMillis();  // Default to current time

                                        if (!fechaAnalisisRaw.isEmpty()) {
                                            try {
                                                Date date = inputFormat.parse(fechaAnalisisRaw);
                                                dateMillis = date.getTime();
                                                fechaAnalisisFormatted = outputFormat.format(date); // Formateo correcto de fecha y hora
                                            } catch (ParseException e) {
                                                Log.e("AnalyzedMessagesActivity", "Error al parsear la fecha: " + e.getMessage());
                                            }
                                        }

                                        // Crear y agregar el objeto SmsModel
                                        SmsModel smsModel = new SmsModel(address, body, dateMillis, analisisSmishguard, analisisGpt, enlace, puntaje, fechaAnalisisFormatted);
                                        reportedMessagesList.add(smsModel);
                                    }

                                    // Ordenar mensajes de más recientes a más antiguos usando el tiempo en milisegundos
                                    Collections.sort(reportedMessagesList, (o1, o2) -> Long.compare(o2.getDate(), o1.getDate()));

                                    runOnUiThread(() -> analyzedMessagesAdapter.notifyDataSetChanged());

                                } catch (JSONException e) {
                                    Log.e("AnalyzedMessagesActivity", "Error al procesar el JSON: " + e.getMessage());
                                    runOnUiThread(() -> Toast.makeText(AnalyzedMessagesActivity.this, "Error al procesar los datos del servidor", Toast.LENGTH_SHORT).show());
                                }
                            } else {
                                Log.e("AnalyzedMessagesActivity", "Error en la respuesta del servidor: " + response.message());
                                runOnUiThread(() -> Toast.makeText(AnalyzedMessagesActivity.this, "Error en el servidor: " + response.message(), Toast.LENGTH_SHORT).show());
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e("AnalyzedMessagesActivity", "Error al codificar el correo: " + e.getMessage());
                }
            } else {
                Toast.makeText(this, "Correo del usuario no disponible", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No se pudo obtener la información del usuario", Toast.LENGTH_SHORT).show();
        }
    }

    private void viewAnalysis(SmsModel sms) {
        Intent intent = new Intent(AnalyzedMessagesActivity.this, AnalysisActivity.class);
        intent.putExtra("address", sms.getAddress());
        intent.putExtra("body", sms.getBody());
        intent.putExtra("date", sms.getDate());
        intent.putExtra("analisisSmishguard", sms.getAnalisisSmishguard());
        intent.putExtra("analisisGpt", sms.getAnalisisGpt());
        intent.putExtra("enlace", sms.getEnlace());
        intent.putExtra("puntaje", sms.getPuntaje());
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(AnalyzedMessagesActivity.this, MainActivity.class);
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