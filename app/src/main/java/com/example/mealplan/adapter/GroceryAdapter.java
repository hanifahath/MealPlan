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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GroceryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private List<GroceryItem> displayList = new ArrayList<>();
    // Raw items tanpa header — untuk rebuild list
    private List<GroceryItem> rawItems = new ArrayList<>();

    private OnProgressChangedListener progressListener;

    public interface OnProgressChangedListener {
        void onProgressChanged(int checked, int total);
    }

    public void setOnProgressChangedListener(OnProgressChangedListener listener) {
        this.progressListener = listener;
    }

    public GroceryAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<GroceryItem> items) {
        this.rawItems = new ArrayList<>(items);
        rebuildDisplayList();
    }

    // Rebuild: kelompokkan per sourceMeal, checked items di bawah
    private void rebuildDisplayList() {
        displayList.clear();

        // Pisahkan unchecked dan checked
        Map<String, List<GroceryItem>> uncheckedBySrc = new LinkedHashMap<>();
        List<GroceryItem> checkedItems = new ArrayList<>();

        for (GroceryItem item : rawItems) {
            if (item.isChecked()) {
                checkedItems.add(item);
            } else {
                uncheckedBySrc.computeIfAbsent(
                        item.getSourceMeal(), k -> new ArrayList<>()).add(item);
            }
        }

        // Tambah unchecked dengan header per resep
        for (Map.Entry<String, List<GroceryItem>> entry : uncheckedBySrc.entrySet()) {
            displayList.add(new GroceryItem(entry.getKey(), GroceryItem.TYPE_HEADER));
            displayList.addAll(entry.getValue());
        }

        // Tambah checked section kalau ada
        if (!checkedItems.isEmpty()) {
            displayList.add(new GroceryItem(
                    "Sudah dibeli (" + checkedItems.size() + ")",
                    GroceryItem.TYPE_CHECKED_HEADER));
            displayList.addAll(checkedItems);
        }

        notifyDataSetChanged();
        notifyProgress();
    }

    private void notifyProgress() {
        if (progressListener == null) return;
        int total = rawItems.size();
        int checked = 0;
        for (GroceryItem item : rawItems) {
            if (item.isChecked()) checked++;
        }
        progressListener.onProgressChanged(checked, total);
    }

    public void clearChecked() {
        rawItems.removeIf(GroceryItem::isChecked);
        rebuildDisplayList();
    }

    public String toShareText() {
        StringBuilder sb = new StringBuilder("Daftar Belanja MealPlan\n\n");
        for (GroceryItem item : displayList) {
            if (item.getViewType() == GroceryItem.TYPE_HEADER) {
                sb.append("\n").append(item.getName()).append("\n");
            } else if (item.getViewType() == GroceryItem.TYPE_ITEM
                    && !item.isChecked()) {
                sb.append("- ").append(item.getName());
                if (!item.getMeasure().isEmpty())
                    sb.append(": ").append(item.getMeasure());
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    public int getItemViewType(int position) {
        return displayList.get(position).getViewType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(context);
        if (viewType == GroceryItem.TYPE_HEADER
                || viewType == GroceryItem.TYPE_CHECKED_HEADER) {
            View v = inf.inflate(R.layout.item_grocery_header, parent, false);
            return new HeaderViewHolder(v);
        } else {
            View v = inf.inflate(R.layout.item_grocery, parent, false);
            return new ItemViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GroceryItem item = displayList.get(position);

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvHeader.setText(item.getName());
            boolean isCheckedSection =
                    item.getViewType() == GroceryItem.TYPE_CHECKED_HEADER;
            ((HeaderViewHolder) holder).tvHeader.setAlpha(isCheckedSection ? 0.5f : 1f);

        } else if (holder instanceof ItemViewHolder) {
            ItemViewHolder h = (ItemViewHolder) holder;
            h.tvName.setText(item.getName());
            if (!item.getMeasure().isEmpty()) {
                h.tvMeasure.setVisibility(View.VISIBLE);
                h.tvMeasure.setText(item.getMeasure());
            } else {
                h.tvMeasure.setVisibility(View.GONE);
            }

            h.cbGrocery.setOnCheckedChangeListener(null);
            h.cbGrocery.setChecked(item.isChecked());
            applyCheckedStyle(h, item.isChecked());

            h.cbGrocery.setOnCheckedChangeListener((btn, isChecked) -> {
                item.setChecked(isChecked);
                rebuildDisplayList();
            });

            h.itemView.setOnClickListener(v -> {
                item.setChecked(!item.isChecked());
                rebuildDisplayList();
            });
        }
    }

    private void applyCheckedStyle(ItemViewHolder h, boolean checked) {
        if (checked) {
            h.tvName.setPaintFlags(
                    h.tvName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            h.tvName.setAlpha(0.4f);
            h.tvMeasure.setAlpha(0.4f);
        } else {
            h.tvName.setPaintFlags(
                    h.tvName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            h.tvName.setAlpha(1f);
            h.tvMeasure.setAlpha(1f);
        }
    }

    @Override
    public int getItemCount() { return displayList.size(); }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;
        HeaderViewHolder(@NonNull View v) {
            super(v);
            tvHeader = v.findViewById(R.id.tv_grocery_header);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbGrocery;
        TextView tvName, tvMeasure;
        ItemViewHolder(@NonNull View v) {
            super(v);
            cbGrocery = v.findViewById(R.id.cb_grocery);
            tvName    = v.findViewById(R.id.tv_grocery_name);
            tvMeasure = v.findViewById(R.id.tv_grocery_measure);
        }
    }
}