package com.smishguard.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.smishguard.databinding.ActivityAdminAnalyticsSmishguardBinding;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.components.LegendEntry;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AdminAnalyticsSmishguardActivity extends AppCompatActivity {

    private ActivityAdminAnalyticsSmishguardBinding binding;
    private static final String URL_STATISTICS = "https://smishguard-api-gateway.onrender.com/estadisticas";
    private static final String TAG = "AdminAnalyticsSmishguard";

    private PieChart pieChartMessages;
    private BarChart barChartPublications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminAnalyticsSmishguardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ocultarBarrasDeSistema();

        pieChartMessages = binding.pieChartMessages;
        barChartPublications = binding.barChartPublications;

        binding.btnBackAnalytics.setOnClickListener(view -> {
            startActivity(new Intent(AdminAnalyticsSmishguardActivity.this, AdminMainActivity.class));
        });

        // Llamada para cargar las estadísticas
        loadStatistics();
    }

    private void loadStatistics() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(URL_STATISTICS)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error en la solicitud de estadísticas: " + e.getMessage(), e);
                runOnUiThread(() ->
                        Toast.makeText(AdminAnalyticsSmishguardActivity.this, "Error al obtener estadísticas", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseData);
                        JSONObject mensajes = jsonResponse.getJSONObject("mensajes");
                        JSONObject mensajesParaPublicar = jsonResponse.getJSONObject("mensajes_para_publicar");

                        int peligrosos = mensajes.getInt("peligrosos");
                        int seguros = mensajes.getInt("seguros");
                        int sospechosos = mensajes.getInt("sospechosos");
                        int totalAnalizados = mensajes.getInt("total_analizados");

                        int publicados = mensajesParaPublicar.getInt("publicados");
                        int noPublicados = mensajesParaPublicar.getInt("no_publicados");

                        // Actualizar los gráficos con los datos
                        runOnUiThread(() -> {
                            setupPieChart(peligrosos, seguros, sospechosos);
                            setupBarChart(publicados, noPublicados);
                        });

                    } catch (JSONException e) {
                        Log.e(TAG, "Error al interpretar el JSON de estadísticas", e);
                        runOnUiThread(() ->
                                Toast.makeText(AdminAnalyticsSmishguardActivity.this, "Error al procesar estadísticas", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    Log.e(TAG, "Error en la respuesta del servidor: " + response.message());
                    runOnUiThread(() ->
                            Toast.makeText(AdminAnalyticsSmishguardActivity.this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void setupPieChart(int peligrosos, int seguros, int sospechosos) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(peligrosos, "Peligrosos"));
        entries.add(new PieEntry(sospechosos, "Sospechosos"));
        entries.add(new PieEntry(seguros, "Seguros"));

        pieChartMessages.setBackgroundColor(Color.WHITE); // Fondo blanco

        PieDataSet dataSet = new PieDataSet(entries, "");

        // Establecer colores
        int colorPeligrosos = Color.parseColor("#ff2a00"); // Rojo suave
        int colorSospechosos = Color.parseColor("#f1d238"); // Amarillo suave
        int colorSeguros = Color.parseColor("#69c836"); // Verde suave
        dataSet.setColors(colorPeligrosos, colorSospechosos, colorSeguros);

        // Configurar el texto de los valores en negro
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(14f);

        // Crear PieData y configurar el formateador para solo mostrar los valores numéricos
        PieData data = new PieData(dataSet);
        data.setValueTextColor(Color.BLACK);
        data.setValueTextSize(14f);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f", value); // Solo muestra el valor sin etiquetas
            }
        });

        pieChartMessages.setData(data);

        // Configurar la leyenda
        Legend legend = pieChartMessages.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextColor(Color.BLACK);
        legend.setTextSize(12f);
        legend.setFormSize(10f);
        legend.setXEntrySpace(25f); // Espacio horizontal entre elementos de la leyenda
        legend.setYEntrySpace(5f); // Espacio vertical entre elementos de la leyenda

        // Desactivar la descripción de la gráfica
        pieChartMessages.getDescription().setEnabled(false);

        pieChartMessages.invalidate(); // Refrescar la gráfica
    }

    private void setupBarChart(int publicados, int noPublicados) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, publicados, "Publicados"));
        entries.add(new BarEntry(1, noPublicados, "No Publicados"));

        BarDataSet dataSet = new BarDataSet(entries, "Mensajes para Publicar");
        int colorPublicados = Color.parseColor("#69c836"); // Verde para "Publicados"
        int colorNoPublicados = Color.parseColor("#ff2a00"); // Rojo para "No Publicados"
        dataSet.setColors(colorPublicados, colorNoPublicados);

        barChartPublications.setBackgroundColor(Color.WHITE);

        BarData data = new BarData(dataSet);
        data.setValueTextColor(Color.BLACK); // Texto en negro
        data.setValueTextSize(14f);
        data.setBarWidth(0.4f);

        // Configurar el ValueFormatter para mostrar solo números enteros
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f", value); // Formato sin decimales
            }
        });

        barChartPublications.setData(data);
        barChartPublications.setFitBars(true);

        XAxis xAxis = barChartPublications.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Publicados", "No Publicados"}));
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(12f);

        YAxis leftAxis = barChartPublications.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setTextSize(12f);

        YAxis rightAxis = barChartPublications.getAxisRight();
        rightAxis.setEnabled(false);

        Legend legend = barChartPublications.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextColor(Color.BLACK);
        legend.setTextSize(12f);
        legend.setFormSize(10f);
        legend.setXEntrySpace(80f); // Aumentar el espacio entre las entradas de la leyenda en el eje X
        legend.setYEntrySpace(50f);

        LegendEntry[] legendEntries = new LegendEntry[2];
        legendEntries[0] = new LegendEntry("Publicados", Legend.LegendForm.SQUARE, 10f, 2f, null, colorPublicados);
        legendEntries[1] = new LegendEntry("No Publicados", Legend.LegendForm.SQUARE, 10f, 2f, null, colorNoPublicados);
        legend.setCustom(legendEntries);

        barChartPublications.getDescription().setEnabled(false);
        barChartPublications.invalidate();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(AdminAnalyticsSmishguardActivity.this, AdminMainActivity.class);
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