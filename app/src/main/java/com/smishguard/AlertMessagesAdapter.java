package com.smishguard;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
    private static final String URL_DELETE_ALERT = "https://smishguard-api-gateway.onrender.com/eliminar-mensaje-para-publicar/";

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

        // Confirmación para publicar el tweet
        holder.btnPostTweet.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Confirmar Publicación")
                    .setMessage("¿Estás seguro de que quieres publicar este tweet?")
                    .setPositiveButton("Publicar", (dialog, which) -> publishTweet(message, position))
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        // Confirmación para eliminar el mensaje de alerta
        holder.btnDeleteAlert.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Confirmar Eliminación")
                    .setMessage("¿Estás seguro de que quieres eliminar este mensaje de alerta?")
                    .setPositiveButton("Eliminar", (dialog, which) -> deleteAlertMessage(message, position))
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return alertMessages.size();
    }

    private void publishTweet(AlertMessageModel message, int position) {
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

                        ((AdminAlertMessagesActivity) context).runOnUiThread(() -> {
                            Toast.makeText(context, mensajeRespuesta, Toast.LENGTH_LONG).show();
                            // Llamar al método para actualizar el estado del mensaje y eliminarlo de la lista
                            updateMessageStatusAndRemove(message.getId(), position);
                        });

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

    private void updateMessageStatusAndRemove(String messageId, int position) {
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
                    ((AdminAlertMessagesActivity) context).runOnUiThread(() -> {
                        // Eliminar el mensaje publicado de la lista
                        alertMessages.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, alertMessages.size());
                    });
                } else {
                    Log.e(TAG, "Error en la respuesta: " + response.message());
                    ((AdminAlertMessagesActivity) context).runOnUiThread(() ->
                            Toast.makeText(context, "Error al actualizar el estado del mensaje", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void deleteAlertMessage(AlertMessageModel message, int position) {
        OkHttpClient client = new OkHttpClient();
        String urlDelete = URL_DELETE_ALERT + message.getId();

        Request request = new Request.Builder()
                .url(urlDelete)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error al eliminar el mensaje de alerta: " + e.getMessage(), e);
                ((AdminAlertMessagesActivity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Error al eliminar el mensaje de alerta", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String mensajeRespuesta = jsonResponse.getString("mensaje");

                        ((AdminAlertMessagesActivity) context).runOnUiThread(() -> {
                            Toast.makeText(context, mensajeRespuesta, Toast.LENGTH_LONG).show();
                            // Remover el mensaje eliminado de la lista y notificar al adaptador
                            alertMessages.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, alertMessages.size());
                        });

                    } catch (JSONException e) {
                        Log.e(TAG, "Error al parsear la respuesta JSON", e);
                        ((AdminAlertMessagesActivity) context).runOnUiThread(() ->
                                Toast.makeText(context, "Error al eliminar el mensaje de alerta", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    Log.e(TAG, "Error en la respuesta: " + response.message());
                    ((AdminAlertMessagesActivity) context).runOnUiThread(() ->
                            Toast.makeText(context, "Error al eliminar el mensaje de alerta", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewBody, textViewAddress;
        Button btnPostTweet, btnDeleteAlert;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewBody = itemView.findViewById(R.id.textViewBody);
            textViewAddress = itemView.findViewById(R.id.textViewAddress);
            btnPostTweet = itemView.findViewById(R.id.btnPostTweet);
            btnDeleteAlert = itemView.findViewById(R.id.btnDeleteAlert);
        }
    }
}