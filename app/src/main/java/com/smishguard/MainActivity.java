package com.smishguard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.smishguard.databinding.ActivityMainBinding;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private LottieAnimationView lottieButton;
    private ImageView imageOff;
    private boolean isOn = false; // Estado inicial
    private static final String PING_URL = "https://smishguard-api-gateway.onrender.com/ping";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        lottieButton = binding.lottieButton;
        imageOff = binding.imageOff;

        // Enviar el ping cuando se abre la pantalla
        enviarPingAlBackend();

        // Al hacer clic en el botón, vuelve a intentar enviar el ping
        lottieButton.setOnClickListener(v -> enviarPingAlBackend());
        imageOff.setOnClickListener(v -> enviarPingAlBackend());

        binding.imgViewManualAnalisis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ManualAnalysisActivity.class));
            }
        });

        binding.btnConfiguration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ConfigurationActivity.class));
            }
        });
    }

    // Método para enviar el ping al backend
    private void enviarPingAlBackend() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(PING_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Si hay un error en la solicitud, mantener el botón apagado
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Error al contactar con el backend", Toast.LENGTH_SHORT).show();
                    actualizarEstadoBoton(false);  // Apagar el botón
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Si el ping es exitoso, encender el botón
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Conexión exitosa con el servidor", Toast.LENGTH_SHORT).show();
                        actualizarEstadoBoton(true);  // Encender el botón
                    });
                } else {
                    // Si la respuesta no es exitosa, mantener el botón apagado
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Fallo en la conexión con el servidor, intentalo de nuevo más tarde", Toast.LENGTH_SHORT).show();
                        actualizarEstadoBoton(false);  // Apagar el botón
                    });
                }
            }
        });
    }

    // Método para actualizar el estado del botón según el resultado del ping
    private void actualizarEstadoBoton(boolean isBackendOn) {
        if (isBackendOn) {
            lottieButton.setVisibility(View.VISIBLE); // Muestra la animación Lottie (botón encendido)
            imageOff.setVisibility(View.GONE); // Oculta la imagen de apagado
            isOn = true;
        } else {
            lottieButton.setVisibility(View.GONE); // Oculta la animación Lottie (botón apagado)
            imageOff.setVisibility(View.VISIBLE); // Muestra la imagen de apagado
            isOn = false;
        }
    }
}