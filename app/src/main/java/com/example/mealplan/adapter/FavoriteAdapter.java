package com.example.mealplan.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mealplan.R;
import com.example.mealplan.activity.DetailActivity;
import com.example.mealplan.model.FavoriteMeal;
import com.example.mealplan.utils.Constants;
import java.util.ArrayList;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    public interface OnFavoriteActionListener {
        void onDeleteClick(FavoriteMeal meal, int position);
        void onShareClick(FavoriteMeal meal);
    }

    private final Context context;
    private List<FavoriteMeal> favorites = new ArrayList<>();
    private final OnFavoriteActionListener listener;
    private int lastAnimPos = -1;

    public FavoriteAdapter(Context context, OnFavoriteActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setFavorites(List<FavoriteMeal> favorites) {
        this.favorites = new ArrayList<>(favorites);
        lastAnimPos = -1;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < favorites.size()) {
            favorites.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void addItem(FavoriteMeal meal) {
        favorites.add(0, meal);
        notifyItemInserted(0);
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
        holder.tvCategory.setText(com.example.mealplan.utils.LocaleMapper.category(meal.getMealCategory()));

        Glide.with(context)
                .load(meal.getMealThumb())
                .placeholder(R.drawable.ic_placeholder)
                .centerCrop()
                .into(holder.imgThumb);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra(Constants.INTENT_MEAL_ID,       meal.getMealId());
            intent.putExtra(Constants.INTENT_MEAL_NAME,     meal.getMealName());
            intent.putExtra(Constants.INTENT_MEAL_THUMB,    meal.getMealThumb());
            intent.putExtra(Constants.INTENT_MEAL_CATEGORY, meal.getMealCategory());
            if (context instanceof Activity) {
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        (Activity) context, holder.imgThumb, "meal_image");
                context.startActivity(intent, options.toBundle());
            } else {
                context.startActivity(intent);
            }
        });

        holder.btnMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.btnMenu);
            popup.getMenu().add(0, 1, 0, "Bagikan");
            popup.getMenu().add(0, 2, 1, "Hapus dari favorit");
            popup.setOnMenuItemClickListener(item -> {
                int pos = holder.getAdapterPosition();
                if (pos == RecyclerView.NO_ID) return false;
                if (item.getItemId() == 1) {
                    listener.onShareClick(meal);
                } else if (item.getItemId() == 2) {
                    listener.onDeleteClick(meal, pos);
                }
                return true;
            });
            popup.show();
        });

        animateItem(holder.itemView, position);
    }

    private void animateItem(View view, int position) {
        if (position > lastAnimPos) {
            view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.item_slide_up));
            lastAnimPos = position;
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    @Override
    public int getItemCount() { return favorites.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvName, tvCategory;
        ImageButton btnMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumb   = itemView.findViewById(R.id.img_fav_thumb);
            tvName     = itemView.findViewById(R.id.tv_fav_name);
            tvCategory = itemView.findViewById(R.id.tv_fav_category);
            btnMenu    = itemView.findViewById(R.id.btn_fav_menu);
        }
    }
}