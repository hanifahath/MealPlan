package com.example.mealplan.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.example.mealplan.R;
import com.example.mealplan.adapter.FavoriteAdapter;
import com.example.mealplan.database.FavoriteDao;
import com.example.mealplan.model.FavoriteMeal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private View layoutEmpty;
    private TextView tvFavCount;
    private FavoriteAdapter adapter;
    private FavoriteDao favoriteDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    // Sort: 0=newest, 1=oldest, 2=az
    private int currentSort = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvFavorites = view.findViewById(R.id.rv_favorites);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        tvFavCount  = view.findViewById(R.id.tv_fav_count);
        favoriteDao = new FavoriteDao(requireContext());

        adapter = new FavoriteAdapter(requireContext(),
            new FavoriteAdapter.OnFavoriteActionListener() {
                @Override
                public void onDeleteClick(FavoriteMeal meal, int position) {
                    adapter.removeItem(position);
                    checkEmpty();

                    Snackbar snackbar = Snackbar.make(
                            requireView(),
                            meal.getMealName() + " dihapus dari favorit",
                            Snackbar.LENGTH_LONG);

                    snackbar.setAction("Batalkan", v -> {
                        executor.execute(() -> {
                            favoriteDao.insert(meal);
                            requireActivity().runOnUiThread(() -> loadFavorites());
                        });
                    });

                    snackbar.addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar s, int event) {
                            if (event != DISMISS_EVENT_ACTION) {
                                executor.execute(() -> favoriteDao.delete(meal.getMealId()));
                            }
                        }
                    });

                    snackbar.setActionTextColor(
                            getResources().getColor(R.color.primary_light, null));
                    snackbar.show();
                }

                @Override
                public void onShareClick(FavoriteMeal meal) {
                    String text = "🍽️ " + meal.getMealName()
                            + "\n📂 " + meal.getMealCategory()
                            + "\n\nDibagikan dari MealPlan App";
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, text);
                    startActivity(Intent.createChooser(shareIntent,
                            "Bagikan " + meal.getMealName()));
                }
            });

        // Grid 2 kolom
        rvFavorites.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvFavorites.setAdapter(adapter);

        // Sort button
        view.findViewById(R.id.btn_sort).setOnClickListener(v -> showSortDialog());

        loadFavorites();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    public void loadFavorites() {
        executor.execute(() -> {
            List<FavoriteMeal> list = favoriteDao.getAll();
            applySortInPlace(list);
            requireActivity().runOnUiThread(() -> {
                adapter.setFavorites(list);
                tvFavCount.setText(list.size() + " resep");
                checkEmpty();
            });
        });
    }

    private void showSortDialog() {
        String[] options = {"Terbaru", "Terlama", "A–Z"};
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Urutkan berdasarkan")
                .setSingleChoiceItems(options, currentSort, (dialog, which) -> {
                    currentSort = which;
                    dialog.dismiss();
                    loadFavorites();
                })
                .show();
    }

    private void applySortInPlace(List<FavoriteMeal> list) {
        switch (currentSort) {
            case 0: // newest — urutan default dari DB (id DESC sudah dari DAO)
                break;
            case 1: // oldest — balik urutan
                Collections.reverse(list);
                break;
            case 2: // A–Z
                Collections.sort(list, (a, b) ->
                        a.getMealName().compareToIgnoreCase(b.getMealName()));
                break;
        }
    }

    private void checkEmpty() {
        if (adapter.getItemCount() == 0) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvFavorites.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvFavorites.setVisibility(View.VISIBLE);
        }
    }
}
