package com.example.mealplan.fragment;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.example.mealplan.R;
import com.example.mealplan.activity.DetailActivity;
import com.example.mealplan.activity.SearchActivity;
import com.example.mealplan.adapter.CategoryAdapter;
import com.example.mealplan.adapter.MealAdapter;
import com.example.mealplan.model.Category;
import com.example.mealplan.model.Meal;
import com.example.mealplan.network.ApiClient;
import com.example.mealplan.network.MealApiService;
import com.example.mealplan.network.NetworkUtils;
import com.example.mealplan.utils.Constants;
import com.example.mealplan.utils.LocaleMapper;
import com.example.mealplan.utils.ThemeUtils;
import com.example.mealplan.utils.ViewUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView rvMeals, rvCategories;
    private ProgressBar progressBar;
    private View layoutError;
    private Button btnRetry;
    private TextView tvGreeting;
    private TextView tvSectionTitle;
    private ImageView imgErrorIcon;
    private TextView tvErrorTitle, tvErrorSubtitle;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh;
    private View skeleton;
    private ObjectAnimator shimmerAnim;

    private MealAdapter mealAdapter;
    private CategoryAdapter categoryAdapter;
    private MealApiService apiService;
    private String currentCategory = null;
    private final List<String> realCategories = new ArrayList<>();
    private final Random random = new Random();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupAdapters();
        loadCategories();

        view.findViewById(R.id.tv_search_hint).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SearchActivity.class)));

        view.findViewById(R.id.btn_theme_toggle).setOnClickListener(v ->
                ThemeUtils.toggleTheme(requireContext()));

        view.findViewById(R.id.btn_random).setOnClickListener(v -> loadRandomMeal());
    }

    private void initViews(View view) {
        rvMeals      = view.findViewById(R.id.rv_meals);
        rvCategories = view.findViewById(R.id.rv_categories);
        progressBar  = view.findViewById(R.id.progress_bar);
        layoutError  = view.findViewById(R.id.layout_error);
        btnRetry        = view.findViewById(R.id.btn_retry);
        tvGreeting      = view.findViewById(R.id.tv_greeting);
        tvSectionTitle  = view.findViewById(R.id.tv_section_title);
        imgErrorIcon    = view.findViewById(R.id.img_error_icon);
        tvErrorTitle    = view.findViewById(R.id.tv_error_title);
        tvErrorSubtitle = view.findViewById(R.id.tv_error_subtitle);
        updateGreeting();
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.primary_color);
        swipeRefresh.setOnRefreshListener(this::refreshHome);
        skeleton     = view.findViewById(R.id.skeleton);
        apiService   = ApiClient.getService();
    }

    private void updateGreeting() {
        if (tvGreeting == null) return;
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour >= 5 && hour < 11) {
            greeting = "Selamat pagi 🌅";
        } else if (hour >= 11 && hour < 15) {
            greeting = "Selamat siang ☀️";
        } else if (hour >= 15 && hour < 19) {
            greeting = "Selamat sore 🌇";
        } else {
            greeting = "Selamat malam 🌙";
        }
        tvGreeting.setText(greeting);
    }

    private void setupAdapters() {
        mealAdapter = new MealAdapter(requireContext());
        GridLayoutManager glm = new GridLayoutManager(requireContext(), 2);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return mealAdapter.isFeatured(position) ? 2 : 1;
            }
        });
        rvMeals.setLayoutManager(glm);
        rvMeals.setAdapter(mealAdapter);

        categoryAdapter = new CategoryAdapter(requireContext(), categoryName -> {
            if (categoryName == null) {
                currentCategory = null;
                loadDefaultMeals();
            } else {
                currentCategory = categoryName;
                loadMealsByCategory(categoryName);
            }
        });

        rvCategories.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        btnRetry.setOnClickListener(v -> {
            if (currentCategory != null) loadMealsByCategory(currentCategory);
            else loadDefaultMeals();
        });
    }

    private void loadCategories() {
        if (!NetworkUtils.isConnected(requireContext())) { showError(); return; }

        apiService.getCategories().enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> cats = parseCategories(response.body());
                    realCategories.clear();
                    for (Category c : cats) {
                        if (c.getName() != null) realCategories.add(c.getName());
                    }
                    cats.add(0, new Category(null, null, null));
                    categoryAdapter.setCategories(cats);
                    currentCategory = null;
                    loadDefaultMeals();
                    if (cats.size() > 1) {
                        currentCategory = cats.get(1).getName();
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
                    List<Meal> meals = parseMeals(response.body(), category);
                    if (meals.isEmpty()) {
                        showEmpty();
                    } else {
                        mealAdapter.setMeals(meals);
                        showContent(category);
                    }
                } else showError();
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                hideLoading(); showError();
            }
        });
    }

    private void loadDefaultMeals() {
        String category;
        if (!realCategories.isEmpty()) {
            category = realCategories.get(random.nextInt(realCategories.size()));
        } else {
            category = "Chicken";
        }
        loadMealsByCategory(category);
    }

    private void refreshHome() {
        if (!NetworkUtils.isConnected(requireContext())) {
            swipeRefresh.setRefreshing(false);
            showError();
            return;
        }
        if (categoryAdapter.getItemCount() == 0) {
            loadCategories();
        } else if (currentCategory != null) {
            loadMealsByCategory(currentCategory);
        } else {
            loadDefaultMeals();
        }
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
                            Intent intent = new Intent(requireContext(), DetailActivity.class);
                            intent.putExtra(Constants.INTENT_MEAL_ID,
                                    o.get("idMeal").getAsString());
                            intent.putExtra(Constants.INTENT_MEAL_NAME,
                                    o.get("strMeal").getAsString());
                            intent.putExtra(Constants.INTENT_MEAL_THUMB,
                                    o.get("strMealThumb").getAsString());
                            intent.putExtra(Constants.INTENT_MEAL_CATEGORY,
                                    o.has("strCategory") ? o.get("strCategory").getAsString() : "");
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

    private void showLoading()  {
        layoutError.setVisibility(View.GONE);
        tvSectionTitle.setVisibility(View.GONE);
        if (swipeRefresh != null && swipeRefresh.isRefreshing()) {
            return;
        }
        rvMeals.setVisibility(View.GONE);
        showSkeleton();
    }
    private void hideLoading()  {
        hideSkeleton();
        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
    }
    private void showContent(String category)  {
        hideSkeleton();
        rvMeals.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
        tvSectionTitle.setText(LocaleMapper.category(category));
        tvSectionTitle.setVisibility(View.VISIBLE);
    }
    private void showError()    {
        hideSkeleton();
        rvMeals.setVisibility(View.GONE);
        tvSectionTitle.setVisibility(View.GONE);
        imgErrorIcon.setImageResource(R.drawable.ic_no_internet);
        tvErrorTitle.setText("Tidak ada koneksi");
        tvErrorSubtitle.setText("Periksa koneksi internet kamu");
        btnRetry.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.VISIBLE);
        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
    }

    private void showEmpty() {
        hideSkeleton();
        rvMeals.setVisibility(View.GONE);
        tvSectionTitle.setVisibility(View.GONE);
        imgErrorIcon.setImageResource(R.drawable.ic_search);
        tvErrorTitle.setText("Belum ada resep di sini");
        tvErrorSubtitle.setText("Coba kategori lain atau cari resep favoritmu");
        btnRetry.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
    }

    private void showSkeleton() {
        if (skeleton == null) return;
        skeleton.setVisibility(View.VISIBLE);
        shimmerAnim = ViewUtils.startShimmer(skeleton);
    }
    private void hideSkeleton() {
        ViewUtils.stopShimmer(shimmerAnim, skeleton);
        shimmerAnim = null;
        if (skeleton != null) skeleton.setVisibility(View.GONE);
    }
}