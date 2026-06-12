package com.example.mealplan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mealplan.R;
import com.example.mealplan.model.FavoriteMeal;
import com.example.mealplan.utils.LocaleMapper;
import java.util.ArrayList;
import java.util.List;

public class RecipePickerAdapter extends RecyclerView.Adapter<RecipePickerAdapter.ViewHolder> {

    public interface OnPickListener {
        void onPick(FavoriteMeal meal);
    }

    private final Context context;
    private final List<FavoriteMeal> items;
    private final OnPickListener listener;

    public RecipePickerAdapter(Context context, List<FavoriteMeal> items, OnPickListener listener) {
        this.context = context;
        this.items = new ArrayList<>(items);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe_picker, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoriteMeal meal = items.get(position);
        holder.tvName.setText(meal.getMealName());
        holder.tvCategory.setText(LocaleMapper.category(meal.getMealCategory()));
        Glide.with(context)
                .load(meal.getMealThumb())
                .placeholder(R.drawable.ic_placeholder)
                .centerCrop()
                .into(holder.imgThumb);
        holder.itemView.setOnClickListener(v -> listener.onPick(meal));
    }

    @Override
    public int getItemCount() { return items.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvName, tvCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumb   = itemView.findViewById(R.id.img_picker_thumb);
            tvName     = itemView.findViewById(R.id.tv_picker_name);
            tvCategory = itemView.findViewById(R.id.tv_picker_category);
        }
    }
}