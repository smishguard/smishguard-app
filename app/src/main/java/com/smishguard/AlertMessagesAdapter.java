package com.smishguard;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AlertMessagesAdapter extends RecyclerView.Adapter<AlertMessagesAdapter.ViewHolder> {

    private List<AlertMessageModel> alertMessages;
    private Context context;
    private static final String TAG = "AlertMessageAdapter";
    private static final String URL_POST_TWEET = "https://smishguard-api-gateway.onrender.com/publicar-tweet";
    private static final String URL_UPDATE_TWEET = "https://smishguard-api-gateway.onrender.com/actualizar-publicado/";

    public AlertMessagesAdapter(List<AlertMessageModel> alertMessages, Context context) {
        this.alertMessages = alertMessages;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AlertMessageModel message = alertMessages.get(position);
        holder.textViewBody.setText(message.getContenido());
        holder.textViewAddress.setText(message.getUrl());

        // Botón para publicar el tweet
        holder.btnPostTweet.setOnClickListener(v -> publishTweet(message));
    }

    @Override
    public int getItemCount() {
        return alertMessages.size();
    }

    private void publishTweet(AlertMessageModel message) {
        OkHttpClient client = new OkHttpClient();
        String urlPost = URL_POST_TWEET;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("mensaje", message.getContenido());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al preparar el mensaje para publicar", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(urlPost)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error al publicar el tweet: " + e.getMessage(), e);
                ((AdminAlertMessagesActivity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Error al publicar el tweet", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String mensajeRespuesta = jsonResponse.getString("mensaje");

                        ((AdminAlertMessagesActivity) context).runOnUiThread(() ->
                                Toast.makeText(context, mensajeRespuesta, Toast.LENGTH_LONG).show()
                        );

                        // Llamar al método para actualizar el estado del mensaje después de una publicación exitosa
                        updateMessageStatus(message.getId());

                    } catch (JSONException e) {
                        Log.e(TAG, "Error al parsear la respuesta JSON", e);
                        ((AdminAlertMessagesActivity) context).runOnUiThread(() ->
                                Toast.makeText(context, "Error al publicar el tweet", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    Log.e(TAG, "Error en la respuesta: " + response.message());
                    ((AdminAlertMessagesActivity) context).runOnUiThread(() ->
                            Toast.makeText(context, "Error al publicar el tweet", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void updateMessageStatus(String messageId) {
        OkHttpClient client = new OkHttpClient();
        String urlPut = URL_UPDATE_TWEET + messageId;

        Request request = new Request.Builder()
                .url(urlPut)
                .put(RequestBody.create("", null))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error al actualizar el estado de publicación: " + e.getMessage(), e);
                ((AdminAlertMessagesActivity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Error al actualizar el estado del mensaje", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String mensajeRespuesta = jsonResponse.getString("mensaje");

                        ((AdminAlertMessagesActivity) context).runOnUiThread(() ->
                                Toast.makeText(context, mensajeRespuesta, Toast.LENGTH_LONG).show()
                        );

                        // Redirigir al AdminMainActivity
                        Intent intent = new Intent(context, AdminMainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error al parsear la respuesta JSON", e);
                        ((AdminAlertMessagesActivity) context).runOnUiThread(() ->
                                Toast.makeText(context, "Error al actualizar el estado del mensaje", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    Log.e(TAG, "Error en la respuesta: " + response.message());
                    ((AdminAlertMessagesActivity) context).runOnUiThread(() ->
                            Toast.makeText(context, "Error al actualizar el estado del mensaje", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewBody, textViewAddress;
        Button btnPostTweet;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewBody = itemView.findViewById(R.id.textViewBody);
            textViewAddress = itemView.findViewById(R.id.textViewAddress);
            btnPostTweet = itemView.findViewById(R.id.btnPostTweet);
        }
    }
}