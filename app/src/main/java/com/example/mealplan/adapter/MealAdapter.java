package com.example.mealplan.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mealplan.R;
import com.example.mealplan.activity.DetailActivity;
import com.example.mealplan.model.Meal;
import com.example.mealplan.utils.Constants;
import java.util.ArrayList;
import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.ViewHolder> {

    private final Context context;
    private List<Meal> meals = new ArrayList<>();

    public MealAdapter(Context context) {
        this.context = context;
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_meal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Meal meal = meals.get(position);
        holder.tvName.setText(meal.getName());

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
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return meals.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.img_meal_thumb);
            tvName   = itemView.findViewById(R.id.tv_meal_name);
        }
    }
}
