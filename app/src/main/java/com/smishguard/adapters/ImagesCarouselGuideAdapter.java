package com.smishguard.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.smishguard.R;
import java.util.List;

public class ImagesCarouselGuideAdapter extends RecyclerView.Adapter<ImagesCarouselGuideAdapter.PhotoViewHolder> {

    private final List<Integer> photos;
    private final Context context;

    public ImagesCarouselGuideAdapter(Context context, List<Integer> photos) {
        this.context = context;
        this.photos = photos;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla el diseño del item que contiene el ImageView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_education, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        // Ajusta la imagen usando Glide o una librería similar
        Glide.with(context)
                .load(photos.get(position))
                .centerCrop()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            // Opcional: Configura el ImageView aquí en caso de que no tengas un archivo XML para el layout
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setAdjustViewBounds(true);
        }
    }
}