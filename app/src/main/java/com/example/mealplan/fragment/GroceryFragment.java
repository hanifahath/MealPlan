package com.example.mealplan.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
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
import java.util.concurrent.ExecutorService;
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
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
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
        adapter.setOnProgressChangedListener(this::updateProgress);

        btnClearChecked.setOnClickListener(v -> adapter.clearChecked());

        view.findViewById(R.id.btn_share_grocery).setOnClickListener(v -> shareGroceryList());

        generateGroceryList();
    }

    @Override
    public void onResume() {
        super.onResume();
        generateGroceryList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void generateGroceryList() {
        final android.content.Context ctx = requireContext().getApplicationContext();
        executor.execute(() -> {
            List<PlannerItem> plannerItems = plannerDao.getAll();

            List<GroceryItem> items = new ArrayList<>();
            java.util.Set<String> uniqueDays = new java.util.HashSet<>();

            if (!plannerItems.isEmpty()) {
                List<FavoriteMeal> allFavs = favoriteDao.getAll();
                Map<String, FavoriteMeal> favMap = new HashMap<>();
                for (FavoriteMeal f : allFavs) favMap.put(f.getMealId(), f);

                for (PlannerItem p : plannerItems) {
                    FavoriteMeal fav = favMap.get(p.getMealId());
                    if (fav != null && fav.getIngredients() != null) {
                        uniqueDays.add(p.getDayOfWeek());
                        String label = p.getDayOfWeek() + " · " + p.getMealName();
                        items.addAll(parseIngredients(fav.getIngredients(), label));
                    }
                }
            }

            List<GroceryItem> manual =
                    com.example.mealplan.utils.GroceryStore.getItems(ctx);
            items.addAll(manual);

            java.util.Set<String> removedKeys =
                    com.example.mealplan.utils.GroceryStore.loadRemovedKeys(ctx);
            items.removeIf(it ->
                    removedKeys.contains(com.example.mealplan.utils.GroceryStore.keyOf(it)));

            final int finalDays = uniqueDays.size();
            final int manualCount = manual.size();
            final List<GroceryItem> finalItems = items;

            runOnUi(() -> {
                if (finalItems.isEmpty()) {
                    showEmpty();
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    rvGrocery.setVisibility(View.VISIBLE);
                    adapter.setItems(finalItems);
                    String sub;
                    if (finalDays > 0 && manualCount > 0)
                        sub = finalDays + " hari + " + manualCount + " tambahan · "
                                + finalItems.size() + " bahan";
                    else if (finalDays > 0)
                        sub = finalDays + " hari · " + finalItems.size() + " bahan";
                    else
                        sub = finalItems.size() + " bahan";
                    tvSubtitle.setText(sub);
                }
            });
        });
    }

    private void updateProgress(int checked, int total) {
        if (total <= 0) {
            progressGrocery.setProgress(0);
            tvInfo.setText("");
            return;
        }
        int pct = (int) ((checked / (float) total) * 100);
        progressGrocery.setProgress(pct);
        if (checked == 0) {
            tvInfo.setText(total + " bahan. Centang yang sudah dibeli.");
        } else if (checked >= total) {
            tvInfo.setText("Selesai! Semua " + total + " bahan sudah dibeli 🎉");
        } else {
            tvInfo.setText(checked + " dari " + total + " bahan sudah dibeli");
        }
    }

    private void showEmpty() {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvGrocery.setVisibility(View.GONE);
        tvSubtitle.setText("Kosong");
        progressGrocery.setProgress(0);
        tvInfo.setText("");
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

    private void runOnUi(Runnable action) {
        FragmentActivity activity = getActivity();
        if (activity == null) return;
        activity.runOnUiThread(() -> {
            if (isAdded()) action.run();
        });
    }
}