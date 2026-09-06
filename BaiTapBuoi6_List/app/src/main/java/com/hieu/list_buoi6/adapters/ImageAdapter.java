package com.hieu.list_buoi6.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hieu.list_buoi6.R;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private List<Integer> images;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position, View view);
    }

    public ImageAdapter(List<Integer> images, OnItemClickListener listener) {
        this.images = images;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.image_item, null);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.avatar.setImageResource(images.get(position));

        holder.avatar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position, v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
        }
    }
}