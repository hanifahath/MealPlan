package com.example.mealplan.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
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
import com.example.mealplan.utils.ThemeUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GroceryActivity extends AppCompatActivity {

    private RecyclerView rvGrocery;
    private LinearLayout layoutEmpty;
    private TextView tvSubtitle, tvInfo;
    private Button btnClearChecked;
    private GroceryAdapter adapter;

    private PlannerDao plannerDao;
    private FavoriteDao favoriteDao;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery);

        plannerDao  = new PlannerDao(this);
        favoriteDao = new FavoriteDao(this);

        initViews();
        generateGroceryList();
    }

    private void initViews() {
        rvGrocery       = findViewById(R.id.rv_grocery);
        layoutEmpty     = findViewById(R.id.layout_grocery_empty);
        tvSubtitle      = findViewById(R.id.tv_grocery_subtitle);
        tvInfo          = findViewById(R.id.tv_grocery_info);
        btnClearChecked = findViewById(R.id.btn_clear_checked);

        adapter = new GroceryAdapter(this);
        rvGrocery.setLayoutManager(new LinearLayoutManager(this));
        rvGrocery.setAdapter(adapter);

        findViewById(R.id.btn_back_grocery).setOnClickListener(v -> onBackPressed());
        btnClearChecked.setOnClickListener(v -> adapter.clearChecked());
        findViewById(R.id.btn_share_grocery).setOnClickListener(v -> shareGroceryList());
    }

    private void generateGroceryList() {
        executor.execute(() -> {
            // --- FIX: query sekali, build HashMap, lookup O(1) ---
            List<PlannerItem> plannerItems = plannerDao.getAll();

            if (plannerItems.isEmpty()) {
                runOnUiThread(() -> {
                    showEmpty("Planner masih kosong",
                            "Isi meal planner dulu untuk generate daftar belanja");
                });
                return;
            }

            // Ambil semua favorites sekali saja → Map<mealId, FavoriteMeal>
            List<FavoriteMeal> allFavorites = favoriteDao.getAll();
            Map<String, FavoriteMeal> favMap = new HashMap<>();
            for (FavoriteMeal f : allFavorites) {
                favMap.put(f.getMealId(), f);
            }

            List<GroceryItem> groceryItems = new ArrayList<>();
            int daysWithMeal = 0;

            for (PlannerItem plannerItem : plannerItems) {
                FavoriteMeal fav = favMap.get(plannerItem.getMealId());
                if (fav != null && fav.getIngredients() != null
                        && !fav.getIngredients().isEmpty()) {
                    daysWithMeal++;
                    String label = plannerItem.getDayOfWeek()
                            + " · " + plannerItem.getMealName();
                    groceryItems.addAll(parseIngredients(fav.getIngredients(), label));
                }
            }

            final int finalDays      = daysWithMeal;
            final int finalCount     = groceryItems.size();
            final List<GroceryItem> finalList = groceryItems;

            runOnUiThread(() -> {
                if (finalList.isEmpty()) {
                    showEmpty("Resep belum disimpan sebagai favorit",
                            "Buka resep dari planner lalu tambahkan ke favorit terlebih dahulu");
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    rvGrocery.setVisibility(View.VISIBLE);
                    adapter.setItems(finalList);
                    tvSubtitle.setText(finalDays + " hari direncanakan");
                    tvInfo.setText(finalCount + " bahan dari " + finalDays
                            + " resep · Centang yang sudah dibeli");
                }
            });
        });
    }

    private void showEmpty(String title, String subtitle) {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvGrocery.setVisibility(View.GONE);
        tvSubtitle.setText(title);
        // Update teks di dalam empty state layout jika ada
    }

    // Parse JSON ingredients dari SQLite → List<GroceryItem>
    // Format: [{"name":"...", "measure":"..."}, ...]
    private List<GroceryItem> parseIngredients(String json, String sourceMeal) {
        List<GroceryItem> list = new ArrayList<>();
        try {
            JsonArray arr = gson.fromJson(json, JsonArray.class);
            if (arr == null) return list;
            for (int i = 0; i < arr.size(); i++) {
                JsonObject o     = arr.get(i).getAsJsonObject();
                String name      = o.has("name")    ? o.get("name").getAsString()    : "";
                String measure   = o.has("measure") ? o.get("measure").getAsString() : "";
                if (!name.trim().isEmpty()) {
                    list.add(new GroceryItem(name.trim(), measure.trim(), sourceMeal));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
