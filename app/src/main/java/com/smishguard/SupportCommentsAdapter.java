package com.smishguard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SupportCommentsAdapter extends RecyclerView.Adapter<SupportCommentsAdapter.ViewHolder> {

    private List<SupportCommentModel> supportComments;

    public SupportCommentsAdapter(List<SupportCommentModel> supportComments) {
        this.supportComments = supportComments;
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

        // Formatear la fecha antes de mostrarla
        holder.textViewDate.setText(formatDate(comment.getFecha()));
    }

    @Override
    public int getItemCount() {
        return supportComments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewComment, textViewMail, textViewDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewComment = itemView.findViewById(R.id.textViewComment);
            textViewMail = itemView.findViewById(R.id.textViewMail);
            textViewDate = itemView.findViewById(R.id.textViewDate);
        }
    }

    // Método para formatear la fecha
    private String formatDate(String dateString) {
        try {
            // Este es el formato de la fecha recibida
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            // Este es el formato de la fecha de salida
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            // Convertir la fecha de entrada a Date y luego formatearla en el formato de salida
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            // Si hay un error de parseo, devuelve la fecha sin formatear
            return dateString;
        }
    }
}