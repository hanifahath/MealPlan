package com.example.mealplan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mealplan.R;
import com.example.mealplan.model.PlannerItem;
import com.example.mealplan.utils.Constants;

import java.util.List;

public class PlannerAdapter extends RecyclerView.Adapter<PlannerAdapter.ViewHolder> {

    public interface OnPlannerActionListener {
        void onAddClick(String day);
        void onDeleteClick(PlannerItem item, int position);
    }

    private final Context context;
    private final PlannerItem[] slots = new PlannerItem[7];
    private final OnPlannerActionListener listener;

    public PlannerAdapter(Context context, OnPlannerActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setPlannerItems(List<PlannerItem> items) {
        for (int i = 0; i < 7; i++) slots[i] = null;
        for (PlannerItem item : items) {
            for (int i = 0; i < Constants.DAYS_OF_WEEK.length; i++) {
                if (Constants.DAYS_OF_WEEK[i].equals(item.getDayOfWeek())) {
                    slots[i] = item;
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }

    public void removeSlot(int position) {
        slots[position] = null;
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_planner, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String day = Constants.DAYS_OF_WEEK[position];
        PlannerItem item = slots[position];

        holder.tvDay.setText(day);

        if (item != null) {
            // Slot terisi
            holder.tvMealName.setText(item.getMealName());
            holder.tvMealName.setVisibility(View.VISIBLE);
            holder.cardThumb.setVisibility(View.VISIBLE);
            holder.tvEmpty.setVisibility(View.GONE);
            holder.btnAdd.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(item.getMealThumb())
                    .placeholder(R.drawable.ic_placeholder)
                    .centerCrop()
                    .into(holder.imgThumb);

            holder.btnDelete.setOnClickListener(v ->
                    listener.onDeleteClick(item, holder.getAdapterPosition()));

        } else {
            // Slot kosong
            holder.tvMealName.setVisibility(View.GONE);
            holder.cardThumb.setVisibility(View.GONE);
            holder.tvEmpty.setVisibility(View.VISIBLE);
            holder.btnAdd.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.GONE);

            holder.btnAdd.setOnClickListener(v -> listener.onAddClick(day));
        }
    }

    @Override
    public int getItemCount() { return 7; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvMealName, tvEmpty;
        CardView cardThumb;
        ImageView imgThumb;
        ImageButton btnAdd, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay      = itemView.findViewById(R.id.tv_planner_day);
            tvMealName = itemView.findViewById(R.id.tv_planner_meal_name);
            tvEmpty    = itemView.findViewById(R.id.tv_planner_empty);
            cardThumb  = itemView.findViewById(R.id.card_planner_thumb);
            imgThumb   = itemView.findViewById(R.id.img_planner_thumb);
            btnAdd     = itemView.findViewById(R.id.btn_planner_add);
            btnDelete  = itemView.findViewById(R.id.btn_planner_delete);
        }
    }
}
