package com.example.mealplan.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.example.mealplan.R;
import com.example.mealplan.database.FavoriteDao;
import com.example.mealplan.model.FavoriteMeal;
import com.example.mealplan.model.MealDetail;
import com.example.mealplan.network.ApiClient;
import com.example.mealplan.network.MealApiService;
import com.example.mealplan.utils.Constants;
import com.example.mealplan.utils.ThemeUtils;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    private ImageView imgThumb;
    private TextView tvName, tvCategory, tvArea;
    private ImageButton btnBack, btnFavorite, btnShare;
    private Button btnYoutube;
    private View progressDetail;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private FavoriteDao favoriteDao;
    private MealApiService apiService;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();

    private String mealId, mealName, mealThumb, mealCategory;
    private boolean isFavorite = false;
    private MealDetail currentDetail = null;

    // Fragment tabs — dibuat di sini supaya bisa diisi data setelah API response
    private IngredientsTabFragment ingredientsTab;
    private InstructionsTabFragment instructionsTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        if (intent == null) { finish(); return; }

        mealId       = intent.getStringExtra(Constants.INTENT_MEAL_ID);
        mealName     = intent.getStringExtra(Constants.INTENT_MEAL_NAME);
        mealThumb    = intent.getStringExtra(Constants.INTENT_MEAL_THUMB);
        mealCategory = intent.getStringExtra(Constants.INTENT_MEAL_CATEGORY);

        if (mealId == null || mealId.trim().isEmpty()) {
            Toast.makeText(this, "Data resep tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish(); return;
        }
        if (mealName     == null) mealName     = "Resep";
        if (mealThumb    == null) mealThumb    = "";
        if (mealCategory == null) mealCategory = "";

        favoriteDao = new FavoriteDao(this);
        apiService  = ApiClient.getService();

        initViews();
        setupTabs();
        checkFavoriteStatus();
        loadDetail();
    }

    private void initViews() {
        imgThumb       = findViewById(R.id.img_detail_thumb);
        tvName         = findViewById(R.id.tv_detail_name);
        tvCategory     = findViewById(R.id.tv_detail_category);
        tvArea         = findViewById(R.id.tv_detail_area);
        btnBack        = findViewById(R.id.btn_back);
        btnFavorite    = findViewById(R.id.btn_favorite);
        btnShare       = findViewById(R.id.btn_share);
        btnYoutube     = findViewById(R.id.btn_youtube);
        progressDetail = findViewById(R.id.progress_detail);
        tabLayout      = findViewById(R.id.tab_layout);
        viewPager      = findViewById(R.id.view_pager);

        tvName.setText(mealName);
        tvCategory.setText(com.example.mealplan.utils.LocaleMapper.category(mealCategory));
        btnYoutube.setVisibility(View.GONE);

        if (!mealThumb.isEmpty()) {
            Glide.with(this).load(mealThumb).centerCrop().into(imgThumb);
        }

        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        btnFavorite.setOnClickListener(v -> toggleFavorite());
        btnShare.setOnClickListener(v -> shareRecipe());

        final int baseMargin = (int) (14 * getResources().getDisplayMetrics().density);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(btnBack, (v, insets) -> {
            int topInset = insets.getInsets(
                    androidx.core.view.WindowInsetsCompat.Type.systemBars()).top;
            android.view.ViewGroup.MarginLayoutParams lp =
                    (android.view.ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.topMargin = baseMargin + topInset;
            v.setLayoutParams(lp);
            return insets;
        });
        androidx.core.view.ViewCompat.requestApplyInsets(btnBack);
    }

    private void setupTabs() {
        ingredientsTab   = new IngredientsTabFragment();
        instructionsTab  = new InstructionsTabFragment();

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @Override public int getItemCount() { return 2; }
            @Override
            public Fragment createFragment(int position) {
                return position == 0 ? ingredientsTab : instructionsTab;
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0
                    ? getString(R.string.ingredients)
                    : getString(R.string.instructions));
        }).attach();
    }

    private void checkFavoriteStatus() {
        executor.execute(() -> {
            isFavorite = favoriteDao.isFavorite(mealId);
            runOnUiThread(this::updateFavoriteIcon);
        });
    }

    private void updateFavoriteIcon() {
        btnFavorite.setImageResource(
                isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
    }

    private void toggleFavorite() {
        if (currentDetail == null) {
            Toast.makeText(this, "Tunggu detail selesai dimuat", Toast.LENGTH_SHORT).show();
            return;
        }
        executor.execute(() -> {
            if (isFavorite) {
                favoriteDao.delete(mealId);
                isFavorite = false;
            } else {
                FavoriteMeal fav = new FavoriteMeal(
                        mealId, mealName, mealThumb, mealCategory,
                        currentDetail.getIngredientsAsJson(),
                        currentDetail.getInstructions()
                );
                favoriteDao.insert(fav);
                isFavorite = true;
            }
            runOnUiThread(() -> {
                updateFavoriteIcon();
                Toast.makeText(this,
                        isFavorite ? "Ditambahkan ke favorit" : "Dihapus dari favorit",
                        Toast.LENGTH_SHORT).show();
            });
        });
    }

    // Fix 7: share tanpa tanda bintang
    private void shareRecipe() {
        StringBuilder sb = new StringBuilder();
        sb.append("Resep: ").append(mealName).append("\n");

        if (currentDetail != null) {
            if (currentDetail.getCategory() != null)
                sb.append("Kategori: ").append(com.example.mealplan.utils.LocaleMapper.category(currentDetail.getCategory()));
            if (currentDetail.getArea() != null && !currentDetail.getArea().isEmpty())
                sb.append(" | ").append(com.example.mealplan.utils.LocaleMapper.area(currentDetail.getArea()));
            sb.append("\n\n");

            sb.append("Bahan-bahan:\n");
            for (String[] ing : currentDetail.getIngredientList()) {
                sb.append("- ").append(ing[0]);
                if (!ing[1].isEmpty()) sb.append(": ").append(ing[1]);
                sb.append("\n");
            }

            sb.append("\nCara memasak:\n");
            String instructions = currentDetail.getInstructions();
            if (instructions != null && instructions.length() > 600) {
                sb.append(instructions, 0, 600).append("...\n");
            } else if (instructions != null) {
                sb.append(instructions).append("\n");
            }

            String ytUrl = currentDetail.getYoutubeUrl();
            if (ytUrl != null && !ytUrl.trim().isEmpty()) {
                sb.append("\nTutorial: ").append(ytUrl).append("\n");
            }
        }
        sb.append("\nDibagikan dari MealPlan App");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Resep: " + mealName);
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(shareIntent, "Bagikan resep " + mealName));
    }

    private void loadDetail() {
        progressDetail.setVisibility(View.VISIBLE);
        apiService.getMealDetail(mealId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressDetail.setVisibility(View.GONE);
                if (!response.isSuccessful() || response.body() == null) {
                    showDetailError(); return;
                }
                try {
                    JsonArray meals = response.body().getAsJsonArray("meals");
                    if (meals != null && meals.size() > 0) {
                        currentDetail = gson.fromJson(
                                meals.get(0).getAsJsonObject(), MealDetail.class);
                        bindDetail(currentDetail);
                    } else showDetailError();
                } catch (Exception e) {
                    e.printStackTrace(); showDetailError();
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                progressDetail.setVisibility(View.GONE);
                showDetailError();
            }
        });
    }

    private void bindDetail(MealDetail detail) {
        tvName.setText(detail.getName() != null ? detail.getName() : mealName);
        tvCategory.setText(com.example.mealplan.utils.LocaleMapper.category(detail.getCategory() != null ? detail.getCategory() : mealCategory));
        tvArea.setText(com.example.mealplan.utils.LocaleMapper.area(detail.getArea() != null ? detail.getArea() : ""));

        if (detail.getThumb() != null && !detail.getThumb().isEmpty()) {
            Glide.with(this).load(detail.getThumb()).centerCrop().into(imgThumb);
        }

        // Isi tab fragments
        if (ingredientsTab != null)
            ingredientsTab.setIngredients(detail.getIngredientList());
        if (instructionsTab != null)
            instructionsTab.setInstructions(detail.getInstructions());

        // YouTube button
        String ytUrl = detail.getYoutubeUrl();
        if (ytUrl != null && !ytUrl.trim().isEmpty()) {
            btnYoutube.setVisibility(View.VISIBLE);
            btnYoutube.setOnClickListener(v -> openYoutube(ytUrl));
        } else {
            btnYoutube.setVisibility(View.GONE);
        }
    }

    private void openYoutube(String url) {
        try {
            Intent ytIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            ytIntent.setPackage("com.google.android.youtube");
            if (ytIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(ytIntent);
            } else {
                ytIntent.setPackage(null);
                startActivity(ytIntent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Tidak dapat membuka YouTube", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDetailError() {
        Toast.makeText(this, "Gagal memuat detail. Cek koneksi.", Toast.LENGTH_SHORT).show();
    }

    // --- Inner Fragment classes untuk Tab ---

    public static class IngredientsTabFragment extends Fragment {
        private com.example.mealplan.adapter.IngredientAdapter adapter;
        private java.util.List<String[]> pendingData = null;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_tab_ingredients, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            androidx.recyclerview.widget.RecyclerView rv =
                    view.findViewById(R.id.rv_ingredients_tab);
            adapter = new com.example.mealplan.adapter.IngredientAdapter(requireContext());
            rv.setLayoutManager(
                    new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
            rv.setAdapter(adapter);
            rv.setNestedScrollingEnabled(false);
            if (pendingData != null) {
                adapter.setIngredients(pendingData);
                pendingData = null;
            }
        }

        public void setIngredients(java.util.List<String[]> data) {
            if (adapter != null) adapter.setIngredients(data);
            else pendingData = data;
        }
    }

    public static class InstructionsTabFragment extends Fragment {
        private String pendingText = null;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_tab_instructions, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            if (pendingText != null) {
                buildSteps(view, pendingText);
                pendingText = null;
            }
        }

        public void setInstructions(String text) {
            View view = getView();
            if (view != null) {
                buildSteps(view, text != null ? text : "Instruksi tidak tersedia.");
            } else {
                pendingText = text;
            }
        }

        private void buildSteps(View root, String text) {
            android.widget.LinearLayout container =
                    root.findViewById(R.id.container_steps);
            container.removeAllViews();

            // Pisahkan per baris/paragraf yang tidak kosong
            String[] rawSteps = text.split("\r?\n+");
            java.util.List<String> steps = new java.util.ArrayList<>();
            for (String s : rawSteps) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) steps.add(trimmed);
            }

            // Kalau hanya 1 blok panjang, split per kalimat
            if (steps.size() <= 2 && text.length() > 200) {
                steps.clear();
                for (String s : text.split("(?<=[.!?])\\s+")) {
                    String trimmed = s.trim();
                    if (!trimmed.isEmpty()) steps.add(trimmed);
                }
            }

            if (steps.isEmpty()) {
                // Fallback ke plain text
                TextView tvFallback = root.findViewById(R.id.tv_instructions_tab);
                tvFallback.setVisibility(View.VISIBLE);
                tvFallback.setText(text);
                return;
            }

            android.view.LayoutInflater inf = android.view.LayoutInflater.from(requireContext());
            for (int i = 0; i < steps.size(); i++) {
                View stepView = inf.inflate(R.layout.item_instruction_step, container, false);
                ((TextView) stepView.findViewById(R.id.tv_step_number))
                        .setText(String.valueOf(i + 1));
                ((TextView) stepView.findViewById(R.id.tv_step_text))
                        .setText(steps.get(i));
                container.addView(stepView);

                // Divider tipis antar step
                View divider = new View(requireContext());
                android.widget.LinearLayout.LayoutParams lp =
                        new android.widget.LinearLayout.LayoutParams(
                                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1);
                int margin = (int)(16 * getResources().getDisplayMetrics().density);
                lp.setMarginStart(margin + 42); // indent setelah nomor
                lp.setMarginEnd(margin);
                divider.setLayoutParams(lp);
                divider.setBackgroundColor(
                        getResources().getColor(R.color.divider_color, null));
                container.addView(divider);
            }
        }
    }
}
