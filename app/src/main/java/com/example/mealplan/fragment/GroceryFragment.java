package com.example.mealplan.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.example.mealplan.R;
import com.example.mealplan.adapter.GroceryAdapter;
import com.example.mealplan.database.FavoriteDao;
import com.example.mealplan.database.PlannerDao;
import com.example.mealplan.model.FavoriteMeal;
import com.example.mealplan.model.GroceryItem;
import com.example.mealplan.model.PlannerItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GroceryFragment extends Fragment {

    private RecyclerView rvGrocery;
    private LinearLayout layoutEmpty;
    private TextView tvSubtitle, tvInfo;
    private Button btnClearChecked;
    private android.widget.ProgressBar progressGrocery;
    private GroceryAdapter adapter;

    private PlannerDao plannerDao;
    private FavoriteDao favoriteDao;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_grocery, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        plannerDao  = new PlannerDao(requireContext());
        favoriteDao = new FavoriteDao(requireContext());

        rvGrocery       = view.findViewById(R.id.rv_grocery);
        layoutEmpty     = view.findViewById(R.id.layout_grocery_empty);
        tvSubtitle      = view.findViewById(R.id.tv_grocery_subtitle);
        tvInfo          = view.findViewById(R.id.tv_grocery_info);
        btnClearChecked = view.findViewById(R.id.btn_clear_checked);
        progressGrocery = view.findViewById(R.id.progress_grocery);

        adapter = new GroceryAdapter(requireContext());
        rvGrocery.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvGrocery.setAdapter(adapter);

        btnClearChecked.setOnClickListener(v -> adapter.clearChecked());

        view.findViewById(R.id.btn_share_grocery).setOnClickListener(v -> shareGroceryList());

        generateGroceryList();
    }

    @Override
    public void onResume() {
        super.onResume();
        generateGroceryList();
    }

    private void generateGroceryList() {
        executor.execute(() -> {
            List<PlannerItem> plannerItems = plannerDao.getAll();

            if (plannerItems.isEmpty()) {
                requireActivity().runOnUiThread(this::showEmpty);
                return;
            }

            // Query sekali, build HashMap
            List<FavoriteMeal> allFavs = favoriteDao.getAll();
            Map<String, FavoriteMeal> favMap = new HashMap<>();
            for (FavoriteMeal f : allFavs) favMap.put(f.getMealId(), f);

            List<GroceryItem> items = new ArrayList<>();
            int daysCount = 0;
            // Track unique days
            java.util.Set<String> uniqueDays = new java.util.HashSet<>();

            for (PlannerItem p : plannerItems) {
                FavoriteMeal fav = favMap.get(p.getMealId());
                if (fav != null && fav.getIngredients() != null) {
                    uniqueDays.add(p.getDayOfWeek());
                    String label = p.getDayOfWeek() + " · " + p.getMealName();
                    items.addAll(parseIngredients(fav.getIngredients(), label));
                }
            }
            daysCount = uniqueDays.size();

            final int finalDays = daysCount;
            final List<GroceryItem> finalItems = items;

            requireActivity().runOnUiThread(() -> {
                if (finalItems.isEmpty()) {
                    showEmpty();
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    rvGrocery.setVisibility(View.VISIBLE);
                    adapter.setItems(finalItems);
                    tvSubtitle.setText(finalDays + " hari · " + finalItems.size() + " bahan");
                    tvInfo.setText(finalItems.size() + " bahan dari " + finalDays + " resep. Centang yang sudah dibeli.");
                }
            });
        });
    }

    private void showEmpty() {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvGrocery.setVisibility(View.GONE);
        tvSubtitle.setText("Kosong");
    }

    private List<GroceryItem> parseIngredients(String json, String sourceMeal) {
        List<GroceryItem> list = new ArrayList<>();
        try {
            JsonArray arr = gson.fromJson(json, JsonArray.class);
            if (arr == null) return list;
            for (int i = 0; i < arr.size(); i++) {
                JsonObject o   = arr.get(i).getAsJsonObject();
                String name    = o.has("name")    ? o.get("name").getAsString()    : "";
                String measure = o.has("measure") ? o.get("measure").getAsString() : "";
                if (!name.trim().isEmpty())
                    list.add(new GroceryItem(name.trim(), measure.trim(), sourceMeal));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private void shareGroceryList() {
        String text = adapter.toShareText();
        if (text.trim().isEmpty()) return;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(shareIntent, "Bagikan daftar belanja"));
    }
}
