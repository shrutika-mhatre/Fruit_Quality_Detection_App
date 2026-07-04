package com.example.appledisease;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public interface OnDeleteListener {
        void onDelete(DetectionHistory item);
    }

    private final List<DetectionHistory> list;
    private final OnDeleteListener       deleteListener;

    public HistoryAdapter(List<DetectionHistory> list, OnDeleteListener deleteListener) {
        this.list           = list;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DetectionHistory item = list.get(position);

        try {
            Bitmap bmp = BitmapFactory.decodeFile(item.imagePath);
            if (bmp != null) holder.imgThumbnail.setImageBitmap(bmp);
            else holder.imgThumbnail.setImageResource(android.R.drawable.ic_menu_gallery);
        } catch (Exception e) {
            holder.imgThumbnail.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        DiseaseInfo info = DiseaseInfo.getInfo(item.diseaseName);
        holder.tvDiseaseName.setText(info.displayName);
        holder.tvConfidence.setText(String.format("Confidence: %.1f%%", item.confidence * 100));
        holder.tvDateTime.setText(item.dateTime);
        holder.btnDelete.setOnClickListener(v -> deleteListener.onDelete(item));
        // Pura card clickable banao
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(
                    v.getContext(), ResultDetailActivity.class);
            intent.putExtra("imagePath",   item.imagePath);
            intent.putExtra("diseaseName", item.diseaseName);
            intent.putExtra("confidence",  item.confidence);
            intent.putExtra("dateTime",    item.dateTime);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail;
        TextView  tvDiseaseName, tvConfidence, tvDateTime, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            imgThumbnail  = itemView.findViewById(R.id.imgThumbnail);
            tvDiseaseName = itemView.findViewById(R.id.tvDiseaseName);
            tvConfidence  = itemView.findViewById(R.id.tvConfidence);
            tvDateTime    = itemView.findViewById(R.id.tvDateTime);
            btnDelete     = itemView.findViewById(R.id.btnDelete);
        }

    }
}
