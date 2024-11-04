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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SupportCommentsAdapter extends RecyclerView.Adapter<SupportCommentsAdapter.ViewHolder> {

    private List<SupportCommentModel> supportComments;
    private Context context;
    private static final String TAG = "SupportCommentsAdapter";
    private static final String URL_DELETE_COMMENT = "https://smishguard-api-gateway.onrender.com/eliminar-comentario-soporte/";

    public SupportCommentsAdapter(List<SupportCommentModel> supportComments, Context context) {
        this.supportComments = supportComments;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_support_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SupportCommentModel comment = supportComments.get(position);
        holder.textViewComment.setText(comment.getComentario());
        holder.textViewMail.setText(comment.getCorreo());
        holder.textViewDate.setText(comment.getFecha());

        // Configurar botón de eliminar con confirmación
        holder.btnDeleteComment.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Confirmar Eliminación")
                    .setMessage("¿Estás seguro de que quieres eliminar este comentario de soporte?")
                    .setPositiveButton("Eliminar", (dialog, which) -> deleteSupportComment(comment, position))
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return supportComments.size();
    }

    private void deleteSupportComment(SupportCommentModel comment, int position) {
        OkHttpClient client = new OkHttpClient();
        String urlDelete = URL_DELETE_COMMENT + comment.getId();

        Request request = new Request.Builder()
                .url(urlDelete)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error al eliminar el comentario de soporte: " + e.getMessage(), e);
                ((AdminSupportCommentsActivity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Error al eliminar el comentario de soporte", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String mensajeRespuesta = jsonResponse.getString("mensaje");

                        ((AdminSupportCommentsActivity) context).runOnUiThread(() -> {
                            Toast.makeText(context, mensajeRespuesta, Toast.LENGTH_LONG).show();
                            // Remover el comentario eliminado de la lista y notificar al adaptador
                            supportComments.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, supportComments.size());
                        });

                    } catch (JSONException e) {
                        Log.e(TAG, "Error al parsear la respuesta JSON", e);
                        ((AdminSupportCommentsActivity) context).runOnUiThread(() ->
                                Toast.makeText(context, "Error al eliminar el comentario de soporte", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    Log.e(TAG, "Error en la respuesta: " + response.message());
                    ((AdminSupportCommentsActivity) context).runOnUiThread(() ->
                            Toast.makeText(context, "Error al eliminar el comentario de soporte", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewComment, textViewMail, textViewDate;
        Button btnDeleteComment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewComment = itemView.findViewById(R.id.textViewComment);
            textViewMail = itemView.findViewById(R.id.textViewMail);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            btnDeleteComment = itemView.findViewById(R.id.btnDeleteComment);
        }
    }
}