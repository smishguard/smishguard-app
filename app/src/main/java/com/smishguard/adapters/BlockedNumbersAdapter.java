package com.smishguard.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.smishguard.R;
import com.smishguard.models.BlockedNumberModel;
import java.util.List;

public class BlockedNumbersAdapter extends RecyclerView.Adapter<BlockedNumbersAdapter.NumberViewHolder> {

    private List<BlockedNumberModel> numberList;
    private OnUnlockClickListener onUnlockClickListener;

    public BlockedNumbersAdapter(List<BlockedNumberModel> numberList, OnUnlockClickListener onUnlockClickListener) {
        this.numberList = numberList;
        this.onUnlockClickListener = onUnlockClickListener;
    }

    @NonNull
    @Override
    public NumberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blocked_number, parent, false);
        return new NumberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NumberViewHolder holder, int position) {
        BlockedNumberModel numberModel = numberList.get(position);
        holder.numberTextView.setText(numberModel.getNumber());
        holder.dateTextView.setText(numberModel.getFechaBloqueo());

        // Configurar el botón de desbloquear
        holder.btnUnlock.setOnClickListener(v -> {
            if (onUnlockClickListener != null) {
                onUnlockClickListener.onUnlockClick(numberModel);
            }
        });
    }

    @Override
    public int getItemCount() {
        return numberList.size();
    }

    static class NumberViewHolder extends RecyclerView.ViewHolder {
        TextView numberTextView, dateTextView;
        Button btnUnlock;

        NumberViewHolder(@NonNull View itemView) {
            super(itemView);
            numberTextView = itemView.findViewById(R.id.textViewNumber);
            dateTextView = itemView.findViewById(R.id.textViewDate);
            btnUnlock = itemView.findViewById(R.id.btnUnlockNumber);
        }
    }

    // Interfaz para manejar el clic del botón desbloquear
    public interface OnUnlockClickListener {
        void onUnlockClick(BlockedNumberModel number);
    }
}