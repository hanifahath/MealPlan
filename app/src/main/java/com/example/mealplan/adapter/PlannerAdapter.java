package com.example.mealplan.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mealplan.R;
import com.example.mealplan.activity.DetailActivity;
import com.example.mealplan.model.PlannerItem;
import com.example.mealplan.utils.Constants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlannerAdapter extends RecyclerView.Adapter<PlannerAdapter.DayViewHolder> {

    public interface OnPlannerActionListener {
        void onAddClick(String day);
        void onDeleteMealClick(PlannerItem item);
    }

    private final Context context;
    private final OnPlannerActionListener listener;
    // Map dari nama hari ke list resep di hari itu
    private final Map<String, List<PlannerItem>> dayMeals = new HashMap<>();

    public PlannerAdapter(Context context, OnPlannerActionListener listener) {
        this.context = context;
        this.listener = listener;
        // Inisialisasi semua hari dengan list kosong
        for (String day : Constants.DAYS_OF_WEEK) {
            dayMeals.put(day, new ArrayList<>());
        }
    }

    public void setPlannerItems(List<PlannerItem> items) {
        // Reset
        for (String day : Constants.DAYS_OF_WEEK) {
            dayMeals.put(day, new ArrayList<>());
        }
        for (PlannerItem item : items) {
            List<PlannerItem> list = dayMeals.get(item.getDayOfWeek());
            if (list != null) list.add(item);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_planner_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        String day = Constants.DAYS_OF_WEEK[position];
        List<PlannerItem> meals = dayMeals.get(day);

        holder.tvDay.setText(day);
        holder.btnAdd.setOnClickListener(v -> listener.onAddClick(day));

        // Clear container dulu
        holder.containerMeals.removeAllViews();

        if (meals == null || meals.isEmpty()) {
            holder.cardEmptySlot.setVisibility(View.VISIBLE);
        } else {
            holder.cardEmptySlot.setVisibility(View.GONE);
            for (PlannerItem meal : meals) {
                View mealView = LayoutInflater.from(context)
                        .inflate(R.layout.item_planner_meal, holder.containerMeals, false);

                ImageView imgThumb = mealView.findViewById(R.id.img_planner_meal_thumb);
                TextView tvName    = mealView.findViewById(R.id.tv_planner_meal_name);
                ImageButton btnDel = mealView.findViewById(R.id.btn_planner_meal_delete);

                tvName.setText(meal.getMealName());
                Glide.with(context)
                        .load(meal.getMealThumb())
                        .placeholder(R.drawable.ic_placeholder)
                        .centerCrop()
                        .into(imgThumb);

                // Fix 14: klik item buka DetailActivity
                mealView.setOnClickListener(v -> {
                    Intent intent = new Intent(context, DetailActivity.class);
                    intent.putExtra(Constants.INTENT_MEAL_ID,       meal.getMealId());
                    intent.putExtra(Constants.INTENT_MEAL_NAME,     meal.getMealName());
                    intent.putExtra(Constants.INTENT_MEAL_THUMB,    meal.getMealThumb());
                    intent.putExtra(Constants.INTENT_MEAL_CATEGORY, "");
                    context.startActivity(intent);
                });

                btnDel.setOnClickListener(v -> listener.onDeleteMealClick(meal));

                holder.containerMeals.addView(mealView);
            }
        }
    }

    @Override
    public int getItemCount() { return Constants.DAYS_OF_WEEK.length; }

    public static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay;
        ImageButton btnAdd;
        LinearLayout containerMeals;
        View cardEmptySlot;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay          = itemView.findViewById(R.id.tv_planner_day);
            btnAdd         = itemView.findViewById(R.id.btn_planner_add);
            containerMeals = itemView.findViewById(R.id.container_meals);
            cardEmptySlot  = itemView.findViewById(R.id.card_empty_slot);
        }
    }
}
