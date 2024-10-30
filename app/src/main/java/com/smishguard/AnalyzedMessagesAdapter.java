package com.smishguard;

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

public class AnalyzedMessagesAdapter extends RecyclerView.Adapter<AnalyzedMessagesAdapter.ReportedMessageViewHolder> {

    private List<SmsModel> reportedMessages;
    private OnItemClickListener onViewAnalysisClickListener;

    public interface OnItemClickListener {
        void onItemClick(SmsModel sms);
    }

    public AnalyzedMessagesAdapter(List<SmsModel> reportedMessages, OnItemClickListener onViewAnalysisClickListener) {
        this.reportedMessages = reportedMessages;
        this.onViewAnalysisClickListener = onViewAnalysisClickListener;
    }

    @NonNull
    @Override
    public ReportedMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_analyzed_message, parent, false);
        return new ReportedMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportedMessageViewHolder holder, int position) {
        SmsModel sms = reportedMessages.get(position);
        holder.bodyTextView.setText(sms.getBody());
        holder.addressTextView.setText(sms.getAddress());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.dateTextView.setText(sdf.format(new Date(sms.getDate())));

        holder.btnViewAnalysis.setOnClickListener(v -> onViewAnalysisClickListener.onItemClick(sms));
    }

    @Override
    public int getItemCount() {
        return reportedMessages.size();
    }

    static class ReportedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView bodyTextView, addressTextView, dateTextView;
        Button btnViewAnalysis, btnDeleteReport;

        ReportedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            bodyTextView = itemView.findViewById(R.id.textViewBody);
            addressTextView = itemView.findViewById(R.id.textViewAddress);
            dateTextView = itemView.findViewById(R.id.textViewDate);
            btnViewAnalysis = itemView.findViewById(R.id.btnViewAnalysis);
        }
    }
}