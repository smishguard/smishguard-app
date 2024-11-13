package com.smishguard.activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;
import com.smishguard.adapters.SmsAdapter;
import com.smishguard.databinding.ActivityInboxBinding;
import com.smishguard.models.SmsModel;
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

public class InboxActivity extends AppCompatActivity {

    private ActivityInboxBinding binding;
    private static final int REQUEST_CODE_READ_SMS = 101;
    private List<SmsModel> smsList = new ArrayList<>();
    private SmsAdapter smsAdapter;
    private List<String> blockedNumbers = new ArrayList<>();
    private OkHttpClient client = new OkHttpClient();
    private static final String BASE_BLOCKED_NUMBERS_URL = "https://smishguard-api-gateway.onrender.com/numeros-bloqueados/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInboxBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ocultarBarrasDeSistema();

        // Inicializar el adaptador y el RecyclerView
        smsAdapter = new SmsAdapter(smsList, blockedNumbers);
        binding.recyclerViewSms.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewSms.setAdapter(smsAdapter);

        // Configura el clic en los elementos del RecyclerView
        smsAdapter.setOnItemClickListener(sms -> {
            // Crear un Intent para redirigir a MessageActivity y pasar los datos
            Intent intent = new Intent(InboxActivity.this, MessageActivity.class);
            intent.putExtra("address", sms.getAddress());
            intent.putExtra("body", sms.getBody());
            intent.putExtra("date", sms.getDate());
            startActivity(intent);
        });

        // Cargar los números bloqueados antes de cargar los SMS
        obtenerNumerosBloqueados();

        // Verificar y solicitar el permiso READ_SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS}, REQUEST_CODE_READ_SMS);
        } else {
            cargarSms();
        }

        binding.btnBackInbox.setOnClickListener(view -> {
            startActivity(new Intent(InboxActivity.this, MainActivity.class));
        });
    }

    // Método para cargar los números bloqueados desde el backend
    private void obtenerNumerosBloqueados() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = user.getEmail();
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Error al obtener el correo del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        String urlWithEmail = BASE_BLOCKED_NUMBERS_URL + email;

        Request request = new Request.Builder()
                .url(urlWithEmail)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(InboxActivity.this, "Error al obtener números bloqueados", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONArray jsonArray = jsonObject.getJSONArray("numeros");
                        blockedNumbers.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject numberObject = jsonArray.getJSONObject(i);
                            String number = numberObject.getString("numero");
                            blockedNumbers.add(number);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(() -> {
                        smsAdapter.notifyDataSetChanged();
                        cargarSms(); // Llama a cargarSms después de obtener los números bloqueados
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(InboxActivity.this, "No se pudieron obtener los números bloqueados", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    // Método para cargar los SMS del dispositivo
    private void cargarSms() {
        smsList.clear();

        ContentResolver contentResolver = getContentResolver();
        Uri uri = Telephony.Sms.Inbox.CONTENT_URI;
        String[] projection = new String[]{
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE
        };

        try (Cursor cursor = contentResolver.query(uri, projection, null, null, Telephony.Sms.DATE + " DESC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String address = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                    String body = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY));
                    long date = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE));

                    smsList.add(new SmsModel(address, body, date));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar los SMS", Toast.LENGTH_SHORT).show();
        }

        smsAdapter.notifyDataSetChanged();
    }

    // Manejar la respuesta de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cargarSms();
            } else {
                Toast.makeText(this, "Permiso para leer SMS denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ocultarBarrasDeSistema();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(InboxActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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