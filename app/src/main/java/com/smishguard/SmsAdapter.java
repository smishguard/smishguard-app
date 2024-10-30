package com.smishguard;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SmsAdapter extends RecyclerView.Adapter<SmsAdapter.SmsViewHolder> {

    private List<SmsModel> smsList;
    private List<String> blockedNumbers;
    private OnItemClickListener onItemClickListener;
    private boolean checkBlockedNumbers;

    public SmsAdapter(List<SmsModel> smsList, List<String> blockedNumbers) {
        this.smsList = smsList;
        this.blockedNumbers = blockedNumbers;
        this.checkBlockedNumbers = true;
    }

    public interface OnItemClickListener {
        void onItemClick(SmsModel sms);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public SmsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sms, parent, false);
        return new SmsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SmsViewHolder holder, int position) {
        SmsModel sms = smsList.get(position);
        holder.addressTextView.setText(sms.getAddress());
        holder.bodyTextView.setText(sms.getBody());

        // Cambiar colores y habilitar/deshabilitar botón según estado de bloqueo
        if (checkBlockedNumbers && blockedNumbers != null && blockedNumbers.contains(sms.getAddress())) {
            // Colores en rojo más claro para el mensaje bloqueado
            int lightRed = Color.parseColor("#FF9999"); // Rojo claro
            holder.addressTextView.setTextColor(lightRed);
            holder.bodyTextView.setTextColor(lightRed);

            // Deshabilitar y aplicar fondo rojo claro al botón de análisis
            holder.analyzeButton.setEnabled(false);
            applyBackground(holder.analyzeButton, Color.parseColor("#FFC1C1")); // Rojo claro para botón deshabilitado
        } else {
            // Colores normales para mensajes no bloqueados
            holder.addressTextView.setTextColor(Color.BLACK);
            holder.bodyTextView.setTextColor(Color.BLACK);

            // Habilitar y restaurar el fondo rojo normal en el botón de análisis
            holder.analyzeButton.setEnabled(true);
            applyBackground(holder.analyzeButton, Color.parseColor("#FF0000")); // Rojo normal para botón habilitado
        }

        // Configurar el clic en el botón de análisis
        holder.analyzeButton.setOnClickListener(view -> {
            if (onItemClickListener != null && holder.analyzeButton.isEnabled()) {
                onItemClickListener.onItemClick(sms);
            }
        });
    }

    private void applyBackground(Button button, int color) {
        Drawable background = button.getBackground();
        if (background instanceof GradientDrawable) {
            ((GradientDrawable) background).setColor(color);
        } else {
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setColor(color);
            gradientDrawable.setCornerRadius(100); // Redondeado del botón
            button.setBackground(gradientDrawable);
        }
    }

    @Override
    public int getItemCount() {
        return smsList.size();
    }

    static class SmsViewHolder extends RecyclerView.ViewHolder {
        TextView addressTextView, bodyTextView;
        Button analyzeButton;

        SmsViewHolder(@NonNull View itemView) {
            super(itemView);
            addressTextView = itemView.findViewById(R.id.textViewAddress);
            bodyTextView = itemView.findViewById(R.id.textViewBody);
            analyzeButton = itemView.findViewById(R.id.btnAnalyzeSms);
        }
    }
}