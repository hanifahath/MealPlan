package com.example.mealplan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mealplan.R;
import com.example.mealplan.utils.MeasureScaler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ViewHolder> {

    private final Context context;
    private List<String[]> ingredients = new ArrayList<>();
    private boolean[] checked = new boolean[0];
    private float servingFactor = 1f;

    public IngredientAdapter(Context context) {
        this.context = context;
    }

    public void setIngredients(List<String[]> ingredients) {
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
        this.checked = new boolean[this.ingredients.size()];
        Arrays.fill(this.checked, true);
        notifyDataSetChanged();
    }

    public void setServingFactor(float factor) {
        this.servingFactor = factor;
        notifyDataSetChanged();
    }

    public void setAllChecked(boolean value) {
        Arrays.fill(checked, value);
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        int c = 0;
        for (boolean b : checked) if (b) c++;
        return c;
    }

    public List<String[]> getSelectedItems() {
        List<String[]> out = new ArrayList<>();
        for (int i = 0; i < ingredients.size(); i++) {
            if (i < checked.length && checked[i]) {
                String name = ingredients.get(i)[0];
                String measure = ingredients.get(i).length > 1 ? ingredients.get(i)[1] : "";
                out.add(new String[]{name, MeasureScaler.scale(measure, servingFactor)});
            }
        }
        return out;
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
        String measure = item.length > 1 ? item[1] : "";
        String scaled = MeasureScaler.scale(measure, servingFactor);
        holder.tvMeasure.setText(scaled == null || scaled.isEmpty() ? "-" : scaled);

        holder.cb.setOnCheckedChangeListener(null);
        holder.cb.setChecked(position < checked.length && checked[position]);
        holder.cb.setOnCheckedChangeListener((btn, isChecked) -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && pos < checked.length)
                checked[pos] = isChecked;
        });
        holder.itemView.setOnClickListener(v -> holder.cb.toggle());
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cb;
        TextView tvIngredientName, tvMeasure;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cb = itemView.findViewById(R.id.cb_ingredient);
            tvIngredientName = itemView.findViewById(R.id.tv_ingredient_name);
            tvMeasure = itemView.findViewById(R.id.tv_ingredient_measure);
        }
    }
}