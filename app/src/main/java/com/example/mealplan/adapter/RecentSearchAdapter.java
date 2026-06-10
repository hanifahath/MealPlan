package com.example.mealplan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mealplan.R;
import java.util.ArrayList;
import java.util.List;

public class RecentSearchAdapter extends RecyclerView.Adapter<RecentSearchAdapter.ViewHolder> {

    public interface OnRecentSearchListener {
        void onQueryClick(String query);
        void onRemoveClick(String query, int position);
    }

    private final Context context;
    private List<String> queries = new ArrayList<>();
    private final OnRecentSearchListener listener;

    public RecentSearchAdapter(Context context, OnRecentSearchListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setQueries(List<String> queries) {
        this.queries = new ArrayList<>(queries);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        queries.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_recent_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String query = queries.get(position);
        holder.tvQuery.setText(query);
        holder.itemView.setOnClickListener(v -> listener.onQueryClick(query));
        holder.btnRemove.setOnClickListener(v ->
                listener.onRemoveClick(query, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() { return queries.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuery;
        ImageButton btnRemove;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuery   = itemView.findViewById(R.id.tv_recent_query);
            btnRemove = itemView.findViewById(R.id.btn_remove_recent);
        }
    }
}
