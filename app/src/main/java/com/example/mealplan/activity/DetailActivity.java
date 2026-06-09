package com.example.mealplan.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.example.mealplan.R;
import com.example.mealplan.adapter.IngredientAdapter;
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
    private TextView tvName, tvCategory, tvArea, tvInstructions;
    private ImageButton btnBack, btnFavorite, btnShare;
    private Button btnYoutube;
    private ProgressBar progressDetail;
    private RecyclerView rvIngredients;
    private IngredientAdapter ingredientAdapter;

    private FavoriteDao favoriteDao;
    private MealApiService apiService;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();

    private String mealId, mealName, mealThumb, mealCategory;
    private boolean isFavorite = false;
    private MealDetail currentDetail = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // --- BUG FIX: null-check Intent extras sebelum dipakai ---
        Intent intent = getIntent();
        if (intent == null) { finish(); return; }

        mealId       = intent.getStringExtra(Constants.INTENT_MEAL_ID);
        mealName     = intent.getStringExtra(Constants.INTENT_MEAL_NAME);
        mealThumb    = intent.getStringExtra(Constants.INTENT_MEAL_THUMB);
        mealCategory = intent.getStringExtra(Constants.INTENT_MEAL_CATEGORY);

        // Kalau mealId null/kosong tidak ada yang bisa dimuat — tutup activity
        if (mealId == null || mealId.trim().isEmpty()) {
            Toast.makeText(this, "Data resep tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fallback aman untuk field lain
        if (mealName     == null) mealName     = "Resep";
        if (mealThumb    == null) mealThumb    = "";
        if (mealCategory == null) mealCategory = "";

        favoriteDao = new FavoriteDao(this);
        apiService  = ApiClient.getService();

        initViews();
        checkFavoriteStatus();
        loadDetail();
    }

    private void initViews() {
        imgThumb       = findViewById(R.id.img_detail_thumb);
        tvName         = findViewById(R.id.tv_detail_name);
        tvCategory     = findViewById(R.id.tv_detail_category);
        tvArea         = findViewById(R.id.tv_detail_area);
        tvInstructions = findViewById(R.id.tv_detail_instructions);
        btnBack        = findViewById(R.id.btn_back);
        btnFavorite    = findViewById(R.id.btn_favorite);
        btnShare       = findViewById(R.id.btn_share);
        btnYoutube     = findViewById(R.id.btn_youtube);
        progressDetail = findViewById(R.id.progress_detail);
        rvIngredients  = findViewById(R.id.rv_ingredients);

        ingredientAdapter = new IngredientAdapter(this);
        rvIngredients.setLayoutManager(new LinearLayoutManager(this));
        rvIngredients.setAdapter(ingredientAdapter);
        rvIngredients.setNestedScrollingEnabled(false);

        // Tampilkan data awal dari Intent agar tidak blank saat API loading
        tvName.setText(mealName);
        tvCategory.setText(mealCategory);
        if (!mealThumb.isEmpty()) {
            Glide.with(this).load(mealThumb).centerCrop().into(imgThumb);
        }

        // Sembunyikan tombol YouTube sampai URL tersedia
        btnYoutube.setVisibility(View.GONE);

        btnBack.setOnClickListener(v -> onBackPressed());
        btnFavorite.setOnClickListener(v -> toggleFavorite());
        btnShare.setOnClickListener(v -> shareRecipe());
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
                        isFavorite ? "✓ Ditambahkan ke favorit" : "Dihapus dari favorit",
                        Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void shareRecipe() {
        StringBuilder sb = new StringBuilder();
        sb.append("🍽️ *").append(mealName).append("*\n");

        if (currentDetail != null) {
            if (currentDetail.getCategory() != null && !currentDetail.getCategory().isEmpty())
                sb.append("📂 ").append(currentDetail.getCategory());
            if (currentDetail.getArea() != null && !currentDetail.getArea().isEmpty())
                sb.append(" · ").append(currentDetail.getArea());
            sb.append("\n\n");

            sb.append("🧂 *Bahan-bahan:*\n");
            for (String[] ing : currentDetail.getIngredientList()) {
                sb.append("  • ").append(ing[0]);
                if (!ing[1].isEmpty()) sb.append(" — ").append(ing[1]);
                sb.append("\n");
            }

            sb.append("\n📋 *Cara memasak:*\n");
            String instructions = currentDetail.getInstructions();
            if (instructions != null && instructions.length() > 500) {
                sb.append(instructions, 0, 500).append("...\n");
            } else if (instructions != null) {
                sb.append(instructions).append("\n");
            }

            // Sertakan link YouTube kalau ada
            String ytUrl = currentDetail.getYoutubeUrl();
            if (ytUrl != null && !ytUrl.trim().isEmpty()) {
                sb.append("\n▶️ Tutorial: ").append(ytUrl).append("\n");
            }
        } else {
            // Detail belum dimuat — share minimal dari Intent data
            sb.append("📂 ").append(mealCategory).append("\n");
        }

        sb.append("\n🔗 Dibagikan dari MealPlan App");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Resep: " + mealName);
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(shareIntent, "Bagikan resep " + mealName));
    }

    private void openYoutube(String url) {
        // Validasi URL sebelum buka
        if (url == null || url.trim().isEmpty()) return;
        try {
            Intent ytIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            // Coba buka di app YouTube dulu, fallback ke browser
            ytIntent.setPackage("com.google.android.youtube");
            if (ytIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(ytIntent);
            } else {
                // YouTube tidak terinstall → buka di browser
                ytIntent.setPackage(null);
                startActivity(ytIntent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Tidak dapat membuka link YouTube", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDetail() {
        progressDetail.setVisibility(View.VISIBLE);
        apiService.getMealDetail(mealId).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                progressDetail.setVisibility(View.GONE);
                if (!response.isSuccessful() || response.body() == null) {
                    showDetailError();
                    return;
                }
                try {
                    JsonArray meals = response.body().getAsJsonArray("meals");
                    if (meals != null && meals.size() > 0) {
                        JsonObject o = meals.get(0).getAsJsonObject();
                        currentDetail = gson.fromJson(o, MealDetail.class);
                        bindDetail(currentDetail);
                    } else {
                        showDetailError();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showDetailError();
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
        tvCategory.setText(detail.getCategory() != null ? detail.getCategory() : mealCategory);
        tvArea.setText(detail.getArea() != null ? detail.getArea() : "");

        String instructions = detail.getInstructions();
        tvInstructions.setText(instructions != null ? instructions : "Instruksi tidak tersedia.");

        if (detail.getThumb() != null && !detail.getThumb().isEmpty()) {
            Glide.with(this).load(detail.getThumb()).centerCrop().into(imgThumb);
        }
        ingredientAdapter.setIngredients(detail.getIngredientList());

        // --- YouTube link ---
        String ytUrl = detail.getYoutubeUrl();
        if (ytUrl != null && !ytUrl.trim().isEmpty()) {
            btnYoutube.setVisibility(View.VISIBLE);
            btnYoutube.setOnClickListener(v -> openYoutube(ytUrl));
        } else {
            btnYoutube.setVisibility(View.GONE);
        }
    }

    private void showDetailError() {
        Toast.makeText(this,
                "Gagal memuat detail. Cek koneksi lalu coba lagi.",
                Toast.LENGTH_SHORT).show();
    }
}
