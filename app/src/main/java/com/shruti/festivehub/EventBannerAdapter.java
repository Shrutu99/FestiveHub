
package com.shruti.festivehub;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class EventBannerAdapter
        extends RecyclerView.Adapter<EventBannerAdapter.BannerViewHolder> {

    private final String[] titles = {
            "Technical Events",
            "Cultural Festival",
            "Sports Events"
    };

    private final String[] descriptions = {
            "Discover exciting technology events!",
            "Celebrate creativity, art and culture!",
            "Join exciting sports competitions!"
    };

    private final int[] colors = {
            Color.parseColor("#12345A"),
            Color.parseColor("#B85C00"),
            Color.parseColor("#315B82")
    };

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_banner, parent, false);

        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull BannerViewHolder holder, int position) {

        int index = position % titles.length;

        holder.title.setText(titles[index]);
        holder.description.setText(descriptions[index]);
        holder.container.setBackgroundColor(colors[index]);
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {

        FrameLayout container;
        TextView title, description;

        BannerViewHolder(@NonNull View itemView) {
            super(itemView);

            container = itemView.findViewById(R.id.bannerContainer);
            title = itemView.findViewById(R.id.tvBannerTitle);
            description = itemView.findViewById(R.id.tvBannerDescription);
        }
    }
}
