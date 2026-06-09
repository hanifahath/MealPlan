package com.example.mealplan.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mealplan.R;
import com.example.mealplan.activity.DetailActivity;
import com.example.mealplan.model.FavoriteMeal;
import com.example.mealplan.utils.Constants;
import java.util.ArrayList;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(FavoriteMeal meal, int position);
    }

    private final Context context;
    private List<FavoriteMeal> favorites = new ArrayList<>();
    private final OnDeleteClickListener deleteListener;

    public FavoriteAdapter(Context context, OnDeleteClickListener deleteListener) {
        this.context = context;
        this.deleteListener = deleteListener;
    }

    public void setFavorites(List<FavoriteMeal> favorites) {
        this.favorites = favorites;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        favorites.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoriteMeal meal = favorites.get(position);
        holder.tvName.setText(meal.getMealName());
        holder.tvCategory.setText(meal.getMealCategory());

        Glide.with(context)
                .load(meal.getMealThumb())
                .placeholder(R.drawable.ic_placeholder)
                .centerCrop()
                .into(holder.imgThumb);

        // Buka detail saat card diklik
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra(Constants.INTENT_MEAL_ID,       meal.getMealId());
            intent.putExtra(Constants.INTENT_MEAL_NAME,     meal.getMealName());
            intent.putExtra(Constants.INTENT_MEAL_THUMB,    meal.getMealThumb());
            intent.putExtra(Constants.INTENT_MEAL_CATEGORY, meal.getMealCategory());
            context.startActivity(intent);
        });

        // Tombol hapus dari favorit
        holder.btnDelete.setOnClickListener(v ->
                deleteListener.onDeleteClick(meal, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() { return favorites.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvName, tvCategory;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumb   = itemView.findViewById(R.id.img_fav_thumb);
            tvName     = itemView.findViewById(R.id.tv_fav_name);
            tvCategory = itemView.findViewById(R.id.tv_fav_category);
            btnDelete  = itemView.findViewById(R.id.btn_fav_delete);
        }
    }
}
