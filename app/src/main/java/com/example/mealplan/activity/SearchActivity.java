package com.example.mealplan.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.animation.ObjectAnimator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.example.mealplan.R;
import com.example.mealplan.adapter.MealAdapter;
import com.example.mealplan.adapter.RecentSearchAdapter;
import com.example.mealplan.model.Meal;
import com.example.mealplan.network.ApiClient;
import com.example.mealplan.network.MealApiService;
import com.example.mealplan.utils.ThemeUtils;
import com.example.mealplan.utils.ViewUtils;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private static final int MAX_RECENT = 8;
    private static final long DEBOUNCE_MS = 500;
    private static final String PREF_RECENT = "recent_searches";
    private static final String KEY_RECENT_LIST = "recent_list";

    private EditText etSearch;
    private ImageButton btnBack, btnClear;
    private RecyclerView rvRecent, rvResults;
    private LinearLayout layoutRecent, layoutResults, layoutNoResult, layoutSearchError;
    private View progressSearch;
    private View skeletonSearch;
    private ObjectAnimator shimmerAnim;
    private TextView tvNoResultQuery, tvResultCount;
    private String lastQuery = "";

    private MealAdapter mealAdapter;
    private RecentSearchAdapter recentAdapter;
    private MealApiService apiService;
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable debounceRunnable;

    private LinkedList<String> recentQueries = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        apiService = ApiClient.getService();
        initViews();
        loadRecentSearches();

        etSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
    }

    private void initViews() {
        etSearch          = findViewById(R.id.et_search);
        btnBack           = findViewById(R.id.btn_back_search);
        btnClear          = findViewById(R.id.btn_clear_search);
        rvRecent          = findViewById(R.id.rv_recent_search);
        rvResults         = findViewById(R.id.rv_search_results);
        layoutRecent      = findViewById(R.id.layout_recent);
        layoutResults     = findViewById(R.id.layout_results);
        layoutNoResult    = findViewById(R.id.layout_no_result);
        progressSearch    = findViewById(R.id.progress_search);
        skeletonSearch    = findViewById(R.id.skeleton_search);
        tvNoResultQuery   = findViewById(R.id.tv_no_result_query);
        tvResultCount     = findViewById(R.id.tv_result_count);
        layoutSearchError = findViewById(R.id.layout_search_error);
        findViewById(R.id.btn_search_retry).setOnClickListener(v -> {
            if (!lastQuery.isEmpty()) performSearch(lastQuery);
        });

        mealAdapter = new MealAdapter(this);
        mealAdapter.setFeaturedEnabled(false);
        rvResults.setLayoutManager(new GridLayoutManager(this, 2));
        rvResults.setAdapter(mealAdapter);

        recentAdapter = new RecentSearchAdapter(this, new RecentSearchAdapter.OnRecentSearchListener() {
            @Override public void onQueryClick(String query) {
                etSearch.setText(query);
                etSearch.setSelection(query.length());
                performSearch(query);
            }
            @Override public void onRemoveClick(String query, int position) {
                recentQueries.remove(query);
                recentAdapter.removeItem(position);
                saveRecentSearches();
                checkRecentEmpty();
            }
        });
        rvRecent.setLayoutManager(new LinearLayoutManager(this));
        rvRecent.setAdapter(recentAdapter);

        btnBack.setOnClickListener(v -> finish());

        btnClear.setOnClickListener(v -> {
            etSearch.setText("");
            showRecentState();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                if (s.length() == 0) { showRecentState(); return; }
                debounceHandler.removeCallbacks(debounceRunnable);
                debounceRunnable = () -> performSearch(s.toString().trim());
                debounceHandler.postDelayed(debounceRunnable, DEBOUNCE_MS);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String q = etSearch.getText().toString().trim();
                if (!q.isEmpty()) performSearch(q);
                return true;
            }
            return false;
        });
    }

    private void performSearch(String query) {
        if (query.isEmpty()) return;
        lastQuery = query;
        showLoadingState();
        apiService.searchMeals(query).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Meal> meals = parseMeals(response.body());
                    if (meals.isEmpty()) {
                        showNoResultState(query);
                    } else {
                        saveQuery(query);
                        mealAdapter.setMeals(meals);
                        showResultState(meals.size(), query);
                    }
                } else { showErrorState(); }
            }
            @Override public void onFailure(Call<JsonObject> call, Throwable t) {
                showErrorState();
            }
        });
    }

    private List<Meal> parseMeals(JsonObject json) {
        List<Meal> list = new ArrayList<>();
        try {
            JsonArray arr = json.getAsJsonArray("meals");
            if (arr == null || arr.isJsonNull()) return list;
            for (int i = 0; i < arr.size(); i++) {
                JsonObject o = arr.get(i).getAsJsonObject();
                String id    = o.has("idMeal")       ? o.get("idMeal").getAsString()       : "";
                String name  = o.has("strMeal")      ? o.get("strMeal").getAsString()      : "";
                String thumb = o.has("strMealThumb") ? o.get("strMealThumb").getAsString() : "";
                String cat   = o.has("strCategory")  ? o.get("strCategory").getAsString()  : "";
                list.add(new Meal(id, name, thumb, cat));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private void saveQuery(String query) {
        recentQueries.remove(query);
        recentQueries.addFirst(query);
        if (recentQueries.size() > MAX_RECENT)
            recentQueries.removeLast();
        saveRecentSearches();
    }

    private void loadRecentSearches() {
        SharedPreferences prefs = getSharedPreferences(PREF_RECENT, MODE_PRIVATE);
        String raw = prefs.getString(KEY_RECENT_LIST, "");
        recentQueries = new LinkedList<>();
        if (!raw.isEmpty()) {
            for (String q : raw.split("\\|\\|")) {
                if (!q.isEmpty()) recentQueries.add(q);
            }
        }
        recentAdapter.setQueries(new ArrayList<>(recentQueries));
        checkRecentEmpty();
    }

    private void saveRecentSearches() {
        SharedPreferences prefs = getSharedPreferences(PREF_RECENT, MODE_PRIVATE);
        prefs.edit().putString(KEY_RECENT_LIST,
                String.join("||", recentQueries)).apply();
    }

    private void checkRecentEmpty() {
        View tvNoRecent = findViewById(R.id.tv_no_recent);
        if (tvNoRecent != null)
            tvNoRecent.setVisibility(recentQueries.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showRecentState() {
        hideSkeletonSearch();
        layoutRecent.setVisibility(View.VISIBLE);
        progressSearch.setVisibility(View.GONE);
        layoutResults.setVisibility(View.GONE);
        layoutNoResult.setVisibility(View.GONE);
        layoutSearchError.setVisibility(View.GONE);
        recentAdapter.setQueries(new ArrayList<>(recentQueries));
        checkRecentEmpty();
    }

    private void showLoadingState() {
        layoutRecent.setVisibility(View.GONE);
        progressSearch.setVisibility(View.GONE);
        layoutResults.setVisibility(View.GONE);
        layoutNoResult.setVisibility(View.GONE);
        layoutSearchError.setVisibility(View.GONE);
        showSkeletonSearch();
    }

    private void showResultState(int count, String query) {
        hideSkeletonSearch();
        layoutRecent.setVisibility(View.GONE);
        progressSearch.setVisibility(View.GONE);
        layoutResults.setVisibility(View.VISIBLE);
        layoutNoResult.setVisibility(View.GONE);
        layoutSearchError.setVisibility(View.GONE);
        tvResultCount.setText(count + " hasil untuk \"" + query + "\"");
        rvResults.scrollToPosition(0);
    }

    private void showNoResultState(String query) {
        hideSkeletonSearch();
        layoutRecent.setVisibility(View.GONE);
        progressSearch.setVisibility(View.GONE);
        layoutResults.setVisibility(View.GONE);
        layoutSearchError.setVisibility(View.GONE);
        layoutNoResult.setVisibility(View.VISIBLE);
        tvNoResultQuery.setText("Tidak ada hasil untuk \"" + query + "\"");
    }

    private void showErrorState() {
        hideSkeletonSearch();
        layoutRecent.setVisibility(View.GONE);
        progressSearch.setVisibility(View.GONE);
        layoutResults.setVisibility(View.GONE);
        layoutNoResult.setVisibility(View.GONE);
        layoutSearchError.setVisibility(View.VISIBLE);
    }

    private void showSkeletonSearch() {
        if (skeletonSearch == null) return;
        skeletonSearch.setVisibility(View.VISIBLE);
        shimmerAnim = ViewUtils.startShimmer(skeletonSearch);
    }

    private void hideSkeletonSearch() {
        ViewUtils.stopShimmer(shimmerAnim, skeletonSearch);
        shimmerAnim = null;
        if (skeletonSearch != null) skeletonSearch.setVisibility(View.GONE);
    }
}