package com.example.mealplan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mealplan.R;
import com.example.mealplan.model.Category;
import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(String categoryName);
    }

    private final Context context;
    private List<Category> categories = new ArrayList<>();
    private int selectedPosition = 0;
    private final OnCategoryClickListener listener;

    public CategoryAdapter(Context context, OnCategoryClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category cat = categories.get(position);
        String displayName = (cat.getName() == null) ? "Semua" : com.example.mealplan.utils.LocaleMapper.category(cat.getName());
        holder.tvCategory.setText(displayName);

        if (position == selectedPosition) {
            holder.tvCategory.setBackgroundResource(R.drawable.bg_chip_selected);
            holder.tvCategory.setTextColor(
                    ContextCompat.getColor(context, R.color.chip_text_selected));
        } else {
            holder.tvCategory.setBackgroundResource(R.drawable.bg_chip_normal);
            holder.tvCategory.setTextColor(
                    ContextCompat.getColor(context, R.color.chip_text_normal));
        }

        holder.itemView.setOnClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            // Kirim null kalau "Semua"
            listener.onCategoryClick(cat.getName());
        });
    }

    @Override
    public int getItemCount() { return categories.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_category_name);
        }
    }
}
