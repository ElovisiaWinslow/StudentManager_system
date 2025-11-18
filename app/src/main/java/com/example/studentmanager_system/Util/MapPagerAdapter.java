package com.example.studentmanager_system.Util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanager_system.R;

public class MapPagerAdapter extends RecyclerView.Adapter<MapPagerAdapter.MapViewHolder> {

    private int[] mapImages = {
            R.drawable.xianlin,     // 仙林校区地图
            R.drawable.sanpailou,     // 三牌楼校区地图
            R.drawable.suojincun        // 莫愁校区地图
    };

    private String[] mapNames = {
            "南京邮电大学仙林校区",
            "南京邮电大学三牌楼校区",
            "南京邮电大学锁金村校区"
    };

    private OnMapClickListener onMapClickListener;

    public interface OnMapClickListener {
        void onMapClick(int position);
    }

    public void setOnMapClickListener(OnMapClickListener listener) {
        this.onMapClickListener = listener;
    }

    @NonNull
    @Override
    public MapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_map_preview, parent, false);
        return new MapViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MapViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mapImages.length;
    }

    public String getMapName(int position) {
        return mapNames[position];
    }

    class MapViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        MapViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView_map);
        }

        void bind(int position) {
            imageView.setImageResource(mapImages[position]);

            imageView.setOnClickListener(v -> {
                if (onMapClickListener != null) {
                    onMapClickListener.onMapClick(position);
                }
            });
        }
    }
}
