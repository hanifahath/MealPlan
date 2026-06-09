package com.example.mealplan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mealplan.R;
import java.util.ArrayList;
import java.util.List;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ViewHolder> {

    private final Context context;
    // Setiap item adalah array {"nama bahan", "takaran"}
    private List<String[]> ingredients = new ArrayList<>();

    public IngredientAdapter(Context context) {
        this.context = context;
    }

    public void setIngredients(List<String[]> ingredients) {
        this.ingredients = ingredients;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ingredient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] item = ingredients.get(position);
        holder.tvIngredientName.setText(item[0]);
        holder.tvMeasure.setText(item[1].isEmpty() ? "-" : item[1]);
    }

    @Override
    public int getItemCount() { return ingredients.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIngredientName, tvMeasure;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIngredientName = itemView.findViewById(R.id.tv_ingredient_name);
            tvMeasure        = itemView.findViewById(R.id.tv_ingredient_measure);
        }
    }
}
