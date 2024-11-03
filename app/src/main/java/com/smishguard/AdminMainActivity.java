package com.smishguard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.smishguard.databinding.ActivityAdminMainBinding;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class AdminMainActivity extends AppCompatActivity {

    private ActivityAdminMainBinding binding;
    private LottieAnimationView lottieButton;
    private ImageView imageOff;
    private TextView connectionStatusText;
    private boolean doubleBackToExitPressedOnce = false;
    private boolean isOn = false; // Estado inicial
    private static final String PING_URL = "https://smishguard-api-gateway.onrender.com/ping";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ocultarBarrasDeSistema();

        lottieButton = binding.lottieButton;
        imageOff = binding.imageOff;
        connectionStatusText = binding.textViewConnectionStatus; // Conectar el TextView

        // Enviar el ping cuando se abre la pantalla
        enviarPingAlBackend();

        try{
            // Al hacer clic en el botón, vuelve a intentar enviar el ping
            lottieButton.setOnClickListener(v -> enviarPingAlBackend());
            imageOff.setOnClickListener(v -> enviarPingAlBackend());
        }catch(Exception e){
            Toast.makeText(this, "Ha ocurrido un error, intentalo de nuevo más tarde", Toast.LENGTH_SHORT).show();
        }

        binding.imgPostAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminMainActivity.this, AdminAlertMessagesActivity.class));
            }
        });

        binding.imgViewUpdateEducation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminMainActivity.this, AdminUpdateEducationActivity.class));
            }
        });

        binding.imgViewSupportComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminMainActivity.this, AdminSupportCommentsActivity.class));
            }
        });

        binding.imgViewStaticsSmishguard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminMainActivity.this, AdminStaticsSmishguardActivity.class));
            }
        });

        binding.btnAdminProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminMainActivity.this, AdminProfileActivity.class));
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        ocultarBarrasDeSistema();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            // Si el botón se presionó dos veces en el intervalo, salir de la aplicación
            super.onBackPressed();
            finishAffinity();
            return;
        }

        // Mostrar el mensaje para presionar nuevamente
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Presiona nuevamente para salir de la aplicación", Toast.LENGTH_SHORT).show();

        // Reiniciar el estado después de 2 segundos
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    private void ocultarBarrasDeSistema() {
        // Método para ocultar las barras del sistema
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
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
                // Si hay un error en la solicitud, mantener el botón apagado y mostrar "Desconectado"
                runOnUiThread(() -> {
                    Toast.makeText(AdminMainActivity.this, "Error al contactar con el servidor", Toast.LENGTH_SHORT).show();
                    actualizarEstadoBoton(false);  // Apagar el botón
                    actualizarEstadoConexion("OFLINE");  // Mostrar el estado de desconexión
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Si el ping es exitoso, encender el botón y mostrar "Conectado"
                    runOnUiThread(() -> {
                        actualizarEstadoBoton(true);  // Encender el botón
                        actualizarEstadoConexion("ONLINE");  // Mostrar el estado de conexión
                    });
                } else {
                    // Si la respuesta no es exitosa, mantener el botón apagado y mostrar "Desconectado"
                    runOnUiThread(() -> {
                        Toast.makeText(AdminMainActivity.this, "Fallo en la conexión con el servidor", Toast.LENGTH_SHORT).show();
                        actualizarEstadoBoton(false);  // Apagar el botón
                        actualizarEstadoConexion("OFLINE");  // Mostrar el estado de desconexión
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

    // Método para actualizar el texto que muestra el estado de la conexión
    private void actualizarEstadoConexion(String estado) {
        connectionStatusText.setText(estado);
    }
}