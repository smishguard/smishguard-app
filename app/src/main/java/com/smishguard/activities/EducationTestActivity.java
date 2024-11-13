package com.smishguard.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.smishguard.databinding.ActivityEducationTestBinding;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EducationTestActivity extends AppCompatActivity {

    private ActivityEducationTestBinding binding;
    private OkHttpClient client;
    private static final String MESSAGE_URL = "https://smishguard-api-gateway.onrender.com/mensaje-aleatorio";
    private static final String TAG = "EducationTestActivity";

    private String correctAnswer;
    private String analysis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEducationTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        client = new OkHttpClient();

        ocultarBarrasDeSistema();
        loadRandomMessage();

        binding.btnReviewResponse.setOnClickListener(view -> reviewResponse());
        binding.btnTryAgain.setOnClickListener(view -> tryAgain());

        binding.btnBackEducationTest.setOnClickListener(view -> {
            startActivity(new Intent(EducationTestActivity.this, EducationActivity.class));
        });
    }

    private void loadRandomMessage() {
        Request request = new Request.Builder()
                .url(MESSAGE_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error loading message: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(EducationTestActivity.this, "Error al cargar el mensaje", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseData).getJSONObject("mensaje");
                        final String messageContent = jsonObject.getString("contenido");
                        final int ponderado = jsonObject.getJSONObject("analisis").getInt("ponderado");
                        analysis = jsonObject.getJSONObject("analisis").getString("justificacion_gpt");

                        correctAnswer = ponderado > 5 ? "Es Smishing" : "No es Smishing";

                        runOnUiThread(() -> {
                            binding.textViewMessage.setText(messageContent);
                            binding.textViewAnalysis.setVisibility(View.GONE);
                            binding.textViewResult.setVisibility(View.GONE);
                            binding.btnTryAgain.setVisibility(View.GONE);
                        });

                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "Error in response: " + response.code());
                }
            }
        });
    }

    private void reviewResponse() {
        int selectedId = binding.radioGroupOpciones.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Por favor selecciona una respuesta", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadioButton = findViewById(selectedId);
        String selectedAnswer = selectedRadioButton.getText().toString();

        boolean isCorrect = selectedAnswer.equals(correctAnswer);
        binding.textViewResult.setText(isCorrect ? "Correcto" : "Incorrecto");
        binding.textViewResult.setTextColor(isCorrect ? getColor(android.R.color.holo_green_dark) : getColor(android.R.color.holo_red_dark));
        binding.textViewResult.setVisibility(View.VISIBLE);

        binding.textViewAnalysis.setText(analysis);
        binding.textViewAnalysis.setVisibility(View.VISIBLE);

        binding.btnTryAgain.setVisibility(View.VISIBLE);
    }

    private void tryAgain() {
        binding.textViewResult.setVisibility(View.GONE);
        binding.textViewAnalysis.setVisibility(View.GONE);

        binding.btnTryAgain.setVisibility(View.GONE);

        binding.radioGroupOpciones.clearCheck();

        loadRandomMessage();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(EducationTestActivity.this, EducationActivity.class);
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