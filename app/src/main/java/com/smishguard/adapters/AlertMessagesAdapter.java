package com.smishguard.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.smishguard.activities.AdminInfoAlertMessageActivity;
import com.smishguard.R;
import com.smishguard.models.AlertMessageModel;

import java.util.List;

public class AlertMessagesAdapter extends RecyclerView.Adapter<AlertMessagesAdapter.ViewHolder> {

    private List<AlertMessageModel> alertMessages;
    private Context context;

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

        // Configuración del botón "Ver Informe"
        holder.btnViewInform.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminInfoAlertMessageActivity.class);
            intent.putExtra("alertMessageId", message.getId());
            intent.putExtra("contenido", message.getContenido());
            intent.putExtra("url", message.getUrl());
            intent.putExtra("nivel_peligro", message.getNivelPeligro());
            intent.putExtra("justificacion_gpt", message.getJustificacionGpt());
            intent.putExtra("ponderado", message.getPonderado());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return alertMessages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewBody, textViewAddress;
        Button btnViewInform;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewBody = itemView.findViewById(R.id.textViewBody);
            textViewAddress = itemView.findViewById(R.id.textViewAddress);
            btnViewInform = itemView.findViewById(R.id.btnViewInform);
        }
    }
}