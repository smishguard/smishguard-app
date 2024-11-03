package com.smishguard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.smishguard.databinding.ActivityAdminAlertMessagesBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AdminAlertMessagesActivity extends AppCompatActivity {

    private ActivityAdminAlertMessagesBinding binding;
    private List<AlertMessageModel> alertMessages;
    private AlertMessagesAdapter adapter;
    private static final String TAG = "AdminInboxAlerts";
    private static final String URL_ALERT_MESSAGES = "https://smishguard-api-gateway.onrender.com/mensajes-para-publicar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAlertMessagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ocultarBarrasDeSistema();

        binding.btnBackInboxAlerts.setOnClickListener(view -> {
            startActivity(new Intent(AdminAlertMessagesActivity.this, AdminMainActivity.class));
        });

        // Configurar el RecyclerView
        alertMessages = new ArrayList<>();
        adapter = new AlertMessagesAdapter(alertMessages, this);
        binding.recyclerViewSupportComments.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewSupportComments.setAdapter(adapter);

        // Cargar mensajes de alerta
        loadAlertMessages();
    }

    private void loadAlertMessages() {
        OkHttpClient client = new OkHttpClient();
        String url = URL_ALERT_MESSAGES;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error al obtener alertas: " + e.getMessage(), e);
                runOnUiThread(() ->
                        Toast.makeText(AdminAlertMessagesActivity.this, "Error al cargar alertas", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray alertArray = jsonResponse.getJSONArray("documentos");

                        for (int i = 0; i < alertArray.length(); i++) {
                            JSONObject alertJson = alertArray.getJSONObject(i);
                            String id = alertJson.getString("_id");
                            String contenido = alertJson.getString("contenido");
                            String url = alertJson.getString("url");

                            AlertMessageModel alertMessage = new AlertMessageModel(id, contenido, url);
                            alertMessages.add(alertMessage);
                        }

                        runOnUiThread(() -> adapter.notifyDataSetChanged());

                    } catch (JSONException e) {
                        Log.e(TAG, "Error al parsear respuesta JSON", e);
                        runOnUiThread(() ->
                                Toast.makeText(AdminAlertMessagesActivity.this, "Error al cargar alertas", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    Log.e(TAG, "Error en la respuesta: " + response.message());
                    runOnUiThread(() ->
                            Toast.makeText(AdminAlertMessagesActivity.this, "Error al cargar alertas", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(AdminAlertMessagesActivity.this, AdminMainActivity.class);
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