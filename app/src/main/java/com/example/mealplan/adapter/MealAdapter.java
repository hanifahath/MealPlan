package com.example.mealplan.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mealplan.R;
import com.example.mealplan.activity.DetailActivity;
import com.example.mealplan.model.Meal;
import com.example.mealplan.utils.Constants;
import com.example.mealplan.utils.LocaleMapper;
import java.util.ArrayList;
import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.ViewHolder> {

    private static final int TYPE_FEATURED = 0;
    private static final int TYPE_GRID = 1;

    private final Context context;
    private List<Meal> meals = new ArrayList<>();
    private boolean featuredEnabled = true;
    private int lastAnimPos = -1;

    public MealAdapter(Context context) {
        this.context = context;
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
        lastAnimPos = -1;
        notifyDataSetChanged();
    }

    public void setFeaturedEnabled(boolean enabled) {
        this.featuredEnabled = enabled;
    }

    public boolean isFeatured(int position) {
        return featuredEnabled && position == 0;
    }

    @Override
    public int getItemViewType(int position) {
        return isFeatured(position) ? TYPE_FEATURED : TYPE_GRID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == TYPE_FEATURED ? R.layout.item_meal_featured : R.layout.item_meal;
        View view = LayoutInflater.from(context).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Meal meal = meals.get(position);
        holder.tvName.setText(meal.getName());

        if (meal.getCategory() != null && !meal.getCategory().isEmpty()) {
            holder.tvCategory.setVisibility(View.VISIBLE);
            holder.tvCategory.setText(LocaleMapper.category(meal.getCategory()));
        } else {
            holder.tvCategory.setVisibility(View.GONE);
        }

        Glide.with(context)
                .load(meal.getThumb())
                .placeholder(R.drawable.ic_placeholder)
                .centerCrop()
                .into(holder.imgThumb);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra(Constants.INTENT_MEAL_ID,       meal.getId());
            intent.putExtra(Constants.INTENT_MEAL_NAME,     meal.getName());
            intent.putExtra(Constants.INTENT_MEAL_THUMB,    meal.getThumb());
            intent.putExtra(Constants.INTENT_MEAL_CATEGORY, meal.getCategory());
            if (context instanceof Activity) {
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        (Activity) context, holder.imgThumb, "meal_image");
                context.startActivity(intent, options.toBundle());
            } else {
                context.startActivity(intent);
            }
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
    public int getItemCount() { return meals.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvName, tvCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumb   = itemView.findViewById(R.id.img_meal_thumb);
            tvName     = itemView.findViewById(R.id.tv_meal_name);
            tvCategory = itemView.findViewById(R.id.tv_meal_category);
        }
    }
}