package com.example.mealplan.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.example.mealplan.R;
import com.example.mealplan.activity.DetailActivity;
import com.example.mealplan.adapter.CategoryAdapter;
import com.example.mealplan.adapter.MealAdapter;
import com.example.mealplan.model.Category;
import com.example.mealplan.model.Meal;
import com.example.mealplan.network.ApiClient;
import com.example.mealplan.network.MealApiService;
import com.example.mealplan.network.NetworkUtils;
import com.example.mealplan.utils.Constants;
import com.example.mealplan.utils.ThemeUtils;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView rvMeals, rvCategories;
    private ProgressBar progressBar;
    private View layoutError;
    private Button btnRetry;
    private SearchView searchView;

    private MealAdapter mealAdapter;
    private CategoryAdapter categoryAdapter;
    private MealApiService apiService;
    private String currentCategory = "Beef";

    // Debounce untuk search
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long SEARCH_DEBOUNCE_MS = 500;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupAdapters();
        setupSearch();
        loadCategories();

        // Theme toggle
        view.findViewById(R.id.btn_theme_toggle).setOnClickListener(v ->
                ThemeUtils.toggleTheme(requireContext()));

        // Random meal
        view.findViewById(R.id.btn_random).setOnClickListener(v -> loadRandomMeal());
    }

    private void initViews(View view) {
        rvMeals      = view.findViewById(R.id.rv_meals);
        rvCategories = view.findViewById(R.id.rv_categories);
        progressBar  = view.findViewById(R.id.progress_bar);
        layoutError  = view.findViewById(R.id.layout_error);
        btnRetry     = view.findViewById(R.id.btn_retry);
        searchView   = view.findViewById(R.id.search_view);
        apiService   = ApiClient.getService();
    }

    private void setupAdapters() {
        mealAdapter = new MealAdapter(requireContext());
        rvMeals.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvMeals.setAdapter(mealAdapter);

        categoryAdapter = new CategoryAdapter(requireContext(), categoryName -> {
            currentCategory = categoryName;
            // Clear search saat pilih kategori
            searchView.setQuery("", false);
            searchView.clearFocus();
            loadMealsByCategory(categoryName);
        });
        rvCategories.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        btnRetry.setOnClickListener(v -> {
            if (!searchView.getQuery().toString().isEmpty()) {
                searchMeals(searchView.getQuery().toString());
            } else {
                loadMealsByCategory(currentCategory);
            }
        });
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchHandler.removeCallbacks(searchRunnable);
                searchMeals(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchHandler.removeCallbacks(searchRunnable);
                if (newText.trim().isEmpty()) {
                    loadMealsByCategory(currentCategory);
                    return true;
                }
                // Debounce 500ms
                searchRunnable = () -> searchMeals(newText.trim());
                searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_MS);
                return true;
            }
        });
    }

    private void loadCategories() {
        if (!NetworkUtils.isConnected(requireContext())) { showError(); return; }

        apiService.getCategories().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> cats = parseCategories(response.body());
                    categoryAdapter.setCategories(cats);
                    if (!cats.isEmpty()) {
                        currentCategory = cats.get(0).getName();
                        loadMealsByCategory(currentCategory);
                    }
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) { showError(); }
        });
    }

    private void loadMealsByCategory(String category) {
        if (!NetworkUtils.isConnected(requireContext())) { showError(); return; }
        showLoading();
        apiService.getMealsByCategory(category).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    mealAdapter.setMeals(parseMeals(response.body(), category));
                    showContent();
                } else showError();
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                hideLoading(); showError();
            }
        });
    }

    private void searchMeals(String query) {
        if (!NetworkUtils.isConnected(requireContext())) { showError(); return; }
        showLoading();
        apiService.searchMeals(query).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    List<Meal> meals = parseMeals(response.body(), "");
                    if (meals.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Resep \"" + query + "\" tidak ditemukan",
                                Toast.LENGTH_SHORT).show();
                    }
                    mealAdapter.setMeals(meals);
                    showContent();
                } else showError();
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                hideLoading(); showError();
            }
        });
    }

    private void loadRandomMeal() {
        if (!NetworkUtils.isConnected(requireContext())) {
            Toast.makeText(requireContext(), "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show();
            return;
        }
        apiService.getRandomMeal().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JsonArray meals = response.body().getAsJsonArray("meals");
                        if (meals != null && meals.size() > 0) {
                            JsonObject o = meals.get(0).getAsJsonObject();
                            String id    = o.get("idMeal").getAsString();
                            String name  = o.get("strMeal").getAsString();
                            String thumb = o.get("strMealThumb").getAsString();
                            String cat   = o.has("strCategory") ? o.get("strCategory").getAsString() : "";

                            Intent intent = new Intent(requireContext(), DetailActivity.class);
                            intent.putExtra(Constants.INTENT_MEAL_ID,       id);
                            intent.putExtra(Constants.INTENT_MEAL_NAME,     name);
                            intent.putExtra(Constants.INTENT_MEAL_THUMB,    thumb);
                            intent.putExtra(Constants.INTENT_MEAL_CATEGORY, cat);
                            startActivity(intent);
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(requireContext(), "Gagal memuat resep acak", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Parser ---
    private List<Category> parseCategories(JsonObject json) {
        List<Category> list = new ArrayList<>();
        try {
            JsonArray arr = json.getAsJsonArray("categories");
            for (int i = 0; i < arr.size(); i++) {
                JsonObject o = arr.get(i).getAsJsonObject();
                list.add(new Category(
                        o.get("strCategory").getAsString(),
                        o.get("strCategoryThumb").getAsString(),
                        o.get("strCategoryDescription").getAsString()
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private List<Meal> parseMeals(JsonObject json, String category) {
        List<Meal> list = new ArrayList<>();
        try {
            JsonArray arr = json.getAsJsonArray("meals");
            if (arr == null || arr.isJsonNull()) return list;
            for (int i = 0; i < arr.size(); i++) {
                JsonObject o = arr.get(i).getAsJsonObject();
                String id    = o.has("idMeal")       ? o.get("idMeal").getAsString()       : "";
                String name  = o.has("strMeal")      ? o.get("strMeal").getAsString()      : "";
                String thumb = o.has("strMealThumb") ? o.get("strMealThumb").getAsString() : "";
                String cat   = o.has("strCategory")  ? o.get("strCategory").getAsString()  : category;
                list.add(new Meal(id, name, thumb, cat));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // --- State helpers ---
    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        rvMeals.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
    }
    private void hideLoading()  { progressBar.setVisibility(View.GONE); }
    private void showContent()  { rvMeals.setVisibility(View.VISIBLE); layoutError.setVisibility(View.GONE); }
    private void showError()    { progressBar.setVisibility(View.GONE); rvMeals.setVisibility(View.GONE); layoutError.setVisibility(View.VISIBLE); }
}
