package com.smishguard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.smishguard.databinding.ActivityAdminSupportCommentsBinding;
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

public class AdminSupportCommentsActivity extends AppCompatActivity {

    private ActivityAdminSupportCommentsBinding binding;
    private List<SupportCommentModel> supportComments;
    private SupportCommentsAdapter adapter;
    private static final String TAG = "AdminInboxSupport";
    private static final String URL_SUPPORT_COMMENTS = "https://smishguard-api-gateway.onrender.com/comentario-soporte";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminSupportCommentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ocultarBarrasDeSistema();

        binding.btnBackSupport.setOnClickListener(view -> {
            startActivity(new Intent(AdminSupportCommentsActivity.this, AdminMainActivity.class));
        });

        // Configurar el RecyclerView
        supportComments = new ArrayList<>();
        adapter = new SupportCommentsAdapter(supportComments, this); // Pasar el contexto como segundo argumento
        binding.recyclerViewSupportComments.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewSupportComments.setAdapter(adapter);

        // Cargar comentarios de soporte
        loadSupportComments();
    }

    private void loadSupportComments() {
        OkHttpClient client = new OkHttpClient();
        String url = URL_SUPPORT_COMMENTS;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error al obtener comentarios: " + e.getMessage(), e);
                runOnUiThread(() ->
                        Toast.makeText(AdminSupportCommentsActivity.this, "Error al cargar comentarios", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray commentsArray = jsonResponse.getJSONArray("comentarios");

                        for (int i = 0; i < commentsArray.length(); i++) {
                            JSONObject commentJson = commentsArray.getJSONObject(i);
                            String id = commentJson.getString("_id");  // Extraemos el ID
                            String comentario = commentJson.getString("comentario");
                            String correo = commentJson.getString("correo");
                            String fecha = commentJson.getString("fecha");

                            SupportCommentModel comment = new SupportCommentModel(id, comentario, correo, fecha);
                            supportComments.add(comment);
                        }

                        runOnUiThread(() -> adapter.notifyDataSetChanged());

                    } catch (JSONException e) {
                        Log.e(TAG, "Error al parsear respuesta JSON", e);
                        runOnUiThread(() ->
                                Toast.makeText(AdminSupportCommentsActivity.this, "Error al cargar comentarios", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    Log.e(TAG, "Error en la respuesta: " + response.message());
                    runOnUiThread(() ->
                            Toast.makeText(AdminSupportCommentsActivity.this, "Error al cargar comentarios", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(AdminSupportCommentsActivity.this, AdminMainActivity.class);
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