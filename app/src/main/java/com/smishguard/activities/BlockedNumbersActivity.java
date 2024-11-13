package com.smishguard.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.smishguard.adapters.BlockedNumbersAdapter;
import com.smishguard.databinding.ActivityBlockedNumbersBinding;
import com.smishguard.models.BlockedNumberModel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
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

public class BlockedNumbersActivity extends AppCompatActivity {

    private ActivityBlockedNumbersBinding binding;
    private List<BlockedNumberModel> blockedNumbersList = new ArrayList<>();
    private BlockedNumbersAdapter blockedNumbersAdapter;
    private OkHttpClient client;
    private static final String BASE_URL = "https://smishguard-api-gateway.onrender.com/numeros-bloqueados/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBlockedNumbersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ocultarBarrasDeSistema();

        client = new OkHttpClient();

        blockedNumbersAdapter = new BlockedNumbersAdapter(blockedNumbersList, this::desbloquearNumero);
        binding.recyclerViewBlockedNumbers.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewBlockedNumbers.setAdapter(blockedNumbersAdapter);

        binding.btnBackReportedNumbers.setOnClickListener(view -> {
            startActivity(new Intent(BlockedNumbersActivity.this, MainActivity.class));
        });

        cargarNumerosBloqueados();
    }

    private void cargarNumerosBloqueados() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            String urlWithEmail = BASE_URL + userEmail;
            Request request = new Request.Builder()
                    .url(urlWithEmail)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(BlockedNumbersActivity.this, "Error al cargar los números", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseData = response.body().string();
                        try {
                            JSONObject jsonObject = new JSONObject(responseData);
                            JSONArray jsonArray = jsonObject.getJSONArray("numeros");
                            blockedNumbersList.clear();

                            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject numberObject = jsonArray.getJSONObject(i);
                                String number = numberObject.getString("numero");
                                String fechaBloqueoRaw = numberObject.getString("fecha_bloqueo");

                                // Convertir y formatear la fecha
                                Date date = inputFormat.parse(fechaBloqueoRaw);
                                String fechaBloqueoFormatted = outputFormat.format(date);

                                blockedNumbersList.add(new BlockedNumberModel(number, fechaBloqueoFormatted));
                            }

                            // Ordenar por fecha de los más recientes a los más antiguos
                            Collections.sort(blockedNumbersList, (o1, o2) -> {
                                try {
                                    Date date1 = outputFormat.parse(o1.getFechaBloqueo());
                                    Date date2 = outputFormat.parse(o2.getFechaBloqueo());
                                    return date2.compareTo(date1); // Orden inverso
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    return 0;
                                }
                            });

                            runOnUiThread(() -> blockedNumbersAdapter.notifyDataSetChanged());
                        } catch (JSONException | ParseException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> Toast.makeText(BlockedNumbersActivity.this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(BlockedNumbersActivity.this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } else {
            Toast.makeText(this, "No se pudo obtener la información del usuario", Toast.LENGTH_SHORT).show();
        }
    }

    private void desbloquearNumero(BlockedNumberModel number) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            new AlertDialog.Builder(this)
                    .setTitle("Confirmar Desbloqueo")
                    .setMessage("¿Estás seguro de que deseas desbloquear el número " + number.getNumber() + "?")
                    .setPositiveButton("Sí", (dialog, which) -> realizarDesbloqueo(number, userEmail))
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            Toast.makeText(this, "No se pudo obtener la información del usuario", Toast.LENGTH_SHORT).show();
        }
    }

    private void realizarDesbloqueo(BlockedNumberModel number, String userEmail) {
        String urlWithParams = BASE_URL + "/" + userEmail + "/" + number.getNumber();

        Request request = new Request.Builder()
                .url(urlWithParams)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(BlockedNumbersActivity.this, "Error al desbloquear el número", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(BlockedNumbersActivity.this, "Número desbloqueado exitosamente", Toast.LENGTH_SHORT).show();
                        blockedNumbersList.remove(number);
                        blockedNumbersAdapter.notifyDataSetChanged();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(BlockedNumbersActivity.this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(BlockedNumbersActivity.this, MainActivity.class);
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