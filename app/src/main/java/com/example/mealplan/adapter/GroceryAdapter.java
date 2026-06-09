package com.example.mealplan.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mealplan.R;
import com.example.mealplan.model.GroceryItem;
import java.util.ArrayList;
import java.util.List;

public class GroceryAdapter extends RecyclerView.Adapter<GroceryAdapter.ViewHolder> {

    private final Context context;
    private List<GroceryItem> items = new ArrayList<>();

    public GroceryAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<GroceryItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    // Hapus semua yang sudah dicentang
    public void clearChecked() {
        items.removeIf(GroceryItem::isChecked);
        notifyDataSetChanged();
    }

    // Generate teks plain untuk share
    public String toShareText() {
        StringBuilder sb = new StringBuilder("🛒 Daftar Belanja MealPlan\n\n");
        String lastMeal = "";
        for (GroceryItem item : items) {
            if (!item.getSourceMeal().equals(lastMeal)) {
                sb.append("📍 ").append(item.getSourceMeal()).append("\n");
                lastMeal = item.getSourceMeal();
            }
            sb.append("  • ").append(item.getName());
            if (!item.getMeasure().isEmpty()) {
                sb.append(" — ").append(item.getMeasure());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_grocery, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroceryItem item = items.get(position);

        holder.tvName.setText(item.getName());

        if (!item.getMeasure().isEmpty()) {
            holder.tvMeasure.setVisibility(View.VISIBLE);
            holder.tvMeasure.setText(item.getMeasure());
        } else {
            holder.tvMeasure.setVisibility(View.GONE);
        }

        // Update tampilan sesuai status checked
        holder.cbGrocery.setOnCheckedChangeListener(null);
        holder.cbGrocery.setChecked(item.isChecked());
        applyCheckedStyle(holder, item.isChecked());

        holder.cbGrocery.setOnCheckedChangeListener((btn, isChecked) -> {
            item.setChecked(isChecked);
            applyCheckedStyle(holder, isChecked);
        });

        holder.itemView.setOnClickListener(v -> {
            boolean newState = !item.isChecked();
            item.setChecked(newState);
            holder.cbGrocery.setChecked(newState);
            applyCheckedStyle(holder, newState);
        });
    }

    private void applyCheckedStyle(ViewHolder holder, boolean checked) {
        if (checked) {
            holder.tvName.setPaintFlags(holder.tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvName.setAlpha(0.4f);
            holder.tvMeasure.setAlpha(0.4f);
        } else {
            holder.tvName.setPaintFlags(holder.tvName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvName.setAlpha(1f);
            holder.tvMeasure.setAlpha(1f);
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbGrocery;
        TextView tvName, tvMeasure;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbGrocery  = itemView.findViewById(R.id.cb_grocery);
            tvName     = itemView.findViewById(R.id.tv_grocery_name);
            tvMeasure  = itemView.findViewById(R.id.tv_grocery_measure);
        }
    }
}
